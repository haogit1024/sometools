package com.czh.util;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class App {
    public static void main(String[] args) {
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
        ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
        System.out.println("submit final");
    }
}
