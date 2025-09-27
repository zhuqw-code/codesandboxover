package com.qwoj.yuojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 只执行代码，并返回结果【不执行其他业务。如：记录执行时间】
 */

@Data                   // 自动生成get/set方法
@Builder     // 对象链式生成
@NoArgsConstructor   // 无参构造器
@AllArgsConstructor // 不同参数的构造器
public class ExecuteCodeRequest {

    /**
     * 接收一组输入
     */
    private List<String> inputList;

    /**
     * 接收语言
     */
    private String language;

    /**
     * 接收提交的代码
     */
    private String code;
}
