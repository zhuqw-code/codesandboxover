import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * 用户编写 java 程序 读取我们服务器的配置信息
 */
public class Main {

    public static void main(String[] args) throws IOException {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "/src/main/resources/application.yml";
        List<String> allLines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        System.out.println(String.join("\n", allLines));
    }
}
