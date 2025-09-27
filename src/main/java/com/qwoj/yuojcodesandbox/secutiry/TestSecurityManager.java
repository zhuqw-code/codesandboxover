package com.qwoj.yuojcodesandbox.secutiry;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestSecurityManager {
    public static void main(String[] args) {
        System.setSecurityManager(new MySecurityManager());
        String filePath = "C:\\Users\\zhuqw\\Desktop\\Project\\yu资料\\yuoj-code-sandbox\\src\\main\\resources\\application.yml";
        String output = FileUtil.readString(new File(filePath), StandardCharsets.UTF_8);
        List<String> strings = FileUtil.readLines(new File(filePath), StandardCharsets.UTF_8);
        System.out.println(output);
        System.out.println(strings);
    }
}
