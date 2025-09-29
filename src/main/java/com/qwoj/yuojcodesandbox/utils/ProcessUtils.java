package com.qwoj.yuojcodesandbox.utils;

import cn.hutool.core.util.StrUtil;
import com.qwoj.yuojcodesandbox.model.ExecuteMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StopWatch;

import java.io.*;
import java.util.ArrayList;

/**
 * 对终端执行的结果进行搜集
 */
public class ProcessUtils {

    /**
     * @param runProcess 编译/运行 进程
     * @param opName     编译/运行
     * @return 返沪 Terminal 的执行结果
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            Integer exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            if (exitValue == 0) {
                System.out.println(opName + "成功");
                // 获取程序正常输出流
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                String compilerOutputLine;
                ArrayList<String> outputStrList = new ArrayList<>();
                while ((compilerOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compilerOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(outputStrList, "\n"));
            } else {
                System.out.println(opName + "失败" + exitValue);
                // 获取程序正常输出流
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                String compilerOutputLine;
                ArrayList<String> outputStrList = new ArrayList<>();
                while ((compilerOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compilerOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(outputStrList, "\n"));

                // 获取程序错误输出流
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                String errorCompilerOutputLine;

                ArrayList<String> errorOutputStrList = new ArrayList<>();
                while ((errorCompilerOutputLine = errorBufferedReader.readLine()) != null) {
                    errorOutputStrList.add(errorCompilerOutputLine);
                }
                executeMessage.setErrorMessage(StringUtils.join(errorOutputStrList, "\n"));
            }
            stopWatch.stop();
            long time = stopWatch.getLastTaskTimeMillis();
            executeMessage.setTime(time);
        } catch (InterruptedException | IOException e) {
            // e.printStackTrace();
            throw new RuntimeException(e);
        }
        return executeMessage;
    }

    /**
     * @param runProcess 编译/运行 进程
     * @param opName     编译/运行
     * @return 返沪 Terminal 的执行结果
     * todo 哪里实现交互了？ 不还是处理已知值吗？用户还是没有在命令行输入啊？？？？？？？？
     */
    public static ExecuteMessage runInterProcessAndGetMessage(Process runProcess, String opName, String args) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            // 获取到写+读
            OutputStream outputStream = runProcess.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            String[] s = args.split(" ");

            outputStreamWriter.write(StrUtil.join("\n", s) + "\n");
            // 相当于按回车键
            outputStreamWriter.flush();


            InputStreamReader inputStreamReader = new InputStreamReader(runProcess.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder outPutStringBuilder = new StringBuilder();
            String outPutLine;
            while ((outPutLine = bufferedReader.readLine()) != null) {
                outPutStringBuilder.append(outPutLine);
            }
            executeMessage.setMessage(outPutStringBuilder.toString());
            outputStreamWriter.close();
            outputStream.close();
            inputStreamReader.close();
            runProcess.destroy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return executeMessage;
    }

}
