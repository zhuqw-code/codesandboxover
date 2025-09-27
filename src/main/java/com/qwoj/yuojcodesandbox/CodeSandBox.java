package com.qwoj.yuojcodesandbox;

import com.qwoj.yuojcodesandbox.model.ExecuteCodeRequest;
import com.qwoj.yuojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Service;

/**
 * 定义代码沙箱接口，规范方法
 */
@Service
public interface CodeSandBox {
    /**
     * 每个代码编辑器执行代码的方法
     *
     * @param executeCodeRequest 喂给代码沙箱的信息
     * @return 代码沙箱执行代码后返回的信息
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);

}
