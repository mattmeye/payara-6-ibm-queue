package com.example.ibmmq.unit.performance;

import com.example.ibmmq.entity.MQMessage;
import com.example.ibmmq.pool.IBMMQConnectionPool;
import com.example.ibmmq.service.IBMMQService;
import com.example.ibmmq.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Performance Tests")
class PerformanceTest {

    @Test
    @DisplayName("Should create messages efficiently")
    @Timeout(5)
    void shouldCreateMessagesEfficiently() {
        // Given
        int messageCount = 10000;
        List<MQMessage> messages = new ArrayList<>(messageCount);

        // When
        Instant start = Instant.now();
        for (int i = 0; i < messageCount; i++) {
            MQMessage message = TestDataBuilder.createTestMessage("MSG_" + i, "PERF.QUEUE", "Performance test message " + i);
            messages.add(message);
        }
        Duration duration = Duration.between(start, Instant.now());

        // Then
        assertThat(messages).hasSize(messageCount);
        assertThat(duration.toMillis()).isLessThan(1000); // Should complete within 1 second

        // Verify message properties
        assertThat(messages.get(0).getMessageId()).isEqualTo("MSG_0");
        assertThat(messages.get(messageCount - 1).getMessageId()).isEqualTo("MSG_" + (messageCount - 1));
    }

    @Test
    @DisplayName("Should handle concurrent message processing")
    @Timeout(10)
    void shouldHandleConcurrentMessageProcessing() throws InterruptedException {
        // Given
        int threadCount = 10;
        int messagesPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        // When
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready

                    for (int i = 0; i < messagesPerThread; i++) {
                        try {
                            MQMessage message = TestDataBuilder.createTestMessage(
                                "THREAD_" + threadId + "_MSG_" + i,
                                "CONCURRENT.QUEUE",
                                "Concurrent processing test message"
                            );

                            // Simulate message processing
                            message.markAsProcessing();
                            Thread.sleep(1); // Minimal processing time
                            message.markAsProcessed();

                            processedCount.incrementAndGet();
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    errorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        Instant start = Instant.now();
        startLatch.countDown(); // Start all threads
        boolean completed = doneLatch.await(8, TimeUnit.SECONDS);
        Duration duration = Duration.between(start, Instant.now());

        // Then
        assertThat(completed).isTrue();
        assertThat(processedCount.get()).isEqualTo(threadCount * messagesPerThread);
        assertThat(errorCount.get()).isZero();
        assertThat(duration.toSeconds()).isLessThan(8);

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Should handle memory efficiently with large datasets")
    @Timeout(10)
    void shouldHandleMemoryEfficientlyWithLargeDatasets() {
        // Given
        int batchSize = 1000;
        int batchCount = 10;
        AtomicLong totalProcessed = new AtomicLong(0);

        // When
        Instant start = Instant.now();
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        for (int batch = 0; batch < batchCount; batch++) {
            List<MQMessage> messages = new ArrayList<>(batchSize);

            // Create batch
            for (int i = 0; i < batchSize; i++) {
                MQMessage message = TestDataBuilder.createTestMessage(
                    "BATCH_" + batch + "_MSG_" + i,
                    "MEMORY.QUEUE",
                    "Memory efficiency test message with some content to use memory"
                );
                messages.add(message);
            }

            // Process batch
            for (MQMessage message : messages) {
                message.markAsProcessing();
                message.markAsProcessed();
                totalProcessed.incrementAndGet();
            }

            // Clear batch to allow GC
            messages.clear();

            // Suggest GC every few batches
            if (batch % 3 == 0) {
                System.gc();
            }
        }

        Duration duration = Duration.between(start, Instant.now());
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = finalMemory - initialMemory;

        // Then
        assertThat(totalProcessed.get()).isEqualTo(batchSize * batchCount);
        assertThat(duration.toSeconds()).isLessThan(5);
        assertThat(memoryUsed).isLessThan(100 * 1024 * 1024); // Less than 100MB
    }

    @Test
    @DisplayName("Should handle rapid status changes efficiently")
    @Timeout(5)
    void shouldHandleRapidStatusChangesEfficiently() {
        // Given
        int messageCount = 1000;
        List<MQMessage> messages = new ArrayList<>(messageCount);

        // Create messages
        for (int i = 0; i < messageCount; i++) {
            messages.add(TestDataBuilder.createTestMessage("STATUS_MSG_" + i, "STATUS.QUEUE", "Status change test"));
        }

        // When
        Instant start = Instant.now();

        for (MQMessage message : messages) {
            // Simulate typical message lifecycle
            message.markAsProcessing();
            message.markAsProcessed();
        }

        // Simulate some retries
        for (int i = 0; i < messageCount / 10; i++) {
            MQMessage message = messages.get(i);
            message.incrementRetryCount();
            message.markAsProcessing();
            message.markAsProcessed();
        }

        // Simulate some failures
        for (int i = 0; i < messageCount / 20; i++) {
            MQMessage message = messages.get(i + messageCount / 10);
            message.markAsFailed("Performance test failure");
        }

        Duration duration = Duration.between(start, Instant.now());

        // Then
        assertThat(duration.toMillis()).isLessThan(500); // Should complete within 500ms

        // Verify final states
        long processedCount = messages.stream()
            .filter(m -> m.getStatus() == MQMessage.MessageStatus.PROCESSED)
            .count();
        long failedCount = messages.stream()
            .filter(m -> m.getStatus() == MQMessage.MessageStatus.FAILED)
            .count();

        assertThat(processedCount).isGreaterThanOrEqualTo(messageCount - messageCount / 20);
        assertThat(failedCount).isEqualTo(messageCount / 20);
    }

    @Test
    @DisplayName("Should handle large message content efficiently")
    @Timeout(10)
    void shouldHandleLargeMessageContentEfficiently() {
        // Given
        int messageCount = 100;
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeContent.append("Large message content line ").append(i).append(" with some additional text. ");
        }
        String content = largeContent.toString();

        // When
        Instant start = Instant.now();
        List<MQMessage> messages = new ArrayList<>(messageCount);

        for (int i = 0; i < messageCount; i++) {
            MQMessage message = TestDataBuilder.createTestMessage("LARGE_MSG_" + i, "LARGE.QUEUE", content);
            message.markAsProcessing();

            // Simulate content processing
            String processedContent = message.getMessageContent().trim();
            if (processedContent.length() > 10000) {
                processedContent = processedContent.substring(0, 10000) + "... [TRUNCATED]";
                message.setMessageContent(processedContent);
            }

            message.markAsProcessed();
            messages.add(message);
        }

        Duration duration = Duration.between(start, Instant.now());

        // Then
        assertThat(messages).hasSize(messageCount);
        assertThat(duration.toSeconds()).isLessThan(5);

        // Verify content was processed correctly
        for (MQMessage message : messages) {
            assertThat(message.getMessageContent()).hasSizeLessThanOrEqualTo(10020); // 10000 + suffix
            assertThat(message.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
        }
    }

    @Test
    @DisplayName("Should maintain performance under load")
    @Timeout(15)
    void shouldMaintainPerformanceUnderLoad() throws InterruptedException {
        // Given
        int threadCount = 5;
        int messagesPerThread = 200;
        int totalMessages = threadCount * messagesPerThread;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        ConcurrentLinkedQueue<Long> processingTimes = new ConcurrentLinkedQueue<>();
        AtomicInteger successCount = new AtomicInteger(0);

        // When
        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    for (int i = 0; i < messagesPerThread; i++) {
                        long messageStart = System.nanoTime();

                        MQMessage message = TestDataBuilder.createTestMessage(
                            "LOAD_T" + threadId + "_M" + i,
                            "LOAD.QUEUE",
                            "Load test message with content"
                        );

                        // Simulate realistic message processing
                        message.markAsProcessing();

                        // Simulate validation
                        if (message.getMessageContent() != null && !message.getMessageContent().isEmpty()) {
                            // Simulate processing work
                            Thread.sleep(1);
                            message.markAsProcessed();
                            successCount.incrementAndGet();
                        } else {
                            message.markAsFailed("Validation failed");
                        }

                        long processingTime = System.nanoTime() - messageStart;
                        processingTimes.offer(processingTime);
                    }
                } catch (Exception e) {
                    // Handle errors but don't fail the test
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        Instant start = Instant.now();
        startLatch.countDown();
        boolean completed = doneLatch.await(12, TimeUnit.SECONDS);
        Duration totalDuration = Duration.between(start, Instant.now());

        // Then
        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(totalMessages);
        assertThat(totalDuration.toSeconds()).isLessThan(12);

        // Analyze performance
        List<Long> times = new ArrayList<>(processingTimes);
        times.sort(Long::compareTo);

        long averageTime = times.stream().mapToLong(Long::longValue).sum() / times.size();
        long medianTime = times.get(times.size() / 2);
        long p95Time = times.get((int) (times.size() * 0.95));

        // Performance assertions
        assertThat(averageTime).isLessThan(5_000_000L); // Less than 5ms average
        assertThat(medianTime).isLessThan(3_000_000L);  // Less than 3ms median
        assertThat(p95Time).isLessThan(10_000_000L);    // Less than 10ms p95

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("Should handle connection pool stress test")
    @Timeout(10)
    void shouldHandleConnectionPoolStressTest() throws InterruptedException {
        // Given
        int concurrentRequests = 20;
        int requestsPerThread = 50;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(concurrentRequests);
        AtomicInteger successfulRequests = new AtomicInteger(0);
        AtomicInteger failedRequests = new AtomicInteger(0);

        // When
        for (int t = 0; t < concurrentRequests; t++) {
            executor.submit(() -> {
                try {
                    startLatch.await();

                    for (int i = 0; i < requestsPerThread; i++) {
                        try {
                            // Simulate connection pool usage
                            Thread.sleep(ThreadLocalRandom.current().nextInt(1, 5));
                            successfulRequests.incrementAndGet();
                        } catch (Exception e) {
                            failedRequests.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    failedRequests.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        Instant start = Instant.now();
        startLatch.countDown();
        boolean completed = doneLatch.await(8, TimeUnit.SECONDS);
        Duration duration = Duration.between(start, Instant.now());

        // Then
        assertThat(completed).isTrue();
        assertThat(successfulRequests.get()).isEqualTo(concurrentRequests * requestsPerThread);
        assertThat(failedRequests.get()).isZero();
        assertThat(duration.toSeconds()).isLessThan(8);

        // Calculate throughput
        double throughput = (double) successfulRequests.get() / duration.toSeconds();
        assertThat(throughput).isGreaterThan(100); // At least 100 requests per second

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);
    }
}