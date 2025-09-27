package com.qwoj.yuojcodesandbox.unsafe;

/**
 * 无限循环（阻塞程序执行）
 */
public class SleepError {
    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(60 * 60 * 1000l);
        System.out.println("睡眠阻塞");
    }
}
