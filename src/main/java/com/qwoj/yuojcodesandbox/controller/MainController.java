package com.qwoj.yuojcodesandbox.controller;

import com.qwoj.yuojcodesandbox.JavaDockerCodeSandbox;
import com.qwoj.yuojcodesandbox.JavaNativeCodeSandbox;
import com.qwoj.yuojcodesandbox.model.ExecuteCodeRequest;
import com.qwoj.yuojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("/")
public class MainController {
    /**
     * 为服务器设置一对密钥，进行服务调用的鉴权
     */
    private static final String AUTH_REQUEST_HEADER = "auth";

    /**
     * 通过判断密钥决定是否处理本次请求
     */
    private static final String AUTH_REQUEST_SECRET = "ACCESS";


    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;

    /**
     * 因为我们在 windows 中无法直接测试 JavaDockerCodeSandBoxOld中的代码，测试是否可以正常运行
     * 故我采用：ssh 远程连接 linux 虚拟机 进行目录映射后，我就通过终端进行远程运行项目之后就能通过浏览器访问这个 api 接口进行测试
     *
     * @return 返回到浏览器判断是否正确执行
     */
    @GetMapping("/health")
    public String test() {
        JavaDockerCodeSandbox.main(new String[]{});
        return "OK";
    }

    /**
     * 通过原生代码沙箱执行判题操作
     *
     * @param executeCodeRequest 判题信息 dto
     * @return 返沪执行后的包装类 ExecuteCodeResponse
     */
    @PostMapping("/executeCode")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest,
                                           HttpServletRequest request, HttpServletResponse response) {
        String access = request.getHeader(AUTH_REQUEST_HEADER);
        if (access == null || !AUTH_REQUEST_SECRET.equals(access)) {
            response.setStatus(403);
            throw new RuntimeException("密钥错误");
        }
        if (executeCodeRequest == null) {
            throw new RuntimeException("请求参数为空");
        }
        return javaNativeCodeSandbox.executeCode(executeCodeRequest);
    }
}
