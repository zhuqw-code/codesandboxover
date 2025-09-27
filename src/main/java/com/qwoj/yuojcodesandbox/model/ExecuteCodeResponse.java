package com.qwoj.yuojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {

    /**
     * 输出信息
     */
    private List<String> output;

    /**
     * 代码沙箱内部信息，如系统错误
     */
    private String message;

    /**
     * 执行状态
     */
    private Integer status;

    /**
     * 运行所消耗的时间,内存,堆栈
     */
    private JudgeInfo judgeInfo;
}
