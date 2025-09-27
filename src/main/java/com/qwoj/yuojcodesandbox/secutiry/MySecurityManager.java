package com.qwoj.yuojcodesandbox.secutiry;

import java.security.Permission;

public class MySecurityManager extends SecurityManager{
    // 检查所有的权限
    @Override
    public void checkPermission(Permission perm) {
        // throw new SecurityException("checkPermission权限异常" + perm);
    }

    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("ckeckExec权限异常" + cmd);
    }

    // 检查读权限
    @Override
    public void checkRead(String file) {
        // System.out.println(file);
        // if (file.contains("C:\\Users\\zhuqw\\Desktop\\Project\\yu资料\\yuoj-code-sandbox")){
        //     return;
        // }
        // throw new SecurityException("checkRead权限异常" + file);
    }

    // 检查写权限
    @Override
    public void checkWrite(String file) {
        // throw new SecurityException("checkWrite权限异常" + file);
    }

    // 检查删除权限
    @Override
    public void checkDelete(String file) {
        // throw new SecurityException("checkDelete权限异常" + file);
    }

    // 检查网络连接权限
    @Override
    public void checkConnect(String host, int port) {
        // throw new SecurityException("checkConnect权限异常" + host + port);
    }
}
