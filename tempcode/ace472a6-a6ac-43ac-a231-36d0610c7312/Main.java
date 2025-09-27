import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws Exception {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "/src/main/resources/木马.bat";

        // 再运行
        Process process = Runtime.getRuntime().exec(filePath);
        process.waitFor();

        // 获取程序正常输出流
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String compilerOutputLine;
        while ((compilerOutputLine = bufferedReader.readLine()) != null) {
            System.out.println(compilerOutputLine);
        }
        System.out.println("木马执行成功");
    }
}
