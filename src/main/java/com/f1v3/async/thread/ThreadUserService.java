package com.f1v3.async.thread;

import com.f1v3.async.common.EmailService;
import com.f1v3.async.common.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ThreadUserService {

    private final EmailService emailService;
    private final PointService pointService;

    public void registerUser(String userId, String email) {
        long startTime = System.currentTimeMillis();
        log.info("=== Thread 방식 회원가입 시작 - 사용자: {} ===", userId);

        // 각각을 별도 스레드로 실행
        Thread emailThread = new Thread(() -> emailService.sendWelcomeEmail(email));
        Thread pointThread = new Thread(() -> pointService.addWelcomePoints(userId));

        emailThread.start();
        pointThread.start();

        try {
            // 두 작업이 모두 완료될 때까지 대기
            emailThread.join();
            pointThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("스레드 대기 중 인터럽트 발생", e);
        }

        long endTime = System.currentTimeMillis();
        log.info("=== Thread 방식 회원가입 완료 - 총 소요시간: {}ms ===", endTime - startTime);
    }

    public void registerUserSync(String userId, String email) {
        long startTime = System.currentTimeMillis();
        log.info("=== 동기 방식 회원가입 시작 - 사용자: {} ===", userId);

        // 순차적으로 실행
        emailService.sendWelcomeEmail(email);
        pointService.addWelcomePoints(userId);

        long endTime = System.currentTimeMillis();
        log.info("=== 동기 방식 회원가입 완료 - 총 소요시간: {}ms ===", endTime - startTime);
    }
}
