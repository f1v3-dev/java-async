package com.f1v3.async.springasync;

import com.f1v3.async.common.EmailService;
import com.f1v3.async.common.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpringAsyncUserService {

    private final EmailService emailService;
    private final PointService pointService;

    public void registerUser(String userId, String email) {
        long startTime = System.currentTimeMillis();
        log.info("=== Spring Async 방식 회원가입 시작 - 사용자: {} ===", userId);

        // @Async 메서드를 호출하여 비동기 처리
        CompletableFuture<String> emailFuture = sendEmailAsync(email);
        CompletableFuture<String> pointFuture = addPointsAsync(userId);

        // 두 작업이 모두 완료될 때까지 대기
        CompletableFuture.allOf(emailFuture, pointFuture).thenRun(() -> {
            try {
                String emailResult = emailFuture.get();
                String pointResult = pointFuture.get();
                long endTime = System.currentTimeMillis();
                log.info("=== Spring Async 방식 회원가입 완료 - {} | {} | 총 소요시간: {}ms ===",
                        emailResult, pointResult, endTime - startTime);
            } catch (Exception e) {
                log.error("Spring Async 결과 조회 중 오류 발생", e);
            }
        }).join();
    }

    @Async("taskExecutor")
    public CompletableFuture<String> sendEmailAsync(String email) {
        emailService.sendWelcomeEmail(email);
        return CompletableFuture.completedFuture("메일 발송 완료: " + email);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> addPointsAsync(String userId) {
        pointService.addWelcomePoints(userId);
        return CompletableFuture.completedFuture("포인트 적립 완료: " + userId);
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> registerUserAsync(String userId, String email) {
        log.info("=== Spring Async 비동기 회원가입 시작 - 사용자: {} ===", userId);

        return CompletableFuture.runAsync(() -> {
            emailService.sendWelcomeEmail(email);
            pointService.addWelcomePoints(userId);
            log.info("Spring Async 비동기 처리 완료: {}", userId);
        });
    }

    @Async("taskExecutor")
    public CompletableFuture<String> registerUserWithException(String userId, String email) {
        log.info("=== Spring Async 예외 처리 방식 회원가입 시작 - 사용자: {} ===", userId);

        return CompletableFuture.supplyAsync(() -> {
            if (userId.contains("exception")) {
                throw new RuntimeException("Spring Async 의도적인 예외 발생");
            }
            emailService.sendWelcomeEmail(email);
            pointService.addWelcomePoints(userId);
            return "Spring Async 정상 처리 완료: " + userId;
        });
    }

    @Async("taskExecutor")
    public void registerUserFireAndForget(String userId, String email) {
        log.info("=== Spring Async Fire-and-Forget 방식 회원가입 시작 - 사용자: {} ===", userId);

        // Fire-and-Forget 방식 (결과를 기다리지 않음)
        sendEmailAsync(email);
        addPointsAsync(userId);

        log.info("=== Spring Async Fire-and-Forget 방식 회원가입 요청 완료 - 사용자: {} ===", userId);
    }

    @Async("taskExecutor")
    public CompletableFuture<String> registerUserAsyncWithResult(String userId, String email) {
        log.info("=== Spring Async 결과 반환 비동기 회원가입 시작 - 사용자: {} ===", userId);

        return CompletableFuture.supplyAsync(() -> {
            emailService.sendWelcomeEmail(email);
            pointService.addWelcomePoints(userId);
            log.info("Spring Async 비동기 처리 완료: {}", userId);
            return "Spring Async 처리 완료: " + userId;
        });
    }
}
