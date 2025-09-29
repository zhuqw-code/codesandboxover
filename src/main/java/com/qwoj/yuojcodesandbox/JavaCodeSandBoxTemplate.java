package com.qwoj.yuojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.qwoj.yuojcodesandbox.model.ExecuteCodeRequest;
import com.qwoj.yuojcodesandbox.model.ExecuteCodeResponse;
import com.qwoj.yuojcodesandbox.model.ExecuteMessage;
import com.qwoj.yuojcodesandbox.model.JudgeInfo;
import com.qwoj.yuojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 模板方法中的抽象类
 * todo 对于编译错误需要如何进行处理，包括返回给前端的数据
 */
@Slf4j
public abstract class JavaCodeSandBoxTemplate implements CodeSandBox {

    private static final String GLOBAL_CODE_DIR = "tempcode";

    private static final String GLOBAL_JAVA_CLASS = "Main.java";

    private static final long TIME_OUT = 10000;

    /**
     * 将前端返回的code保存在本地/docker容器【容器挂载目录】
     *
     * @param code 用户提交的代码
     * @return 返回存储后的目录信息
     */
    public File saveCodeFile(String code) {
        // 获取到项目根目录
        String userDir = System.getProperty("user.dir");
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR;
        // 判断全局代码文件是否存在
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }
        // 创建目录 + 文件

        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        // System.out.println("创建成功");
        return userCodeFile;
    }

    /**
     * 对保存在本地的提交文件进行编译
     *
     * @param userCodeFile 告诉该方法需要编译的文件目录
     * @return 返回编译信息
     */
    public ExecuteMessage compileFile(File userCodeFile) {
        // 通过获取到命令行
        try {
            // String compiler = String.format("javac -encoding utf-8 %s", userCodePath);
            String compilerCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
            Process compilerProcess = Runtime.getRuntime().exec(compilerCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compilerProcess, "编译");
            // 判断日志信息
            if (executeMessage.getExitValue() != 0) {
                // return getErrorResponse(e);
                throw new RuntimeException("编译错误");
            }
            return executeMessage;
        } catch (Throwable e) {
            throw new RuntimeException("编译错误");
        }
    }

    /**
     * 运行编译后的文件
     *
     * @param userCodeFile 本地/docker容器 存储的用户代码目录
     * @param inputList    投喂给判题机的样例
     * @return 返回每个样例的执行信息
     */
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        String userCodeParentPath = userCodeFile.getParent();

        ArrayList<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String input : inputList) {
            String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, input);
            try {
                // 分别执行每个样例获取到输出数据
                Process runProcess = Runtime.getRuntime().exec(runCmd); // 开启一个新的子进程
                // 超时判断
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        runProcess.destroy();      // 守护线程执行后如果子进程还在执行，就杀死
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                // 第一种相当于 lettcode 直接输入样例
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                executeMessageList.add(executeMessage);
                System.out.println(executeMessage);
            } catch (IOException e) {
                throw new RuntimeException("程序执行异常", e);
            }
        }
        return executeMessageList;
    }

    /**
     * 对运行后的返回信息进行处理封装成响应对象
     *
     * @param executeMessageList 传递的运行信息
     * @return 返回总的判题信息
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        ArrayList<String> outputList = new ArrayList<>();
        long maxTime = Long.MIN_VALUE;
        for (ExecuteMessage executeMessage : executeMessageList) {
            if (StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
                // 将错误信息搜集
                executeCodeResponse.setMessage(executeMessage.getErrorMessage());
                // 设置运行状态为错误
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
        }
        executeCodeResponse.setOutput(outputList);

        // 如果所有样例都有答案我们将状态设置为成功
        if (executeMessageList.size() == outputList.size()) {
            executeCodeResponse.setStatus(1);
        }
        JudgeInfo judgeInfo = new JudgeInfo();

        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(666l);     // 不好获取内存直接设置一个默认值吧

        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 将存储在本地的用户提交文件删除
     *
     * @param userCodeFile 存放在本地/docker容器的文件
     * @return 删除是否成功
     */
    public boolean deleteFile(File userCodeFile) {
        if (userCodeFile.getParent() != null) {
            String userCodeParentPath = userCodeFile.getParent();
            boolean isDel = FileUtil.del(userCodeParentPath);
            return isDel;
        }
        return true;
    }

    /**
     * 封装一个错误处理类，当程序抛出异常时执行该方法返回一个结果
     *
     * @param e 异常
     * @return 返回一个含有异常的错误处理类
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutput(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }

    /**
     * @param executeCodeRequest 喂给代码沙箱的信息
     * @return 返回 ExecuteCodeResponse 对象，包含的 status 属性 1：正常  2：代码沙箱异常  3：用户代码存在错误
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {

        List<String> inputList = executeCodeRequest.getInputList();
        String language = executeCodeRequest.getLanguage();
        String code = executeCodeRequest.getCode();

        // 1.把用户提交的代码保存为文件
        File userCodeFile = saveCodeFile(code);

        // 2. 编译代码，得到 .class 文件
        ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
        System.out.println(compileFileExecuteMessage);

        // 3. 运行代码，得到输出结果
        List<ExecuteMessage> executeMessageList = runFile(userCodeFile, inputList);

        // 4. 搜集结果，将结果返回给判题系统
        ExecuteCodeResponse executeCodeResponse = getOutputResponse(executeMessageList);

        // 5. 文件清理
        boolean b = deleteFile(userCodeFile);
        if (!b) {
            log.info("删除{}文件失败", userCodeFile.getAbsoluteFile());
        }

        return executeCodeResponse;
    }
}
