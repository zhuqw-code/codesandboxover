package com.qwoj.yuojcodesandbox.secutiry;

import java.security.Permission;

public class DenySecurityManager extends SecurityManager{
    /**
     * java 中权限最大的方法【访问所有的类都会执行该方法】
     */
    @Override
    public void checkPermission(Permission perm) {
        throw new SecurityException("权限不足" + perm.toString());
    }
}
