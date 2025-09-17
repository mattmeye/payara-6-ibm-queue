package com.example.ibmmq.unit.adapter;

import com.example.ibmmq.adapter.JakartaJMSAdapter;
import com.example.ibmmq.adapter.MessageWrappers.*;
import com.example.ibmmq.adapter.DestinationWrappers.*;
import jakarta.jms.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Jakarta JMS Adapter Comprehensive Tests")
class JakartaJMSAdapterTest {

    @Mock
    private javax.jms.ConnectionFactory javaxConnectionFactory;

    @Mock
    private javax.jms.Connection javaxConnection;

    @Mock
    private javax.jms.Session javaxSession;

    @Mock
    private javax.jms.TextMessage javaxTextMessage;

    @Mock
    private javax.jms.Queue javaxQueue;

    @Mock
    private javax.jms.Topic javaxTopic;

    @Mock
    private javax.jms.MessageProducer javaxProducer;

    @Mock
    private javax.jms.MessageConsumer javaxConsumer;

    @Mock
    private javax.jms.ConnectionMetaData javaxMetadata;

    @Mock
    private javax.jms.ExceptionListener javaxExceptionListener;

    @Mock
    private javax.jms.MessageListener javaxMessageListener;

    private JakartaJMSAdapter.ConnectionFactoryWrapper connectionFactoryWrapper;
    private JakartaJMSAdapter.ConnectionWrapper connectionWrapper;
    private JakartaJMSAdapter.SessionWrapper sessionWrapper;

    @BeforeEach
    void setUp() {
        connectionFactoryWrapper = new JakartaJMSAdapter.ConnectionFactoryWrapper(javaxConnectionFactory);
        connectionWrapper = new JakartaJMSAdapter.ConnectionWrapper(javaxConnection);
        sessionWrapper = new JakartaJMSAdapter.SessionWrapper(javaxSession);
    }

    @Test
    @DisplayName("ConnectionFactory: Should create connection")
    void connectionFactoryShouldCreateConnection() throws Exception {
        // Given
        when(javaxConnectionFactory.createConnection()).thenReturn(javaxConnection);

        // When
        jakarta.jms.Connection result = connectionFactoryWrapper.createConnection();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(JakartaJMSAdapter.ConnectionWrapper.class);
        verify(javaxConnectionFactory).createConnection();
    }

    @Test
    @DisplayName("ConnectionFactory: Should create connection with credentials")
    void connectionFactoryShouldCreateConnectionWithCredentials() throws Exception {
        // Given
        when(javaxConnectionFactory.createConnection("user", "pass")).thenReturn(javaxConnection);

        // When
        jakarta.jms.Connection result = connectionFactoryWrapper.createConnection("user", "pass");

        // Then
        assertThat(result).isNotNull();
        verify(javaxConnectionFactory).createConnection("user", "pass");
    }

    @Test
    @DisplayName("ConnectionFactory: Should throw UnsupportedOperationException for JMSContext")
    void connectionFactoryShouldThrowUnsupportedForJMSContext() {
        // When & Then
        assertThatThrownBy(() -> connectionFactoryWrapper.createContext())
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("JMSContext not supported");

        assertThatThrownBy(() -> connectionFactoryWrapper.createContext("user", "pass"))
            .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> connectionFactoryWrapper.createContext("user", "pass", Session.AUTO_ACKNOWLEDGE))
            .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> connectionFactoryWrapper.createContext(Session.AUTO_ACKNOWLEDGE))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("ConnectionFactory: Should handle javax JMSException")
    void connectionFactoryShouldHandleJavaxJMSException() throws Exception {
        // Given
        when(javaxConnectionFactory.createConnection()).thenThrow(new javax.jms.JMSException("Test error"));

        // When & Then
        assertThatThrownBy(() -> connectionFactoryWrapper.createConnection())
            .isInstanceOf(JMSException.class)
            .hasMessage("Test error");
    }

    @Test
    @DisplayName("Connection: Should create session with transaction and acknowledge mode")
    void connectionShouldCreateSession() throws Exception {
        // Given
        when(javaxConnection.createSession(true, Session.AUTO_ACKNOWLEDGE)).thenReturn(javaxSession);

        // When
        jakarta.jms.Session result = connectionWrapper.createSession(true, Session.AUTO_ACKNOWLEDGE);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(JakartaJMSAdapter.SessionWrapper.class);
        verify(javaxConnection).createSession(true, Session.AUTO_ACKNOWLEDGE);
    }

    @Test
    @DisplayName("Connection: Should create session with session mode")
    void connectionShouldCreateSessionWithMode() throws Exception {
        // Given
        when(javaxConnection.createSession(Session.AUTO_ACKNOWLEDGE)).thenReturn(javaxSession);

        // When
        jakarta.jms.Session result = connectionWrapper.createSession(Session.AUTO_ACKNOWLEDGE);

        // Then
        assertThat(result).isNotNull();
        verify(javaxConnection).createSession(Session.AUTO_ACKNOWLEDGE);
    }

    @Test
    @DisplayName("Connection: Should create default session")
    void connectionShouldCreateDefaultSession() throws Exception {
        // Given
        when(javaxConnection.createSession()).thenReturn(javaxSession);

        // When
        jakarta.jms.Session result = connectionWrapper.createSession();

        // Then
        assertThat(result).isNotNull();
        verify(javaxConnection).createSession();
    }

    @Test
    @DisplayName("Connection: Should get and set client ID")
    void connectionShouldGetAndSetClientId() throws Exception {
        // Given
        when(javaxConnection.getClientID()).thenReturn("test-client");

        // When
        String clientId = connectionWrapper.getClientID();
        connectionWrapper.setClientID("new-client");

        // Then
        assertThat(clientId).isEqualTo("test-client");
        verify(javaxConnection).getClientID();
        verify(javaxConnection).setClientID("new-client");
    }

    @Test
    @DisplayName("Connection: Should get metadata")
    void connectionShouldGetMetadata() throws Exception {
        // Given
        when(javaxConnection.getMetaData()).thenReturn(javaxMetadata);

        // When
        ConnectionMetaData result = connectionWrapper.getMetaData();

        // Then
        assertThat(result).isNotNull();
        verify(javaxConnection).getMetaData();
    }

    @Test
    @DisplayName("Connection: Should handle exception listener")
    void connectionShouldHandleExceptionListener() throws Exception {
        // Given
        when(javaxConnection.getExceptionListener()).thenReturn(javaxExceptionListener);
        ExceptionListener jakartaListener = mock(ExceptionListener.class);

        // When
        ExceptionListener result = connectionWrapper.getExceptionListener();
        connectionWrapper.setExceptionListener(jakartaListener);
        connectionWrapper.setExceptionListener(null);

        // Then
        assertThat(result).isNotNull();
        verify(javaxConnection).getExceptionListener();
        verify(javaxConnection, times(2)).setExceptionListener(any());
    }

    @Test
    @DisplayName("Connection: Should start, stop and close")
    void connectionShouldStartStopClose() throws Exception {
        // When
        connectionWrapper.start();
        connectionWrapper.stop();
        connectionWrapper.close();

        // Then
        verify(javaxConnection).start();
        verify(javaxConnection).stop();
        verify(javaxConnection).close();
    }

    @Test
    @DisplayName("Connection: Should throw UnsupportedOperationException for ConnectionConsumer")
    void connectionShouldThrowUnsupportedForConnectionConsumer() {
        // Given
        Destination destination = mock(Destination.class);
        Topic topic = mock(Topic.class);
        ServerSessionPool pool = mock(ServerSessionPool.class);

        // When & Then
        assertThatThrownBy(() -> connectionWrapper.createConnectionConsumer(destination, null, pool, 1))
            .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> connectionWrapper.createSharedConnectionConsumer(topic, "sub", null, pool, 1))
            .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> connectionWrapper.createDurableConnectionConsumer(topic, "sub", null, pool, 1))
            .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> connectionWrapper.createSharedDurableConnectionConsumer(topic, "sub", null, pool, 1))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Session: Should create all message types")
    void sessionShouldCreateAllMessageTypes() throws Exception {
        // Given
        when(javaxSession.createBytesMessage()).thenReturn(mock(javax.jms.BytesMessage.class));
        when(javaxSession.createMapMessage()).thenReturn(mock(javax.jms.MapMessage.class));
        when(javaxSession.createMessage()).thenReturn(mock(javax.jms.Message.class));
        when(javaxSession.createObjectMessage()).thenReturn(mock(javax.jms.ObjectMessage.class));
        when(javaxSession.createObjectMessage(any())).thenReturn(mock(javax.jms.ObjectMessage.class));
        when(javaxSession.createStreamMessage()).thenReturn(mock(javax.jms.StreamMessage.class));
        when(javaxSession.createTextMessage()).thenReturn(javaxTextMessage);
        when(javaxSession.createTextMessage("test")).thenReturn(javaxTextMessage);

        // When
        BytesMessage bytesMessage = sessionWrapper.createBytesMessage();
        MapMessage mapMessage = sessionWrapper.createMapMessage();
        Message message = sessionWrapper.createMessage();
        ObjectMessage objectMessage1 = sessionWrapper.createObjectMessage();
        ObjectMessage objectMessage2 = sessionWrapper.createObjectMessage("test");
        StreamMessage streamMessage = sessionWrapper.createStreamMessage();
        TextMessage textMessage1 = sessionWrapper.createTextMessage();
        TextMessage textMessage2 = sessionWrapper.createTextMessage("test");

        // Then
        assertThat(bytesMessage).isNotNull();
        assertThat(mapMessage).isNotNull();
        assertThat(message).isNotNull();
        assertThat(objectMessage1).isNotNull();
        assertThat(objectMessage2).isNotNull();
        assertThat(streamMessage).isNotNull();
        assertThat(textMessage1).isNotNull();
        assertThat(textMessage2).isNotNull();
    }

    @Test
    @DisplayName("Session: Should handle transaction methods")
    void sessionShouldHandleTransactionMethods() throws Exception {
        // Given
        when(javaxSession.getTransacted()).thenReturn(true);
        when(javaxSession.getAcknowledgeMode()).thenReturn(Session.AUTO_ACKNOWLEDGE);

        // When
        boolean transacted = sessionWrapper.getTransacted();
        int ackMode = sessionWrapper.getAcknowledgeMode();
        sessionWrapper.commit();
        sessionWrapper.rollback();
        sessionWrapper.recover();

        // Then
        assertThat(transacted).isTrue();
        assertThat(ackMode).isEqualTo(Session.AUTO_ACKNOWLEDGE);
        verify(javaxSession).commit();
        verify(javaxSession).rollback();
        verify(javaxSession).recover();
    }

    @Test
    @DisplayName("Session: Should close and run")
    void sessionShouldCloseAndRun() throws Exception {
        // When
        sessionWrapper.close();
        sessionWrapper.run();

        // Then
        verify(javaxSession).close();
        verify(javaxSession).run();
    }

    @Test
    @DisplayName("Session: Should handle message listeners")
    void sessionShouldHandleMessageListeners() throws Exception {
        // Given
        when(javaxSession.getMessageListener()).thenReturn(javaxMessageListener);
        MessageListener jakartaListener = mock(MessageListener.class);

        // When
        MessageListener result = sessionWrapper.getMessageListener();
        sessionWrapper.setMessageListener(jakartaListener);
        sessionWrapper.setMessageListener(null);

        // Then
        assertThat(result).isNotNull();
        verify(javaxSession).getMessageListener();
        verify(javaxSession, times(2)).setMessageListener(any());
    }

    @Test
    @DisplayName("Session: Should create producer and consumer")
    void sessionShouldCreateProducerAndConsumer() throws Exception {
        // Given
        QueueWrapper queueWrapper = new QueueWrapper(javaxQueue);
        when(javaxSession.createProducer(any())).thenReturn(javaxProducer);
        when(javaxSession.createConsumer(any())).thenReturn(javaxConsumer);
        when(javaxSession.createConsumer(any(), anyString())).thenReturn(javaxConsumer);
        when(javaxSession.createConsumer(any(), anyString(), anyBoolean())).thenReturn(javaxConsumer);

        // When
        MessageProducer producer = sessionWrapper.createProducer(queueWrapper);
        MessageConsumer consumer1 = sessionWrapper.createConsumer(queueWrapper);
        MessageConsumer consumer2 = sessionWrapper.createConsumer(queueWrapper, "selector");
        MessageConsumer consumer3 = sessionWrapper.createConsumer(queueWrapper, "selector", true);

        // Then
        assertThat(producer).isNotNull();
        assertThat(consumer1).isNotNull();
        assertThat(consumer2).isNotNull();
        assertThat(consumer3).isNotNull();
    }

    @Test
    @DisplayName("Session: Should create queue and topic")
    void sessionShouldCreateQueueAndTopic() throws Exception {
        // Given
        when(javaxSession.createQueue("test-queue")).thenReturn(javaxQueue);
        when(javaxSession.createTopic("test-topic")).thenReturn(javaxTopic);

        // When
        Queue queue = sessionWrapper.createQueue("test-queue");
        Topic topic = sessionWrapper.createTopic("test-topic");

        // Then
        assertThat(queue).isNotNull();
        assertThat(topic).isNotNull();
        verify(javaxSession).createQueue("test-queue");
        verify(javaxSession).createTopic("test-topic");
    }

    @Test
    @DisplayName("Session: Should unsubscribe")
    void sessionShouldUnsubscribe() throws Exception {
        // When
        sessionWrapper.unsubscribe("subscription");

        // Then
        verify(javaxSession).unsubscribe("subscription");
    }

    @Test
    @DisplayName("Exception conversion: Should convert exceptions")
    void shouldConvertExceptions() {
        // Given
        javax.jms.JMSException javaxException = new javax.jms.JMSException("javax error");
        JMSException jakartaException = new JMSException("jakarta error");

        // When
        JMSException convertedToJakarta = JakartaJMSAdapter.convertException(javaxException);
        javax.jms.JMSException convertedToJavax = JakartaJMSAdapter.convertException(jakartaException);

        // Then
        assertThat(convertedToJakarta.getMessage()).isEqualTo("javax error");
        assertThat(convertedToJavax.getMessage()).isEqualTo("jakarta error");
    }

    @Test
    @DisplayName("Should handle null delegates gracefully")
    void shouldHandleNullDelegatesGracefully() {
        // When & Then
        assertThatCode(() -> {
            new JakartaJMSAdapter.ConnectionFactoryWrapper(null);
            new JakartaJMSAdapter.ConnectionWrapper(null);
            new JakartaJMSAdapter.SessionWrapper(null);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should implement Jakarta JMS interfaces")
    void shouldImplementJakartaJmsInterfaces() {
        // Then
        assertThat(connectionFactoryWrapper).isInstanceOf(ConnectionFactory.class);
        assertThat(connectionWrapper).isInstanceOf(Connection.class);
        assertThat(sessionWrapper).isInstanceOf(Session.class);
    }

    @Test
    @DisplayName("Should provide wrapper for all JMS types")
    void shouldProvideWrapperForAllJmsTypes() {
        // Then
        assertThat(JakartaJMSAdapter.ConnectionFactoryWrapper.class).isNotNull();
        assertThat(JakartaJMSAdapter.ConnectionWrapper.class).isNotNull();
        assertThat(JakartaJMSAdapter.SessionWrapper.class).isNotNull();
    }

    @Test
    @DisplayName("Session: Should create temporary queue and topic")
    void sessionShouldCreateTemporaryQueueAndTopic() throws Exception {
        // Given
        javax.jms.TemporaryQueue javaxTempQueue = mock(javax.jms.TemporaryQueue.class);
        javax.jms.TemporaryTopic javaxTempTopic = mock(javax.jms.TemporaryTopic.class);
        when(javaxSession.createTemporaryQueue()).thenReturn(javaxTempQueue);
        when(javaxSession.createTemporaryTopic()).thenReturn(javaxTempTopic);

        // When
        TemporaryQueue tempQueue = sessionWrapper.createTemporaryQueue();
        TemporaryTopic tempTopic = sessionWrapper.createTemporaryTopic();

        // Then
        assertThat(tempQueue).isNotNull();
        assertThat(tempTopic).isNotNull();
        verify(javaxSession).createTemporaryQueue();
        verify(javaxSession).createTemporaryTopic();
    }

    @Test
    @DisplayName("Session: Should create durable consumer")
    void sessionShouldCreateDurableConsumer() throws Exception {
        // Given
        TopicWrapper topicWrapper = new TopicWrapper(javaxTopic);
        when(javaxSession.createDurableConsumer(any(), anyString())).thenReturn(javaxConsumer);
        when(javaxSession.createDurableConsumer(any(), anyString(), anyString(), anyBoolean())).thenReturn(javaxConsumer);

        // When
        MessageConsumer durableConsumer1 = sessionWrapper.createDurableConsumer(topicWrapper, "subscription");
        MessageConsumer durableConsumer2 = sessionWrapper.createDurableConsumer(topicWrapper, "subscription", "selector", true);

        // Then
        assertThat(durableConsumer1).isNotNull();
        assertThat(durableConsumer2).isNotNull();
        verify(javaxSession).createDurableConsumer(javaxTopic, "subscription");
        verify(javaxSession).createDurableConsumer(javaxTopic, "subscription", "selector", true);
    }

    @Test
    @DisplayName("Session: Should create shared consumer")
    void sessionShouldCreateSharedConsumer() throws Exception {
        // Given
        TopicWrapper topicWrapper = new TopicWrapper(javaxTopic);
        when(javaxSession.createSharedConsumer(any(), anyString())).thenReturn(javaxConsumer);
        when(javaxSession.createSharedConsumer(any(), anyString(), anyString())).thenReturn(javaxConsumer);
        when(javaxSession.createSharedDurableConsumer(any(), anyString())).thenReturn(javaxConsumer);
        when(javaxSession.createSharedDurableConsumer(any(), anyString(), anyString())).thenReturn(javaxConsumer);

        // When
        MessageConsumer sharedConsumer1 = sessionWrapper.createSharedConsumer(topicWrapper, "subscription");
        MessageConsumer sharedConsumer2 = sessionWrapper.createSharedConsumer(topicWrapper, "subscription", "selector");
        MessageConsumer sharedDurableConsumer1 = sessionWrapper.createSharedDurableConsumer(topicWrapper, "subscription");
        MessageConsumer sharedDurableConsumer2 = sessionWrapper.createSharedDurableConsumer(topicWrapper, "subscription", "selector");

        // Then
        assertThat(sharedConsumer1).isNotNull();
        assertThat(sharedConsumer2).isNotNull();
        assertThat(sharedDurableConsumer1).isNotNull();
        assertThat(sharedDurableConsumer2).isNotNull();
    }

    @Test
    @DisplayName("Session: Should create queue browser")
    void sessionShouldCreateQueueBrowser() throws Exception {
        // Given
        QueueWrapper queueWrapper = new QueueWrapper(javaxQueue);
        javax.jms.QueueBrowser javaxBrowser = mock(javax.jms.QueueBrowser.class);
        when(javaxSession.createBrowser(any())).thenReturn(javaxBrowser);
        when(javaxSession.createBrowser(any(), anyString())).thenReturn(javaxBrowser);

        // When
        QueueBrowser browser1 = sessionWrapper.createBrowser(queueWrapper);
        QueueBrowser browser2 = sessionWrapper.createBrowser(queueWrapper, "selector");

        // Then
        assertThat(browser1).isNotNull();
        assertThat(browser2).isNotNull();
        verify(javaxSession).createBrowser(javaxQueue);
        verify(javaxSession).createBrowser(javaxQueue, "selector");
    }

    @Test
    @DisplayName("Connection: Should handle javax JMSException for all methods")
    void connectionShouldHandleJavaxJMSExceptionForAllMethods() throws Exception {
        // Given
        javax.jms.JMSException javaxException = new javax.jms.JMSException("Test error");
        when(javaxConnection.createSession()).thenThrow(javaxException);
        when(javaxConnection.getClientID()).thenThrow(javaxException);
        when(javaxConnection.getMetaData()).thenThrow(javaxException);

        // When & Then
        assertThatThrownBy(() -> connectionWrapper.createSession())
            .isInstanceOf(JMSException.class)
            .hasMessage("Test error");

        assertThatThrownBy(() -> connectionWrapper.getClientID())
            .isInstanceOf(JMSException.class)
            .hasMessage("Test error");

        assertThatThrownBy(() -> connectionWrapper.getMetaData())
            .isInstanceOf(JMSException.class)
            .hasMessage("Test error");
    }

    @Test
    @DisplayName("Session: Should handle javax JMSException for all methods")
    void sessionShouldHandleJavaxJMSExceptionForAllMethods() throws Exception {
        // Given
        javax.jms.JMSException javaxException = new javax.jms.JMSException("Session error");
        when(javaxSession.createTextMessage()).thenThrow(javaxException);
        when(javaxSession.getTransacted()).thenThrow(javaxException);
        when(javaxSession.createQueue("test")).thenThrow(javaxException);

        // When & Then
        assertThatThrownBy(() -> sessionWrapper.createTextMessage())
            .isInstanceOf(JMSException.class)
            .hasMessage("Session error");

        assertThatThrownBy(() -> sessionWrapper.getTransacted())
            .isInstanceOf(JMSException.class)
            .hasMessage("Session error");

        assertThatThrownBy(() -> sessionWrapper.createQueue("test"))
            .isInstanceOf(JMSException.class)
            .hasMessage("Session error");
    }

    @Test
    @DisplayName("ConnectionFactory: Should handle null return values")
    void connectionFactoryShouldHandleNullReturnValues() throws Exception {
        // Given
        when(javaxConnectionFactory.createConnection()).thenReturn(null);

        // When
        jakarta.jms.Connection result = connectionFactoryWrapper.createConnection();

        // Then
        assertThat(result).isInstanceOf(JakartaJMSAdapter.ConnectionWrapper.class);
        verify(javaxConnectionFactory).createConnection();
    }

    @Test
    @DisplayName("Session: Should handle null destinations")
    void sessionShouldHandleNullDestinations() throws Exception {
        // Given
        when(javaxSession.createProducer(null)).thenReturn(javaxProducer);
        when(javaxSession.createConsumer(null)).thenReturn(javaxConsumer);

        // When & Then - Creating with null destination should work at session level
        assertThatCode(() -> {
            MessageProducer producer = sessionWrapper.createProducer(null);
            MessageConsumer consumer = sessionWrapper.createConsumer(null);
            assertThat(producer).isNotNull();
            assertThat(consumer).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle edge cases in exception conversion")
    void shouldHandleEdgeCasesInExceptionConversion() {
        // Given
        javax.jms.JMSException javaxExceptionWithCause = new javax.jms.JMSException("error");
        javaxExceptionWithCause.initCause(new RuntimeException("root cause"));

        // When
        JMSException converted = JakartaJMSAdapter.convertException(javaxExceptionWithCause);

        // Then
        assertThat(converted).isNotNull();
        assertThat(converted.getMessage()).isEqualTo("error");
        if (converted.getCause() != null) {
            assertThat(converted.getCause()).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    @DisplayName("Session: Should create durable subscriber")
    void sessionShouldCreateDurableSubscriber() throws Exception {
        // Given
        TopicWrapper topicWrapper = new TopicWrapper(javaxTopic);
        javax.jms.TopicSubscriber javaxSubscriber = mock(javax.jms.TopicSubscriber.class);
        when(javaxSession.createDurableSubscriber(any(), anyString())).thenReturn(javaxSubscriber);
        when(javaxSession.createDurableSubscriber(any(), anyString(), anyString(), anyBoolean())).thenReturn(javaxSubscriber);

        // When
        TopicSubscriber subscriber1 = sessionWrapper.createDurableSubscriber(topicWrapper, "subscription");
        TopicSubscriber subscriber2 = sessionWrapper.createDurableSubscriber(topicWrapper, "subscription", "selector", true);

        // Then
        assertThat(subscriber1).isNotNull();
        assertThat(subscriber2).isNotNull();
        verify(javaxSession).createDurableSubscriber(javaxTopic, "subscription");
        verify(javaxSession).createDurableSubscriber(javaxTopic, "subscription", "selector", true);
    }

    @Test
    @DisplayName("Session: Should handle more JMSException scenarios")
    void sessionShouldHandleMoreJMSExceptionScenarios() throws Exception {
        // Given
        javax.jms.JMSException javaxException = new javax.jms.JMSException("Error occurred");
        when(javaxSession.createBytesMessage()).thenThrow(javaxException);
        when(javaxSession.createMapMessage()).thenThrow(javaxException);
        when(javaxSession.createMessage()).thenThrow(javaxException);
        when(javaxSession.createObjectMessage()).thenThrow(javaxException);
        when(javaxSession.createStreamMessage()).thenThrow(javaxException);
        doThrow(javaxException).when(javaxSession).commit();
        doThrow(javaxException).when(javaxSession).rollback();
        doThrow(javaxException).when(javaxSession).recover();
        doThrow(javaxException).when(javaxSession).close();

        // When & Then - Test all message creation methods throw JakartaJMSException
        assertThatThrownBy(() -> sessionWrapper.createBytesMessage())
            .isInstanceOf(JMSException.class)
            .hasMessage("Error occurred");

        assertThatThrownBy(() -> sessionWrapper.createMapMessage())
            .isInstanceOf(JMSException.class)
            .hasMessage("Error occurred");

        assertThatThrownBy(() -> sessionWrapper.createMessage())
            .isInstanceOf(JMSException.class)
            .hasMessage("Error occurred");

        assertThatThrownBy(() -> sessionWrapper.createObjectMessage())
            .isInstanceOf(JMSException.class)
            .hasMessage("Error occurred");

        assertThatThrownBy(() -> sessionWrapper.createStreamMessage())
            .isInstanceOf(JMSException.class)
            .hasMessage("Error occurred");

        // Test transaction methods
        assertThatThrownBy(() -> sessionWrapper.commit())
            .isInstanceOf(JMSException.class)
            .hasMessage("Error occurred");

        assertThatThrownBy(() -> sessionWrapper.rollback())
            .isInstanceOf(JMSException.class)
            .hasMessage("Error occurred");

        assertThatThrownBy(() -> sessionWrapper.recover())
            .isInstanceOf(JMSException.class)
            .hasMessage("Error occurred");

        assertThatThrownBy(() -> sessionWrapper.close())
            .isInstanceOf(JMSException.class)
            .hasMessage("Error occurred");
    }

    @Test
    @DisplayName("Session: Should handle JMSException for consumer/producer creation")
    void sessionShouldHandleJMSExceptionForConsumerProducerCreation() throws Exception {
        // Given
        QueueWrapper queueWrapper = new QueueWrapper(javaxQueue);
        TopicWrapper topicWrapper = new TopicWrapper(javaxTopic);
        javax.jms.JMSException javaxException = new javax.jms.JMSException("Producer/Consumer error");

        when(javaxSession.createProducer(any())).thenThrow(javaxException);
        when(javaxSession.createConsumer(any())).thenThrow(javaxException);
        when(javaxSession.createConsumer(any(), anyString())).thenThrow(javaxException);
        when(javaxSession.createConsumer(any(), anyString(), anyBoolean())).thenThrow(javaxException);
        when(javaxSession.createDurableConsumer(any(), anyString())).thenThrow(javaxException);
        when(javaxSession.createSharedConsumer(any(), anyString())).thenThrow(javaxException);
        when(javaxSession.createSharedDurableConsumer(any(), anyString())).thenThrow(javaxException);
        when(javaxSession.createDurableSubscriber(any(), anyString())).thenThrow(javaxException);

        // When & Then
        assertThatThrownBy(() -> sessionWrapper.createProducer(queueWrapper))
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer/Consumer error");

        assertThatThrownBy(() -> sessionWrapper.createConsumer(queueWrapper))
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer/Consumer error");

        assertThatThrownBy(() -> sessionWrapper.createConsumer(queueWrapper, "selector"))
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer/Consumer error");

        assertThatThrownBy(() -> sessionWrapper.createConsumer(queueWrapper, "selector", true))
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer/Consumer error");

        assertThatThrownBy(() -> sessionWrapper.createDurableConsumer(topicWrapper, "subscription"))
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer/Consumer error");

        assertThatThrownBy(() -> sessionWrapper.createSharedConsumer(topicWrapper, "subscription"))
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer/Consumer error");

        assertThatThrownBy(() -> sessionWrapper.createSharedDurableConsumer(topicWrapper, "subscription"))
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer/Consumer error");

        assertThatThrownBy(() -> sessionWrapper.createDurableSubscriber(topicWrapper, "subscription"))
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer/Consumer error");
    }

    @Test
    @DisplayName("Session: Should handle JMSException for destination and browser creation")
    void sessionShouldHandleJMSExceptionForDestinationAndBrowserCreation() throws Exception {
        // Given
        QueueWrapper queueWrapper = new QueueWrapper(javaxQueue);
        javax.jms.JMSException javaxException = new javax.jms.JMSException("Destination/Browser error");

        when(javaxSession.createTemporaryQueue()).thenThrow(javaxException);
        when(javaxSession.createTemporaryTopic()).thenThrow(javaxException);
        when(javaxSession.createBrowser(any())).thenThrow(javaxException);
        when(javaxSession.createBrowser(any(), anyString())).thenThrow(javaxException);
        doThrow(javaxException).when(javaxSession).unsubscribe(anyString());
        when(javaxSession.getMessageListener()).thenThrow(javaxException);
        doThrow(javaxException).when(javaxSession).setMessageListener(any());

        // When & Then
        assertThatThrownBy(() -> sessionWrapper.createTemporaryQueue())
            .isInstanceOf(JMSException.class)
            .hasMessage("Destination/Browser error");

        assertThatThrownBy(() -> sessionWrapper.createTemporaryTopic())
            .isInstanceOf(JMSException.class)
            .hasMessage("Destination/Browser error");

        assertThatThrownBy(() -> sessionWrapper.createBrowser(queueWrapper))
            .isInstanceOf(JMSException.class)
            .hasMessage("Destination/Browser error");

        assertThatThrownBy(() -> sessionWrapper.createBrowser(queueWrapper, "selector"))
            .isInstanceOf(JMSException.class)
            .hasMessage("Destination/Browser error");

        assertThatThrownBy(() -> sessionWrapper.unsubscribe("subscription"))
            .isInstanceOf(JMSException.class)
            .hasMessage("Destination/Browser error");

        assertThatThrownBy(() -> sessionWrapper.getMessageListener())
            .isInstanceOf(JMSException.class)
            .hasMessage("Destination/Browser error");

        MessageListener jakartaListener = mock(MessageListener.class);
        assertThatThrownBy(() -> sessionWrapper.setMessageListener(jakartaListener))
            .isInstanceOf(JMSException.class)
            .hasMessage("Destination/Browser error");
    }

    @Test
    @DisplayName("Session: Should handle ObjectMessage with Serializable parameter exception")
    void sessionShouldHandleObjectMessageWithSerializableParameterException() throws Exception {
        // Given
        String testObject = "Test Serializable Object";
        javax.jms.JMSException javaxException = new javax.jms.JMSException("ObjectMessage serializable error");
        when(javaxSession.createObjectMessage(testObject)).thenThrow(javaxException);

        // When & Then
        assertThatThrownBy(() -> sessionWrapper.createObjectMessage(testObject))
            .isInstanceOf(JMSException.class)
            .hasMessage("ObjectMessage serializable error");
    }

    @Test
    @DisplayName("Session: Should handle TextMessage with text parameter exception")
    void sessionShouldHandleTextMessageWithTextParameterException() throws Exception {
        // Given
        String testText = "Test message text";
        javax.jms.JMSException javaxException = new javax.jms.JMSException("TextMessage text error");
        when(javaxSession.createTextMessage(testText)).thenThrow(javaxException);

        // When & Then
        assertThatThrownBy(() -> sessionWrapper.createTextMessage(testText))
            .isInstanceOf(JMSException.class)
            .hasMessage("TextMessage text error");
    }

    @Test
    @DisplayName("Session: Should handle run method - no exception expected")
    void sessionShouldHandleRunMethodNoException() {
        // When & Then - run() method doesn't throw checked exceptions
        assertThatCode(() -> sessionWrapper.run()).doesNotThrowAnyException();
        verify(javaxSession).run();
    }
}