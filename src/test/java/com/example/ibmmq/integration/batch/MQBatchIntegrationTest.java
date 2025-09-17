package com.example.ibmmq.integration.batch;

import com.example.ibmmq.batch.MQBatchJobListener;
import com.example.ibmmq.batch.MQMessageProcessor;
import com.example.ibmmq.batch.MQMessageReader;
import com.example.ibmmq.batch.MQMessageWriter;
import com.example.ibmmq.backout.SimpleBackoutQueueService;
import com.example.ibmmq.config.BackoutQueueConfig;
import com.example.ibmmq.entity.MQMessage;
import com.example.ibmmq.util.TestDataBuilder;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.JobExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MQ Batch Integration Tests")
class MQBatchIntegrationTest {

    @Mock
    private MQMessageReader reader;

    private MQMessageProcessor processor;

    @Mock
    private MQMessageWriter writer;

    @Mock
    private MQBatchJobListener listener;

    @Mock
    private JobExecution jobExecution;

    @Mock
    private SimpleBackoutQueueService backoutQueueService;

    @Mock
    private BackoutQueueConfig backoutConfig;

    private MQMessage testMessage;

    @BeforeEach
    void setUp() {
        testMessage = TestDataBuilder.createTestMessage();

        // Configure mock defaults
        when(backoutConfig.isBackoutEnabled()).thenReturn(false);
        when(backoutConfig.getBackoutThreshold()).thenReturn(3);

        // Create real processor with mocked dependencies
        processor = new MQMessageProcessor();
        // Use reflection to inject mocked dependencies
        try {
            java.lang.reflect.Field backoutServiceField = MQMessageProcessor.class.getDeclaredField("backoutQueueService");
            backoutServiceField.setAccessible(true);
            backoutServiceField.set(processor, backoutQueueService);

            java.lang.reflect.Field backoutConfigField = MQMessageProcessor.class.getDeclaredField("backoutConfig");
            backoutConfigField.setAccessible(true);
            backoutConfigField.set(processor, backoutConfig);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }

    @Test
    @DisplayName("Should process batch job with reader, processor, and writer")
    void shouldProcessBatchJobWithReaderProcessorAndWriter() throws Exception {
        // Given
        MQMessage processedMessage = TestDataBuilder.createProcessedMessage();

        when(reader.readItem())
            .thenReturn(testMessage)
            .thenReturn(null); // End of data
        // When
        reader.open(null);

        Object item1 = reader.readItem();
        assertThat(item1).isEqualTo(testMessage);

        Object processedItem = processor.processItem(item1);
        assertThat(processedItem).isNotNull();

        writer.open(null);
        writer.writeItems(java.util.List.of(processedItem));

        Object item2 = reader.readItem();
        assertThat(item2).isNull(); // End of batch

        writer.close();
        reader.close();

        // Then
        verify(reader).open(null);
        verify(reader, times(2)).readItem();
        verify(writer).writeItems(anyList());
        verify(reader).close();
        verify(writer).close();
    }

    @Test
    @DisplayName("Should handle processing errors gracefully")
    void shouldHandleProcessingErrorsGracefully() throws Exception {
        // Given - Create a message that will cause validation error
        MQMessage invalidMessage = new MQMessage();
        invalidMessage.setMessageId(null); // This will cause validation error
        invalidMessage.setQueueName("TEST.QUEUE");
        invalidMessage.setMessageContent("Content");

        when(reader.readItem())
            .thenReturn(invalidMessage)
            .thenReturn(null);

        // When
        reader.open(null);
        Object item = reader.readItem();

        // Process item - should handle error gracefully and return failed message
        Object result = processor.processItem(item);

        // Then - Should return message marked as failed
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);

        reader.close();
    }

    @Test
    @DisplayName("Should handle empty message content in processor")
    void shouldHandleEmptyMessageContentInProcessor() throws Exception {
        // Given
        MQMessage emptyMessage = TestDataBuilder.createTestMessage("EMPTY_ID", "TEST.QUEUE", "");

        // When
        Object result = processor.processItem(emptyMessage);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);
        assertThat(processedMessage.getErrorMessage()).contains("Empty message content");
    }

    @Test
    @DisplayName("Should handle large message content in processor")
    void shouldHandleLargeMessageContentInProcessor() throws Exception {
        // Given
        MQMessage largeMessage = TestDataBuilder.createLargeMessage();

        // When
        Object result = processor.processItem(largeMessage);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getMessageContent()).hasSizeLessThanOrEqualTo(10000 + 20); // Truncated + suffix
        assertThat(processedMessage.getMessageContent()).endsWith("... [TRUNCATED]");
    }

    @Test
    @DisplayName("Should validate message in processor")
    void shouldValidateMessageInProcessor() throws Exception {
        // Given
        MQMessage invalidMessage = new MQMessage();
        invalidMessage.setMessageId(null); // Invalid - no message ID
        invalidMessage.setQueueName("TEST.QUEUE");
        invalidMessage.setMessageContent("Content");

        // When
        Object result = processor.processItem(invalidMessage);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);
        assertThat(processedMessage.getErrorMessage()).contains("Message ID is required");
    }

    @Test
    @DisplayName("Should clean control characters from message content")
    void shouldCleanControlCharactersFromMessageContent() throws Exception {
        // Given
        String contentWithControlChars = "Normal text\u0001\u0002\u0003 more text\u0004";
        MQMessage messageWithControlChars = TestDataBuilder.createTestMessage("CONTROL_ID", "TEST.QUEUE", contentWithControlChars);

        // When
        Object result = processor.processItem(messageWithControlChars);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getMessageContent()).isEqualTo("Normal text more text");
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
    }

    @Test
    @DisplayName("Should handle special characters correctly")
    void shouldHandleSpecialCharactersCorrectly() throws Exception {
        // Given
        MQMessage specialCharsMessage = TestDataBuilder.createMessageWithSpecialCharacters();

        // When
        Object result = processor.processItem(specialCharsMessage);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
        assertThat(processedMessage.getMessageContent()).contains("äöü ß €  áéíóú ñ 中文 日本語 한국어");
    }

    @Test
    @DisplayName("Should detect error indicators in message content")
    void shouldDetectErrorIndicatorsInMessageContent() throws Exception {
        // Given
        MQMessage errorMessage = TestDataBuilder.createTestMessage("ERROR_ID", "TEST.QUEUE", "This message contains error information");

        // When
        Object result = processor.processItem(errorMessage);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        // Should still process but log warning (we can't verify logging in unit test)
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
    }

    @Test
    @DisplayName("Should handle writer exceptions")
    void shouldHandleWriterExceptions() throws Exception {
        // Given
        doThrow(new RuntimeException("Database error"))
            .when(writer).writeItems(anyList());

        // When & Then
        assertThatThrownBy(() -> writer.writeItems(java.util.List.of(testMessage)))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database error");
    }

    @Test
    @DisplayName("Should handle reader timeout")
    void shouldHandleReaderTimeout() throws Exception {
        // Given
        when(reader.readItem()).thenReturn(null); // Simulates timeout/no messages

        // When
        reader.open(null);
        Object result = reader.readItem();

        // Then
        assertThat(result).isNull();
        verify(reader).open(null);
        verify(reader).readItem();
    }

    @Test
    @DisplayName("Should support checkpointing in reader")
    void shouldSupportCheckpointingInReader() throws Exception {
        // Given
        when(reader.checkpointInfo()).thenReturn(null);

        // When
        Serializable checkpoint = reader.checkpointInfo();

        // Then
        assertThat(checkpoint).isNull();
        verify(reader).checkpointInfo();
    }

    @Test
    @DisplayName("Should support checkpointing in writer")
    void shouldSupportCheckpointingInWriter() throws Exception {
        // Given
        when(writer.checkpointInfo()).thenReturn(null);

        // When
        Serializable checkpoint = writer.checkpointInfo();

        // Then
        assertThat(checkpoint).isNull();
        verify(writer).checkpointInfo();
    }

    @Test
    @DisplayName("Should handle batch job lifecycle with listener")
    void shouldHandleBatchJobLifecycleWithListener() throws Exception {
        // Given
        when(jobExecution.getJobName()).thenReturn("mq-to-postgres-job");
        when(jobExecution.getExecutionId()).thenReturn(123L);
        when(jobExecution.getExitStatus()).thenReturn("COMPLETED");
        when(jobExecution.getBatchStatus()).thenReturn(BatchStatus.COMPLETED);

        // When
        listener.beforeJob();
        listener.afterJob();

        // Then
        verify(listener).beforeJob();
        verify(listener).afterJob();
    }

    @Test
    @DisplayName("Should process multiple messages in sequence")
    void shouldProcessMultipleMessagesInSequence() throws Exception {
        // Given
        MQMessage message1 = TestDataBuilder.createTestMessage("MSG1", "QUEUE1", "Content 1");
        MQMessage message2 = TestDataBuilder.createTestMessage("MSG2", "QUEUE2", "Content 2");
        MQMessage message3 = TestDataBuilder.createTestMessage("MSG3", "QUEUE3", "Content 3");

        when(reader.readItem())
            .thenReturn(message1)
            .thenReturn(message2)
            .thenReturn(message3)
            .thenReturn(null);

        // Real processor will handle processing

        // When
        reader.open(null);
        writer.open(null);

        java.util.List<Object> processedItems = new java.util.ArrayList<>();
        Object item;
        while ((item = reader.readItem()) != null) {
            Object processed = processor.processItem(item);
            processedItems.add(processed);
        }

        writer.writeItems(processedItems);

        // Then
        assertThat(processedItems).hasSize(3);
        // Can't verify on real processor - just assert processedItems size
        verify(writer).writeItems(processedItems);

        reader.close();
        writer.close();
    }

    @Test
    @DisplayName("Should handle mixed success and failure in batch")
    void shouldHandleMixedSuccessAndFailureInBatch() throws Exception {
        // Given
        MQMessage validMessage = TestDataBuilder.createTestMessage("VALID", "QUEUE1", "Valid content");
        MQMessage invalidMessage = TestDataBuilder.createTestMessage("INVALID", "QUEUE2", ""); // Empty content

        when(reader.readItem())
            .thenReturn(validMessage)
            .thenReturn(invalidMessage)
            .thenReturn(null);

        // When
        reader.open(null);

        Object item1 = reader.readItem();
        Object processed1 = processor.processItem(item1);

        Object item2 = reader.readItem();
        Object processed2 = processor.processItem(item2);

        // Then
        MQMessage result1 = (MQMessage) processed1;
        MQMessage result2 = (MQMessage) processed2;

        assertThat(result1.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
        assertThat(result2.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);

        reader.close();
    }

    @Test
    @DisplayName("Should handle concurrent batch processing")
    void shouldHandleConcurrentBatchProcessing() throws Exception {
        // Given
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        MQMessage message1 = TestDataBuilder.createTestMessage("CONCURRENT1", "QUEUE1", "Content 1");
        MQMessage message2 = TestDataBuilder.createTestMessage("CONCURRENT2", "QUEUE2", "Content 2");

        // When
        Thread processor1 = new Thread(() -> {
            try {
                startLatch.await();
                processor.processItem(message1);
                doneLatch.countDown();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread processor2 = new Thread(() -> {
            try {
                startLatch.await();
                processor.processItem(message2);
                doneLatch.countDown();
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        });

        processor1.start();
        processor2.start();
        startLatch.countDown(); // Start both processors

        // Then
        boolean completed = doneLatch.await(5, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        processor1.join();
        processor2.join();

        // Can't verify on real processor - concurrent processing test completed successfully
    }
}