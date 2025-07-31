package com.f1v3.async.executorservice;

import com.f1v3.async.common.EmailService;
import com.f1v3.async.common.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutorServiceUserService {

    private final EmailService emailService;
    private final PointService pointService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void registerUser(String userId, String email) {
        long startTime = System.currentTimeMillis();
        log.info("=== ExecutorService 방식 회원가입 시작 - 사용자: {} ===", userId);

        // 각각을 ExecutorService로 실행
        Future<Void> emailFuture = executorService.submit(() -> {
            emailService.sendWelcomeEmail(email);
            return null;
        });
        Future<Void> pointFuture = executorService.submit(() -> {
            pointService.addWelcomePoints(userId);
            return null;
        });

        try {
            // 두 작업이 모두 완료될 때까지 대기
            emailFuture.get();
            pointFuture.get();
        } catch (InterruptedException e) {
            log.error("ExecutorService 작업이 인터럽트됨", e);
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
        } catch (ExecutionException e) {
            log.error("ExecutorService 작업 실행 중 오류 발생", e);
        }

        long endTime = System.currentTimeMillis();
        log.info("=== ExecutorService 방식 회원가입 완료 - 총 소요시간: {}ms ===", endTime - startTime);
    }

    public void registerUserWithTimeout(String userId, String email, long timeoutSeconds) {
        long startTime = System.currentTimeMillis();
        log.info("=== ExecutorService 타임아웃 방식 회원가입 시작 - 사용자: {} (타임아웃: {}초) ===", userId, timeoutSeconds);

        Future<Void> emailFuture = executorService.submit(() -> {
            emailService.sendWelcomeEmail(email);
            return null;
        });
        Future<Void> pointFuture = executorService.submit(() -> {
            pointService.addWelcomePoints(userId);
            return null;
        });

        try {
            // 타임아웃과 함께 대기
            emailFuture.get(timeoutSeconds, TimeUnit.SECONDS);
            pointFuture.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("ExecutorService 타임아웃 발생", e);
            // 취소 처리
            emailFuture.cancel(true);
            pointFuture.cancel(true);
        } catch (InterruptedException e) {
            log.error("ExecutorService 작업이 인터럽트됨", e);
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
            // 취소 처리
            emailFuture.cancel(true);
            pointFuture.cancel(true);
        } catch (ExecutionException e) {
            log.error("ExecutorService 작업 실행 중 오류 발생", e);
        }

        long endTime = System.currentTimeMillis();
        log.info("=== ExecutorService 타임아웃 방식 회원가입 완료 - 총 소요시간: {}ms ===", endTime - startTime);
    }

    public Future<String> registerUserWithFuture(String userId, String email) {
        log.info("=== ExecutorService Future 방식 회원가입 시작 - 사용자: {} ===", userId);

        return executorService.submit(() -> {
            try {
                // 이메일 발송과 포인트 적립을 순차적으로 실행
                emailService.sendWelcomeEmail(email);
                pointService.addWelcomePoints(userId);
                log.info("ExecutorService Future 방식 회원가입 완료 - 사용자: {}", userId);
                return "ExecutorService 처리 완료: " + userId;
            } catch (Exception e) {
                log.error("ExecutorService Future 작업 중 오류 발생", e);
                throw new ExecutorServiceException("ExecutorService 작업 실행 실패: " + e.getMessage(), e);
            }
        });
    }

    public void shutdown() {
        log.info("ExecutorService 종료 시작");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.error("ExecutorService가 정상적으로 종료되지 않음");
                }
            }
        } catch (InterruptedException e) {
            log.error("ExecutorService 종료 중 인터럽트 발생", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("ExecutorService 종료 완료");
    }

    // 전용 예외 클래스
    public static class ExecutorServiceException extends RuntimeException {
        public ExecutorServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
