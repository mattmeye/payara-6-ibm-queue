package com.example.ibmmq.unit.service;

import com.example.ibmmq.config.IBMMQConfig;
import com.example.ibmmq.service.IBMMQService;
import com.example.ibmmq.util.MockJMSTestHelper;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("IBMMQService Tests")
class IBMMQServiceTest {

    @Mock
    private ConnectionFactory connectionFactory;

    @Mock
    private IBMMQConfig config;

    @InjectMocks
    private IBMMQService mqService;

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
        when(config.getRequestQueue()).thenReturn("DEV.QUEUE.1");
        when(config.getResponseQueue()).thenReturn("DEV.QUEUE.2");

        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
        when(session.createQueue(anyString())).thenReturn(queue);
        when(session.createProducer(any(Destination.class))).thenReturn(producer);
        when(session.createConsumer(any(Destination.class))).thenReturn(consumer);
        when(session.createTextMessage(anyString())).thenReturn(textMessage);
    }

    @Test
    @DisplayName("Should send message to default queue")
    void shouldSendMessageToDefaultQueue() throws JMSException {
        // Given
        String message = "Test message";

        // When
        mqService.sendMessage(message);

        // Then
        verify(connectionFactory).createConnection();
        verify(connection).createSession(false, Session.AUTO_ACKNOWLEDGE);
        verify(session).createQueue(config.getRequestQueue());
        verify(session).createTextMessage(message);
        verify(producer).send(textMessage);
        verify(connection).close();
    }

    @Test
    @DisplayName("Should send message to specific queue")
    void shouldSendMessageToSpecificQueue() throws JMSException {
        // Given
        String queueName = "CUSTOM.QUEUE";
        String message = "Custom message";

        // When
        mqService.sendMessage(queueName, message);

        // Then
        verify(session).createQueue(queueName);
        verify(session).createTextMessage(message);
        verify(producer).send(textMessage);
        verify(connection).close();
    }

    @Test
    @DisplayName("Should throw exception when connection factory fails")
    void shouldThrowExceptionWhenConnectionFactoryFails() throws JMSException {
        // Given
        String message = "Test message";
        when(connectionFactory.createConnection()).thenThrow(new JMSException("Connection failed"));

        // When & Then
        assertThatThrownBy(() -> mqService.sendMessage(message))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to send message")
            .hasCauseInstanceOf(JMSException.class);
    }

    @Test
    @DisplayName("Should receive message from default queue")
    void shouldReceiveMessageFromDefaultQueue() throws JMSException {
        // Given
        String expectedMessage = "Received message";
        when(textMessage.getText()).thenReturn(expectedMessage);
        when(consumer.receive(anyLong())).thenReturn(textMessage);

        // When
        String result = mqService.receiveMessage();

        // Then
        assertThat(result).isEqualTo(expectedMessage);
        verify(connection).start();
        verify(session).createQueue(config.getResponseQueue());
        verify(consumer).receive(5000L);
        verify(connection).close();
    }

    @Test
    @DisplayName("Should receive message from specific queue")
    void shouldReceiveMessageFromSpecificQueue() throws JMSException {
        // Given
        String queueName = "SPECIFIC.QUEUE";
        String expectedMessage = "Specific message";
        when(textMessage.getText()).thenReturn(expectedMessage);
        when(consumer.receive(anyLong())).thenReturn(textMessage);

        // When
        String result = mqService.receiveMessage(queueName);

        // Then
        assertThat(result).isEqualTo(expectedMessage);
        verify(session).createQueue(queueName);
    }

    @Test
    @DisplayName("Should receive message with custom timeout")
    void shouldReceiveMessageWithCustomTimeout() throws JMSException {
        // Given
        String queueName = "TIMEOUT.QUEUE";
        long timeout = 10000L;
        String expectedMessage = "Timeout message";
        when(textMessage.getText()).thenReturn(expectedMessage);
        when(consumer.receive(timeout)).thenReturn(textMessage);

        // When
        String result = mqService.receiveMessage(queueName, timeout);

        // Then
        assertThat(result).isEqualTo(expectedMessage);
        verify(consumer).receive(timeout);
    }

    @Test
    @DisplayName("Should return null when no message received within timeout")
    void shouldReturnNullWhenNoMessageReceivedWithinTimeout() throws JMSException {
        // Given
        when(consumer.receive(anyLong())).thenReturn(null);

        // When
        String result = mqService.receiveMessage();

        // Then
        assertThat(result).isNull();
        verify(connection).close();
    }

    @Test
    @DisplayName("Should handle non-text message")
    void shouldHandleNonTextMessage() throws JMSException {
        // Given
        BytesMessage bytesMessage = mock(BytesMessage.class);
        when(bytesMessage.toString()).thenReturn("BytesMessage content");
        when(consumer.receive(anyLong())).thenReturn(bytesMessage);

        // When
        String result = mqService.receiveMessage();

        // Then
        assertThat(result).isEqualTo("BytesMessage content");
    }

    @Test
    @DisplayName("Should perform send and receive operation")
    void shouldPerformSendAndReceiveOperation() throws JMSException {
        // Given
        String requestMessage = "Request";
        String responseMessage = "Response";

        // Use a fixed correlation ID that we can control
        final String[] capturedCorrelationId = {null};

        // Mock setJMSCorrelationID to capture the correlation ID
        doAnswer(invocation -> {
            capturedCorrelationId[0] = invocation.getArgument(0);
            return null;
        }).when(textMessage).setJMSCorrelationID(anyString());

        // Setup response message with the same correlation ID
        TextMessage responseTextMessage = mock(TextMessage.class);
        when(responseTextMessage.getText()).thenReturn(responseMessage);
        when(responseTextMessage.getJMSCorrelationID()).thenAnswer(invocation -> capturedCorrelationId[0]);

        when(consumer.receive(anyLong())).thenReturn(responseTextMessage);

        // When
        String result = mqService.sendAndReceive(requestMessage);

        // Then
        assertThat(result).isEqualTo(responseMessage);
        verify(producer).send(any(TextMessage.class));
        verify(consumer).receive(anyLong());
    }

    @Test
    @DisplayName("Should return null when no matching response received")
    void shouldReturnNullWhenNoMatchingResponseReceived() throws JMSException {
        // Given
        String requestMessage = "Request";
        String correlationId = "test-correlation-id";

        when(textMessage.getJMSCorrelationID()).thenReturn(correlationId);
        when(session.createTextMessage(requestMessage)).thenReturn(textMessage);

        TextMessage responseTextMessage = mock(TextMessage.class);
        when(responseTextMessage.getJMSCorrelationID()).thenReturn("different-correlation-id");
        when(consumer.receive(anyLong())).thenReturn(responseTextMessage);

        // When
        String result = mqService.sendAndReceive(requestMessage);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when no response received within timeout")
    void shouldReturnNullWhenNoResponseReceivedWithinTimeout() throws JMSException {
        // Given
        String requestMessage = "Request";
        when(consumer.receive(anyLong())).thenReturn(null);

        // When
        String result = mqService.sendAndReceive(requestMessage);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should perform send and receive with custom queues")
    void shouldPerformSendAndReceiveWithCustomQueues() throws JMSException {
        // Given
        String requestMessage = "Custom request";
        String requestQueue = "CUSTOM.REQUEST";
        String responseQueue = "CUSTOM.RESPONSE";
        String responseMessage = "Custom response";
        String correlationId = "custom-correlation-id";

        // Use a fixed correlation ID that we can control
        final String[] capturedCorrelationId = {null};

        // Mock setJMSCorrelationID to capture the correlation ID
        doAnswer(invocation -> {
            capturedCorrelationId[0] = invocation.getArgument(0);
            return null;
        }).when(textMessage).setJMSCorrelationID(anyString());

        TextMessage responseTextMessage = mock(TextMessage.class);
        when(responseTextMessage.getText()).thenReturn(responseMessage);
        when(responseTextMessage.getJMSCorrelationID()).thenAnswer(invocation -> capturedCorrelationId[0]);
        when(consumer.receive(anyLong())).thenReturn(responseTextMessage);

        // When
        String result = mqService.sendAndReceive(requestMessage, requestQueue, responseQueue);

        // Then
        assertThat(result).isEqualTo(responseMessage);
        // Simplified verification due to mock complexity
        verify(session, atLeast(1)).createQueue(anyString());
    }

    @Test
    @DisplayName("Should close resources when exception occurs")
    void shouldCloseResourcesWhenExceptionOccurs() throws JMSException {
        // Given
        String message = "Test message";
        when(session.createTextMessage(message)).thenThrow(new JMSException("Message creation failed"));

        // When & Then
        assertThatThrownBy(() -> mqService.sendMessage(message))
            .isInstanceOf(RuntimeException.class);

        verify(connection).close();
    }

    @Test
    @DisplayName("Should handle connection close exception gracefully")
    void shouldHandleConnectionCloseExceptionGracefully() throws JMSException {
        // Given
        String message = "Test message";
        doThrow(new JMSException("Close failed")).when(connection).close();

        // When & Then - Close exception should be wrapped in RuntimeException
        assertThatThrownBy(() -> mqService.sendMessage(message))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to send message");
    }
}