package com.qwoj.yuojcodesandbox.secutiry;

import java.security.Permission;

public class DefaultSecurityManager extends SecurityManager{
    /**
     * java 中权限最大的方法【访问所有的类都会执行该方法】
     */
    @Override
    public void checkPermission(Permission perm) {
        System.out.println("默认不执行所有方法");
        System.out.println(perm);
        super.checkPermission(perm);  // 有 super 默认禁用多有的权限
    }
}
