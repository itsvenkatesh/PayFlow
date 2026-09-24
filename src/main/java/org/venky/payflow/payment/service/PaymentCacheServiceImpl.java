package org.venky.payflow.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.venky.payflow.payment.dto.PaymentResponse;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentCacheServiceImpl implements PaymentCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    @Value("${payment.cache.ttl}")
    private long paymentCacheTtl;

    public PaymentCacheServiceImpl(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResponse get(UUID paymentId) {
        String key = "payment:" + paymentId;

        try {
            String json = redisTemplate.opsForValue().get(key);

            if (json == null) {
                return null;
            }

            return objectMapper.readValue(json, PaymentResponse.class);

        } catch (Exception e) {
            System.out.println("Redis GET failed: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void put(UUID paymentId, PaymentResponse payment) {
        String key = "payment:" + paymentId;

        try {
            String json = objectMapper.writeValueAsString(payment);

            redisTemplate.opsForValue().set(
                    key,
                    json,
                    paymentCacheTtl,
                    TimeUnit.MINUTES
            );

        } catch (Exception e) {
            System.out.println("Redis PUT failed: " + e.getMessage());
        }
    }

    @Override
    public void evict(UUID paymentId) {
        String key = "payment:" + paymentId;

        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            System.out.println("Redis EVICT failed: " + e.getMessage());
        }
    }
}