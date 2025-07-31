package com.f1v3.async.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EmailService {

    public void sendWelcomeEmail(String email) {
        try {
            log.info("[{}] 메일 발송 시작 - 이메일: {}", Thread.currentThread().getName(), email);

            // 메일 발송 시뮬레이션 (2초 소요)
            TimeUnit.SECONDS.sleep(2);

            log.info("[{}] 메일 발송 완료 - 이메일: {}", Thread.currentThread().getName(), email);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("메일 발송 중 인터럽트 발생", e);
        }
    }
}
