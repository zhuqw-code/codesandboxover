package com.qwoj.yuojcodesandbox;

import cn.hutool.core.io.resource.ResourceUtil;
import com.qwoj.yuojcodesandbox.model.ExecuteCodeRequest;
import com.qwoj.yuojcodesandbox.model.ExecuteMessage;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * java 原生代码沙箱实现（直接复用抽象父类的方法）
 */
@Component
public class JavaNativeCodeSandbox extends JavaCodeSandBoxTemplate {

    /**
     * 重写 java 原生代码沙箱的执行逻辑
     *
     * @param userCodeFile 本地/docker容器 存储的用户代码目录
     * @param inputList    投喂给判题机的样例
     * @return
     */
    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        System.out.println("这里使用的是原生代码沙箱");
        return super.runFile(userCodeFile, inputList);
    }

}
