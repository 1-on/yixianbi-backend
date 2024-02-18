package com.yixian.yixianbi;

import com.google.common.util.concurrent.RateLimiter;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class test {

    Map<String, RateLimiter> userLimiters = new ConcurrentHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        test limiter = new test();

            // 模拟不同用户操作不同次数的情况
        for (int i = 0; i < 10; i++) {
            String userId = "user" + i;
            int operationCount = i + 1; // 不同用户操作次数不同
            for (int j = 0; j < operationCount; j++) {
                boolean canPerformOperation = limiter.tryPerformOperation(userId);
                System.out.println("User " + userId + " can perform operation: " + canPerformOperation);
            }
        }
    }
    public boolean tryPerformOperation(String userId) {
        // 根据用户ID获取对应的 RateLimiter，如果不存在则创建
        RateLimiter limiter = userLimiters.computeIfAbsent(userId, k -> RateLimiter.create(1.0)); // 每秒最多操作一次

        // 尝试获取令牌，如果成功则执行操作，否则返回 false
        return limiter.tryAcquire();
    }



    public static void testNoRateLimiter() {
        Long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            System.out.println("call execute.." + i);
        }
        Long end = System.currentTimeMillis();
        System.out.println(end - start);

    }

    public static void testWithRateLimiter() {
        Long start = System.currentTimeMillis();
        RateLimiter limiter = RateLimiter.create(10); // 每秒不超过10个任务被提交
        for (int i = 0; i < 10; i++) {
            limiter.acquire(); // 请求RateLimiter, 超过permits会被阻塞
            System.out.println("call execute.." + i);
        }
        Long end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}
