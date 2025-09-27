package com.qwoj.yuojcodesandbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.LogContainerResultCallback;

import java.util.Arrays;
import java.util.List;

public class DockerDemo {
    public static void main(String[] args) throws InterruptedException {
        // 获取到全都是默认参数的 docker核心对象 【建造者模式】
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();


//        // 拉取容器
//        // 需要拉取的镜像仓库地址
//        String image = "nginx:latest";
//        // 封装特殊的拉去镜像的 command 对象
//        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
//        // 因为这个安装是异步的我们需要传入一个回调函数，在执行过程中做一些操作
//        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback(){
//            @Override
//            public void onNext(PullResponseItem item) {
//                System.out.println("正在安装镜像：" + item);
//                super.onNext(item);
//            }
//        };
//        // 使用拉取对象执行 exec 方法进行拉取
//        pullImageCmd
//                .exec(pullImageResultCallback)   // 执行 pull 指令过程中调用回调函数
//                .awaitCompletion();              // 阻塞该 pull 过程当拉取镜像完成后才放行
//        System.out.println("下载完成");

//        // 创建容器
//        // 获取创建容器的 command 对象
//        String image = "nginx:latest";
//        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
//        // CreateContainerResponse createContainerResponse = containerCmd.exec();
//        CreateContainerResponse createContainerResponse = containerCmd
//                .withCmd("echo hello Docker")    // 在创建容器时增加一些骚操作
//                .exec();
//        System.out.println(createContainerResponse);
//
//        String containerId = createContainerResponse.getId();
//        System.out.println(containerId);

        // 获取容器信息
        // 获取执行查看容器信息的 command 对象
        ListContainersCmd listContainersCmd = dockerClient.listContainersCmd();
        List<Container> containerList = listContainersCmd
                .withShowAll(true).       // 展示所有容器
                exec();
        for (Container container : containerList) {
            System.out.println(container);
        }

//        // 启动容器
//        dockerClient.startContainerCmd(containerId).exec();

//        // 查看日志
//
//        LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(containerId);
//
//        LogContainerResultCallback pullImageResultCallback = new LogContainerResultCallback(){
//            @Override
//            public void onNext(Frame item) {       // 执行中一行一行输出
//                System.out.println(item.getPayload());
//                System.out.println("日志：" + item);
//                super.onNext(item);
//            }
//        };
//        logContainerCmd
//                .withStdErr(true)   // 必须要指定输出的形式
//                .withStdOut(true)
//                .exec(pullImageResultCallback)
//                .awaitCompletion();        // 必须阻塞，否则日志还没输出程序就结束了

//        // 删除容器
//        dockerClient.removeContainerCmd(containerId).exec();

//        // 删除镜像
//        dockerClient.removeImageCmd(image).exec();
    }
}
