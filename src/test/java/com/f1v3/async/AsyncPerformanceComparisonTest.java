package com.f1v3.async;

import com.f1v3.async.completablefuture.CompletableFutureUserService;
import com.f1v3.async.executorservice.ExecutorServiceUserService;
import com.f1v3.async.future.FutureUserService;
import com.f1v3.async.springasync.SpringAsyncUserService;
import com.f1v3.async.thread.ThreadUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

/**
 * 모든 비동기 방식의 성능을 비교하는 통합 테스트
 *
 * @author Seungjo, Jeong
 */
@Slf4j
@SpringBootTest
class AsyncPerformanceComparisonTest {

    @Autowired
    private ThreadUserService threadUserService;

    @Autowired
    private ExecutorServiceUserService executorServiceUserService;

    @Autowired
    private FutureUserService futureUserService;

    @Autowired
    private CompletableFutureUserService completableFutureUserService;

    @Autowired
    private SpringAsyncUserService springAsyncUserService;

    @AfterEach
    void cleanup() {
        // 리소스 정리
        executorServiceUserService.shutdown();
        futureUserService.shutdown();
        completableFutureUserService.shutdown();
    }

    @Test
    void compareAllAsyncMethods() throws InterruptedException {
        log.info("==================== 모든 비동기 방식 성능 비교 테스트 ====================");

        int userCount = 10;

        // 1. Thread 방식
        long threadStart = System.currentTimeMillis();
        List<Thread> threads = IntStream.range(0, userCount)
            .mapToObj(i -> new Thread(() ->
                threadUserService.registerUser("thread-" + i, "thread-" + i + "@example.com")))
            .toList();
        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }
        long threadEnd = System.currentTimeMillis();
        long threadTime = threadEnd - threadStart;

        // 2. ExecutorService 방식
        long executorStart = System.currentTimeMillis();
        List<Future<String>> executorFutures = IntStream.range(0, userCount)
            .mapToObj(i -> executorServiceUserService.registerUserWithFuture("executor-" + i, "executor-" + i + "@example.com"))
            .toList();
        for (Future<String> future : executorFutures) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("ExecutorService 작업 실행 중 오류", e);
            }
        }
        long executorEnd = System.currentTimeMillis();
        long executorTime = executorEnd - executorStart;

        // 3. Future 방식
        long futureStart = System.currentTimeMillis();
        List<Future<String>> futureFutures = IntStream.range(0, userCount)
            .mapToObj(i -> futureUserService.registerUserWithResult("future-" + i, "future-" + i + "@example.com"))
            .toList();
        for (Future<String> future : futureFutures) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("Future 작업 실행 중 오류", e);
            }
        }
        long futureEnd = System.currentTimeMillis();
        long futureTime = futureEnd - futureStart;

        // 4. CompletableFuture 방식
        long cfStart = System.currentTimeMillis();
        List<CompletableFuture<Void>> cfFutures = IntStream.range(0, userCount)
            .mapToObj(i -> CompletableFuture.runAsync(() ->
                completableFutureUserService.registerUser("cf-" + i, "cf-" + i + "@example.com")))
            .toList();
        CompletableFuture.allOf(cfFutures.toArray(new CompletableFuture[0])).join();
        long cfEnd = System.currentTimeMillis();
        long cfTime = cfEnd - cfStart;

        // 5. Spring Async 방식
        long springStart = System.currentTimeMillis();
        List<CompletableFuture<Void>> springFutures = IntStream.range(0, userCount)
            .mapToObj(i -> springAsyncUserService.registerUserAsync("spring-" + i, "spring-" + i + "@example.com"))
            .toList();
        CompletableFuture.allOf(springFutures.toArray(new CompletableFuture[0])).join();
        long springEnd = System.currentTimeMillis();
        long springTime = springEnd - springStart;

        // 결과 출력
        log.info("==================== 성능 비교 결과 ({} 사용자) ====================", userCount);
        log.info("Thread 방식:           {}ms (평균: {}ms/user)", threadTime, threadTime / userCount);
        log.info("ExecutorService 방식:  {}ms (평균: {}ms/user)", executorTime, executorTime / userCount);
        log.info("Future 방식:           {}ms (평균: {}ms/user)", futureTime, futureTime / userCount);
        log.info("CompletableFuture 방식: {}ms (평균: {}ms/user)", cfTime, cfTime / userCount);
        log.info("Spring Async 방식:     {}ms (평균: {}ms/user)", springTime, springTime / userCount);

        // 가장 빠른 방식 찾기
        String fastest = "Thread";
        long fastestTime = threadTime;

        if (executorTime < fastestTime) {
            fastest = "ExecutorService";
            fastestTime = executorTime;
        }
        if (futureTime < fastestTime) {
            fastest = "Future";
            fastestTime = futureTime;
        }
        if (cfTime < fastestTime) {
            fastest = "CompletableFuture";
            fastestTime = cfTime;
        }
        if (springTime < fastestTime) {
            fastest = "Spring Async";
            fastestTime = springTime;
        }

        log.info("==================== 결론 ====================");
        log.info("가장 빠른 방식: {} ({}ms)", fastest, fastestTime);
        log.info("========================================");
    }

    @Test
    void compareSingleOperationLatency() {
        log.info("==================== 단일 작업 지연시간 비교 ====================");

        // 각 방식별로 단일 작업의 시작 지연시간 측정

        // Thread 방식
        long threadStart = System.currentTimeMillis();
        threadUserService.registerUser("latency-thread", "latency-thread@example.com");
        long threadLatency = System.currentTimeMillis() - threadStart;

        // ExecutorService 방식
        long executorStart = System.currentTimeMillis();
        executorServiceUserService.registerUser("latency-executor", "latency-executor@example.com");
        long executorLatency = System.currentTimeMillis() - executorStart;

        // Future 방식
        long futureStart = System.currentTimeMillis();
        futureUserService.registerUser("latency-future", "latency-future@example.com");
        long futureLatency = System.currentTimeMillis() - futureStart;

        // CompletableFuture 방식
        long cfStart = System.currentTimeMillis();
        completableFutureUserService.registerUser("latency-cf", "latency-cf@example.com");
        long cfLatency = System.currentTimeMillis() - cfStart;

        // Spring Async 방식
        long springStart = System.currentTimeMillis();
        springAsyncUserService.registerUser("latency-spring", "latency-spring@example.com");
        long springLatency = System.currentTimeMillis() - springStart;

        log.info("==================== 단일 작업 지연시간 결과 ====================");
        log.info("Thread 방식:           {}ms", threadLatency);
        log.info("ExecutorService 방식:  {}ms", executorLatency);
        log.info("Future 방식:           {}ms", futureLatency);
        log.info("CompletableFuture 방식: {}ms", cfLatency);
        log.info("Spring Async 방식:     {}ms", springLatency);
        log.info("===================================================");
    }
}
