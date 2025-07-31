package com.f1v3.async.executorservice;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
class ExecutorServiceUserServiceTest {

    @Autowired
    private ExecutorServiceUserService executorServiceUserService;

    @AfterEach
    void tearDown() {
        executorServiceUserService.shutdown();
    }

    @Test
    void testBasicUserRegistration() {
        log.info("==================== ExecutorService 방식 기본 사용자 등록 테스트 ====================");

        long start = System.currentTimeMillis();
        executorServiceUserService.registerUser("executor-user", "executor@example.com");
        long end = System.currentTimeMillis();

        log.info("ExecutorService 방식 소요시간: {}ms", end - start);
    }

    @Test
    void testBulkProcessing() {
        log.info("==================== ExecutorService 방식 대량 처리 테스트 ====================");

        int userCount = 15;
        long bulkStart = System.currentTimeMillis();

        List<Future<String>> futures = IntStream.range(0, userCount)
                .mapToObj(i -> executorServiceUserService.registerUserWithFuture("bulk-executor-" + i, "bulk-executor-" + i + "@example.com"))
                .toList();

        // 모든 작업 완료 대기
        for (Future<String> future : futures) {
            try {
                String result = future.get();
                log.debug("작업 완료 결과: {}", result);
            } catch (Exception e) {
                log.error("작업 실행 중 오류 발생", e);
            }
        }

        long bulkEnd = System.currentTimeMillis();
        log.info("ExecutorService 방식 {}명 처리 소요시간: {}ms", userCount, bulkEnd - bulkStart);
        log.info("평균 처리시간: {}ms/user", (bulkEnd - bulkStart) / userCount);
    }

    @Test
    void testThreadPoolPerformance() {
        log.info("==================== ExecutorService 스레드 풀 성능 테스트 ====================");

        int taskCount = 20;
        long start = System.currentTimeMillis();

        // 동시에 여러 작업 제출
        List<Future<String>> futures = IntStream.range(0, taskCount)
                .mapToObj(i -> executorServiceUserService.registerUserWithFuture("pool-test-" + i, "pool-test-" + i + "@example.com"))
                .toList();

        // 모든 작업 완료 대기
        for (Future<String> future : futures) {
            try {
                String result = future.get();
                log.debug("스레드 풀 작업 완료: {}", result);
            } catch (Exception e) {
                log.error("작업 실행 중 오류 발생", e);
            }
        }

        long end = System.currentTimeMillis();
        log.info("스레드 풀 {}개 작업 처리 소요시간: {}ms", taskCount, end - start);
        log.info("평균 처리시간: {}ms/task", (end - start) / taskCount);
    }
}
