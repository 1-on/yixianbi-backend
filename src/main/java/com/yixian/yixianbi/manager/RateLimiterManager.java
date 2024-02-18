package com.yixian.yixianbi.manager;

import com.google.common.util.concurrent.RateLimiter;
import com.yixian.yixianbi.constant.MessageConstant;
import com.yixian.yixianbi.exception.BaseException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提供RateLimiter限流服务
 */
@Service
public class RateLimiterManager {


    /**
     * 每个用户每秒只能执行 n 次操作
     *
     * @param userId
     * @param n
     */
    public void doRateLimiter(String userId, double n) {
        Map<String, RateLimiter> userLimiters = new ConcurrentHashMap<>();
        // 根据用户ID获取对应的 RateLimiter，如果不存在则创建
        RateLimiter rateLimiter = userLimiters.computeIfAbsent(userId, k -> RateLimiter.create(n));
        // 尝试获取令牌，如果成功则执行操作，否则返回 false
        boolean canGen = rateLimiter.tryAcquire();
        if (!canGen) {
            throw new BaseException(MessageConstant.TOO_MANY_REQUEST);
        }

    }
}
