package com.example.ibmmq.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mq_messages")
@NamedQueries({
    @NamedQuery(name = "MQMessage.findAll", query = "SELECT m FROM MQMessage m ORDER BY m.receivedAt DESC"),
    @NamedQuery(name = "MQMessage.findByStatus", query = "SELECT m FROM MQMessage m WHERE m.status = :status ORDER BY m.receivedAt"),
    @NamedQuery(name = "MQMessage.findByQueue", query = "SELECT m FROM MQMessage m WHERE m.queueName = :queueName ORDER BY m.receivedAt DESC")
})
public class MQMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", unique = true)
    private String messageId;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "queue_name", nullable = false)
    private String queueName;

    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;

    @Column(name = "message_type")
    private String messageType;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "expiry")
    private Long expiry;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "backout_count")
    private Integer backoutCount = 0;

    @Column(name = "backout_at")
    private LocalDateTime backoutAt;


    @Version
    private Long version;

    public enum MessageStatus {
        RECEIVED, PROCESSING, PROCESSED, FAILED, RETRY, BACKOUT
    }

    public MQMessage() {
        this.receivedAt = LocalDateTime.now();
        this.status = MessageStatus.RECEIVED;
    }

    public MQMessage(String messageId, String queueName, String messageContent) {
        this();
        this.messageId = messageId;
        this.queueName = queueName;
        this.messageContent = messageContent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Long getExpiry() {
        return expiry;
    }

    public void setExpiry(Long expiry) {
        this.expiry = expiry;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public void markAsProcessing() {
        this.status = MessageStatus.PROCESSING;
    }

    public void markAsProcessed() {
        this.status = MessageStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = MessageStatus.FAILED;
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }

    public void incrementRetryCount() {
        this.retryCount++;
        this.status = MessageStatus.RETRY;
    }

    public void markAsBackout(String errorMessage) {
        this.status = MessageStatus.BACKOUT;
        this.backoutCount++;
        this.backoutAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
        // Message bleibt final in Backout Queue
    }

    // New getters and setters
    public Integer getBackoutCount() { return backoutCount; }
    public void setBackoutCount(Integer backoutCount) { this.backoutCount = backoutCount; }

    public LocalDateTime getBackoutAt() { return backoutAt; }
    public void setBackoutAt(LocalDateTime backoutAt) { this.backoutAt = backoutAt; }

}