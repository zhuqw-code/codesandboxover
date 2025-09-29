package com.qwoj.yuojcodesandbox.controller;

import com.qwoj.yuojcodesandbox.JavaDockerCodeSandbox;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Array;
import java.util.Arrays;

@RestController("/")
public class MainController {

    @GetMapping("/health")
    public String test() {
        JavaDockerCodeSandbox.main(new String[]{});
        return "OK";
    }
}
