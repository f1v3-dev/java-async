package com.f1v3.async.thread;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
class ThreadUserServiceTest {

    @Autowired
    private ThreadUserService threadUserService;

    @Test
    void testSyncVsAsyncComparison() throws InterruptedException {
        log.info("==================== Thread 방식 동기 vs 비동기 비교 테스트 ====================");

        // 1. 동기 방식 (기준점)
        log.info("===== 동기 방식 =====");
        long syncStart = System.currentTimeMillis();
        threadUserService.registerUserSync("sync-user", "sync@example.com");
        long syncEnd = System.currentTimeMillis();
        log.info("동기 방식 소요시간: {}ms", syncEnd - syncStart);

        Thread.sleep(1000);

        // 2. Thread 방식
        log.info("===== Thread 방식 =====");
        long threadStart = System.currentTimeMillis();
        threadUserService.registerUser("thread-user", "thread@example.com");
        long threadEnd = System.currentTimeMillis();
        log.info("Thread 방식 소요시간: {}ms", threadEnd - threadStart);

        log.info("성능 개선: {}ms 단축", (syncEnd - syncStart) - (threadEnd - threadStart));
    }

    @Test
    void testBulkProcessing() throws InterruptedException {
        log.info("==================== Thread 방식 대량 처리 테스트 ====================");

        int userCount = 10;
        long bulkStart = System.currentTimeMillis();

        List<Thread> threads = IntStream.range(0, userCount)
            .mapToObj(i -> new Thread(() ->
                threadUserService.registerUser("bulk-thread-" + i, "bulk-thread-" + i + "@example.com")))
            .toList();

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        long bulkEnd = System.currentTimeMillis();
        log.info("Thread 방식 {}명 처리 소요시간: {}ms", userCount, bulkEnd - bulkStart);
        log.info("평균 처리시간: {}ms/user", (bulkEnd - bulkStart) / userCount);
    }

    @Test
    void testSingleUserRegistration() {
        log.info("==================== Thread 방식 단일 사용자 등록 테스트 ====================");

        long start = System.currentTimeMillis();
        threadUserService.registerUser("test-user", "test@example.com");
        long end = System.currentTimeMillis();

        log.info("단일 사용자 등록 소요시간: {}ms", end - start);
    }
}
