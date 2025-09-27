import java.util.ArrayList;
import java.util.List;

/**
 *  无限占用内存资源
 */
public class Main {

    public static void main(String[] args) {
        List<Byte[]> list = new ArrayList<>();
        while (true){
            list.add(new Byte[10000]);
        }
    }
}
