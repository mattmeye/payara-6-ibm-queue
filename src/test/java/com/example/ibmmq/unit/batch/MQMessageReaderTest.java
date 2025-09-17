package com.example.ibmmq.unit.batch;

import com.example.ibmmq.batch.MQMessageReader;
import com.example.ibmmq.config.IBMMQConfig;
import com.example.ibmmq.entity.MQMessage;
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

import java.io.Serializable;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("MQMessageReader Comprehensive Tests")
class MQMessageReaderTest {

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private IBMMQConfig config;

    @Mock
    private Connection connection;

    @Mock
    private Session session;

    @Mock
    private MessageConsumer consumer;

    @Mock
    private Queue queue;

    @Mock
    private TextMessage textMessage;

    @Mock
    private BytesMessage bytesMessage;

    @Mock
    private Message genericMessage;

    @InjectMocks
    private MQMessageReader messageReader;

    @BeforeEach
    void setUp() throws JMSException {
        when(config.getRequestQueue()).thenReturn("TEST.QUEUE");
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createQueue("TEST.QUEUE")).thenReturn(queue);
        when(session.createConsumer(queue)).thenReturn(consumer);
    }

    @Test
    @DisplayName("Should open MQ Message Reader successfully")
    void shouldOpenMQMessageReaderSuccessfully() throws Exception {
        // When
        messageReader.open(null);

        // Then
        verify(config).getRequestQueue();
        verify(connectionFactory).createConnection();
        verify(connection).createSession(false, Session.AUTO_ACKNOWLEDGE);
        verify(session).createQueue("TEST.QUEUE");
        verify(session).createConsumer(queue);
        verify(connection).start();
    }

    @Test
    @DisplayName("Should open with checkpoint parameter")
    void shouldOpenWithCheckpointParameter() throws Exception {
        // Given
        Serializable checkpoint = "test-checkpoint";

        // When
        messageReader.open(checkpoint);

        // Then
        verify(connectionFactory).createConnection();
        verify(connection).start();
    }

    @Test
    @DisplayName("Should handle exception during open")
    void shouldHandleExceptionDuringOpen() throws JMSException {
        // Given
        when(connectionFactory.createConnection()).thenThrow(new JMSException("Connection failed"));

        // When & Then
        assertThatThrownBy(() -> messageReader.open(null))
            .isInstanceOf(JMSException.class)
            .hasMessage("Connection failed");
    }

    @Test
    @DisplayName("Should close MQ Message Reader successfully")
    void shouldCloseMQMessageReaderSuccessfully() throws Exception {
        // Given - simulate opened state
        messageReader.open(null);

        // When
        messageReader.close();

        // Then
        verify(consumer).close();
        verify(session).close();
        verify(connection).close();
    }

    @Test
    @DisplayName("Should handle null resources during close")
    void shouldHandleNullResourcesDuringClose() throws Exception {
        // When - close without opening
        messageReader.close();

        // Then - should not throw exception
        // No verification needed as resources are null
    }

    @Test
    @DisplayName("Should handle JMSException during close")
    void shouldHandleJMSExceptionDuringClose() throws Exception {
        // Given
        messageReader.open(null);
        doThrow(new JMSException("Close failed")).when(consumer).close();

        // When & Then - should not throw exception
        assertThatCode(() -> messageReader.close()).doesNotThrowAnyException();

        verify(consumer).close();
    }

    @Test
    @DisplayName("Should read TextMessage successfully")
    void shouldReadTextMessageSuccessfully() throws Exception {
        // Given
        messageReader.open(null);
        when(consumer.receive(5000)).thenReturn(textMessage);
        when(textMessage.getJMSMessageID()).thenReturn("MSG-001");
        when(textMessage.getJMSCorrelationID()).thenReturn("CORR-001");
        when(textMessage.getJMSPriority()).thenReturn(5);
        when(textMessage.getJMSExpiration()).thenReturn(System.currentTimeMillis() + 60000);
        when(textMessage.getText()).thenReturn("Test message content");

        // When
        Object result = messageReader.readItem();

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage mqMessage = (MQMessage) result;
        assertThat(mqMessage.getQueueName()).isEqualTo("TEST.QUEUE");
        assertThat(mqMessage.getMessageId()).isEqualTo("MSG-001");
        assertThat(mqMessage.getCorrelationId()).isEqualTo("CORR-001");
        assertThat(mqMessage.getPriority()).isEqualTo(5);
        assertThat(mqMessage.getMessageContent()).isEqualTo("Test message content");
        assertThat(mqMessage.getMessageType()).isEqualTo("TEXT");

        verify(consumer).receive(5000);
    }

    @Test
    @DisplayName("Should read BytesMessage successfully")
    void shouldReadBytesMessageSuccessfully() throws Exception {
        // Given
        messageReader.open(null);
        when(consumer.receive(5000)).thenReturn(bytesMessage);
        when(bytesMessage.getJMSMessageID()).thenReturn("MSG-002");
        when(bytesMessage.getJMSCorrelationID()).thenReturn("CORR-002");
        when(bytesMessage.getJMSPriority()).thenReturn(3);
        when(bytesMessage.getJMSExpiration()).thenReturn(0L);
        when(bytesMessage.getBodyLength()).thenReturn(12L);

        // Mock the readBytes method to fill the array
        doAnswer(invocation -> {
            byte[] bytes = invocation.getArgument(0);
            System.arraycopy("Hello World!".getBytes(), 0, bytes, 0, 12);
            return 12;
        }).when(bytesMessage).readBytes(any(byte[].class));

        // When
        Object result = messageReader.readItem();

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage mqMessage = (MQMessage) result;
        assertThat(mqMessage.getMessageId()).isEqualTo("MSG-002");
        assertThat(mqMessage.getMessageContent()).isEqualTo("Hello World!");
        assertThat(mqMessage.getMessageType()).isEqualTo("BYTES");
        assertThat(mqMessage.getExpiry()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should read generic Message successfully")
    void shouldReadGenericMessageSuccessfully() throws Exception {
        // Given
        messageReader.open(null);
        when(consumer.receive(5000)).thenReturn(genericMessage);
        when(genericMessage.getJMSMessageID()).thenReturn("MSG-003");
        when(genericMessage.getJMSCorrelationID()).thenReturn(null);
        when(genericMessage.getJMSPriority()).thenReturn(0);
        when(genericMessage.getJMSExpiration()).thenReturn(0L);
        when(genericMessage.toString()).thenReturn("Generic message string");

        // When
        Object result = messageReader.readItem();

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage mqMessage = (MQMessage) result;
        assertThat(mqMessage.getMessageId()).isEqualTo("MSG-003");
        assertThat(mqMessage.getCorrelationId()).isNull();
        assertThat(mqMessage.getPriority()).isEqualTo(0);
        assertThat(mqMessage.getMessageContent()).isEqualTo("Generic message string");
        assertThat(mqMessage.getMessageType()).startsWith("Message");
    }

    @Test
    @DisplayName("Should return null when no message received within timeout")
    void shouldReturnNullWhenNoMessageReceivedWithinTimeout() throws Exception {
        // Given
        messageReader.open(null);
        when(consumer.receive(5000)).thenReturn(null);

        // When
        Object result = messageReader.readItem();

        // Then
        assertThat(result).isNull();
        verify(consumer).receive(5000);
    }

    @Test
    @DisplayName("Should handle JMSException during readItem")
    void shouldHandleJMSExceptionDuringReadItem() throws Exception {
        // Given
        messageReader.open(null);
        when(consumer.receive(5000)).thenThrow(new JMSException("Receive failed"));

        // When & Then
        assertThatThrownBy(() -> messageReader.readItem())
            .isInstanceOf(Exception.class)
            .hasMessage("Failed to read message from MQ")
            .hasCauseInstanceOf(JMSException.class);
    }

    @Test
    @DisplayName("Should handle TextMessage with null text")
    void shouldHandleTextMessageWithNullText() throws Exception {
        // Given
        messageReader.open(null);
        when(consumer.receive(5000)).thenReturn(textMessage);
        when(textMessage.getJMSMessageID()).thenReturn("MSG-004");
        when(textMessage.getText()).thenReturn(null);

        // When
        Object result = messageReader.readItem();

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage mqMessage = (MQMessage) result;
        assertThat(mqMessage.getMessageContent()).isNull();
        assertThat(mqMessage.getMessageType()).isEqualTo("TEXT");
    }

    @Test
    @DisplayName("Should handle BytesMessage with zero length")
    void shouldHandleBytesMessageWithZeroLength() throws Exception {
        // Given
        messageReader.open(null);
        when(consumer.receive(5000)).thenReturn(bytesMessage);
        when(bytesMessage.getJMSMessageID()).thenReturn("MSG-005");
        when(bytesMessage.getBodyLength()).thenReturn(0L);

        // When
        Object result = messageReader.readItem();

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage mqMessage = (MQMessage) result;
        assertThat(mqMessage.getMessageContent()).isEqualTo("");
        assertThat(mqMessage.getMessageType()).isEqualTo("BYTES");
    }

    @Test
    @DisplayName("Should handle message with all null JMS properties")
    void shouldHandleMessageWithAllNullJMSProperties() throws Exception {
        // Given
        messageReader.open(null);
        when(consumer.receive(5000)).thenReturn(textMessage);
        when(textMessage.getJMSMessageID()).thenReturn(null);
        when(textMessage.getJMSCorrelationID()).thenReturn(null);
        when(textMessage.getJMSPriority()).thenReturn(0);
        when(textMessage.getJMSExpiration()).thenReturn(0L);
        when(textMessage.getText()).thenReturn("Test content");

        // When
        Object result = messageReader.readItem();

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage mqMessage = (MQMessage) result;
        assertThat(mqMessage.getMessageId()).isNull();
        assertThat(mqMessage.getCorrelationId()).isNull();
        assertThat(mqMessage.getMessageContent()).isEqualTo("Test content");
    }

    @Test
    @DisplayName("Should return null for checkpointInfo")
    void shouldReturnNullForCheckpointInfo() throws Exception {
        // When
        Serializable checkpoint = messageReader.checkpointInfo();

        // Then
        assertThat(checkpoint).isNull();
    }

    @Test
    @DisplayName("Should handle exception during getText from TextMessage")
    void shouldHandleExceptionDuringGetTextFromTextMessage() throws Exception {
        // Given
        messageReader.open(null);
        when(consumer.receive(5000)).thenReturn(textMessage);
        when(textMessage.getJMSMessageID()).thenReturn("MSG-006");
        when(textMessage.getText()).thenThrow(new JMSException("getText failed"));

        // When & Then
        assertThatThrownBy(() -> messageReader.readItem())
            .isInstanceOf(Exception.class)
            .hasMessage("Failed to read message from MQ");
    }

    @Test
    @DisplayName("Should handle exception during readBytes from BytesMessage")
    void shouldHandleExceptionDuringReadBytesFromBytesMessage() throws Exception {
        // Given
        messageReader.open(null);
        when(consumer.receive(5000)).thenReturn(bytesMessage);
        when(bytesMessage.getJMSMessageID()).thenReturn("MSG-007");
        when(bytesMessage.getBodyLength()).thenReturn(10L);
        when(bytesMessage.readBytes(any(byte[].class))).thenThrow(new JMSException("readBytes failed"));

        // When & Then
        assertThatThrownBy(() -> messageReader.readItem())
            .isInstanceOf(Exception.class)
            .hasMessage("Failed to read message from MQ");
    }

    @Test
    @DisplayName("Should create MQMessage with correct queue name from config")
    void shouldCreateMQMessageWithCorrectQueueNameFromConfig() throws Exception {
        // Given
        when(config.getRequestQueue()).thenReturn("CUSTOM.QUEUE.NAME");
        when(session.createQueue("CUSTOM.QUEUE.NAME")).thenReturn(queue);
        messageReader.open(null);
        when(consumer.receive(5000)).thenReturn(textMessage);
        when(textMessage.getJMSMessageID()).thenReturn("MSG-008");
        when(textMessage.getText()).thenReturn("Test");

        // When
        Object result = messageReader.readItem();

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage mqMessage = (MQMessage) result;
        assertThat(mqMessage.getQueueName()).isEqualTo("CUSTOM.QUEUE.NAME");
    }

    @Test
    @DisplayName("Should handle large BytesMessage correctly")
    void shouldHandleLargeBytesMessageCorrectly() throws Exception {
        // Given
        messageReader.open(null);
        when(consumer.receive(5000)).thenReturn(bytesMessage);
        when(bytesMessage.getJMSMessageID()).thenReturn("MSG-009");
        when(bytesMessage.getBodyLength()).thenReturn(1000L);

        doAnswer(invocation -> {
            byte[] bytes = invocation.getArgument(0);
            for (int i = 0; i < 1000; i++) {
                bytes[i] = (byte) ('A' + (i % 26));
            }
            return 1000;
        }).when(bytesMessage).readBytes(any(byte[].class));

        // When
        Object result = messageReader.readItem();

        // Then
        assertThat(result).isInstanceOf(MQMessage.class);
        MQMessage mqMessage = (MQMessage) result;
        assertThat(mqMessage.getMessageContent()).hasSize(1000);
        assertThat(mqMessage.getMessageType()).isEqualTo("BYTES");
    }
}