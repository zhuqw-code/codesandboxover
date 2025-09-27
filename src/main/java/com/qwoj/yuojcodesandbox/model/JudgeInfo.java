package com.qwoj.yuojcodesandbox.model;

import lombok.Data;

/**
 * 判题信息
 * 提交代码后后端需要返回的执行结果
 */
@Data
public class JudgeInfo {

    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 执行占用的内存
     */
    private Long memory;

    /**
     * 执行的时间
     */
    private Long time;
}
