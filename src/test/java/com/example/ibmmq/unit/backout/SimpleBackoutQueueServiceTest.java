package com.example.ibmmq.unit.backout;

import com.example.ibmmq.backout.SimpleBackoutQueueService;
import com.example.ibmmq.config.BackoutQueueConfig;
import com.example.ibmmq.config.IBMMQConfig;
import com.example.ibmmq.entity.MQMessage;
import com.example.ibmmq.pool.IBMMQConnectionPool;
import com.example.ibmmq.repository.MQMessageRepository;
import jakarta.jms.*;
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
import java.util.Enumeration;
import java.util.Vector;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SimpleBackoutQueueService Comprehensive Tests")
class SimpleBackoutQueueServiceTest {

    @InjectMocks
    private SimpleBackoutQueueService backoutQueueService;

    @Mock
    private IBMMQConnectionPool connectionPool;

    @Mock
    private BackoutQueueConfig backoutConfig;

    @Mock
    private IBMMQConfig mqConfig;

    @Mock
    private MQMessageRepository messageRepository;

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
    private QueueBrowser browser;

    @Mock
    private TextMessage textMessage;

    @Mock
    private BytesMessage bytesMessage;

    @Mock
    private ObjectMessage objectMessage;

    private MQMessage createTestMessage(String messageId, String content) {
        MQMessage message = new MQMessage();
        message.setId(1L);
        message.setMessageId(messageId);
        message.setQueueName("TEST.QUEUE");
        message.setMessageContent(content);
        message.setCorrelationId("TEST-CORR-ID");
        message.setPriority(4);
        return message;
    }

    @BeforeEach
    void setUp() throws JMSException {
        // Default mocking setup
        when(backoutConfig.isBackoutEnabled()).thenReturn(true);
        when(backoutConfig.getBackoutQueueSuffix()).thenReturn(".BACKOUT");
        when(connectionPool.getConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createQueue(anyString())).thenReturn(queue);
        when(session.createProducer(any(Queue.class))).thenReturn(producer);
        when(session.createConsumer(any(Queue.class))).thenReturn(consumer);
        when(session.createConsumer(any(Queue.class), anyString())).thenReturn(consumer);
        when(session.createBrowser(any(Queue.class))).thenReturn(browser);
        when(session.createTextMessage(anyString())).thenReturn(textMessage);
        when(messageRepository.save(any(MQMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Should send message to backout queue successfully")
    void shouldSendMessageToBackoutQueueSuccessfully() throws JMSException {
        // Given
        MQMessage message = createTestMessage("MSG-001", "Test message content");

        // When
        backoutQueueService.sendToBackoutQueue(message, "Test error reason");

        // Then
        verify(connectionPool).getConnection();
        verify(session).createQueue("TEST.QUEUE.BACKOUT");
        verify(session).createProducer(queue);
        verify(session).createTextMessage("Test message content");
        verify(textMessage).setStringProperty("ORIGINAL_QUEUE", "TEST.QUEUE");
        verify(textMessage).setStringProperty("ORIGINAL_MESSAGE_ID", "MSG-001");
        verify(textMessage).setStringProperty("BACKOUT_REASON", "Test error reason");
        verify(textMessage).setStringProperty(eq("BACKOUT_TIMESTAMP"), anyString());
        verify(textMessage).setStringProperty("APP_ID", "PayaraIBMMQApp");
        verify(textMessage).setJMSCorrelationID("TEST-CORR-ID");
        verify(textMessage).setJMSPriority(4);
        verify(producer).send(textMessage);
        verify(messageRepository).save(message);
        verify(connectionPool).releaseConnection(connection);

        assertThat(message.getStatus()).isEqualTo(MQMessage.MessageStatus.BACKOUT);
        assertThat(message.getBackoutCount()).isEqualTo(1);
        assertThat(message.getBackoutAt()).isNotNull();
        assertThat(message.getErrorMessage()).isEqualTo("Test error reason");
    }

    @Test
    @DisplayName("Should handle disabled backout queues gracefully")
    void shouldHandleDisabledBackoutQueuesGracefully() throws JMSException {
        // Given
        when(backoutConfig.isBackoutEnabled()).thenReturn(false);
        MQMessage message = createTestMessage("MSG-002", "Test content");

        // When
        backoutQueueService.sendToBackoutQueue(message, "Error reason");

        // Then
        verify(connectionPool, never()).getConnection();
        verify(messageRepository).save(message);
        assertThat(message.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);
        assertThat(message.getErrorMessage()).isEqualTo("Error reason");
    }

    @Test
    @DisplayName("Should handle JMS exception during backout gracefully")
    void shouldHandleJMSExceptionDuringBackoutGracefully() throws JMSException {
        // Given
        when(connectionPool.getConnection()).thenThrow(new JMSException("Connection failed"));
        MQMessage message = createTestMessage("MSG-003", "Test content");

        // When
        backoutQueueService.sendToBackoutQueue(message, "Original error");

        // Then
        verify(connectionPool).getConnection();
        verify(messageRepository).save(message);
        assertThat(message.getStatus()).isEqualTo(MQMessage.MessageStatus.FAILED);
        assertThat(message.getErrorMessage()).contains("Backout failed: Connection failed");
        assertThat(message.getErrorMessage()).contains("Original: Original error");
    }

    @Test
    @DisplayName("Should send original JMS message to backout queue")
    void shouldSendOriginalJMSMessageToBackoutQueue() throws JMSException {
        // Given
        when(textMessage.getText()).thenReturn("Original message text");
        when(textMessage.getJMSCorrelationID()).thenReturn("ORIG-CORR-ID");
        when(textMessage.getJMSPriority()).thenReturn(8);
        when(textMessage.getJMSExpiration()).thenReturn(123456789L);

        // When
        backoutQueueService.sendToBackoutQueue("ORIGINAL.QUEUE", textMessage, "Processing failed");

        // Then
        verify(session).createQueue("ORIGINAL.QUEUE.BACKOUT");
        verify(session).createTextMessage("Original message text");
        verify(textMessage, atLeastOnce()).setStringProperty("ORIGINAL_QUEUE", "ORIGINAL.QUEUE");
        verify(textMessage, atLeastOnce()).setStringProperty("BACKOUT_REASON", "Processing failed");
        verify(producer).send(textMessage);
    }

    @Test
    @DisplayName("Should handle disabled backout for original JMS message")
    void shouldHandleDisabledBackoutForOriginalJMSMessage() throws JMSException {
        // Given
        when(backoutConfig.isBackoutEnabled()).thenReturn(false);

        // When
        backoutQueueService.sendToBackoutQueue("ORIGINAL.QUEUE", textMessage, "Processing failed");

        // Then
        verify(connectionPool, never()).getConnection();
    }

    @Test
    @DisplayName("Should get backout queue statistics successfully")
    void shouldGetBackoutQueueStatisticsSuccessfully() throws JMSException {
        // Given
        Vector<Message> messages = new Vector<>();
        messages.add(textMessage);
        messages.add(textMessage);
        Enumeration<Message> messageEnum = messages.elements();
        when(browser.getEnumeration()).thenReturn(messageEnum);
        when(messageRepository.countByStatus(MQMessage.MessageStatus.BACKOUT)).thenReturn(5L);

        // When
        SimpleBackoutQueueService.BackoutQueueStats stats =
            backoutQueueService.getBackoutQueueStats("TEST.QUEUE");

        // Then
        assertThat(stats.getBackoutQueueName()).isEqualTo("TEST.QUEUE.BACKOUT");
        assertThat(stats.getBackoutMessageCount()).isEqualTo(2);
        assertThat(stats.getDbBackoutCount()).isEqualTo(5L);
        verify(session).createBrowser(queue);
    }

    @Test
    @DisplayName("Should handle statistics exception gracefully")
    void shouldHandleStatisticsExceptionGracefully() throws JMSException {
        // Given
        when(connectionPool.getConnection()).thenThrow(new JMSException("Browser failed"));

        // When
        SimpleBackoutQueueService.BackoutQueueStats stats =
            backoutQueueService.getBackoutQueueStats("TEST.QUEUE");

        // Then
        assertThat(stats.getBackoutQueueName()).isEqualTo("TEST.QUEUE.BACKOUT");
        assertThat(stats.getBackoutMessageCount()).isEqualTo(-1);
        assertThat(stats.getDbBackoutCount()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Should move all messages back to original queue successfully")
    void shouldMoveAllMessagesBackToOriginalQueueSuccessfully() throws JMSException {
        // Given
        when(consumer.receive(1000))
            .thenReturn(textMessage)
            .thenReturn(textMessage)
            .thenReturn(null); // End of messages
        when(textMessage.getText()).thenReturn("Message content");
        when(textMessage.getJMSCorrelationID()).thenReturn("CORR-ID");
        when(textMessage.getJMSPriority()).thenReturn(5);
        when(textMessage.getJMSExpiration()).thenReturn(987654321L);

        Vector<String> propertyNames = new Vector<>();
        propertyNames.add("ORIGINAL_QUEUE");
        propertyNames.add("BACKOUT_REASON");
        propertyNames.add("CUSTOM_PROP");
        when(textMessage.getPropertyNames()).thenReturn(propertyNames.elements());
        when(textMessage.getObjectProperty("CUSTOM_PROP")).thenReturn("custom_value");

        // When
        int movedCount = backoutQueueService.moveAllBackToOriginalQueue(
            "TEST.QUEUE.BACKOUT", "TEST.QUEUE");

        // Then
        assertThat(movedCount).isEqualTo(2);
        verify(session).createQueue("TEST.QUEUE.BACKOUT");
        verify(session).createQueue("TEST.QUEUE");
        verify(session).createConsumer(queue);
        verify(session).createProducer(queue);
        verify(consumer, times(3)).receive(1000);
        verify(producer, times(2)).send(any(TextMessage.class));
        verify(connection).start();
    }

    @Test
    @DisplayName("Should move batch messages back to original queue successfully")
    void shouldMoveBatchMessagesBackToOriginalQueueSuccessfully() throws JMSException {
        // Given
        when(consumer.receive(1000))
            .thenReturn(textMessage)
            .thenReturn(textMessage)
            .thenReturn(null); // Only 2 messages available
        when(textMessage.getText()).thenReturn("Batch message");
        when(textMessage.getPropertyNames()).thenReturn(new Vector<String>().elements());

        // When
        int movedCount = backoutQueueService.moveBatchBackToOriginalQueue(
            "TEST.QUEUE.BACKOUT", "TEST.QUEUE", 10); // Request more than available

        // Then
        assertThat(movedCount).isEqualTo(2); // Should return actual moved count
        verify(consumer, times(3)).receive(1000); // Called until null
        verify(producer, times(2)).send(any(Message.class));
    }

    @Test
    @DisplayName("Should handle move back exception gracefully")
    void shouldHandleMoveBackExceptionGracefully() throws JMSException {
        // Given
        when(connectionPool.getConnection()).thenThrow(new JMSException("Move failed"));

        // When
        int movedCount = backoutQueueService.moveAllBackToOriginalQueue(
            "TEST.QUEUE.BACKOUT", "TEST.QUEUE");

        // Then
        assertThat(movedCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Should copy BytesMessage correctly")
    void shouldCopyBytesMessageCorrectly() throws JMSException {
        // Given
        when(bytesMessage.getBodyLength()).thenReturn(10L);
        when(bytesMessage.getPropertyNames()).thenReturn(new Vector<String>().elements());
        doAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            // Simulate reading bytes
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = (byte) i;
            }
            return buffer.length;
        }).when(bytesMessage).readBytes(any(byte[].class));

        BytesMessage copyBytes = mock(BytesMessage.class);
        when(session.createBytesMessage()).thenReturn(copyBytes);

        when(consumer.receive(1000)).thenReturn(bytesMessage).thenReturn(null);

        // When
        int movedCount = backoutQueueService.moveAllBackToOriginalQueue(
            "TEST.QUEUE.BACKOUT", "TEST.QUEUE");

        // Then
        assertThat(movedCount).isEqualTo(1);
        verify(session).createBytesMessage();
        verify(copyBytes).writeBytes(any(byte[].class));
        verify(producer).send(any(Message.class));
    }

    @Test
    @DisplayName("Should copy ObjectMessage correctly")
    void shouldCopyObjectMessageCorrectly() throws JMSException {
        // Given
        String testObject = "Test Object";
        when(objectMessage.getObject()).thenReturn(testObject);
        when(objectMessage.getPropertyNames()).thenReturn(new Vector<String>().elements());
        ObjectMessage copyObject = mock(ObjectMessage.class);
        when(session.createObjectMessage(testObject)).thenReturn(copyObject);

        when(consumer.receive(1000)).thenReturn(objectMessage).thenReturn(null);

        // When
        int movedCount = backoutQueueService.moveAllBackToOriginalQueue(
            "TEST.QUEUE.BACKOUT", "TEST.QUEUE");

        // Then
        assertThat(movedCount).isEqualTo(1);
        verify(session).createObjectMessage(testObject);
        verify(producer).send(any(Message.class));
    }

    @Test
    @DisplayName("Should handle unknown message type")
    void shouldHandleUnknownMessageType() throws JMSException {
        // Given
        Message unknownMessage = mock(Message.class);
        when(unknownMessage.toString()).thenReturn("Unknown message type");
        when(unknownMessage.getJMSCorrelationID()).thenReturn("UNKNOWN-CORR");
        when(unknownMessage.getJMSPriority()).thenReturn(3);
        when(unknownMessage.getJMSExpiration()).thenReturn(555555L);
        when(unknownMessage.getPropertyNames()).thenReturn(new Vector<String>().elements());

        TextMessage fallbackMessage = mock(TextMessage.class);
        when(session.createTextMessage("Unknown message type")).thenReturn(fallbackMessage);

        when(consumer.receive(1000)).thenReturn(unknownMessage).thenReturn(null);

        // When
        int movedCount = backoutQueueService.moveAllBackToOriginalQueue(
            "TEST.QUEUE.BACKOUT", "TEST.QUEUE");

        // Then
        assertThat(movedCount).isEqualTo(1);
        verify(session).createTextMessage("Unknown message type");
        verify(producer).send(any(Message.class));
    }

    @Test
    @DisplayName("Should filter backout properties when copying original properties")
    void shouldFilterBackoutPropertiesWhenCopyingOriginalProperties() throws JMSException {
        // Given
        Vector<String> propertyNames = new Vector<>();
        propertyNames.add("BACKOUT_REASON");
        propertyNames.add("BACKOUT_TIMESTAMP");
        propertyNames.add("ORIGINAL_QUEUE");
        propertyNames.add("ORIGINAL_MESSAGE_ID");
        propertyNames.add("VALID_PROP");
        propertyNames.add("ANOTHER_VALID_PROP");

        when(textMessage.getPropertyNames()).thenReturn(propertyNames.elements());
        when(textMessage.getObjectProperty("VALID_PROP")).thenReturn("valid_value");
        when(textMessage.getObjectProperty("ANOTHER_VALID_PROP")).thenReturn("another_value");
        when(textMessage.getText()).thenReturn("Test message");

        when(consumer.receive(1000)).thenReturn(textMessage).thenReturn(null);

        // When
        int movedCount = backoutQueueService.moveAllBackToOriginalQueue(
            "TEST.QUEUE.BACKOUT", "TEST.QUEUE");

        // Then
        assertThat(movedCount).isEqualTo(1);

        // Verify that backout properties are NOT copied
        verify(textMessage, never()).setObjectProperty(eq("BACKOUT_REASON"), any());
        verify(textMessage, never()).setObjectProperty(eq("BACKOUT_TIMESTAMP"), any());
        verify(textMessage, never()).setObjectProperty(eq("ORIGINAL_QUEUE"), any());
        verify(textMessage, never()).setObjectProperty(eq("ORIGINAL_MESSAGE_ID"), any());

        // Verify that valid properties ARE copied
        verify(textMessage).setObjectProperty("VALID_PROP", "valid_value");
        verify(textMessage).setObjectProperty("ANOTHER_VALID_PROP", "another_value");
    }

    @Test
    @DisplayName("BackoutQueueStats should have correct getters")
    void backoutQueueStatsShouldHaveCorrectGetters() {
        // Given
        SimpleBackoutQueueService.BackoutQueueStats stats =
            new SimpleBackoutQueueService.BackoutQueueStats("TEST.QUEUE.BACKOUT", 10, 15L);

        // Then
        assertThat(stats.getBackoutQueueName()).isEqualTo("TEST.QUEUE.BACKOUT");
        assertThat(stats.getBackoutMessageCount()).isEqualTo(10);
        assertThat(stats.getDbBackoutCount()).isEqualTo(15L);
    }
}