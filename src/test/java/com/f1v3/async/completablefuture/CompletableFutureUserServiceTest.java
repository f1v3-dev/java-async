package com.f1v3.async.completablefuture;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
class CompletableFutureUserServiceTest {

    @Autowired
    private CompletableFutureUserService completableFutureUserService;

    @AfterEach
    void tearDown() {
        completableFutureUserService.shutdown();
    }

    @Test
    void testBasicUserRegistration() {
        log.info("==================== CompletableFuture 방식 기본 사용자 등록 테스트 ====================");

        long start = System.currentTimeMillis();
        completableFutureUserService.registerUser("cf-user", "cf@example.com");
        long end = System.currentTimeMillis();

        log.info("CompletableFuture 방식 소요시간: {}ms", end - start);
    }

    @Test
    void testChainingOperations() throws ExecutionException, InterruptedException {
        log.info("==================== CompletableFuture 체이닝 테스트 ====================");

        long start = System.currentTimeMillis();
        CompletableFuture<String> future = completableFutureUserService.registerUserWithChaining("chaining-user", "chaining@example.com");

        String result = future.get();
        long end = System.currentTimeMillis();

        log.info("체이닝 결과: {}", result);
        log.info("체이닝 작업 소요시간: {}ms", end - start);
    }

    @Test
    void testTimeoutHandling() {
        log.info("==================== CompletableFuture 타임아웃 테스트 ====================");

        CompletableFuture<String> future = completableFutureUserService.registerUserWithTimeout("timeout-user", "timeout@example.com");

        try {
            String result = future.get(5, TimeUnit.SECONDS);
            log.info("타임아웃 내 완료된 결과: {}", result);
        } catch (TimeoutException e) {
            log.warn("작업이 5초 내에 완료되지 않음");
        } catch (ExecutionException | InterruptedException e) {
            log.error("작업 실행 중 오류 발생", e);
        }
    }

    @Test
    void testExceptionHandling() {
        log.info("==================== CompletableFuture 예외 처리 테스트 ====================");

        CompletableFuture<String> future = completableFutureUserService.registerUserWithException("exception-user", "exception@example.com");

        future.handle((result, throwable) -> {
            if (throwable != null) {
                log.info("예외가 성공적으로 처리됨: {}", throwable.getMessage());
                return "예외 처리 완료";
            } else {
                log.info("정상 처리 결과: {}", result);
                return result;
            }
        }).join();
    }

    @Test
    void testBulkProcessing() {
        log.info("==================== CompletableFuture 방식 대량 처리 테스트 ====================");

        int userCount = 15;
        long bulkStart = System.currentTimeMillis();

        List<CompletableFuture<Void>> futures = IntStream.range(0, userCount)
                .mapToObj(i -> CompletableFuture.runAsync(() ->
                        completableFutureUserService.registerUser("bulk-cf-" + i, "bulk-cf-" + i + "@example.com")))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long bulkEnd = System.currentTimeMillis();

        log.info("CompletableFuture 방식 {}명 처리 소요시간: {}ms", userCount, bulkEnd - bulkStart);
        log.info("평균 처리시간: {}ms/user", (bulkEnd - bulkStart) / userCount);
    }

    @Test
    void testCombiningFutures() throws ExecutionException, InterruptedException {
        log.info("==================== CompletableFuture 조합 테스트 ====================");

        CompletableFuture<String> future1 = completableFutureUserService.registerUserWithChaining("combine1", "combine1@example.com");
        CompletableFuture<String> future2 = completableFutureUserService.registerUserWithChaining("combine2", "combine2@example.com");

        CompletableFuture<String> combinedFuture = future1.thenCombine(future2, (result1, result2) -> {
            log.info("두 작업 모두 완료됨");
            return "결합된 결과: " + result1 + " & " + result2;
        });

        String result = combinedFuture.get();
        log.info("조합 결과: {}", result);
    }

    @Test
    void testAsyncComposition() throws ExecutionException, InterruptedException {
        log.info("==================== CompletableFuture 비동기 조합 테스트 ====================");

        CompletableFuture<String> composedFuture = completableFutureUserService
                .registerUserWithChaining("compose-user", "compose@example.com")
                .thenCompose(result -> {
                    log.info("첫 번째 작업 완료: {}", result);
                    return completableFutureUserService.registerUserWithChaining("compose-user2", "compose2@example.com");
                });

        String finalResult = composedFuture.get();
        log.info("조합된 최종 결과: {}", finalResult);
    }
}
