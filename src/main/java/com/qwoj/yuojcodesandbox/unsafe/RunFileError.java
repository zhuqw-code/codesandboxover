package com.qwoj.yuojcodesandbox.unsafe;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class RunFileError {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 先写进去
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "/src/main/resources/木马.bat";

        // 再运行
        Process process = Runtime.getRuntime().exec(filePath);
        process.waitFor();

        // 获取程序正常输出流
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String compilerOutputLine;
        while ((compilerOutputLine = bufferedReader.readLine()) != null) {
            System.out.println(compilerOutputLine);
        }
        System.out.println("病毒植入成功");
    }
}
