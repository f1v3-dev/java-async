package com.f1v3.async.future;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
class FutureUserServiceTest {

    @Autowired
    private FutureUserService futureUserService;

    @AfterEach
    void tearDown() {
        futureUserService.shutdown();
    }

    @Test
    void testBasicUserRegistration() {
        log.info("==================== Future 방식 기본 사용자 등록 테스트 ====================");

        long start = System.currentTimeMillis();
        futureUserService.registerUser("future-user", "future@example.com");
        long end = System.currentTimeMillis();

        log.info("Future 방식 소요시간: {}ms", end - start);
    }

    @Test
    void testFutureWithResult() throws ExecutionException, InterruptedException {
        log.info("==================== Future 결과 반환 테스트 ====================");

        long start = System.currentTimeMillis();
        Future<String> future = futureUserService.registerUserWithResult("future-result-user", "future-result@example.com");

        String result = future.get(); // 블로킹 대기
        long end = System.currentTimeMillis();

        log.info("Future 결과: {}", result);
        log.info("Future 결과 반환 소요시간: {}ms", end - start);
    }

    @Test
    void testFutureWithTimeout() throws ExecutionException, InterruptedException {
        log.info("==================== Future 타임아웃 테스트 ====================");

        Future<String> future = futureUserService.registerUserWithResult("timeout-user", "timeout@example.com");

        try {
            String result = future.get(3, TimeUnit.SECONDS); // 3초 타임아웃
            log.info("타임아웃 내 완료된 결과: {}", result);
        } catch (TimeoutException e) {
            log.warn("작업이 3초 내에 완료되지 않음");
            future.cancel(true); // 작업 취소
        }
    }

    @Test
    void testBulkProcessingWithFutures() throws InterruptedException {
        log.info("==================== Future 방식 대량 처리 테스트 ====================");

        int userCount = 12;
        long bulkStart = System.currentTimeMillis();

        List<Future<String>> futures = IntStream.range(0, userCount)
            .mapToObj(i -> futureUserService.registerUserWithResult("bulk-future-" + i, "bulk-future-" + i + "@example.com"))
            .toList();

        // 모든 Future 완료 대기
        for (Future<String> future : futures) {
            try {
                String result = future.get();
                log.debug("완료된 작업 결과: {}", result);
            } catch (ExecutionException e) {
                log.error("작업 실행 중 오류 발생", e);
            }
        }

        long bulkEnd = System.currentTimeMillis();
        log.info("Future 방식 {}명 처리 소요시간: {}ms", userCount, bulkEnd - bulkStart);
        log.info("평균 처리시간: {}ms/user", (bulkEnd - bulkStart) / userCount);
    }

    @Test
    void testCancelledFuture() {
        log.info("==================== Future 취소 테스트 ====================");

        Future<String> future = futureUserService.registerUserWithResult("cancel-user", "cancel@example.com");

        // 즉시 취소 시도
        boolean cancelled = future.cancel(true);
        log.info("Future 취소 성공: {}", cancelled);
        log.info("Future 취소됨: {}", future.isCancelled());
        log.info("Future 완료됨: {}", future.isDone());
    }
}
