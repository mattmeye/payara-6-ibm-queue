package com.example.ibmmq.unit.batch;

import com.example.ibmmq.batch.MQMessageWriter;
import com.example.ibmmq.entity.MQMessage;
import com.example.ibmmq.repository.MQMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MQMessageWriter Comprehensive Tests")
class MQMessageWriterTest {

    @Mock
    private MQMessageRepository messageRepository;

    @InjectMocks
    private MQMessageWriter messageWriter;

    private MQMessage createTestMessage(String messageId, String content) {
        MQMessage message = new MQMessage();
        message.setMessageId(messageId);
        message.setQueueName("TEST.QUEUE");
        message.setMessageContent(content);
        message.setMessageType("TEXT");
        message.setStatus(MQMessage.MessageStatus.PROCESSED);
        return message;
    }

    @BeforeEach
    void setUp() {
        // Default behavior - message doesn't exist
        when(messageRepository.findByMessageId(anyString())).thenReturn(Optional.empty());
        when(messageRepository.save(any(MQMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should open MQ Message Writer successfully")
    void shouldOpenMQMessageWriterSuccessfully() throws Exception {
        // When & Then - should not throw exception
        assertThatCode(() -> messageWriter.open(null)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should open with checkpoint parameter")
    void shouldOpenWithCheckpointParameter() throws Exception {
        // Given
        Serializable checkpoint = "test-checkpoint";

        // When & Then - should not throw exception
        assertThatCode(() -> messageWriter.open(checkpoint)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should close MQ Message Writer successfully")
    void shouldCloseMQMessageWriterSuccessfully() throws Exception {
        // When & Then - should not throw exception
        assertThatCode(() -> messageWriter.close()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should write single new message successfully")
    void shouldWriteSingleNewMessageSuccessfully() throws Exception {
        // Given
        MQMessage message = createTestMessage("MSG-001", "Test content");
        List<Object> items = Collections.singletonList(message);

        // When
        messageWriter.writeItems(items);

        // Then
        verify(messageRepository).findByMessageId("MSG-001");
        verify(messageRepository).save(message);
    }

    @Test
    @DisplayName("Should write multiple new messages successfully")
    void shouldWriteMultipleNewMessagesSuccessfully() throws Exception {
        // Given
        MQMessage message1 = createTestMessage("MSG-001", "Content 1");
        MQMessage message2 = createTestMessage("MSG-002", "Content 2");
        MQMessage message3 = createTestMessage("MSG-003", "Content 3");
        List<Object> items = Arrays.asList(message1, message2, message3);

        // When
        messageWriter.writeItems(items);

        // Then
        verify(messageRepository).findByMessageId("MSG-001");
        verify(messageRepository).findByMessageId("MSG-002");
        verify(messageRepository).findByMessageId("MSG-003");
        verify(messageRepository, times(3)).save(any(MQMessage.class));
    }

    @Test
    @DisplayName("Should update existing message")
    void shouldUpdateExistingMessage() throws Exception {
        // Given
        MQMessage existingMessage = createTestMessage("MSG-001", "Old content");
        MQMessage newMessage = createTestMessage("MSG-001", "New content");
        newMessage.setStatus(MQMessage.MessageStatus.FAILED);
        newMessage.setErrorMessage("Processing error");
        newMessage.setRetryCount(2);

        when(messageRepository.findByMessageId("MSG-001")).thenReturn(Optional.of(existingMessage));

        List<Object> items = Collections.singletonList(newMessage);

        // When
        messageWriter.writeItems(items);

        // Then
        verify(messageRepository, times(2)).findByMessageId("MSG-001"); // Called twice in the implementation
        verify(messageRepository).save(existingMessage);

        // Verify the existing message was updated
        assertThat(existingMessage.getMessageContent()).isEqualTo("New content");
        assertThat(existingMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);
        assertThat(existingMessage.getErrorMessage()).isEqualTo("Processing error");
        assertThat(existingMessage.getRetryCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle empty items list")
    void shouldHandleEmptyItemsList() throws Exception {
        // Given
        List<Object> items = Collections.emptyList();

        // When
        messageWriter.writeItems(items);

        // Then
        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle non-MQMessage items")
    void shouldHandleNonMQMessageItems() throws Exception {
        // Given
        List<Object> items = Arrays.asList("Not an MQMessage", 12345, null);

        // When & Then
        assertThatThrownBy(() -> messageWriter.writeItems(items))
            .isInstanceOf(Exception.class)
            .hasMessage("Failed to write 3 out of 3 messages");

        verify(messageRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle mixed valid and invalid items")
    void shouldHandleMixedValidAndInvalidItems() throws Exception {
        // Given
        MQMessage validMessage = createTestMessage("MSG-001", "Valid content");
        List<Object> items = Arrays.asList(validMessage, "Invalid item", null);

        // When & Then
        assertThatThrownBy(() -> messageWriter.writeItems(items))
            .isInstanceOf(Exception.class)
            .hasMessage("Failed to write 2 out of 3 messages");

        verify(messageRepository).save(validMessage);
    }

    @Test
    @DisplayName("Should handle repository save exception for new message")
    void shouldHandleRepositorySaveExceptionForNewMessage() throws Exception {
        // Given
        MQMessage message = createTestMessage("MSG-001", "Test content");
        when(messageRepository.save(any(MQMessage.class)))
            .thenThrow(new RuntimeException("Database error"))
            .thenReturn(message); // Second call for error state save

        List<Object> items = Collections.singletonList(message);

        // When & Then
        assertThatThrownBy(() -> messageWriter.writeItems(items))
            .isInstanceOf(Exception.class)
            .hasMessage("Failed to write 1 out of 1 messages");

        verify(messageRepository, times(2)).save(any(MQMessage.class));
    }

    @Test
    @DisplayName("Should handle repository save exception for error state save")
    void shouldHandleRepositorySaveExceptionForErrorStateSave() throws Exception {
        // Given
        MQMessage message = createTestMessage("MSG-001", "Test content");
        when(messageRepository.save(any(MQMessage.class)))
            .thenThrow(new RuntimeException("Database error"));

        List<Object> items = Collections.singletonList(message);

        // When & Then
        assertThatThrownBy(() -> messageWriter.writeItems(items))
            .isInstanceOf(Exception.class)
            .hasMessage("Failed to write 1 out of 1 messages");

        verify(messageRepository, times(2)).save(any(MQMessage.class));
    }

    @Test
    @DisplayName("Should handle repository exception during findByMessageId")
    void shouldHandleRepositoryExceptionDuringFindByMessageId() throws Exception {
        // Given
        MQMessage message = createTestMessage("MSG-001", "Test content");
        when(messageRepository.findByMessageId("MSG-001")).thenThrow(new RuntimeException("Database error"));

        List<Object> items = Collections.singletonList(message);

        // When & Then
        assertThatThrownBy(() -> messageWriter.writeItems(items))
            .isInstanceOf(Exception.class)
            .hasMessage("Failed to write 1 out of 1 messages");

        verify(messageRepository).findByMessageId("MSG-001");
        verify(messageRepository).save(any(MQMessage.class)); // Only one save call when findByMessageId throws exception
    }

    @Test
    @DisplayName("Should return null for checkpointInfo")
    void shouldReturnNullForCheckpointInfo() throws Exception {
        // When
        Serializable checkpoint = messageWriter.checkpointInfo();

        // Then
        assertThat(checkpoint).isNull();
    }

    @Test
    @DisplayName("Should handle partial success scenario")
    void shouldHandlePartialSuccessScenario() throws Exception {
        // Given
        MQMessage message1 = createTestMessage("MSG-001", "Content 1");
        MQMessage message2 = createTestMessage("MSG-002", "Content 2");
        MQMessage message3 = createTestMessage("MSG-003", "Content 3");

        // Make the second message save fail
        when(messageRepository.save(message2))
            .thenThrow(new RuntimeException("Database error"))
            .thenReturn(message2); // Second call for error state save

        List<Object> items = Arrays.asList(message1, message2, message3);

        // When & Then
        assertThatThrownBy(() -> messageWriter.writeItems(items))
            .isInstanceOf(Exception.class)
            .hasMessage("Failed to write 1 out of 3 messages");

        verify(messageRepository).save(message1);
        verify(messageRepository, times(2)).save(message2); // Original + error state
        verify(messageRepository).save(message3);
    }

    @Test
    @DisplayName("Should update all fields when updating existing message")
    void shouldUpdateAllFieldsWhenUpdatingExistingMessage() throws Exception {
        // Given
        MQMessage existingMessage = createTestMessage("MSG-001", "Old content");
        existingMessage.setStatus(MQMessage.MessageStatus.RECEIVED);
        existingMessage.setErrorMessage(null);
        existingMessage.setProcessedAt(null);
        existingMessage.setRetryCount(0);

        MQMessage newMessage = createTestMessage("MSG-001", "Updated content");
        newMessage.setStatus(MQMessage.MessageStatus.PROCESSED);
        newMessage.setErrorMessage("No error");
        newMessage.setProcessedAt(java.time.LocalDateTime.now());
        newMessage.setRetryCount(1);

        when(messageRepository.findByMessageId("MSG-001")).thenReturn(Optional.of(existingMessage));

        List<Object> items = Collections.singletonList(newMessage);

        // When
        messageWriter.writeItems(items);

        // Then
        assertThat(existingMessage.getMessageContent()).isEqualTo("Updated content");
        assertThat(existingMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
        assertThat(existingMessage.getErrorMessage()).isEqualTo("No error");
        assertThat(existingMessage.getProcessedAt()).isEqualTo(newMessage.getProcessedAt());
        assertThat(existingMessage.getRetryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle message with null message ID")
    void shouldHandleMessageWithNullMessageId() throws Exception {
        // Given
        MQMessage message = createTestMessage(null, "Test content");
        List<Object> items = Collections.singletonList(message);

        // When
        messageWriter.writeItems(items);

        // Then - should save the message even with null ID
        verify(messageRepository).findByMessageId(null);
        verify(messageRepository).save(message);
    }

    @Test
    @DisplayName("Should handle large batch of messages")
    void shouldHandleLargeBatchOfMessages() throws Exception {
        // Given
        List<Object> items = Arrays.asList(
            createTestMessage("MSG-001", "Content 1"),
            createTestMessage("MSG-002", "Content 2"),
            createTestMessage("MSG-003", "Content 3"),
            createTestMessage("MSG-004", "Content 4"),
            createTestMessage("MSG-005", "Content 5")
        );

        // When
        messageWriter.writeItems(items);

        // Then
        verify(messageRepository, times(5)).findByMessageId(anyString());
        verify(messageRepository, times(5)).save(any(MQMessage.class));
    }

    @Test
    @DisplayName("Should handle update of existing message with null fields")
    void shouldHandleUpdateOfExistingMessageWithNullFields() throws Exception {
        // Given
        MQMessage existingMessage = createTestMessage("MSG-001", "Old content");
        existingMessage.setStatus(MQMessage.MessageStatus.RECEIVED);

        MQMessage newMessage = createTestMessage("MSG-001", null);
        newMessage.setStatus(null);
        newMessage.setErrorMessage(null);
        newMessage.setProcessedAt(null);

        when(messageRepository.findByMessageId("MSG-001")).thenReturn(Optional.of(existingMessage));

        List<Object> items = Collections.singletonList(newMessage);

        // When
        messageWriter.writeItems(items);

        // Then
        assertThat(existingMessage.getMessageContent()).isNull();
        assertThat(existingMessage.getStatus()).isNull();
        assertThat(existingMessage.getErrorMessage()).isNull();
        assertThat(existingMessage.getProcessedAt()).isNull();
    }

    @Test
    @DisplayName("Should mark message as failed on save error with proper error message")
    void shouldMarkMessageAsFailedOnSaveErrorWithProperErrorMessage() throws Exception {
        // Given
        MQMessage message = createTestMessage("MSG-001", "Test content");
        RuntimeException saveException = new RuntimeException("Constraint violation");

        when(messageRepository.save(message))
            .thenThrow(saveException)
            .thenReturn(message); // Second call succeeds

        List<Object> items = Collections.singletonList(message);

        // When & Then
        assertThatThrownBy(() -> messageWriter.writeItems(items))
            .isInstanceOf(Exception.class);

        // Verify the message was marked as failed with correct error message
        assertThat(message.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);
        assertThat(message.getErrorMessage()).isEqualTo("Database save error: Constraint violation");
    }
}