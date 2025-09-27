package com.qwoj.yuojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.qwoj.yuojcodesandbox.model.ExecuteCodeRequest;
import com.qwoj.yuojcodesandbox.model.ExecuteCodeResponse;
import com.qwoj.yuojcodesandbox.model.ExecuteMessage;
import com.qwoj.yuojcodesandbox.model.JudgeInfo;
import com.qwoj.yuojcodesandbox.secutiry.DefaultSecurityManager;
import com.qwoj.yuojcodesandbox.secutiry.DenySecurityManager;
import com.qwoj.yuojcodesandbox.secutiry.MySecurityManager;
import com.qwoj.yuojcodesandbox.utils.ProcessUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * todo runCmd 还有bug不能正确加载权限管理
 */
public class JavaNativeCodeSandbox implements CodeSandBox {

    private static final String GLOBAL_CODE_DIR = "tempcode";

    private static final String GLOBAL_JAVA_CLASS = "Main.java";

    private static final long TIME_OUT = 5000;

    private static final List<String> blackList = Arrays.asList("Files", "exec");

    /**
     * 设置编译好的安全管理器的路径，用于命令行执行用户代码时进行设置【注意这里要是 .class 因为要与运行编译后的文件】
     * 并且要设置为安全管理器所在的目录
     */
    private static final String SECURITY_MANAGER_PATH = "C:\\Users\\zhuqw\\Desktop\\Project\\yu资料\\yuoj-code-sandbox\\src\\main\\resources\\security";

    private static final String SECURITY_MANAGER_CLASS_NAME = "MySecurityManager";

    // final 修饰的属性不能不给初始值。全局只加载一次
    // private static final WordTree WORD_TREE;
    // static {
    //     WORD_TREE = new WordTree();
    //     WORD_TREE.addWords(blackList);
    // }

    /**
     *
     * @param executeCodeRequest 喂给代码沙箱的信息
     * @return 返回 ExecuteCodeResponse 对象，包含的 status 属性 1：正常  2：代码沙箱异常  3：用户代码存在错误
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        // 打开 java 安全管理器
        // System.setSecurityManager(new MySecurityManager());

        List<String> inputList = executeCodeRequest.getInputList();
        String language = executeCodeRequest.getLanguage();
        String code = executeCodeRequest.getCode();

        /***
         *  【校验代码】 编译前就进行 code 的检查
         */
        // WordTree WORD_TREE = new WordTree();   // 构建字典树
        // WORD_TREE.addWords(blackList);         // 进行建树
        // FoundWord foundWord = WORD_TREE.matchWord(code);// 待匹配数据传入进行校验
        // if (foundWord.getFoundWord() != null) {
        //     System.out.println("您的程序中含有危险操作：" + foundWord.getFoundWord());
        //     return null;
        // }

        // 1.把用户提交的代码保存为文件
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

        // 2. 编译代码，得到 .class 文件
        // 通过获取到命令行
        try {
            // String compiler = String.format("javac -encoding utf-8 %s", userCodePath);
            String compilerCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
            Process compilerProcess = Runtime.getRuntime().exec(compilerCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compilerProcess, "编译");
            System.out.println(executeMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
            // e.printStackTrace();
            // return this.getErrorResponse(e);
        }

        // 3. 运行代码，得到输出结果
        ArrayList<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String input : inputList) {
            // String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, input);
            // String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=WriteManager Main %s",
            //         userCodeParentPath, SECURITY_MANAGER_PATH, input);
            // String runCmd = String.format(
            //         "java -Xmx256m -Dfile.encoding=UTF-8 " +
            //                 "-cp \"%s;%s\" " + // 使用引号包裹路径
            //                 "-Djava.security.manager=%s " + // 使用完整类名
            //                 "Main",
            //         userCodeParentPath,
            //         SECURITY_MANAGER_PATH,
            //         SECURITY_MANAGER_CLASS_NAME
            // );
            String runCmd = String.format("java -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=MySecurityManager Main",
                    userCodeParentPath, SECURITY_MANAGER_PATH);
            try {
                // 分别执行每个样例获取到输出数据
                Process runProcess = Runtime.getRuntime().exec(runCmd); // 开启一个新的子进程
                /**
                // 【超时解决】：new 一个守护线程，让其随眠一段时间，如果睡醒后还是没有结束就直接将判题线程杀死
                 new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        runProcess.destroy();      // 守护线程执行后如果子进程还在执行，就杀死
                        System.out.println("超时了");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start(); */
                // 第一种相当于 lettcode 直接输入样例
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                // 第二种相当于 dev 直接在命令行上让用户输入
                // ExecuteMessage executeMessage = ProcessUtils.runInterProcessAndGetMessage(runProcess, "运行", input);
                executeMessageList.add(executeMessage);
                System.out.println(executeMessage);
            } catch (IOException e) {
                // e.printStackTrace();
                throw new RuntimeException(e);
                // return this.getErrorResponse(e);
            }
        }

        // 4. 搜集结果，将结果返回给判题系统
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
        if (inputList.size() == outputList.size()) {
            executeCodeResponse.setStatus(1);
        }
        JudgeInfo judgeInfo = new JudgeInfo();

        judgeInfo.setTime(maxTime);
        // judgeInfo.setMemory(2);

        executeCodeResponse.setJudgeInfo(judgeInfo);

        // 5. 文件清理
        // if (FileUtil.isDirectory(userCodeParentPath)){
        if (userCodeFile.getParent() != null) {
            boolean isDel = FileUtil.del(userCodeParentPath);
            System.out.println(isDel ? "删除成功！！！" : "删除失败！！！");
        }

        // 6. 模拟错误，提高代码沙箱的健壮性
        return executeCodeResponse;
    }

    /**
     * 封装一个错误处理类，当程序抛出异常时执行该方法返回一个结果
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
     * 测试程序
     * @param args
     */
    public static void main(String[] args) {
        JavaNativeCodeSandbox javaNativeCodeSandbox = new JavaNativeCodeSandbox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        executeCodeRequest.setLanguage("java");
        // 将用户上传的数据传递（使用 resource 下的 Main.java 程序作为用户传递的代码）
        // String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
        String code = ResourceUtil.readStr("testCode/unsafecode/RunFileError.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        javaNativeCodeSandbox.executeCode(executeCodeRequest);
    }
}
