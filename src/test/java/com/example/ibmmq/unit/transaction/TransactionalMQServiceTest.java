package com.example.ibmmq.unit.transaction;

import com.example.ibmmq.config.IBMMQConfig;
import com.example.ibmmq.entity.MQMessage;
import com.example.ibmmq.pool.IBMMQConnectionPool;
import com.example.ibmmq.repository.MQMessageRepository;
import com.example.ibmmq.transaction.TransactionalMQService;
import jakarta.jms.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("TransactionalMQService Tests")
class TransactionalMQServiceTest {

    @Mock
    private IBMMQConnectionPool connectionPool;

    @Mock
    private IBMMQConfig config;

    @Mock
    private MQMessageRepository messageRepository;

    @InjectMocks
    private TransactionalMQService transactionalService;

    @Mock
    private Connection connection;

    @Mock
    private Session session;

    @Mock
    private Queue queue;

    @Mock
    private MessageProducer producer;

    @Mock
    private MessageConsumer consumer;

    @Mock
    private TextMessage textMessage;

    @BeforeEach
    void setUp() throws JMSException {
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.createSession(true, Session.SESSION_TRANSACTED)).thenReturn(session);
        when(session.createQueue(anyString())).thenReturn(queue);
        when(session.createProducer(any(Destination.class))).thenReturn(producer);
        when(session.createConsumer(any(Destination.class))).thenReturn(consumer);
        when(session.createTextMessage(anyString())).thenReturn(textMessage);
        when(textMessage.getJMSMessageID()).thenReturn("TEST_MSG_ID");
    }

    @Test
    @DisplayName("Should send message transactionally and persist to database")
    void shouldSendMessageTransactionallyAndPersistToDatabase() throws JMSException {
        // Given
        String queueName = "TEST.QUEUE";
        String message = "Test transactional message";

        // When
        transactionalService.sendMessageTransactional(queueName, message);

        // Then
        verify(connectionPool).getConnection();
        verify(connection).createSession(true, Session.SESSION_TRANSACTED);
        verify(session).createQueue(queueName);
        verify(session).createTextMessage(message);
        verify(textMessage).setJMSDeliveryMode(DeliveryMode.PERSISTENT);
        verify(textMessage).setStringProperty("APP_ID", "PayaraIBMMQApp");
        verify(textMessage).setLongProperty(eq("TIMESTAMP"), anyLong());
        verify(producer).send(textMessage);
        verify(session).commit();

        ArgumentCaptor<MQMessage> messageCaptor = ArgumentCaptor.forClass(MQMessage.class);
        verify(messageRepository).save(messageCaptor.capture());

        MQMessage savedMessage = messageCaptor.getValue();
        assertThat(savedMessage.getQueueName()).isEqualTo(queueName);
        assertThat(savedMessage.getMessageContent()).isEqualTo(message);
        assertThat(savedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
        assertThat(savedMessage.getMessageType()).isEqualTo("TEXT");

        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    @DisplayName("Should rollback transaction when database save fails")
    void shouldRollbackTransactionWhenDatabaseSaveFails() throws JMSException {
        // Given
        String queueName = "TEST.QUEUE";
        String message = "Test message";
        when(messageRepository.save(any(MQMessage.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> transactionalService.sendMessageTransactional(queueName, message))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Transactional message sending failed");

        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    @DisplayName("Should receive message transactionally and persist to database")
    void shouldReceiveMessageTransactionallyAndPersistToDatabase() throws JMSException {
        // Given
        String queueName = "RECEIVE.QUEUE";
        long timeout = 5000L;
        String messageContent = "Received message";

        when(textMessage.getText()).thenReturn(messageContent);
        when(textMessage.getJMSCorrelationID()).thenReturn("CORR_ID");
        when(textMessage.getJMSPriority()).thenReturn(5);
        when(textMessage.getJMSExpiration()).thenReturn(System.currentTimeMillis() + 60000);
        when(consumer.receive(timeout)).thenReturn(textMessage);

        // When
        String result = transactionalService.receiveMessageTransactional(queueName, timeout);

        // Then
        assertThat(result).isEqualTo(messageContent);

        verify(connection).start();
        verify(session).createQueue(queueName);
        verify(consumer).receive(timeout);
        verify(session).commit();

        ArgumentCaptor<MQMessage> messageCaptor = ArgumentCaptor.forClass(MQMessage.class);
        verify(messageRepository).save(messageCaptor.capture());

        MQMessage savedMessage = messageCaptor.getValue();
        assertThat(savedMessage.getQueueName()).isEqualTo(queueName);
        assertThat(savedMessage.getMessageContent()).isEqualTo(messageContent);
        assertThat(savedMessage.getStatus()).isEqualTo(MQMessage.MessageStatus.PROCESSED);
        assertThat(savedMessage.getCorrelationId()).isEqualTo("CORR_ID");
        assertThat(savedMessage.getPriority()).isEqualTo(5);

        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    @DisplayName("Should rollback transaction when non-text message received")
    void shouldRollbackTransactionWhenNonTextMessageReceived() throws JMSException {
        // Given
        String queueName = "RECEIVE.QUEUE";
        long timeout = 5000L;
        BytesMessage bytesMessage = mock(BytesMessage.class);
        when(consumer.receive(timeout)).thenReturn(bytesMessage);

        // When
        String result = transactionalService.receiveMessageTransactional(queueName, timeout);

        // Then
        assertThat(result).isNull();
        verify(session).rollback();
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    @DisplayName("Should commit empty transaction when no message received")
    void shouldCommitEmptyTransactionWhenNoMessageReceived() throws JMSException {
        // Given
        String queueName = "EMPTY.QUEUE";
        long timeout = 1000L;
        when(consumer.receive(timeout)).thenReturn(null);

        // When
        String result = transactionalService.receiveMessageTransactional(queueName, timeout);

        // Then
        assertThat(result).isNull();
        verify(session).commit();
        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    @DisplayName("Should perform send and receive with correlation ID matching")
    void shouldPerformSendAndReceiveWithCorrelationIdMatching() throws JMSException {
        // Given
        String requestMessage = "Request message";
        String requestQueue = "REQUEST.QUEUE";
        String responseQueue = "RESPONSE.QUEUE";
        String responseMessage = "Response message";
        String correlationId = "test-correlation";

        // Setup request message
        TextMessage requestTextMessage = mock(TextMessage.class);
        when(session.createTextMessage(requestMessage)).thenReturn(requestTextMessage);
        when(requestTextMessage.getJMSMessageID()).thenReturn("REQ_MSG_ID");

        // Setup response message
        TextMessage responseTextMessage = mock(TextMessage.class);
        when(responseTextMessage.getText()).thenReturn(responseMessage);
        when(responseTextMessage.getJMSCorrelationID()).thenReturn(correlationId);
        when(responseTextMessage.getJMSMessageID()).thenReturn("RESP_MSG_ID");
        when(consumer.receive(30000)).thenReturn(responseTextMessage);

        // Mock UUID generation for correlation ID
        try (var uuidMock = mockStatic(java.util.UUID.class)) {
            java.util.UUID mockUuid = mock(java.util.UUID.class);
            when(mockUuid.toString()).thenReturn(correlationId);
            uuidMock.when(java.util.UUID::randomUUID).thenReturn(mockUuid);

            // When
            String result = transactionalService.sendAndReceiveTransactional(requestMessage, requestQueue, responseQueue);

            // Then
            assertThat(result).isEqualTo(responseMessage);

            verify(requestTextMessage).setJMSCorrelationID(correlationId);
            verify(requestTextMessage).setJMSReplyTo(any(Queue.class));
            verify(requestTextMessage).setJMSDeliveryMode(DeliveryMode.PERSISTENT);
            verify(requestTextMessage).setStringProperty("APP_ID", "PayaraIBMMQApp");
            verify(requestTextMessage).setLongProperty(eq("TIMESTAMP"), anyLong());

            verify(producer).send(requestTextMessage);
            verify(consumer).receive(30000);
            verify(session).commit();

            // Verify both request and response messages are saved
            verify(messageRepository, times(2)).save(any(MQMessage.class));
        }
    }

    @Test
    @DisplayName("Should rollback when no matching correlation ID in response")
    void shouldRollbackWhenNoMatchingCorrelationIdInResponse() throws JMSException {
        // Given
        String requestMessage = "Request message";
        String requestQueue = "REQUEST.QUEUE";
        String responseQueue = "RESPONSE.QUEUE";
        String correlationId = "test-correlation";

        TextMessage requestTextMessage = mock(TextMessage.class);
        when(session.createTextMessage(requestMessage)).thenReturn(requestTextMessage);
        when(requestTextMessage.getJMSMessageID()).thenReturn("REQ_MSG_ID");

        TextMessage responseTextMessage = mock(TextMessage.class);
        when(responseTextMessage.getJMSCorrelationID()).thenReturn("different-correlation");
        when(consumer.receive(30000)).thenReturn(responseTextMessage);

        try (var uuidMock = mockStatic(java.util.UUID.class)) {
            java.util.UUID mockUuid = mock(java.util.UUID.class);
            when(mockUuid.toString()).thenReturn(correlationId);
            uuidMock.when(java.util.UUID::randomUUID).thenReturn(mockUuid);

            // When
            String result = transactionalService.sendAndReceiveTransactional(requestMessage, requestQueue, responseQueue);

            // Then
            assertThat(result).isNull();
            verify(session).rollback();
        }
    }

    @Test
    @DisplayName("Should process message with compensation successfully")
    void shouldProcessMessageWithCompensationSuccessfully() throws JMSException {
        // Given
        String queueName = "PROCESS.QUEUE";
        String message = "Valid message";

        when(textMessage.getJMSMessageID()).thenReturn("PROCESSED_MSG_ID");

        // When
        transactionalService.processMessageWithCompensation(queueName, message);

        // Then
        verify(session).createTextMessage("PROCESSED: " + message);
        verify(textMessage).setJMSDeliveryMode(DeliveryMode.PERSISTENT);
        verify(textMessage).setStringProperty(eq("ORIGINAL_MESSAGE_ID"), nullable(String.class));
        verify(producer).send(textMessage);
        verify(session).commit();

        // Verify message saved twice (once as processing, once as processed)
        verify(messageRepository, times(2)).save(any(MQMessage.class));
    }

    @Test
    @DisplayName("Should handle validation failure in message processing")
    void shouldHandleValidationFailureInMessageProcessing() {
        // Given
        String queueName = "PROCESS.QUEUE";
        String invalidMessage = null; // This will cause validation to fail

        // When & Then
        assertThatThrownBy(() -> transactionalService.processMessageWithCompensation(queueName, invalidMessage))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Message processing with compensation failed");
    }

    @Test
    @DisplayName("Should handle error message validation")
    void shouldHandleErrorMessageValidation() {
        // Given
        String queueName = "PROCESS.QUEUE";
        String errorMessage = "This message contains error keyword";

        // When & Then
        assertThatThrownBy(() -> transactionalService.processMessageWithCompensation(queueName, errorMessage))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Message processing with compensation failed");
    }

    @Test
    @DisplayName("Should handle large message validation")
    void shouldHandleLargeMessageValidation() {
        // Given
        String queueName = "PROCESS.QUEUE";
        StringBuilder largeMessage = new StringBuilder();
        for (int i = 0; i < 10001; i++) {
            largeMessage.append("a");
        }

        // When & Then
        assertThatThrownBy(() -> transactionalService.processMessageWithCompensation(queueName, largeMessage.toString()))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Message processing with compensation failed");
    }

    @Test
    @DisplayName("Should handle empty message validation")
    void shouldHandleEmptyMessageValidation() {
        // Given
        String queueName = "PROCESS.QUEUE";
        String emptyMessage = "";

        // When & Then
        assertThatThrownBy(() -> transactionalService.processMessageWithCompensation(queueName, emptyMessage))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Message processing with compensation failed");
    }

    @Test
    @DisplayName("Should always release connection even when exception occurs")
    void shouldAlwaysReleaseConnectionEvenWhenExceptionOccurs() throws JMSException {
        // Given
        String queueName = "TEST.QUEUE";
        String message = "Test message";
        when(session.createTextMessage(message)).thenThrow(new JMSException("Session error"));

        // When & Then
        assertThatThrownBy(() -> transactionalService.sendMessageTransactional(queueName, message))
            .isInstanceOf(RuntimeException.class);

        verify(connectionPool).releaseConnection(connection);
    }

    @Test
    @DisplayName("Should handle connection pool exception gracefully")
    void shouldHandleConnectionPoolExceptionGracefully() throws JMSException {
        // Given
        String queueName = "TEST.QUEUE";
        String message = "Test message";
        when(connectionPool.getConnection()).thenThrow(new JMSException("Pool exhausted"));

        // When & Then
        assertThatThrownBy(() -> transactionalService.sendMessageTransactional(queueName, message))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Transactional message sending failed");
    }
}