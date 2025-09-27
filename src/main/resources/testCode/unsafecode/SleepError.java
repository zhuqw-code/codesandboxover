/**
 * 无限循环（阻塞程序执行）
 */

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(10 * 1000l);
        System.out.println("睡眠阻塞");
    }
}
