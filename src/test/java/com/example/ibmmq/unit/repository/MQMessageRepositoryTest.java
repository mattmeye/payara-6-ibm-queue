package com.example.ibmmq.unit.repository;

import com.example.ibmmq.entity.MQMessage;
import com.example.ibmmq.repository.MQMessageRepository;
import com.example.ibmmq.util.TestDataBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MQMessageRepository Tests")
class MQMessageRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private MQMessageRepository repository;

    @Mock
    private TypedQuery<MQMessage> typedQuery;

    @Mock
    private TypedQuery<Long> longQuery;

    private MQMessage testMessage;

    @BeforeEach
    void setUp() {
        testMessage = TestDataBuilder.createTestMessage();
    }

    @Test
    @DisplayName("Should save new message")
    void shouldSaveNewMessage() {
        // Given
        MQMessage newMessage = TestDataBuilder.createTestMessage();
        newMessage.setId(null); // New message has no ID

        // When
        MQMessage result = repository.save(newMessage);

        // Then
        verify(entityManager).persist(newMessage);
        assertThat(result).isEqualTo(newMessage);
    }

    @Test
    @DisplayName("Should update existing message")
    void shouldUpdateExistingMessage() {
        // Given
        testMessage.setId(1L);
        when(entityManager.merge(testMessage)).thenReturn(testMessage);

        // When
        MQMessage result = repository.save(testMessage);

        // Then
        verify(entityManager).merge(testMessage);
        assertThat(result).isEqualTo(testMessage);
    }

    @Test
    @DisplayName("Should find message by ID")
    void shouldFindMessageById() {
        // Given
        Long messageId = 1L;
        when(entityManager.find(MQMessage.class, messageId)).thenReturn(testMessage);

        // When
        Optional<MQMessage> result = repository.findById(messageId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testMessage);
        verify(entityManager).find(MQMessage.class, messageId);
    }

    @Test
    @DisplayName("Should return empty when message not found by ID")
    void shouldReturnEmptyWhenMessageNotFoundById() {
        // Given
        Long messageId = 999L;
        when(entityManager.find(MQMessage.class, messageId)).thenReturn(null);

        // When
        Optional<MQMessage> result = repository.findById(messageId);

        // Then
        assertThat(result).isEmpty();
        verify(entityManager).find(MQMessage.class, messageId);
    }

    @Test
    @DisplayName("Should find message by message ID")
    void shouldFindMessageByMessageId() {
        // Given
        String messageId = "TEST_MSG_ID";
        when(entityManager.createQuery(anyString(), eq(MQMessage.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("messageId", messageId)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of(testMessage));

        // When
        Optional<MQMessage> result = repository.findByMessageId(messageId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testMessage);
        verify(typedQuery).setParameter("messageId", messageId);
    }

    @Test
    @DisplayName("Should return empty when message not found by message ID")
    void shouldReturnEmptyWhenMessageNotFoundByMessageId() {
        // Given
        String messageId = "NON_EXISTENT";
        when(entityManager.createQuery(anyString(), eq(MQMessage.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter("messageId", messageId)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(List.of());

        // When
        Optional<MQMessage> result = repository.findByMessageId(messageId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find all messages")
    void shouldFindAllMessages() {
        // Given
        List<MQMessage> messages = Arrays.asList(
            TestDataBuilder.createTestMessage(),
            TestDataBuilder.createTestMessage(),
            TestDataBuilder.createTestMessage()
        );
        when(entityManager.createNamedQuery("MQMessage.findAll", MQMessage.class)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(messages);

        // When
        List<MQMessage> result = repository.findAll();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyElementsOf(messages);
        verify(entityManager).createNamedQuery("MQMessage.findAll", MQMessage.class);
    }

    @Test
    @DisplayName("Should find messages by status")
    void shouldFindMessagesByStatus() {
        // Given
        MQMessage.MessageStatus status = MQMessage.MessageStatus.PROCESSED;
        List<MQMessage> processedMessages = Arrays.asList(
            TestDataBuilder.createProcessedMessage(),
            TestDataBuilder.createProcessedMessage()
        );
        when(entityManager.createNamedQuery("MQMessage.findByStatus", MQMessage.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("status", status)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(processedMessages);

        // When
        List<MQMessage> result = repository.findByStatus(status);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(processedMessages);
        verify(typedQuery).setParameter("status", status);
    }

    @Test
    @DisplayName("Should find messages by queue")
    void shouldFindMessagesByQueue() {
        // Given
        String queueName = "TEST.QUEUE";
        List<MQMessage> queueMessages = Arrays.asList(
            TestDataBuilder.createTestMessage("MSG1", queueName, "Content 1"),
            TestDataBuilder.createTestMessage("MSG2", queueName, "Content 2")
        );
        when(entityManager.createNamedQuery("MQMessage.findByQueue", MQMessage.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("queueName", queueName)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(queueMessages);

        // When
        List<MQMessage> result = repository.findByQueue(queueName);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(queueMessages);
        verify(typedQuery).setParameter("queueName", queueName);
    }

    @Test
    @DisplayName("Should find messages by status with limit")
    void shouldFindMessagesByStatusWithLimit() {
        // Given
        MQMessage.MessageStatus status = MQMessage.MessageStatus.FAILED;
        int maxResults = 5;
        List<MQMessage> failedMessages = Arrays.asList(
            TestDataBuilder.createFailedMessage(),
            TestDataBuilder.createFailedMessage()
        );
        when(entityManager.createNamedQuery("MQMessage.findByStatus", MQMessage.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("status", status)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(maxResults)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(failedMessages);

        // When
        List<MQMessage> result = repository.findByStatusWithLimit(status, maxResults);

        // Then
        assertThat(result).hasSize(2);
        verify(typedQuery).setMaxResults(maxResults);
    }

    @Test
    @DisplayName("Should count messages by status")
    void shouldCountMessagesByStatus() {
        // Given
        MQMessage.MessageStatus status = MQMessage.MessageStatus.RECEIVED;
        Long expectedCount = 10L;
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(longQuery);
        when(longQuery.setParameter("status", status)).thenReturn(longQuery);
        when(longQuery.getSingleResult()).thenReturn(expectedCount);

        // When
        long result = repository.countByStatus(status);

        // Then
        assertThat(result).isEqualTo(expectedCount);
        verify(longQuery).setParameter("status", status);
    }

    @Test
    @DisplayName("Should delete message")
    void shouldDeleteMessage() {
        // Given
        when(entityManager.contains(testMessage)).thenReturn(true);

        // When
        repository.delete(testMessage);

        // Then
        verify(entityManager).remove(testMessage);
    }

    @Test
    @DisplayName("Should merge and delete detached message")
    void shouldMergeAndDeleteDetachedMessage() {
        // Given
        when(entityManager.contains(testMessage)).thenReturn(false);
        when(entityManager.merge(testMessage)).thenReturn(testMessage);

        // When
        repository.delete(testMessage);

        // Then
        verify(entityManager).merge(testMessage);
        verify(entityManager).remove(testMessage);
    }

    @Test
    @DisplayName("Should delete message by ID")
    void shouldDeleteMessageById() {
        // Given
        Long messageId = 1L;
        when(entityManager.find(MQMessage.class, messageId)).thenReturn(testMessage);
        when(entityManager.contains(testMessage)).thenReturn(true);

        // When
        repository.deleteById(messageId);

        // Then
        verify(entityManager).find(MQMessage.class, messageId);
        verify(entityManager).remove(testMessage);
    }

    @Test
    @DisplayName("Should not delete when message not found by ID")
    void shouldNotDeleteWhenMessageNotFoundById() {
        // Given
        Long messageId = 999L;
        when(entityManager.find(MQMessage.class, messageId)).thenReturn(null);

        // When
        repository.deleteById(messageId);

        // Then
        verify(entityManager).find(MQMessage.class, messageId);
        verify(entityManager, never()).remove(any());
    }

    @Test
    @DisplayName("Should delete old processed messages")
    void shouldDeleteOldProcessedMessages() {
        // Given
        int daysOld = 30;
        int deletedCount = 5;
        jakarta.persistence.Query query = mock(jakarta.persistence.Query.class);
        when(entityManager.createQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.executeUpdate()).thenReturn(deletedCount);

        // When
        int result = repository.deleteOldProcessedMessages(daysOld);

        // Then
        assertThat(result).isEqualTo(deletedCount);
    }

    @Test
    @DisplayName("Should handle exceptions gracefully in count operation")
    void shouldHandleExceptionsGracefullyInCountOperation() {
        // Given
        MQMessage.MessageStatus status = MQMessage.MessageStatus.RECEIVED;
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> repository.countByStatus(status))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Database error");
    }
}