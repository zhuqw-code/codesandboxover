package com.qwoj.yuojcodesandbox.model;

import lombok.Data;

/**
 * 对终端执行的结果进行封装的实体类
 */
@Data
public class ExecuteMessage {
    /**
     * 终端执行后返回的状态码
     */
    private Integer exitValue;

    /**
     * 执行成功后返回的正确信息
     */
    private String message;

    /**
     * 执行后的错误码
     */
    private String errorMessage;

    /**
     * 记录每次程序执行的时间
     */
    private Long time;
    /**
     * 记录每个样例所消耗的最大内存
     */
    private Long memory;
}
