package com.czh.util;

import java.util.concurrent.*;

public class App {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        System.out.println("this is util app");
        for (int i = 0; i < 50; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(finalI);
            });
        }
        executor.shutdown();
//        ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
//        System.out.println("size = " + pool.getQueue().size());
        System.out.println("submit final");
        executor.awaitTermination(5, TimeUnit.MINUTES);
        System.out.println("最后完成");
    }
}
