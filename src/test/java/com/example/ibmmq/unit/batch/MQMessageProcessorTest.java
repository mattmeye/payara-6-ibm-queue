package com.example.ibmmq.unit.batch;

import com.example.ibmmq.batch.MQMessageProcessor;
import com.example.ibmmq.backout.SimpleBackoutQueueService;
import com.example.ibmmq.config.BackoutQueueConfig;
import com.example.ibmmq.entity.MQMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MQMessageProcessor Comprehensive Tests")
class MQMessageProcessorTest {

    @InjectMocks
    private MQMessageProcessor messageProcessor;

    @Mock
    private SimpleBackoutQueueService backoutQueueService;

    @Mock
    private BackoutQueueConfig backoutConfig;

    @BeforeEach
    void setUp() {
        when(backoutConfig.isBackoutEnabled()).thenReturn(true);
        when(backoutConfig.getBackoutThreshold()).thenReturn(3);
    }

    private MQMessage createTestMessage(String messageId, String content) {
        MQMessage message = new MQMessage();
        message.setMessageId(messageId);
        message.setQueueName("TEST.QUEUE");
        message.setMessageContent(content);
        message.setMessageType("TEXT");
        return message;
    }

    @Test
    @DisplayName("Should process valid message successfully")
    void shouldProcessValidMessageSuccessfully() throws Exception {
        // Given
        MQMessage message = createTestMessage("MSG-001", "Valid message content");

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getMessageId()).isEqualTo("MSG-001");
        assertThat(processedMessage.getMessageContent()).isEqualTo("Valid message content");
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
    }

    @Test
    @DisplayName("Should return null for non-MQMessage input")
    void shouldReturnNullForNonMQMessageInput() throws Exception {
        // Given
        String notAnMQMessage = "This is not an MQMessage";

        // When
        Object result = messageProcessor.processItem(notAnMQMessage);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should handle null input")
    void shouldHandleNullInput() throws Exception {
        // When
        Object result = messageProcessor.processItem(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should trim whitespace from message content")
    void shouldTrimWhitespaceFromMessageContent() throws Exception {
        // Given
        MQMessage message = createTestMessage("MSG-002", "  Content with spaces  ");

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getMessageContent()).isEqualTo("Content with spaces");
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
    }

    @Test
    @DisplayName("Should mark message as failed for empty content")
    void shouldMarkMessageAsFailedForEmptyContent() throws Exception {
        // Given
        MQMessage message = createTestMessage("MSG-003", "   ");

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);
        assertThat(processedMessage.getErrorMessage()).isEqualTo("Empty message content");
    }

    @Test
    @DisplayName("Should mark message as failed for null content")
    void shouldMarkMessageAsFailedForNullContent() throws Exception {
        // Given
        MQMessage message = createTestMessage("MSG-004", null);

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);
        assertThat(processedMessage.getErrorMessage()).contains("Processing error");
    }

    @Test
    @DisplayName("Should truncate large message content")
    void shouldTruncateLargeMessageContent() throws Exception {
        // Given
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 15000; i++) {
            largeContent.append("A");
        }
        MQMessage message = createTestMessage("MSG-005", largeContent.toString());

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getMessageContent()).hasSize(10015); // 10000 + "... [TRUNCATED]"
        assertThat(processedMessage.getMessageContent()).endsWith("... [TRUNCATED]");
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
    }

    @Test
    @DisplayName("Should remove control characters from content")
    void shouldRemoveControlCharactersFromContent() throws Exception {
        // Given
        String contentWithControlChars = "Normal text\u0001with\u0002control\u0003chars\u0004but\r\nkeep\tthese";
        MQMessage message = createTestMessage("MSG-006", contentWithControlChars);

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getMessageContent()).isEqualTo("Normal textwithcontrolcharsbut\r\nkeep\tthese");
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
    }

    @Test
    @DisplayName("Should mark message as failed for null message ID")
    void shouldMarkMessageAsFailedForNullMessageId() throws Exception {
        // Given
        MQMessage message = createTestMessage(null, "Valid content");

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);
        assertThat(processedMessage.getErrorMessage()).contains("Message ID is required");
    }

    @Test
    @DisplayName("Should mark message as failed for empty message ID")
    void shouldMarkMessageAsFailedForEmptyMessageId() throws Exception {
        // Given
        MQMessage message = createTestMessage("   ", "Valid content");

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);
        assertThat(processedMessage.getErrorMessage()).contains("Message ID is required");
    }

    @Test
    @DisplayName("Should mark message as failed for null queue name")
    void shouldMarkMessageAsFailedForNullQueueName() throws Exception {
        // Given
        MQMessage message = createTestMessage("MSG-007", "Valid content");
        message.setQueueName(null);

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);
        assertThat(processedMessage.getErrorMessage()).contains("Queue name is required");
    }

    @Test
    @DisplayName("Should mark message as failed for empty queue name")
    void shouldMarkMessageAsFailedForEmptyQueueName() throws Exception {
        // Given
        MQMessage message = createTestMessage("MSG-008", "Valid content");
        message.setQueueName("   ");

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);
        assertThat(processedMessage.getErrorMessage()).contains("Queue name is required");
    }

    @Test
    @DisplayName("Should process message with error keywords but not fail")
    void shouldProcessMessageWithErrorKeywordsButNotFail() throws Exception {
        // Given
        MQMessage message = createTestMessage("MSG-009", "This message contains error and exception words but should still be processed");

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
        assertThat(processedMessage.getMessageContent()).contains("error");
        assertThat(processedMessage.getMessageContent()).contains("exception");
    }

    @Test
    @DisplayName("Should detect error indicators in content")
    void shouldDetectErrorIndicatorsInContent() throws Exception {
        // Given - test various error keywords
        String[] errorContents = {
            "System failed to process",
            "An exception occurred during processing",
            "ERROR: Invalid input parameters"
        };

        for (int i = 0; i < errorContents.length; i++) {
            MQMessage message = createTestMessage("MSG-010-" + i, errorContents[i]);

            // When
            Object result = messageProcessor.processItem(message);

            // Then
            assertThat(result).isInstanceOf(MQMessage.class);
            MQMessage processedMessage = (MQMessage) result;
            // Messages with error indicators should still be processed successfully
            // The processor only logs warnings for error indicators
            assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
        }
    }

    @Test
    @DisplayName("Should handle processing status transitions correctly")
    void shouldHandleProcessingStatusTransitionsCorrectly() throws Exception {
        // Given
        MQMessage message = createTestMessage("MSG-011", "Valid content");
        assertThat(message.getStatus()).isIn(MQMessage.MessageStatus.RECEIVED, null); // Initial state

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
        assertThat(processedMessage.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle exactly 10000 character content")
    void shouldHandleExactly10000CharacterContent() throws Exception {
        // Given - exactly 10000 characters
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            content.append("A");
        }
        MQMessage message = createTestMessage("MSG-012", content.toString());

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getMessageContent()).hasSize(10000);
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
    }

    @Test
    @DisplayName("Should handle content with mixed control characters")
    void shouldHandleContentWithMixedControlCharacters() throws Exception {
        // Given
        String content = "Start\u0000\u0001\u0002text\u0003\u0004\u0005middle\u0006\u0007\u0008\r\n\tEnd";
        MQMessage message = createTestMessage("MSG-013", content);

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getMessageContent()).isEqualTo("Starttextmiddle\r\n\tEnd");
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
    }

    @Test
    @DisplayName("Should handle empty string content after trimming")
    void shouldHandleEmptyStringContentAfterTrimming() throws Exception {
        // Given
        MQMessage message = createTestMessage("MSG-014", "");

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);
        assertThat(processedMessage.getErrorMessage()).isEqualTo("Empty message content");
    }

    @Test
    @DisplayName("Should maintain original message properties during processing")
    void shouldMaintainOriginalMessagePropertiesDuringProcessing() throws Exception {
        // Given
        MQMessage message = createTestMessage("MSG-015", "Test content");
        message.setCorrelationId("CORR-001");
        message.setPriority(5);
        message.setExpiry(System.currentTimeMillis() + 60000);
        message.setMessageType("TEXT");

        // When
        Object result = messageProcessor.processItem(message);

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage processedMessage = (MQMessage) result;
        assertThat(processedMessage.getMessageId()).isEqualTo("MSG-015");
        assertThat(processedMessage.getCorrelationId()).isEqualTo("CORR-001");
        assertThat(processedMessage.getPriority()).isEqualTo(5);
        assertThat(processedMessage.getMessageType()).isEqualTo("TEXT");
        assertThat(processedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
    }
}