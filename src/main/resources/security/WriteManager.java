import java.security.Permission;

public class WriteManager extends SecurityManager{


    // 检查写权限
    @Override
    public void checkWrite(String file) {
        throw new SecurityException("checkWrite权限异常" + file);
    }
}