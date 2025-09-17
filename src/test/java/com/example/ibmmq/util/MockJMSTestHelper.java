package com.example.ibmmq.util;

import jakarta.jms.*;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class MockJMSTestHelper {

    public static ConnectionFactory createMockConnectionFactory() throws JMSException {
        ConnectionFactory factory = mock(ConnectionFactory.class);
        Connection connection = createMockConnection();
        when(factory.createConnection()).thenReturn(connection);
        return factory;
    }

    public static Connection createMockConnection() throws JMSException {
        Connection connection = mock(Connection.class);
        Session session = createMockSession();
        when(connection.createSession(anyBoolean(), anyInt())).thenReturn(session);
        doNothing().when(connection).start();
        doNothing().when(connection).close();
        return connection;
    }

    public static Session createMockSession() throws JMSException {
        Session session = mock(Session.class);

        // Mock Queue creation
        Queue queue = mock(Queue.class);
        when(session.createQueue(any(String.class))).thenReturn(queue);

        // Mock Producer
        MessageProducer producer = mock(MessageProducer.class);
        when(session.createProducer(any(Destination.class))).thenReturn(producer);
        doNothing().when(producer).send(any(Message.class));

        // Mock Consumer
        MessageConsumer consumer = mock(MessageConsumer.class);
        when(session.createConsumer(any(Destination.class))).thenReturn(consumer);

        // Mock TextMessage
        TextMessage textMessage = mock(TextMessage.class);
        when(session.createTextMessage(any(String.class))).thenReturn(textMessage);
        when(session.createTextMessage()).thenReturn(textMessage);

        // Mock BytesMessage
        BytesMessage bytesMessage = mock(BytesMessage.class);
        when(session.createBytesMessage()).thenReturn(bytesMessage);

        return session;
    }

    public static TextMessage createMockTextMessage(String content) throws JMSException {
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn(content);
        when(message.getJMSMessageID()).thenReturn("TEST_MSG_ID_" + System.currentTimeMillis());
        when(message.getJMSCorrelationID()).thenReturn("TEST_CORR_ID");
        when(message.getJMSPriority()).thenReturn(4);
        when(message.getJMSExpiration()).thenReturn(0L);
        when(message.getJMSTimestamp()).thenReturn(System.currentTimeMillis());

        // Mock property access
        when(message.getStringProperty(any(String.class))).thenReturn(null);
        when(message.getObjectProperty(any(String.class))).thenReturn(null);
        when(message.propertyExists(any(String.class))).thenReturn(false);

        return message;
    }

    public static BytesMessage createMockBytesMessage(byte[] content) throws JMSException {
        BytesMessage message = mock(BytesMessage.class);
        when(message.getBodyLength()).thenReturn((long) content.length);
        when(message.readBytes(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(content, 0, buffer, 0, Math.min(content.length, buffer.length));
            return content.length;
        });
        when(message.getJMSMessageID()).thenReturn("TEST_BYTES_MSG_ID_" + System.currentTimeMillis());
        return message;
    }

    public static MessageConsumer createMockConsumerWithMessage(Message message) throws JMSException {
        MessageConsumer consumer = mock(MessageConsumer.class);
        when(consumer.receive(anyLong())).thenReturn(message);
        when(consumer.receive()).thenReturn(message);
        return consumer;
    }

    public static MessageConsumer createMockConsumerWithTimeout() throws JMSException {
        MessageConsumer consumer = mock(MessageConsumer.class);
        when(consumer.receive(anyLong())).thenReturn(null);
        when(consumer.receive()).thenReturn(null);
        return consumer;
    }

    public static QueueBrowser createMockQueueBrowser(Message... messages) throws JMSException {
        QueueBrowser browser = mock(QueueBrowser.class);

        @SuppressWarnings("unchecked")
        java.util.Enumeration<Message> enumeration = mock(java.util.Enumeration.class);

        // Setup enumeration behavior
        boolean[] hasMoreCalled = {false};
        int[] currentIndex = {0};

        when(enumeration.hasMoreElements()).thenAnswer(invocation -> {
            hasMoreCalled[0] = true;
            return currentIndex[0] < messages.length;
        });

        when(enumeration.nextElement()).thenAnswer(invocation -> {
            if (currentIndex[0] < messages.length) {
                return messages[currentIndex[0]++];
            }
            throw new java.util.NoSuchElementException();
        });

        when(browser.getEnumeration()).thenReturn(enumeration);
        return browser;
    }

    public static void verifyMessageSent(MessageProducer producer, int times) throws JMSException {
        verify(producer, times(times)).send(any(Message.class));
    }

    public static void verifyMessageReceived(MessageConsumer consumer, int times) throws JMSException {
        verify(consumer, times(times)).receive(anyLong());
    }

    public static void verifyConnectionClosed(Connection connection) throws JMSException {
        verify(connection, times(1)).close();
    }

    public static void verifySessionClosed(Session session) throws JMSException {
        verify(session, times(1)).close();
    }
}