package com.f1v3.async.completablefuture;

import com.f1v3.async.common.EmailService;
import com.f1v3.async.common.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompletableFutureUserService {

    private final EmailService emailService;
    private final PointService pointService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void registerUser(String userId, String email) {
        long startTime = System.currentTimeMillis();
        log.info("=== CompletableFuture 방식 회원가입 시작 - 사용자: {} ===", userId);

        // CompletableFuture를 사용한 비동기 처리
        CompletableFuture<String> emailFuture = CompletableFuture.supplyAsync(() -> {
            emailService.sendWelcomeEmail(email);
            return "메일 발송 완료: " + email;
        }, executorService);

        CompletableFuture<String> pointFuture = CompletableFuture.supplyAsync(() -> {
            pointService.addWelcomePoints(userId);
            return "포인트 적립 완료: " + userId;
        }, executorService);

        // 두 작업이 모두 완료될 때까지 대기
        CompletableFuture<Void> allTasks = CompletableFuture.allOf(emailFuture, pointFuture);

        allTasks.thenRun(() -> {
            try {
                String emailResult = emailFuture.get();
                String pointResult = pointFuture.get();
                log.info("모든 작업 완료 - {}, {}", emailResult, pointResult);
            } catch (Exception e) {
                log.error("결과 조회 중 오류 발생", e);
            }
        }).join();

        long endTime = System.currentTimeMillis();
        log.info("=== CompletableFuture 방식 회원가입 완료 - 총 소요시간: {}ms ===", endTime - startTime);
    }

    public CompletableFuture<String> registerUserWithChaining(String userId, String email) {
        log.info("=== CompletableFuture 체이닝 방식 회원가입 시작 - 사용자: {} ===", userId);

        return CompletableFuture
            .supplyAsync(() -> {
                emailService.sendWelcomeEmail(email);
                return "메일 발송 완료: " + email;
            }, executorService)
            .thenCompose(emailResult ->
                CompletableFuture.supplyAsync(() -> {
                    pointService.addWelcomePoints(userId);
                    return emailResult + ", 포인트 적립 완료: " + userId;
                }, executorService))
            .thenApply(result -> {
                log.info("체이닝 작업 완료: {}", result);
                return result;
            });
    }

    public CompletableFuture<String> registerUserWithTimeout(String userId, String email) {
        log.info("=== CompletableFuture 타임아웃 방식 회원가입 시작 - 사용자: {} ===", userId);

        return CompletableFuture
            .supplyAsync(() -> {
                emailService.sendWelcomeEmail(email);
                pointService.addWelcomePoints(userId);
                return "타임아웃 처리 완료: " + userId;
            }, executorService)
            .orTimeout(3, TimeUnit.SECONDS)
            .exceptionally(throwable -> {
                log.warn("타임아웃 발생: {}", throwable.getMessage());
                return "타임아웃으로 인한 기본 처리: " + userId;
            });
    }

    public CompletableFuture<String> registerUserWithException(String userId, String email) {
        log.info("=== CompletableFuture 예외 처리 방식 회원가입 시작 - 사용자: {} ===", userId);

        return CompletableFuture
            .supplyAsync(() -> {
                if (userId.contains("exception")) {
                    throw new RuntimeException("의도적인 예외 발생");
                }
                emailService.sendWelcomeEmail(email);
                pointService.addWelcomePoints(userId);
                return "정상 처리 완료: " + userId;
            }, executorService)
            .exceptionally(throwable -> {
                log.error("예외 처리: {}", throwable.getMessage());
                return "예외 처리 완료: " + userId;
            });
    }

    public void shutdown() {
        log.info("CompletableFuture ExecutorService 종료 시작");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("CompletableFuture ExecutorService 종료 완료");
    }
}
