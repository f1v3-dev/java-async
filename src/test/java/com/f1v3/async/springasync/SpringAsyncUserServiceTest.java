package com.f1v3.async.springasync;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
class SpringAsyncUserServiceTest {

    @Autowired
    private SpringAsyncUserService springAsyncUserService;

    @Test
    void testBasicUserRegistration() {
        log.info("==================== Spring Async 방식 기본 사용자 등록 테스트 ====================");

        long start = System.currentTimeMillis();
        springAsyncUserService.registerUser("spring-user", "spring@example.com");
        long end = System.currentTimeMillis();

        log.info("Spring Async 방식 소요시간: {}ms", end - start);
    }

    @Test
    void testAsyncWithResult() throws ExecutionException, InterruptedException {
        log.info("==================== Spring Async 결과 반환 테스트 ====================");

        long start = System.currentTimeMillis();
        CompletableFuture<String> future = springAsyncUserService.registerUserAsyncWithResult("spring-async-user", "spring-async@example.com");

        String result = future.get(); // 블로킹 대기
        long end = System.currentTimeMillis();

        log.info("Spring Async 결과: {}", result);
        log.info("Spring Async 결과 반환 소요시간: {}ms", end - start);
    }

    @Test
    void testBulkProcessing() {
        log.info("==================== Spring Async 방식 대량 처리 테스트 ====================");

        int userCount = 12;
        long bulkStart = System.currentTimeMillis();

        List<CompletableFuture<Void>> futures = IntStream.range(0, userCount)
            .mapToObj(i -> springAsyncUserService.registerUserAsync("bulk-spring-" + i, "bulk-spring-" + i + "@example.com"))
            .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long bulkEnd = System.currentTimeMillis();

        log.info("Spring Async 방식 {}명 처리 소요시간: {}ms", userCount, bulkEnd - bulkStart);
        log.info("평균 처리시간: {}ms/user", (bulkEnd - bulkStart) / userCount);
    }

    @Test
    void testAsyncMethodChaining() throws ExecutionException, InterruptedException {
        log.info("==================== Spring Async 메서드 체이닝 테스트 ====================");

        CompletableFuture<String> future1 = springAsyncUserService.registerUserAsyncWithResult("chain1", "chain1@example.com");
        CompletableFuture<String> future2 = springAsyncUserService.registerUserAsyncWithResult("chain2", "chain2@example.com");

        CompletableFuture<String> combinedFuture = future1.thenCombine(future2, (result1, result2) -> {
            log.info("두 Spring Async 작업 모두 완료됨");
            return "결합된 결과: " + result1 + " & " + result2;
        });

        String result = combinedFuture.get();
        log.info("Spring Async 체이닝 결과: {}", result);
    }

    @Test
    void testThreadPoolConfiguration() {
        log.info("==================== Spring Async 스레드 풀 테스트 ====================");

        int taskCount = 20;
        long start = System.currentTimeMillis();

        // 동시에 여러 작업 제출하여 스레드 풀 동작 확인
        List<CompletableFuture<Void>> futures = IntStream.range(0, taskCount)
            .mapToObj(i -> springAsyncUserService.registerUserAsync("pool-test-" + i, "pool-test-" + i + "@example.com"))
            .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long end = System.currentTimeMillis();

        log.info("Spring Async 스레드 풀 {}개 작업 처리 소요시간: {}ms", taskCount, end - start);
        log.info("평균 처리시간: {}ms/task", (end - start) / taskCount);
    }

    @Test
    void testExceptionHandling() {
        log.info("==================== Spring Async 예외 처리 테스트 ====================");

        CompletableFuture<String> future = springAsyncUserService.registerUserWithException("exception-user", "exception@example.com");

        future.handle((result, throwable) -> {
            if (throwable != null) {
                log.info("Spring Async 예외가 성공적으로 처리됨: {}", throwable.getMessage());
                return "예외 처리 완료";
            } else {
                log.info("Spring Async 정상 처리 결과: {}", result);
                return result;
            }
        }).join();
    }

    @Test
    void testVoidAsyncMethod() throws ExecutionException, InterruptedException {
        log.info("==================== Spring Async Void 반환 테스트 ====================");

        long start = System.currentTimeMillis();
        CompletableFuture<Void> future = springAsyncUserService.registerUserAsync("void-user", "void@example.com");

        future.get(); // 블로킹 대기
        long end = System.currentTimeMillis();

        log.info("Spring Async Void 처리 완료");
        log.info("Spring Async Void 처리 소요시간: {}ms", end - start);
    }
}
