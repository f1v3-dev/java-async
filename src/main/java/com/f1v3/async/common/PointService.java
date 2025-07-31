package com.f1v3.async.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class PointService {

    public void addWelcomePoints(String userId) {
        try {
            log.info("[{}] 포인트 적립 시작 - 사용자: {}", Thread.currentThread().getName(), userId);

            // 포인트 적립 시뮬레이션 (1.5초 소요)
            TimeUnit.MILLISECONDS.sleep(1500);

            log.info("[{}] 포인트 적립 완료 - 사용자: {} (+1000 포인트)", Thread.currentThread().getName(), userId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("포인트 적립 중 인터럽트 발생", e);
        }
    }
}
