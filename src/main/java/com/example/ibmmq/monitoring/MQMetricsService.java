package com.example.ibmmq.monitoring;

import com.example.ibmmq.entity.MQMessage;
import com.example.ibmmq.pool.IBMMQConnectionPool;
import com.example.ibmmq.repository.MQMessageRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

@ApplicationScoped
public class MQMetricsService {

    private static final Logger LOGGER = Logger.getLogger(MQMetricsService.class.getName());

    @Inject
    private MeterRegistry meterRegistry;

    @Inject
    private MQMessageRepository messageRepository;

    @Inject
    private IBMMQConnectionPool connectionPool;

    private Counter messagesSentCounter;
    private Counter messagesReceivedCounter;
    private Counter messagesFailedCounter;
    private Counter messagesProcessedCounter;
    private Timer messageProcessingTimer;
    private Timer batchJobTimer;

    private final AtomicLong activeBatchJobs = new AtomicLong(0);
    private final AtomicLong totalBatchJobs = new AtomicLong(0);
    private final ConcurrentMap<String, AtomicLong> queueMessageCounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer.Sample> activeTimers = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        LOGGER.info("Initializing MQ Metrics Service");

        messagesSentCounter = Counter.builder("mq.messages.sent")
            .description("Total number of messages sent to MQ")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry);

        messagesReceivedCounter = Counter.builder("mq.messages.received")
            .description("Total number of messages received from MQ")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry);

        messagesFailedCounter = Counter.builder("mq.messages.failed")
            .description("Total number of failed messages")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry);

        messagesProcessedCounter = Counter.builder("mq.messages.processed")
            .description("Total number of successfully processed messages")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry);

        messageProcessingTimer = Timer.builder("mq.message.processing.duration")
            .description("Time taken to process messages")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry);

        batchJobTimer = Timer.builder("mq.batch.job.duration")
            .description("Time taken for batch job execution")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry);

        Gauge.builder("mq.connection.pool.active", this, self -> self.getActiveConnections())
            .description("Number of active connections in the pool")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry);

        Gauge.builder("mq.connection.pool.available", this, self -> self.getAvailableConnections())
            .description("Number of available connections in the pool")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry);

        Gauge.builder("mq.connection.pool.total", this, self -> self.getTotalConnections())
            .description("Total number of connections in the pool")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry);

        Gauge.builder("mq.batch.jobs.active", activeBatchJobs, AtomicLong::doubleValue)
            .description("Number of currently active batch jobs")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry);

        Gauge.builder("mq.batch.jobs.total", totalBatchJobs, AtomicLong::doubleValue)
            .description("Total number of batch jobs executed")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry);

        Gauge.builder("mq.messages.pending", this, self -> self.getPendingMessageCount())
            .description("Number of pending messages in database")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry);

        Gauge.builder("mq.messages.failed.total", this, self -> self.getFailedMessageCount())
            .description("Total number of failed messages in database")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry);

        LOGGER.info("MQ Metrics Service initialized successfully");
    }

    public void recordMessageSent(String queueName) {
        Counter.builder("mq.messages.sent")
            .tag("queue", queueName)
            .tag("status", "sent")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry)
            .increment();
        incrementQueueCounter(queueName + ".sent");
    }

    public void recordMessageReceived(String queueName) {
        Counter.builder("mq.messages.received")
            .tag("queue", queueName)
            .tag("status", "received")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry)
            .increment();
        incrementQueueCounter(queueName + ".received");
    }

    public void recordMessageFailed(String queueName, String errorType) {
        Counter.builder("mq.messages.failed")
            .tag("queue", queueName)
            .tag("error_type", errorType)
            .tag("status", "failed")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry)
            .increment();
        incrementQueueCounter(queueName + ".failed");
    }

    public void recordMessageProcessed(String queueName) {
        Counter.builder("mq.messages.processed")
            .tag("queue", queueName)
            .tag("status", "processed")
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry)
            .increment();
        incrementQueueCounter(queueName + ".processed");
    }

    public Timer.Sample startMessageProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopMessageProcessingTimer(Timer.Sample sample, String queueName, String status) {
        sample.stop(Timer.builder("mq.message.processing.duration")
            .tag("queue", queueName)
            .tag("status", status)
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry));
    }

    public Timer.Sample startBatchJobTimer() {
        activeBatchJobs.incrementAndGet();
        totalBatchJobs.incrementAndGet();
        return Timer.start(meterRegistry);
    }

    public void stopBatchJobTimer(Timer.Sample sample, String jobName, String status) {
        activeBatchJobs.decrementAndGet();
        sample.stop(Timer.builder("mq.batch.job.duration")
            .tag("job", jobName)
            .tag("status", status)
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry));
    }

    public void recordBatchJobMetrics(String jobName, long itemsRead, long itemsWritten, long itemsSkipped) {
        Counter.builder("mq.batch.items.read")
            .tag("job", jobName)
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry)
            .increment(itemsRead);

        Counter.builder("mq.batch.items.written")
            .tag("job", jobName)
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry)
            .increment(itemsWritten);

        Counter.builder("mq.batch.items.skipped")
            .tag("job", jobName)
            .tag("application", "payara-ibm-mq")
            .register(meterRegistry)
            .increment(itemsSkipped);
    }

    public void startMessageProcessing(String messageId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        activeTimers.put(messageId, sample);
    }

    public void endMessageProcessing(String messageId, String queueName, String status) {
        Timer.Sample sample = activeTimers.remove(messageId);
        if (sample != null) {
            stopMessageProcessingTimer(sample, queueName, status);
        }
    }

    public ApplicationMetrics getApplicationMetrics() {
        try {
            IBMMQConnectionPool.PoolStatus poolStatus = connectionPool.getPoolStatus();

            return new ApplicationMetrics(
                messagesSentCounter.count(),
                messagesReceivedCounter.count(),
                messagesFailedCounter.count(),
                messagesProcessedCounter.count(),
                poolStatus.getActiveConnections(),
                poolStatus.getAvailableConnections(),
                poolStatus.getTotalConnections(),
                activeBatchJobs.get(),
                totalBatchJobs.get(),
                getPendingMessageCount(),
                getFailedMessageCount()
            );
        } catch (Exception e) {
            LOGGER.warning("Failed to get application metrics: " + e.getMessage());
            return new ApplicationMetrics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
    }

    private void incrementQueueCounter(String counterName) {
        queueMessageCounts.computeIfAbsent(counterName, k -> new AtomicLong(0)).incrementAndGet();
    }

    private double getActiveConnections() {
        try {
            return connectionPool.getPoolStatus().getActiveConnections();
        } catch (Exception e) {
            LOGGER.warning("Failed to get active connections: " + e.getMessage());
            return 0;
        }
    }

    private double getAvailableConnections() {
        try {
            return connectionPool.getPoolStatus().getAvailableConnections();
        } catch (Exception e) {
            LOGGER.warning("Failed to get available connections: " + e.getMessage());
            return 0;
        }
    }

    private double getTotalConnections() {
        try {
            return connectionPool.getPoolStatus().getTotalConnections();
        } catch (Exception e) {
            LOGGER.warning("Failed to get total connections: " + e.getMessage());
            return 0;
        }
    }

    private double getPendingMessageCount() {
        try {
            return messageRepository.countByStatus(MQMessage.MessageStatus.RECEIVED) +
                   messageRepository.countByStatus(MQMessage.MessageStatus.PROCESSING) +
                   messageRepository.countByStatus(MQMessage.MessageStatus.RETRY);
        } catch (Exception e) {
            LOGGER.warning("Failed to get pending message count: " + e.getMessage());
            return 0;
        }
    }

    private double getFailedMessageCount() {
        try {
            return messageRepository.countByStatus(MQMessage.MessageStatus.FAILED);
        } catch (Exception e) {
            LOGGER.warning("Failed to get failed message count: " + e.getMessage());
            return 0;
        }
    }

    public static class ApplicationMetrics {
        private final double messagesSent;
        private final double messagesReceived;
        private final double messagesFailed;
        private final double messagesProcessed;
        private final int activeConnections;
        private final int availableConnections;
        private final int totalConnections;
        private final long activeBatchJobs;
        private final long totalBatchJobs;
        private final double pendingMessages;
        private final double failedMessages;

        public ApplicationMetrics(double messagesSent, double messagesReceived, double messagesFailed,
                                double messagesProcessed, int activeConnections, int availableConnections,
                                int totalConnections, long activeBatchJobs, long totalBatchJobs,
                                double pendingMessages, double failedMessages) {
            this.messagesSent = messagesSent;
            this.messagesReceived = messagesReceived;
            this.messagesFailed = messagesFailed;
            this.messagesProcessed = messagesProcessed;
            this.activeConnections = activeConnections;
            this.availableConnections = availableConnections;
            this.totalConnections = totalConnections;
            this.activeBatchJobs = activeBatchJobs;
            this.totalBatchJobs = totalBatchJobs;
            this.pendingMessages = pendingMessages;
            this.failedMessages = failedMessages;
        }

        public double getMessagesSent() { return messagesSent; }
        public double getMessagesReceived() { return messagesReceived; }
        public double getMessagesFailed() { return messagesFailed; }
        public double getMessagesProcessed() { return messagesProcessed; }
        public int getActiveConnections() { return activeConnections; }
        public int getAvailableConnections() { return availableConnections; }
        public int getTotalConnections() { return totalConnections; }
        public long getActiveBatchJobs() { return activeBatchJobs; }
        public long getTotalBatchJobs() { return totalBatchJobs; }
        public double getPendingMessages() { return pendingMessages; }
        public double getFailedMessages() { return failedMessages; }
    }
}