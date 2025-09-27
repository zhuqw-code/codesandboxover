import java.security.Permission;

public class MySecurityManager extends SecurityManager{

    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("ckeckExec权限异常" + cmd);
    }

    // 检查写权限
    @Override
    public void checkWrite(String file) {
        // throw new SecurityException("checkWrite权限异常" + file);
    }
}