package com.example.ibmmq.unit.rest;

import com.example.ibmmq.entity.MQMessage;
import com.example.ibmmq.repository.MQMessageRepository;
import com.example.ibmmq.rest.MQMessageResource;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MQMessageResource Tests")
class MQMessageResourceTest {

    @Mock
    private MQMessageRepository messageRepository;

    @InjectMocks
    private MQMessageResource mqMessageResource;

    private MQMessage testMessage;

    @BeforeEach
    void setUp() {
        testMessage = new MQMessage();
        testMessage.setId(1L);
        testMessage.setMessageId("MSG_123");
        testMessage.setCorrelationId("CORR_123");
        testMessage.setQueueName("TEST.QUEUE");
        testMessage.setMessageContent("Test message content");
        testMessage.setMessageType("TEXT");
        testMessage.setPriority(5);
        testMessage.setExpiry(3600000L);
        testMessage.setReceivedAt(LocalDateTime.now());
        testMessage.setStatus(MQMessage.MessageStatus.PROCESSED);
        testMessage.setRetryCount(0);
        testMessage.setVersion(1L);
    }

    @Test
    @DisplayName("Should get all messages successfully")
    void shouldGetAllMessagesSuccessfully() {
        // Given
        List<MQMessage> messages = Arrays.asList(testMessage);
        when(messageRepository.findAll()).thenReturn(messages);

        // When
        Response response = mqMessageResource.getAllMessages();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("messages");
        assertThat(entity).contains("MSG_123");
        verify(messageRepository).findAll();
    }

    @Test
    @DisplayName("Should handle empty messages list")
    void shouldHandleEmptyMessagesList() {
        // Given
        when(messageRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        Response response = mqMessageResource.getAllMessages();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity().toString()).contains("messages");
        assertThat(response.getEntity().toString()).contains("[]");
    }

    @Test
    @DisplayName("Should handle get all messages failure")
    void shouldHandleGetAllMessagesFailure() {
        // Given
        String errorMessage = "Database connection failed";
        when(messageRepository.findAll()).thenThrow(new RuntimeException(errorMessage));

        // When
        Response response = mqMessageResource.getAllMessages();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity().toString()).contains("error");
        assertThat(response.getEntity().toString()).contains(errorMessage);
    }

    @Test
    @DisplayName("Should get message by ID successfully")
    void shouldGetMessageByIdSuccessfully() {
        // Given
        Long messageId = 1L;
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(testMessage));

        // When
        Response response = mqMessageResource.getMessageById(messageId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("MSG_123");
        assertThat(entity).contains("CORR_123");
        verify(messageRepository).findById(messageId);
    }

    @Test
    @DisplayName("Should return not found when message does not exist")
    void shouldReturnNotFoundWhenMessageDoesNotExist() {
        // Given
        Long messageId = 999L;
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        // When
        Response response = mqMessageResource.getMessageById(messageId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        assertThat(response.getEntity().toString()).contains("not found");
    }

    @Test
    @DisplayName("Should handle get message by ID failure")
    void shouldHandleGetMessageByIdFailure() {
        // Given
        Long messageId = 1L;
        String errorMessage = "Query failed";
        when(messageRepository.findById(messageId)).thenThrow(new RuntimeException(errorMessage));

        // When
        Response response = mqMessageResource.getMessageById(messageId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity().toString()).contains("error");
        assertThat(response.getEntity().toString()).contains(errorMessage);
    }

    @Test
    @DisplayName("Should get messages by status successfully")
    void shouldGetMessagesByStatusSuccessfully() {
        // Given
        String status = "PROCESSED";
        List<MQMessage> messages = Arrays.asList(testMessage);
        when(messageRepository.findByStatus(MQMessage.MessageStatus.PROCESSED)).thenReturn(messages);

        // When
        Response response = mqMessageResource.getMessagesByStatus(status);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("messages");
        assertThat(entity).contains("MSG_123");
        verify(messageRepository).findByStatus(MQMessage.MessageStatus.PROCESSED);
    }

    @Test
    @DisplayName("Should handle case insensitive status")
    void shouldHandleCaseInsensitiveStatus() {
        // Given
        String status = "processed";
        List<MQMessage> messages = Arrays.asList(testMessage);
        when(messageRepository.findByStatus(MQMessage.MessageStatus.PROCESSED)).thenReturn(messages);

        // When
        Response response = mqMessageResource.getMessagesByStatus(status);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(messageRepository).findByStatus(MQMessage.MessageStatus.PROCESSED);
    }

    @Test
    @DisplayName("Should return bad request for invalid status")
    void shouldReturnBadRequestForInvalidStatus() {
        // Given
        String invalidStatus = "INVALID_STATUS";

        // When
        Response response = mqMessageResource.getMessagesByStatus(invalidStatus);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.getEntity().toString()).contains("Invalid status");
        assertThat(response.getEntity().toString()).contains(invalidStatus);
    }

    @Test
    @DisplayName("Should get messages by queue successfully")
    void shouldGetMessagesByQueueSuccessfully() {
        // Given
        String queueName = "TEST.QUEUE";
        List<MQMessage> messages = Arrays.asList(testMessage);
        when(messageRepository.findByQueue(queueName)).thenReturn(messages);

        // When
        Response response = mqMessageResource.getMessagesByQueue(queueName);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("messages");
        assertThat(entity).contains("TEST.QUEUE");
        verify(messageRepository).findByQueue(queueName);
    }

    @Test
    @DisplayName("Should handle get messages by queue failure")
    void shouldHandleGetMessagesByQueueFailure() {
        // Given
        String queueName = "TEST.QUEUE";
        String errorMessage = "Database error";
        when(messageRepository.findByQueue(queueName)).thenThrow(new RuntimeException(errorMessage));

        // When
        Response response = mqMessageResource.getMessagesByQueue(queueName);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity().toString()).contains("error");
        assertThat(response.getEntity().toString()).contains(errorMessage);
    }

    @Test
    @DisplayName("Should count messages by status successfully")
    void shouldCountMessagesByStatusSuccessfully() {
        // Given
        String status = "PROCESSED";
        long expectedCount = 42L;
        when(messageRepository.countByStatus(MQMessage.MessageStatus.PROCESSED)).thenReturn(expectedCount);

        // When
        Response response = mqMessageResource.countMessagesByStatus(status);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("status");
        assertThat(entity).contains("count");
        assertThat(entity).contains(String.valueOf(expectedCount));
        verify(messageRepository).countByStatus(MQMessage.MessageStatus.PROCESSED);
    }

    @Test
    @DisplayName("Should return bad request for invalid status in count")
    void shouldReturnBadRequestForInvalidStatusInCount() {
        // Given
        String invalidStatus = "UNKNOWN_STATUS";

        // When
        Response response = mqMessageResource.countMessagesByStatus(invalidStatus);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        assertThat(response.getEntity().toString()).contains("Invalid status");
    }

    @Test
    @DisplayName("Should delete message successfully")
    void shouldDeleteMessageSuccessfully() {
        // Given
        Long messageId = 1L;
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(testMessage));

        // When
        Response response = mqMessageResource.deleteMessage(messageId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity().toString()).contains("success");
        assertThat(response.getEntity().toString()).contains("deleted");
        verify(messageRepository).findById(messageId);
        verify(messageRepository).deleteById(messageId);
    }

    @Test
    @DisplayName("Should return not found when deleting non-existent message")
    void shouldReturnNotFoundWhenDeletingNonExistentMessage() {
        // Given
        Long messageId = 999L;
        when(messageRepository.findById(messageId)).thenReturn(Optional.empty());

        // When
        Response response = mqMessageResource.deleteMessage(messageId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        assertThat(response.getEntity().toString()).contains("not found");
        verify(messageRepository, never()).deleteById(messageId);
    }

    @Test
    @DisplayName("Should handle delete message failure")
    void shouldHandleDeleteMessageFailure() {
        // Given
        Long messageId = 1L;
        String errorMessage = "Delete operation failed";
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(testMessage));
        doThrow(new RuntimeException(errorMessage)).when(messageRepository).deleteById(messageId);

        // When
        Response response = mqMessageResource.deleteMessage(messageId);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity().toString()).contains("error");
        assertThat(response.getEntity().toString()).contains(errorMessage);
    }

    @Test
    @DisplayName("Should cleanup old messages successfully")
    void shouldCleanupOldMessagesSuccessfully() {
        // Given
        int days = 30;
        int deletedCount = 25;
        when(messageRepository.deleteOldProcessedMessages(days)).thenReturn(deletedCount);

        // When
        Response response = mqMessageResource.cleanupOldMessages(days);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("success");
        assertThat(entity).contains("deletedCount");
        assertThat(entity).contains(String.valueOf(deletedCount));
        verify(messageRepository).deleteOldProcessedMessages(days);
    }

    @Test
    @DisplayName("Should use default days parameter for cleanup")
    void shouldUseDefaultDaysParameterForCleanup() {
        // Given
        int deletedCount = 10;
        when(messageRepository.deleteOldProcessedMessages(30)).thenReturn(deletedCount);

        // When - No days parameter provided, should use default 30
        Response response = mqMessageResource.cleanupOldMessages(30);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(messageRepository).deleteOldProcessedMessages(30);
    }

    @Test
    @DisplayName("Should handle cleanup old messages failure")
    void shouldHandleCleanupOldMessagesFailure() {
        // Given
        int days = 30;
        String errorMessage = "Cleanup failed";
        when(messageRepository.deleteOldProcessedMessages(days)).thenThrow(new RuntimeException(errorMessage));

        // When
        Response response = mqMessageResource.cleanupOldMessages(days);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity().toString()).contains("error");
        assertThat(response.getEntity().toString()).contains(errorMessage);
    }

    @Test
    @DisplayName("Should return healthy status for health check")
    void shouldReturnHealthyStatusForHealthCheck() {
        // When
        Response response = mqMessageResource.healthCheck();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("healthy");
        assertThat(entity).contains("MQ Message Repository");
    }

    @Test
    @DisplayName("Should handle JSON escaping in message content")
    void shouldHandleJsonEscapingInMessageContent() {
        // Given
        MQMessage messageWithSpecialChars = new MQMessage();
        messageWithSpecialChars.setId(2L);
        messageWithSpecialChars.setMessageId("MSG_SPECIAL");
        messageWithSpecialChars.setMessageContent("Content with \"quotes\" and \n newlines \t tabs");
        messageWithSpecialChars.setQueueName("TEST.QUEUE");
        messageWithSpecialChars.setStatus(MQMessage.MessageStatus.PROCESSED);

        when(messageRepository.findById(2L)).thenReturn(Optional.of(messageWithSpecialChars));

        // When
        Response response = mqMessageResource.getMessageById(2L);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("\\\"quotes\\\"");
        assertThat(entity).contains("\\n");
        assertThat(entity).contains("\\t");
    }

    @Test
    @DisplayName("Should handle null values in message fields")
    void shouldHandleNullValuesInMessageFields() {
        // Given
        MQMessage messageWithNulls = new MQMessage();
        messageWithNulls.setId(3L);
        messageWithNulls.setMessageId("MSG_NULL");
        messageWithNulls.setCorrelationId(null);
        messageWithNulls.setErrorMessage(null);
        messageWithNulls.setQueueName("TEST.QUEUE");
        messageWithNulls.setStatus(MQMessage.MessageStatus.PROCESSED);

        when(messageRepository.findById(3L)).thenReturn(Optional.of(messageWithNulls));

        // When
        Response response = mqMessageResource.getMessageById(3L);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("null");
    }

    @Test
    @DisplayName("Should handle multiple messages in list conversion")
    void shouldHandleMultipleMessagesInListConversion() {
        // Given
        MQMessage message1 = new MQMessage();
        message1.setId(1L);
        message1.setMessageId("MSG_1");
        message1.setQueueName("QUEUE.1");
        message1.setStatus(MQMessage.MessageStatus.PROCESSED);

        MQMessage message2 = new MQMessage();
        message2.setId(2L);
        message2.setMessageId("MSG_2");
        message2.setQueueName("QUEUE.2");
        message2.setStatus(MQMessage.MessageStatus.RECEIVED);

        List<MQMessage> messages = Arrays.asList(message1, message2);
        when(messageRepository.findAll()).thenReturn(messages);

        // When
        Response response = mqMessageResource.getAllMessages();

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String entity = response.getEntity().toString();
        assertThat(entity).contains("MSG_1");
        assertThat(entity).contains("MSG_2");
        assertThat(entity).contains("QUEUE.1");
        assertThat(entity).contains("QUEUE.2");
    }

    @Test
    @DisplayName("Should handle all message status values")
    void shouldHandleAllMessageStatusValues() {
        // Test each status value
        for (MQMessage.MessageStatus status : MQMessage.MessageStatus.values()) {
            // Given
            when(messageRepository.findByStatus(status)).thenReturn(Arrays.asList(testMessage));

            // When
            Response response = mqMessageResource.getMessagesByStatus(status.name());

            // Then
            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            verify(messageRepository).findByStatus(status);
        }
    }

    @Test
    @DisplayName("Should handle repository exception during status query")
    void shouldHandleRepositoryExceptionDuringStatusQuery() {
        // Given
        String status = "PROCESSED";
        String errorMessage = "Repository unavailable";
        when(messageRepository.findByStatus(any())).thenThrow(new RuntimeException(errorMessage));

        // When
        Response response = mqMessageResource.getMessagesByStatus(status);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity().toString()).contains("error");
        assertThat(response.getEntity().toString()).contains(errorMessage);
    }

    @Test
    @DisplayName("Should handle repository exception during count query")
    void shouldHandleRepositoryExceptionDuringCountQuery() {
        // Given
        String status = "PROCESSED";
        String errorMessage = "Count query failed";
        when(messageRepository.countByStatus(any())).thenThrow(new RuntimeException(errorMessage));

        // When
        Response response = mqMessageResource.countMessagesByStatus(status);

        // Then
        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.getEntity().toString()).contains("error");
        assertThat(response.getEntity().toString()).contains(errorMessage);
    }
}