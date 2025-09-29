package com.qwoj.yuojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.qwoj.yuojcodesandbox.model.ExecuteCodeRequest;
import com.qwoj.yuojcodesandbox.model.ExecuteCodeResponse;
import com.qwoj.yuojcodesandbox.model.ExecuteMessage;
import com.qwoj.yuojcodesandbox.model.JudgeInfo;
import com.qwoj.yuojcodesandbox.utils.ProcessUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 重写 docker 代码沙箱
 */
@Component
public class JavaDockerCodeSandbox extends JavaCodeSandBoxTemplate {

    private static final long TIME_OUT = 10000;

    private static final Boolean FIRST_INIT = true;

    /**
     * 重写 docker 代码沙箱的执行逻辑
     *
     * @param userCodeFile 本地/docker容器 存储的用户代码目录
     * @param inputList    投喂给判题机的样例
     * @return
     */
    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        /** 3.创建容器把文件复制到容器内 */
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();

        // 拉取镜像

        String image = "openjdk:8-alpine";
        if (FIRST_INIT) {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("正在安装镜像：" + item);
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd
                        .exec(pullImageResultCallback)
                        .awaitCompletion();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("下载完成");
        }

        // 创建可交互容器容器

        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        HostConfig hostConfig = new HostConfig();
        hostConfig.withCpuCount(1l);                // 设置 cpu 核数
        hostConfig.withMemory(100 * 1000 * 1000l);           // 设置内存限制
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));    // 设置文件路径（linux目录 (mapping) docker容器目录）
//        hostConfig.withSecurityOpts(Arrays.asList("secomp=安全管理配置字符串"));
        CreateContainerResponse createContainerResponse = containerCmd
//                .withReadonlyRootfs(true)
//                .withNetworkDisabled(true)
                .withHostConfig(hostConfig)
                .withAttachStderr(true)     // 设置输入，输出即可交互实现
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withTty(true)              // 不会运行后就销毁，会被守护线程监控
                .exec();

        String containerId = createContainerResponse.getId();

        // 启动容器

        dockerClient.startContainerCmd(containerId).exec();

        // 执行指令
        //  docker exec [OPTIONS] CONTAINER COMMAND [ARG...]
        //  docker exec quirky_rosalind java -cp /app Main 1 2
        ArrayList<ExecuteMessage> executeMessageList = new ArrayList<>();

        for (String inputArgs : inputList) {
            // 开启定时器
            long time = 0l;
            StopWatch stopWatch = new StopWatch();

            // 创建执行命令

            String[] input = inputArgs.split(" ");       // 一定要把”1 2“分开否则就会当成一个参数处理
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, input);
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient
                    .execCreateCmd(containerId)             // 指定那个容器
                    .withCmd(cmdArray)                      // 设置命令行指令
                    .withAttachStderr(true)         // 我们要从命令行输入，获取输出
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();
            System.out.println("创建的执行命令：" + execCreateCmdResponse.toString());

            // 执行命令时的需要搜集的参数
            ExecuteMessage executeMessage = new ExecuteMessage();
            final String[] message = {null};
            final String[] errorMessage = {null};
            final boolean[] timeOut = {true};        // 标志是否超时

            // 执行命令

            String execId  = execCreateCmdResponse.getId();
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback(){
                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        errorMessage[0] = new String(frame.getPayload());
                        System.out.println("错误结果：" + errorMessage[0]);
                    }
                    else {
                        message[0] = new String(frame.getPayload());
                        System.out.println("结果：" + message[0]);
                    }
                    super.onNext(frame);
                }

                @Override
                public void onComplete() {
                    timeOut[0] = false;
                    super.onComplete();
                }
            };

            // 获取占用内存
            final long[] maxMemory = {0l};

            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                    System.out.println("内存占用：" + statistics.getMemoryStats().getUsage());
                }

                @Override
                public void onStart(Closeable closeable) {

                }


                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }

                @Override
                public void close() throws IOException {

                }
            });


            try {
                // 开启定时器
                stopWatch.start();

                dockerClient
                        .execStartCmd(execId)
                        .exec(execStartResultCallback)
                        .awaitCompletion()   // 还是需要编写回调函数
                        .awaitCompletion(TIME_OUT, TimeUnit.MICROSECONDS);   // 还是需要编写回调函数
                stopWatch.stop();
                statsCmd.close();             // 需要关闭内存信息输出，否则一直写
                time = stopWatch.getLastTaskTimeMillis();  // 关闭后就立即记录时间
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // 设置每个样例的执行结果
            executeMessage.setTime(time);
            executeMessage.setMemory(maxMemory[0]);
            executeMessage.setMessage(message[0]);
            executeMessage.setErrorMessage(errorMessage[0]);
            executeMessageList.add(executeMessage);
        }
        return executeMessageList;
    }


    /**
     * todo 这里其实可以直接放在service中直接调用 executeCode 方法
     * 测试 docker 代码沙箱的执行
     *
     * @param args
     */
    public static void main(String[] args) {
        JavaDockerCodeSandboxOld javaNativeCodeSandbox = new JavaDockerCodeSandboxOld();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        executeCodeRequest.setLanguage("java");
        // 后续这里会使用用户上传的代码
        String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        javaNativeCodeSandbox.executeCode(executeCodeRequest);
    }
}
