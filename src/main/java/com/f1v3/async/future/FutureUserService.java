package com.f1v3.async.future;

import com.f1v3.async.common.EmailService;
import com.f1v3.async.common.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FutureUserService {

    private final EmailService emailService;
    private final PointService pointService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public void registerUser(String userId, String email) {
        long startTime = System.currentTimeMillis();
        log.info("=== Future 방식 회원가입 시작 - 사용자: {} ===", userId);

        // Future를 사용하여 작업 결과를 추적
        Future<String> emailFuture = executorService.submit(() -> {
            emailService.sendWelcomeEmail(email);
            return "메일 발송 완료: " + email;
        });

        Future<String> pointFuture = executorService.submit(() -> {
            pointService.addWelcomePoints(userId);
            return "포인트 적립 완료: " + userId;
        });

        try {
            // Future.get()을 통해 결과 확인
            String emailResult = emailFuture.get();
            String pointResult = pointFuture.get();

            log.info("작업 결과 - {}, {}", emailResult, pointResult);
        } catch (Exception e) {
            log.error("Future 작업 중 오류 발생", e);
        }

        long endTime = System.currentTimeMillis();
        log.info("=== Future 방식 회원가입 완료 - 총 소요시간: {}ms ===", endTime - startTime);
    }

    public void registerUserWithCustomTimeout(String userId, String email) {
        long startTime = System.currentTimeMillis();
        log.info("=== Future 커스텀 타임아웃 방식 회원가입 시작 - 사용자: {} ===", userId);

        Future<String> emailFuture = executorService.submit(() -> {
            emailService.sendWelcomeEmail(email);
            return "메일 발송 완료";
        });

        Future<String> pointFuture = executorService.submit(() -> {
            pointService.addWelcomePoints(userId);
            return "포인트 적립 완료";
        });

        try {
            // 각 작업별로 다른 타임아웃 설정
            String emailResult = emailFuture.get(3, TimeUnit.SECONDS);
            log.info("이메일 작업 완료: {}", emailResult);

            String pointResult = pointFuture.get(2, TimeUnit.SECONDS);
            log.info("포인트 작업 완료: {}", pointResult);

        } catch (TimeoutException e) {
            log.error("타임아웃 발생", e);
            emailFuture.cancel(true);
            pointFuture.cancel(true);
        } catch (Exception e) {
            log.error("Future 작업 중 오류 발생", e);
        }

        long endTime = System.currentTimeMillis();
        log.info("=== Future 커스텀 타임아웃 방식 회원가입 완료 - 총 소요시간: {}ms ===", endTime - startTime);
    }

    public void registerUserNonBlocking(String userId, String email) {
        long startTime = System.currentTimeMillis();
        log.info("=== Future 논블로킹 방식 회원가입 시작 - 사용자: {} ===", userId);

        Future<String> emailFuture = executorService.submit(() -> {
            emailService.sendWelcomeEmail(email);
            return "메일 발송 완료";
        });

        Future<String> pointFuture = executorService.submit(() -> {
            pointService.addWelcomePoints(userId);
            return "포인트 적립 완료";
        });

        // 논블로킹 체크
        while (!emailFuture.isDone() || !pointFuture.isDone()) {
            log.info("작업 진행 중... 이메일: {}, 포인트: {}",
                    emailFuture.isDone() ? "완료" : "진행중",
                    pointFuture.isDone() ? "완료" : "진행중");

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        try {
            String emailResult = emailFuture.get();
            String pointResult = pointFuture.get();
            log.info("최종 결과 - {}, {}", emailResult, pointResult);
        } catch (Exception e) {
            log.error("결과 조회 중 오류 발생", e);
        }

        long endTime = System.currentTimeMillis();
        log.info("=== Future 논블로킹 방식 회원가입 완료 - 총 소요시간: {}ms ===", endTime - startTime);
    }

    public Future<String> registerUserWithResult(String userId, String email) {
        log.info("=== Future 결과 반환 방식 회원가입 시작 - 사용자: {} ===", userId);

        return executorService.submit(() -> {
            try {
                // 이메일 발송과 포인트 적립을 순차적으로 실행
                emailService.sendWelcomeEmail(email);
                pointService.addWelcomePoints(userId);
                String result = "Future 처리 완료: " + userId;
                log.info("Future 결과 반환 방식 회원가입 완료 - 사용자: {}", userId);
                return result;
            } catch (Exception e) {
                log.error("Future 결과 반환 작업 중 오류 발생", e);
                throw new RuntimeException(e);
            }
        });
    }

    public void shutdown() {
        log.info("Future ExecutorService 종료 시작");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("Future ExecutorService 종료 완료");
    }
}
