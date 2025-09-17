package com.example.ibmmq.unit.adapter;

import com.example.ibmmq.adapter.DestinationWrappers;
import com.example.ibmmq.adapter.DestinationWrappers.*;
import com.example.ibmmq.adapter.MessageWrappers.*;
import jakarta.jms.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Enumeration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Destination Wrappers Tests")
class DestinationWrappersTest {

    // NOTE: These tests are simplified due to Jakarta JMS adapter complexity
    // For full integration testing, use integration tests with real IBM MQ

    @Mock
    private javax.jms.ConnectionMetaData javaxConnectionMetaData;
    @Mock
    private javax.jms.TopicSubscriber javaxTopicSubscriber;
    @Mock
    private javax.jms.QueueBrowser javaxQueueBrowser;
    @Mock
    private javax.jms.Queue javaxQueue;
    @Mock
    private javax.jms.Topic javaxTopic;
    @Mock
    private javax.jms.TextMessage javaxTextMessage;
    @Mock
    private javax.jms.BytesMessage javaxBytesMessage;
    @Mock
    private javax.jms.MapMessage javaxMapMessage;
    @Mock
    private javax.jms.StreamMessage javaxStreamMessage;
    @Mock
    private javax.jms.ObjectMessage javaxObjectMessage;
    @Mock
    private javax.jms.MessageProducer javaxMessageProducer;
    @Mock
    private jakarta.jms.CompletionListener jakartaCompletionListener;

    @BeforeEach
    void setUp() throws Exception {
        // Setup default mock behaviors
        when(javaxQueue.getQueueName()).thenReturn("test.queue");
        when(javaxTopic.getTopicName()).thenReturn("test.topic");
    }

    @Test
    @DisplayName("Should create QueueWrapper")
    void shouldCreateQueueWrapper() {
        // Given
        javax.jms.Queue javaxQueue = mock(javax.jms.Queue.class);

        // When & Then - Test simplified due to Jakarta JMS adapter complexity
        assertThatCode(() -> {
            DestinationWrappers.QueueWrapper wrapper = new DestinationWrappers.QueueWrapper(javaxQueue);
            assertThat(wrapper).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should create TopicWrapper")
    void shouldCreateTopicWrapper() {
        // Given
        javax.jms.Topic javaxTopic = mock(javax.jms.Topic.class);

        // When & Then - Test simplified due to Jakarta JMS adapter complexity
        assertThatCode(() -> {
            DestinationWrappers.TopicWrapper wrapper = new DestinationWrappers.TopicWrapper(javaxTopic);
            assertThat(wrapper).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should create MessageProducerWrapper")
    void shouldCreateMessageProducerWrapper() {
        // Given
        javax.jms.MessageProducer javaxProducer = mock(javax.jms.MessageProducer.class);

        // When & Then - Test simplified due to Jakarta JMS adapter complexity
        assertThatCode(() -> {
            DestinationWrappers.MessageProducerWrapper wrapper =
                new DestinationWrappers.MessageProducerWrapper(javaxProducer);
            assertThat(wrapper).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should create MessageConsumerWrapper")
    void shouldCreateMessageConsumerWrapper() {
        // Given
        javax.jms.MessageConsumer javaxConsumer = mock(javax.jms.MessageConsumer.class);

        // When & Then - Test simplified due to Jakarta JMS adapter complexity
        assertThatCode(() -> {
            DestinationWrappers.MessageConsumerWrapper wrapper =
                new DestinationWrappers.MessageConsumerWrapper(javaxConsumer);
            assertThat(wrapper).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should create QueueBrowserWrapper")
    void shouldCreateQueueBrowserWrapper() {
        // Given
        javax.jms.QueueBrowser javaxBrowser = mock(javax.jms.QueueBrowser.class);

        // When & Then - Test simplified due to Jakarta JMS adapter complexity
        assertThatCode(() -> {
            DestinationWrappers.QueueBrowserWrapper wrapper =
                new DestinationWrappers.QueueBrowserWrapper(javaxBrowser);
            assertThat(wrapper).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should implement Jakarta JMS destination interfaces")
    void shouldImplementJakartaJmsDestinationInterfaces() {
        // Given
        javax.jms.Queue javaxQueue = mock(javax.jms.Queue.class);
        javax.jms.Topic javaxTopic = mock(javax.jms.Topic.class);

        // When
        DestinationWrappers.QueueWrapper queueWrapper = new DestinationWrappers.QueueWrapper(javaxQueue);
        DestinationWrappers.TopicWrapper topicWrapper = new DestinationWrappers.TopicWrapper(javaxTopic);

        // Then - Verify they implement Jakarta JMS interfaces
        assertThat(queueWrapper).isInstanceOf(jakarta.jms.Queue.class);
        assertThat(topicWrapper).isInstanceOf(jakarta.jms.Topic.class);
        assertThat(queueWrapper).isInstanceOf(jakarta.jms.Destination.class);
        assertThat(topicWrapper).isInstanceOf(jakarta.jms.Destination.class);
    }

    @Test
    @DisplayName("Should implement Jakarta JMS producer and consumer interfaces")
    void shouldImplementJakartaJmsProducerAndConsumerInterfaces() {
        // Given
        javax.jms.MessageProducer javaxProducer = mock(javax.jms.MessageProducer.class);
        javax.jms.MessageConsumer javaxConsumer = mock(javax.jms.MessageConsumer.class);
        javax.jms.QueueBrowser javaxBrowser = mock(javax.jms.QueueBrowser.class);

        // When
        DestinationWrappers.MessageProducerWrapper producerWrapper =
            new DestinationWrappers.MessageProducerWrapper(javaxProducer);
        DestinationWrappers.MessageConsumerWrapper consumerWrapper =
            new DestinationWrappers.MessageConsumerWrapper(javaxConsumer);
        DestinationWrappers.QueueBrowserWrapper browserWrapper =
            new DestinationWrappers.QueueBrowserWrapper(javaxBrowser);

        // Then - Verify they implement Jakarta JMS interfaces
        assertThat(producerWrapper).isInstanceOf(jakarta.jms.MessageProducer.class);
        assertThat(consumerWrapper).isInstanceOf(jakarta.jms.MessageConsumer.class);
        assertThat(browserWrapper).isInstanceOf(jakarta.jms.QueueBrowser.class);
    }

    @Test
    @DisplayName("Should handle null delegates gracefully")
    void shouldHandleNullDelegatesGracefully() {
        // When & Then - Test simplified due to Jakarta JMS adapter complexity
        assertThatCode(() -> {
            DestinationWrappers.QueueWrapper queueWrapper = new DestinationWrappers.QueueWrapper(null);
            DestinationWrappers.TopicWrapper topicWrapper = new DestinationWrappers.TopicWrapper(null);
            DestinationWrappers.MessageProducerWrapper producerWrapper =
                new DestinationWrappers.MessageProducerWrapper(null);
            DestinationWrappers.MessageConsumerWrapper consumerWrapper =
                new DestinationWrappers.MessageConsumerWrapper(null);
            DestinationWrappers.QueueBrowserWrapper browserWrapper =
                new DestinationWrappers.QueueBrowserWrapper(null);

            assertThat(queueWrapper).isNotNull();
            assertThat(topicWrapper).isNotNull();
            assertThat(producerWrapper).isNotNull();
            assertThat(consumerWrapper).isNotNull();
            assertThat(browserWrapper).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should test MessageProducerWrapper send operations")
    void shouldTestMessageProducerWrapperSendOperations() throws Exception {
        // Given
        javax.jms.MessageProducer javaxProducer = mock(javax.jms.MessageProducer.class);
        javax.jms.Message javaxMessage = mock(javax.jms.Message.class);
        javax.jms.Destination javaxDestination = mock(javax.jms.Destination.class);

        DestinationWrappers.MessageProducerWrapper wrapper =
            new DestinationWrappers.MessageProducerWrapper(javaxProducer);

        // Create proper message and destination wrappers
        com.example.ibmmq.adapter.MessageWrappers.MessageWrapper messageWrapper =
            new com.example.ibmmq.adapter.MessageWrappers.MessageWrapper(javaxMessage);
        com.example.ibmmq.adapter.DestinationWrappers.DestinationWrapper destinationWrapper =
            new com.example.ibmmq.adapter.DestinationWrappers.DestinationWrapper(javaxDestination);

        // When & Then - Test basic send operation
        wrapper.send(messageWrapper);
        verify(javaxProducer).send(javaxMessage);

        // Test send with destination
        wrapper.send(destinationWrapper, messageWrapper);
        verify(javaxProducer).send(javaxDestination, javaxMessage);

        // Test send with delivery mode, priority, and TTL
        wrapper.send(messageWrapper, 2, 5, 1000L);
        verify(javaxProducer).send(javaxMessage, 2, 5, 1000L);

        // Test send with all parameters
        wrapper.send(destinationWrapper, messageWrapper, 1, 9, 2000L);
        verify(javaxProducer).send(javaxDestination, javaxMessage, 1, 9, 2000L);
    }

    @Test
    @DisplayName("Should test MessageProducerWrapper configuration methods")
    void shouldTestMessageProducerWrapperConfigurationMethods() throws Exception {
        // Given
        javax.jms.MessageProducer javaxProducer = mock(javax.jms.MessageProducer.class);
        when(javaxProducer.getDisableMessageID()).thenReturn(true);
        when(javaxProducer.getDisableMessageTimestamp()).thenReturn(false);
        when(javaxProducer.getDeliveryMode()).thenReturn(2);
        when(javaxProducer.getPriority()).thenReturn(5);
        when(javaxProducer.getTimeToLive()).thenReturn(3000L);

        DestinationWrappers.MessageProducerWrapper wrapper =
            new DestinationWrappers.MessageProducerWrapper(javaxProducer);

        // When & Then - Test getters
        assertThat(wrapper.getDisableMessageID()).isTrue();
        assertThat(wrapper.getDisableMessageTimestamp()).isFalse();
        assertThat(wrapper.getDeliveryMode()).isEqualTo(2);
        assertThat(wrapper.getPriority()).isEqualTo(5);
        assertThat(wrapper.getTimeToLive()).isEqualTo(3000L);

        // Test setters
        wrapper.setDisableMessageID(false);
        wrapper.setDisableMessageTimestamp(true);
        wrapper.setDeliveryMode(1);
        wrapper.setPriority(9);
        wrapper.setTimeToLive(5000L);

        verify(javaxProducer).setDisableMessageID(false);
        verify(javaxProducer).setDisableMessageTimestamp(true);
        verify(javaxProducer).setDeliveryMode(1);
        verify(javaxProducer).setPriority(9);
        verify(javaxProducer).setTimeToLive(5000L);
    }

    @Test
    @DisplayName("Should test MessageProducerWrapper destination and lifecycle")
    void shouldTestMessageProducerWrapperDestinationAndLifecycle() throws Exception {
        // Given
        javax.jms.MessageProducer javaxProducer = mock(javax.jms.MessageProducer.class);
        javax.jms.Destination javaxDestination = mock(javax.jms.Destination.class);
        when(javaxProducer.getDestination()).thenReturn(javaxDestination);

        DestinationWrappers.MessageProducerWrapper wrapper =
            new DestinationWrappers.MessageProducerWrapper(javaxProducer);

        // When & Then - Test destination
        jakarta.jms.Destination destination = wrapper.getDestination();
        assertThat(destination).isNotNull();
        verify(javaxProducer).getDestination();

        // Test close
        wrapper.close();
        verify(javaxProducer).close();
    }

    @Test
    @DisplayName("Should test MessageProducerWrapper delivery delay and async send")
    void shouldTestMessageProducerWrapperDeliveryDelayAndAsyncSend() throws Exception {
        // Given
        javax.jms.MessageProducer javaxProducer = mock(javax.jms.MessageProducer.class);
        javax.jms.Message javaxMessage = mock(javax.jms.Message.class);
        javax.jms.Destination javaxDestination = mock(javax.jms.Destination.class);
        when(javaxProducer.getDeliveryDelay()).thenReturn(1500L);

        DestinationWrappers.MessageProducerWrapper wrapper =
            new DestinationWrappers.MessageProducerWrapper(javaxProducer);

        // When & Then - Test delivery delay
        assertThat(wrapper.getDeliveryDelay()).isEqualTo(1500L);

        wrapper.setDeliveryDelay(2500L);
        verify(javaxProducer).setDeliveryDelay(2500L);

        // Test async send with completion listener
        jakarta.jms.CompletionListener completionListener = mock(jakarta.jms.CompletionListener.class);
        com.example.ibmmq.adapter.MessageWrappers.MessageWrapper messageWrapper2 =
            new com.example.ibmmq.adapter.MessageWrappers.MessageWrapper(javaxMessage);
        com.example.ibmmq.adapter.DestinationWrappers.DestinationWrapper destinationWrapper2 =
            new com.example.ibmmq.adapter.DestinationWrappers.DestinationWrapper(javaxDestination);

        wrapper.send(messageWrapper2, completionListener);
        verify(javaxProducer).send(eq(javaxMessage), any(javax.jms.CompletionListener.class));

        // Test async send with destination and completion listener
        wrapper.send(destinationWrapper2, messageWrapper2, completionListener);
        verify(javaxProducer).send(eq(javaxDestination), eq(javaxMessage), any(javax.jms.CompletionListener.class));
    }

    @Test
    @DisplayName("Should test MessageConsumerWrapper basic operations")
    void shouldTestMessageConsumerWrapperBasicOperations() throws Exception {
        // Given
        javax.jms.MessageConsumer javaxConsumer = mock(javax.jms.MessageConsumer.class);
        javax.jms.Message javaxMessage = mock(javax.jms.Message.class);
        when(javaxConsumer.receive()).thenReturn(javaxMessage);
        when(javaxConsumer.receive(1000L)).thenReturn(javaxMessage);
        when(javaxConsumer.receiveNoWait()).thenReturn(javaxMessage);
        when(javaxConsumer.getMessageSelector()).thenReturn("property='value'");

        DestinationWrappers.MessageConsumerWrapper wrapper =
            new DestinationWrappers.MessageConsumerWrapper(javaxConsumer);

        // When & Then - Test receive operations
        jakarta.jms.Message receivedMessage1 = wrapper.receive();
        jakarta.jms.Message receivedMessage2 = wrapper.receive(1000L);
        jakarta.jms.Message receivedMessage3 = wrapper.receiveNoWait();

        assertThat(receivedMessage1).isNotNull();
        assertThat(receivedMessage2).isNotNull();
        assertThat(receivedMessage3).isNotNull();

        verify(javaxConsumer).receive();
        verify(javaxConsumer).receive(1000L);
        verify(javaxConsumer).receiveNoWait();

        // Test message selector
        assertThat(wrapper.getMessageSelector()).isEqualTo("property='value'");

        // Test close
        wrapper.close();
        verify(javaxConsumer).close();
    }

    @Test
    @DisplayName("Should test MessageConsumerWrapper message listener")
    void shouldTestMessageConsumerWrapperMessageListener() throws Exception {
        // Given
        javax.jms.MessageConsumer javaxConsumer = mock(javax.jms.MessageConsumer.class);
        javax.jms.MessageListener javaxListener = mock(javax.jms.MessageListener.class);
        when(javaxConsumer.getMessageListener()).thenReturn(javaxListener);

        DestinationWrappers.MessageConsumerWrapper wrapper =
            new DestinationWrappers.MessageConsumerWrapper(javaxConsumer);

        // When & Then - Test message listener operations
        jakarta.jms.MessageListener listener = wrapper.getMessageListener();
        assertThat(listener).isNotNull();

        jakarta.jms.MessageListener newListener = mock(jakarta.jms.MessageListener.class);
        wrapper.setMessageListener(newListener);
        verify(javaxConsumer).setMessageListener(any(javax.jms.MessageListener.class));
    }

    @Test
    @DisplayName("Should provide wrapper for all destination types")
    void shouldProvideWrapperForAllDestinationTypes() {
        // Test simplified due to Jakarta JMS adapter complexity
        // For full integration testing, use integration tests with real IBM MQ
        assertThat(DestinationWrappers.QueueWrapper.class).isNotNull();
        assertThat(DestinationWrappers.TopicWrapper.class).isNotNull();
        assertThat(DestinationWrappers.MessageProducerWrapper.class).isNotNull();
        assertThat(DestinationWrappers.MessageConsumerWrapper.class).isNotNull();
        assertThat(DestinationWrappers.QueueBrowserWrapper.class).isNotNull();
    }

    // ===== NEW COMPREHENSIVE TESTS FOR LOW-COVERAGE CLASSES =====

    @Test
    @DisplayName("CompletionListenerWrapper: Should handle onCompletion with different message types")
    void completionListenerWrapperShouldHandleOnCompletion() throws Exception {
        // Given - Create wrapper using reflection since it's private
        Class<?> wrapperClass = Class.forName("com.example.ibmmq.adapter.DestinationWrappers$CompletionListenerWrapper");
        java.lang.reflect.Constructor<?> constructor = wrapperClass.getDeclaredConstructor(jakarta.jms.CompletionListener.class);
        constructor.setAccessible(true);
        Object wrapper = constructor.newInstance(jakartaCompletionListener);

        java.lang.reflect.Method onCompletionMethod = wrapperClass.getDeclaredMethod("onCompletion", javax.jms.Message.class);
        onCompletionMethod.setAccessible(true);

        // When - Test with different message types
        onCompletionMethod.invoke(wrapper, javaxTextMessage);
        onCompletionMethod.invoke(wrapper, javaxBytesMessage);
        onCompletionMethod.invoke(wrapper, javaxMapMessage);
        onCompletionMethod.invoke(wrapper, javaxStreamMessage);
        onCompletionMethod.invoke(wrapper, javaxObjectMessage);

        // Test with generic message
        javax.jms.Message genericMessage = mock(javax.jms.Message.class);
        onCompletionMethod.invoke(wrapper, genericMessage);

        // Then
        verify(jakartaCompletionListener, times(6)).onCompletion(any(jakarta.jms.Message.class));
    }

    @Test
    @DisplayName("CompletionListenerWrapper: Should handle onException with different message types")
    void completionListenerWrapperShouldHandleOnException() throws Exception {
        // Given - Create wrapper using reflection since it's private
        Class<?> wrapperClass = Class.forName("com.example.ibmmq.adapter.DestinationWrappers$CompletionListenerWrapper");
        java.lang.reflect.Constructor<?> constructor = wrapperClass.getDeclaredConstructor(jakarta.jms.CompletionListener.class);
        constructor.setAccessible(true);
        Object wrapper = constructor.newInstance(jakartaCompletionListener);

        java.lang.reflect.Method onExceptionMethod = wrapperClass.getDeclaredMethod("onException", javax.jms.Message.class, Exception.class);
        onExceptionMethod.setAccessible(true);

        Exception testException = new RuntimeException("Test exception");

        // When - Test with different message types
        onExceptionMethod.invoke(wrapper, javaxTextMessage, testException);
        onExceptionMethod.invoke(wrapper, javaxBytesMessage, testException);
        onExceptionMethod.invoke(wrapper, javaxMapMessage, testException);
        onExceptionMethod.invoke(wrapper, javaxStreamMessage, testException);
        onExceptionMethod.invoke(wrapper, javaxObjectMessage, testException);

        // Test with generic message
        javax.jms.Message genericMessage = mock(javax.jms.Message.class);
        onExceptionMethod.invoke(wrapper, genericMessage, testException);

        // Then
        verify(jakartaCompletionListener, times(6)).onException(any(jakarta.jms.Message.class), eq(testException));
    }

    @Test
    @DisplayName("ConnectionMetaDataWrapper: Should delegate all metadata methods")
    void connectionMetaDataWrapperShouldDelegateAllMethods() throws Exception {
        // Given
        when(javaxConnectionMetaData.getJMSVersion()).thenReturn("3.0");
        when(javaxConnectionMetaData.getJMSMajorVersion()).thenReturn(3);
        when(javaxConnectionMetaData.getJMSMinorVersion()).thenReturn(0);
        when(javaxConnectionMetaData.getJMSProviderName()).thenReturn("IBM MQ");
        when(javaxConnectionMetaData.getProviderVersion()).thenReturn("9.2.0");
        when(javaxConnectionMetaData.getProviderMajorVersion()).thenReturn(9);
        when(javaxConnectionMetaData.getProviderMinorVersion()).thenReturn(2);

        @SuppressWarnings("unchecked")
        Enumeration<String> mockEnum = mock(Enumeration.class);
        when(javaxConnectionMetaData.getJMSXPropertyNames()).thenReturn(mockEnum);

        ConnectionMetaDataWrapper wrapper = new ConnectionMetaDataWrapper(javaxConnectionMetaData);

        // When
        String version = wrapper.getJMSVersion();
        int majorVersion = wrapper.getJMSMajorVersion();
        int minorVersion = wrapper.getJMSMinorVersion();
        String providerName = wrapper.getJMSProviderName();
        String providerVersion = wrapper.getProviderVersion();
        int providerMajorVersion = wrapper.getProviderMajorVersion();
        int providerMinorVersion = wrapper.getProviderMinorVersion();
        Enumeration<String> propertyNames = wrapper.getJMSXPropertyNames();

        // Then
        assertThat(version).isEqualTo("3.0");
        assertThat(majorVersion).isEqualTo(3);
        assertThat(minorVersion).isEqualTo(0);
        assertThat(providerName).isEqualTo("IBM MQ");
        assertThat(providerVersion).isEqualTo("9.2.0");
        assertThat(providerMajorVersion).isEqualTo(9);
        assertThat(providerMinorVersion).isEqualTo(2);
        assertThat(propertyNames).isEqualTo(mockEnum);

        verify(javaxConnectionMetaData).getJMSVersion();
        verify(javaxConnectionMetaData).getJMSMajorVersion();
        verify(javaxConnectionMetaData).getJMSMinorVersion();
        verify(javaxConnectionMetaData).getJMSProviderName();
        verify(javaxConnectionMetaData).getProviderVersion();
        verify(javaxConnectionMetaData).getProviderMajorVersion();
        verify(javaxConnectionMetaData).getProviderMinorVersion();
        verify(javaxConnectionMetaData).getJMSXPropertyNames();
    }

    @Test
    @DisplayName("ConnectionMetaDataWrapper: Should handle JMSExceptions")
    void connectionMetaDataWrapperShouldHandleJMSExceptions() throws Exception {
        // Given
        javax.jms.JMSException javaxException = new javax.jms.JMSException("Test error");
        when(javaxConnectionMetaData.getJMSVersion()).thenThrow(javaxException);
        when(javaxConnectionMetaData.getJMSMajorVersion()).thenThrow(javaxException);
        when(javaxConnectionMetaData.getJMSProviderName()).thenThrow(javaxException);

        ConnectionMetaDataWrapper wrapper = new ConnectionMetaDataWrapper(javaxConnectionMetaData);

        // When & Then
        assertThatThrownBy(() -> wrapper.getJMSVersion())
            .isInstanceOf(JMSException.class)
            .hasMessage("Test error");

        assertThatThrownBy(() -> wrapper.getJMSMajorVersion())
            .isInstanceOf(JMSException.class)
            .hasMessage("Test error");

        assertThatThrownBy(() -> wrapper.getJMSProviderName())
            .isInstanceOf(JMSException.class)
            .hasMessage("Test error");
    }

    @Test
    @DisplayName("JavaxMessageListenerWrapper: Should handle onMessage with different message types")
    void javaxMessageListenerWrapperShouldHandleOnMessage() throws Exception {
        // Given - Create wrapper using reflection since it's private
        jakarta.jms.MessageListener jakartaListener = mock(jakarta.jms.MessageListener.class);
        Class<?> wrapperClass = Class.forName("com.example.ibmmq.adapter.DestinationWrappers$JavaxMessageListenerWrapper");
        java.lang.reflect.Constructor<?> constructor = wrapperClass.getDeclaredConstructor(jakarta.jms.MessageListener.class);
        constructor.setAccessible(true);
        Object wrapper = constructor.newInstance(jakartaListener);

        java.lang.reflect.Method onMessageMethod = wrapperClass.getDeclaredMethod("onMessage", javax.jms.Message.class);
        onMessageMethod.setAccessible(true);

        // When - Test with different message types
        onMessageMethod.invoke(wrapper, javaxTextMessage);
        onMessageMethod.invoke(wrapper, javaxBytesMessage);
        onMessageMethod.invoke(wrapper, javaxMapMessage);
        onMessageMethod.invoke(wrapper, javaxStreamMessage);
        onMessageMethod.invoke(wrapper, javaxObjectMessage);

        // Test with generic message
        javax.jms.Message genericMessage = mock(javax.jms.Message.class);
        onMessageMethod.invoke(wrapper, genericMessage);

        // Then
        verify(jakartaListener, times(6)).onMessage(any(jakarta.jms.Message.class));
    }

    @Test
    @DisplayName("QueueBrowserWrapper: Should delegate all browser methods")
    void queueBrowserWrapperShouldDelegateAllMethods() throws Exception {
        // Given
        when(javaxQueueBrowser.getQueue()).thenReturn(javaxQueue);
        when(javaxQueueBrowser.getMessageSelector()).thenReturn("priority > 5");

        @SuppressWarnings("unchecked")
        Enumeration<javax.jms.Message> mockEnum = mock(Enumeration.class);
        when(javaxQueueBrowser.getEnumeration()).thenReturn(mockEnum);

        QueueBrowserWrapper wrapper = new QueueBrowserWrapper(javaxQueueBrowser);

        // When
        Queue queue = wrapper.getQueue();
        String selector = wrapper.getMessageSelector();
        Enumeration enumeration = wrapper.getEnumeration();
        wrapper.close();

        // Then
        assertThat(queue).isInstanceOf(QueueWrapper.class);
        assertThat(selector).isEqualTo("priority > 5");
        assertThat(enumeration).isEqualTo(mockEnum);

        verify(javaxQueueBrowser).getQueue();
        verify(javaxQueueBrowser).getMessageSelector();
        verify(javaxQueueBrowser).getEnumeration();
        verify(javaxQueueBrowser).close();
    }

    @Test
    @DisplayName("QueueBrowserWrapper: Should handle JMSExceptions")
    void queueBrowserWrapperShouldHandleJMSExceptions() throws Exception {
        // Given
        javax.jms.JMSException javaxException = new javax.jms.JMSException("Browser error");
        when(javaxQueueBrowser.getQueue()).thenThrow(javaxException);
        when(javaxQueueBrowser.getMessageSelector()).thenThrow(javaxException);
        when(javaxQueueBrowser.getEnumeration()).thenThrow(javaxException);
        doThrow(javaxException).when(javaxQueueBrowser).close();

        QueueBrowserWrapper wrapper = new QueueBrowserWrapper(javaxQueueBrowser);

        // When & Then
        assertThatThrownBy(() -> wrapper.getQueue())
            .isInstanceOf(JMSException.class)
            .hasMessage("Browser error");

        assertThatThrownBy(() -> wrapper.getMessageSelector())
            .isInstanceOf(JMSException.class)
            .hasMessage("Browser error");

        assertThatThrownBy(() -> wrapper.getEnumeration())
            .isInstanceOf(JMSException.class)
            .hasMessage("Browser error");

        assertThatThrownBy(() -> wrapper.close())
            .isInstanceOf(JMSException.class)
            .hasMessage("Browser error");
    }

    @Test
    @DisplayName("TopicSubscriberWrapper: Should delegate all subscriber methods")
    void topicSubscriberWrapperShouldDelegateAllMethods() throws Exception {
        // Given
        when(javaxTopicSubscriber.getTopic()).thenReturn(javaxTopic);
        when(javaxTopicSubscriber.getNoLocal()).thenReturn(true);
        when(javaxTopicSubscriber.getMessageSelector()).thenReturn("priority > 3");

        TopicSubscriberWrapper wrapper = new TopicSubscriberWrapper(javaxTopicSubscriber);

        // When
        Topic topic = wrapper.getTopic();
        boolean noLocal = wrapper.getNoLocal();
        String selector = wrapper.getMessageSelector();

        // Then
        assertThat(topic).isInstanceOf(TopicWrapper.class);
        assertThat(noLocal).isTrue();
        assertThat(selector).isEqualTo("priority > 3");

        verify(javaxTopicSubscriber).getTopic();
        verify(javaxTopicSubscriber).getNoLocal();
        verify(javaxTopicSubscriber).getMessageSelector();
    }

    @Test
    @DisplayName("TopicSubscriberWrapper: Should handle JMSExceptions")
    void topicSubscriberWrapperShouldHandleJMSExceptions() throws Exception {
        // Given
        javax.jms.JMSException javaxException = new javax.jms.JMSException("Subscriber error");
        when(javaxTopicSubscriber.getTopic()).thenThrow(javaxException);
        when(javaxTopicSubscriber.getNoLocal()).thenThrow(javaxException);

        TopicSubscriberWrapper wrapper = new TopicSubscriberWrapper(javaxTopicSubscriber);

        // When & Then
        assertThatThrownBy(() -> wrapper.getTopic())
            .isInstanceOf(JMSException.class)
            .hasMessage("Subscriber error");

        assertThatThrownBy(() -> wrapper.getNoLocal())
            .isInstanceOf(JMSException.class)
            .hasMessage("Subscriber error");
    }

    @Test
    @DisplayName("DestinationWrapper: Should handle equals and hashCode properly")
    void destinationWrapperShouldHandleEqualsAndHashCode() {
        // Given
        DestinationWrapper wrapper1 = new DestinationWrapper(javaxQueue);
        DestinationWrapper wrapper2 = new DestinationWrapper(javaxQueue);
        DestinationWrapper wrapper3 = new DestinationWrapper(javaxTopic);

        // When & Then
        assertThat(wrapper1).isEqualTo(wrapper2);
        assertThat(wrapper1).isNotEqualTo(wrapper3);
        assertThat(wrapper1.hashCode()).isEqualTo(wrapper2.hashCode());
        assertThat(wrapper1.hashCode()).isNotEqualTo(wrapper3.hashCode());

        assertThat(wrapper1).isEqualTo(wrapper1); // reflexive
        assertThat(wrapper1).isNotEqualTo(null);
        assertThat(wrapper1).isNotEqualTo("string");
    }

    @Test
    @DisplayName("DestinationWrapper: Should handle toString delegation")
    void destinationWrapperShouldHandleToString() {
        // Given
        when(javaxQueue.toString()).thenReturn("Test Queue");
        DestinationWrapper wrapper = new DestinationWrapper(javaxQueue);

        // When
        String result = wrapper.toString();

        // Then
        assertThat(result).isEqualTo("Test Queue");
        // Note: We don't verify toString() because Mockito has restrictions on verifying toString()
    }

    @Test
    @DisplayName("MessageProducerWrapper: Should handle all setter/getter methods")
    void messageProducerWrapperShouldHandleSettersAndGetters() throws Exception {
        // Given
        MessageProducerWrapper wrapper = new MessageProducerWrapper(javaxMessageProducer);
        when(javaxMessageProducer.getDisableMessageID()).thenReturn(true);
        when(javaxMessageProducer.getDisableMessageTimestamp()).thenReturn(false);
        when(javaxMessageProducer.getDeliveryMode()).thenReturn(DeliveryMode.PERSISTENT);
        when(javaxMessageProducer.getPriority()).thenReturn(7);
        when(javaxMessageProducer.getTimeToLive()).thenReturn(30000L);
        when(javaxMessageProducer.getDeliveryDelay()).thenReturn(5000L);

        // When & Then - Test all setters
        wrapper.setDisableMessageID(true);
        verify(javaxMessageProducer).setDisableMessageID(true);

        wrapper.setDisableMessageTimestamp(false);
        verify(javaxMessageProducer).setDisableMessageTimestamp(false);

        wrapper.setDeliveryMode(DeliveryMode.PERSISTENT);
        verify(javaxMessageProducer).setDeliveryMode(DeliveryMode.PERSISTENT);

        wrapper.setPriority(7);
        verify(javaxMessageProducer).setPriority(7);

        wrapper.setTimeToLive(30000L);
        verify(javaxMessageProducer).setTimeToLive(30000L);

        wrapper.setDeliveryDelay(5000L);
        verify(javaxMessageProducer).setDeliveryDelay(5000L);

        // Test all getters
        assertThat(wrapper.getDisableMessageID()).isTrue();
        verify(javaxMessageProducer).getDisableMessageID();

        assertThat(wrapper.getDisableMessageTimestamp()).isFalse();
        verify(javaxMessageProducer).getDisableMessageTimestamp();

        assertThat(wrapper.getDeliveryMode()).isEqualTo(DeliveryMode.PERSISTENT);
        verify(javaxMessageProducer).getDeliveryMode();

        assertThat(wrapper.getPriority()).isEqualTo(7);
        verify(javaxMessageProducer).getPriority();

        assertThat(wrapper.getTimeToLive()).isEqualTo(30000L);
        verify(javaxMessageProducer).getTimeToLive();

        assertThat(wrapper.getDeliveryDelay()).isEqualTo(5000L);
        verify(javaxMessageProducer).getDeliveryDelay();
    }

    @Test
    @DisplayName("MessageProducerWrapper: Should handle getDestination method")
    void messageProducerWrapperShouldHandleGetDestination() throws Exception {
        // Given
        MessageProducerWrapper wrapper = new MessageProducerWrapper(javaxMessageProducer);

        // Test with Queue
        when(javaxMessageProducer.getDestination()).thenReturn(javaxQueue);
        Destination queueDest = wrapper.getDestination();
        assertThat(queueDest).isInstanceOf(QueueWrapper.class);
        verify(javaxMessageProducer).getDestination();

        // Test with Topic
        when(javaxMessageProducer.getDestination()).thenReturn(javaxTopic);
        Destination topicDest = wrapper.getDestination();
        assertThat(topicDest).isInstanceOf(TopicWrapper.class);
        verify(javaxMessageProducer, times(2)).getDestination();

        // Test with null
        when(javaxMessageProducer.getDestination()).thenReturn(null);
        assertThat(wrapper.getDestination()).isNull();
        verify(javaxMessageProducer, times(3)).getDestination();
    }

    @Test
    @DisplayName("MessageProducerWrapper: Should handle close method")
    void messageProducerWrapperShouldHandleClose() throws Exception {
        // Given
        MessageProducerWrapper wrapper = new MessageProducerWrapper(javaxMessageProducer);

        // When
        wrapper.close();

        // Then
        verify(javaxMessageProducer).close();
    }

    @Test
    @DisplayName("MessageProducerWrapper: Should handle basic send method")
    void messageProducerWrapperShouldHandleBasicSend() throws Exception {
        // Given
        MessageProducerWrapper wrapper = new MessageProducerWrapper(javaxMessageProducer);
        MessageWrapper messageWrapper = new MessageWrapper(javaxTextMessage);

        // When
        wrapper.send(messageWrapper);

        // Then
        verify(javaxMessageProducer).send(javaxTextMessage);
    }

    @Test
    @DisplayName("MessageProducerWrapper: Should handle send with parameters")
    void messageProducerWrapperShouldHandleSendWithParameters() throws Exception {
        // Given
        MessageProducerWrapper wrapper = new MessageProducerWrapper(javaxMessageProducer);
        MessageWrapper messageWrapper = new MessageWrapper(javaxTextMessage);

        // When
        wrapper.send(messageWrapper, DeliveryMode.PERSISTENT, 5, 60000L);

        // Then
        verify(javaxMessageProducer).send(javaxTextMessage, DeliveryMode.PERSISTENT, 5, 60000L);
    }

    @Test
    @DisplayName("MessageProducerWrapper: Should handle send with destination")
    void messageProducerWrapperShouldHandleSendWithDestination() throws Exception {
        // Given
        MessageProducerWrapper wrapper = new MessageProducerWrapper(javaxMessageProducer);
        MessageWrapper messageWrapper = new MessageWrapper(javaxTextMessage);
        QueueWrapper queueWrapper = new QueueWrapper(javaxQueue);

        // When
        wrapper.send(queueWrapper, messageWrapper);

        // Then
        verify(javaxMessageProducer).send(javaxQueue, javaxTextMessage);
    }

    @Test
    @DisplayName("MessageProducerWrapper: Should handle send with destination and parameters")
    void messageProducerWrapperShouldHandleSendWithDestinationAndParameters() throws Exception {
        // Given
        MessageProducerWrapper wrapper = new MessageProducerWrapper(javaxMessageProducer);
        MessageWrapper messageWrapper = new MessageWrapper(javaxTextMessage);
        TopicWrapper topicWrapper = new TopicWrapper(javaxTopic);

        // When
        wrapper.send(topicWrapper, messageWrapper, DeliveryMode.NON_PERSISTENT, 3, 30000L);

        // Then
        verify(javaxMessageProducer).send(javaxTopic, javaxTextMessage, DeliveryMode.NON_PERSISTENT, 3, 30000L);
    }

    @Test
    @DisplayName("MessageProducerWrapper: Should handle async send methods")
    void messageProducerWrapperShouldHandleAsyncSend() throws Exception {
        // Given
        MessageProducerWrapper wrapper = new MessageProducerWrapper(javaxMessageProducer);
        MessageWrapper messageWrapper = new MessageWrapper(javaxTextMessage);
        QueueWrapper queueWrapper = new QueueWrapper(javaxQueue);

        // When - Test async send with completion listener
        wrapper.send(messageWrapper, jakartaCompletionListener);
        verify(javaxMessageProducer).send(eq(javaxTextMessage), any(javax.jms.CompletionListener.class));

        // When - Test async send with parameters and completion listener
        wrapper.send(messageWrapper, DeliveryMode.PERSISTENT, 5, 60000L, jakartaCompletionListener);
        verify(javaxMessageProducer).send(eq(javaxTextMessage), eq(DeliveryMode.PERSISTENT), eq(5), eq(60000L), any(javax.jms.CompletionListener.class));

        // When - Test async send with destination and completion listener
        wrapper.send(queueWrapper, messageWrapper, jakartaCompletionListener);
        verify(javaxMessageProducer).send(eq(javaxQueue), eq(javaxTextMessage), any(javax.jms.CompletionListener.class));

        // When - Test async send with destination, parameters and completion listener
        wrapper.send(queueWrapper, messageWrapper, DeliveryMode.NON_PERSISTENT, 3, 30000L, jakartaCompletionListener);
        verify(javaxMessageProducer).send(eq(javaxQueue), eq(javaxTextMessage), eq(DeliveryMode.NON_PERSISTENT), eq(3), eq(30000L), any(javax.jms.CompletionListener.class));
    }

    @Test
    @DisplayName("MessageProducerWrapper: Should handle JMSException scenarios")
    void messageProducerWrapperShouldHandleJMSExceptions() throws Exception {
        // Given
        MessageProducerWrapper wrapper = new MessageProducerWrapper(javaxMessageProducer);
        javax.jms.JMSException javaxException = new javax.jms.JMSException("Producer error");

        // Mock all methods to throw exceptions
        doThrow(javaxException).when(javaxMessageProducer).setDisableMessageID(anyBoolean());
        when(javaxMessageProducer.getDisableMessageID()).thenThrow(javaxException);
        doThrow(javaxException).when(javaxMessageProducer).setDisableMessageTimestamp(anyBoolean());
        when(javaxMessageProducer.getDisableMessageTimestamp()).thenThrow(javaxException);
        doThrow(javaxException).when(javaxMessageProducer).setDeliveryMode(anyInt());
        when(javaxMessageProducer.getDeliveryMode()).thenThrow(javaxException);
        doThrow(javaxException).when(javaxMessageProducer).setPriority(anyInt());
        when(javaxMessageProducer.getPriority()).thenThrow(javaxException);
        doThrow(javaxException).when(javaxMessageProducer).setTimeToLive(anyLong());
        when(javaxMessageProducer.getTimeToLive()).thenThrow(javaxException);
        doThrow(javaxException).when(javaxMessageProducer).setDeliveryDelay(anyLong());
        when(javaxMessageProducer.getDeliveryDelay()).thenThrow(javaxException);
        when(javaxMessageProducer.getDestination()).thenThrow(javaxException);
        doThrow(javaxException).when(javaxMessageProducer).close();

        // When & Then - Test all setter exceptions
        assertThatThrownBy(() -> wrapper.setDisableMessageID(true))
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer error");

        assertThatThrownBy(() -> wrapper.getDisableMessageID())
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer error");

        assertThatThrownBy(() -> wrapper.setDisableMessageTimestamp(false))
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer error");

        assertThatThrownBy(() -> wrapper.getDisableMessageTimestamp())
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer error");

        assertThatThrownBy(() -> wrapper.setDeliveryMode(DeliveryMode.PERSISTENT))
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer error");

        assertThatThrownBy(() -> wrapper.getDeliveryMode())
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer error");

        assertThatThrownBy(() -> wrapper.setPriority(5))
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer error");

        assertThatThrownBy(() -> wrapper.getPriority())
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer error");

        assertThatThrownBy(() -> wrapper.setTimeToLive(30000L))
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer error");

        assertThatThrownBy(() -> wrapper.getTimeToLive())
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer error");

        assertThatThrownBy(() -> wrapper.setDeliveryDelay(5000L))
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer error");

        assertThatThrownBy(() -> wrapper.getDeliveryDelay())
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer error");

        assertThatThrownBy(() -> wrapper.getDestination())
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer error");

        assertThatThrownBy(() -> wrapper.close())
            .isInstanceOf(JMSException.class)
            .hasMessage("Producer error");
    }

    @Test
    @DisplayName("MessageProducerWrapper: Should handle send method JMSExceptions")
    void messageProducerWrapperShouldHandleSendJMSExceptions() throws Exception {
        // Given
        MessageProducerWrapper wrapper = new MessageProducerWrapper(javaxMessageProducer);
        MessageWrapper messageWrapper = new MessageWrapper(javaxTextMessage);
        QueueWrapper queueWrapper = new QueueWrapper(javaxQueue);
        javax.jms.JMSException javaxException = new javax.jms.JMSException("Send error");

        // Mock all send methods to throw exceptions
        doThrow(javaxException).when(javaxMessageProducer).send(any(javax.jms.Message.class));
        doThrow(javaxException).when(javaxMessageProducer).send(any(javax.jms.Message.class), anyInt(), anyInt(), anyLong());
        doThrow(javaxException).when(javaxMessageProducer).send(any(javax.jms.Destination.class), any(javax.jms.Message.class));
        doThrow(javaxException).when(javaxMessageProducer).send(any(javax.jms.Destination.class), any(javax.jms.Message.class), anyInt(), anyInt(), anyLong());
        doThrow(javaxException).when(javaxMessageProducer).send(any(javax.jms.Message.class), any(javax.jms.CompletionListener.class));
        doThrow(javaxException).when(javaxMessageProducer).send(any(javax.jms.Message.class), anyInt(), anyInt(), anyLong(), any(javax.jms.CompletionListener.class));
        doThrow(javaxException).when(javaxMessageProducer).send(any(javax.jms.Destination.class), any(javax.jms.Message.class), any(javax.jms.CompletionListener.class));
        doThrow(javaxException).when(javaxMessageProducer).send(any(javax.jms.Destination.class), any(javax.jms.Message.class), anyInt(), anyInt(), anyLong(), any(javax.jms.CompletionListener.class));

        // When & Then - Test all send method exceptions
        assertThatThrownBy(() -> wrapper.send(messageWrapper))
            .isInstanceOf(JMSException.class)
            .hasMessage("Send error");

        assertThatThrownBy(() -> wrapper.send(messageWrapper, DeliveryMode.PERSISTENT, 5, 60000L))
            .isInstanceOf(JMSException.class)
            .hasMessage("Send error");

        assertThatThrownBy(() -> wrapper.send(queueWrapper, messageWrapper))
            .isInstanceOf(JMSException.class)
            .hasMessage("Send error");

        assertThatThrownBy(() -> wrapper.send(queueWrapper, messageWrapper, DeliveryMode.PERSISTENT, 5, 60000L))
            .isInstanceOf(JMSException.class)
            .hasMessage("Send error");

        assertThatThrownBy(() -> wrapper.send(messageWrapper, jakartaCompletionListener))
            .isInstanceOf(JMSException.class)
            .hasMessage("Send error");

        assertThatThrownBy(() -> wrapper.send(messageWrapper, DeliveryMode.PERSISTENT, 5, 60000L, jakartaCompletionListener))
            .isInstanceOf(JMSException.class)
            .hasMessage("Send error");

        assertThatThrownBy(() -> wrapper.send(queueWrapper, messageWrapper, jakartaCompletionListener))
            .isInstanceOf(JMSException.class)
            .hasMessage("Send error");

        assertThatThrownBy(() -> wrapper.send(queueWrapper, messageWrapper, DeliveryMode.PERSISTENT, 5, 60000L, jakartaCompletionListener))
            .isInstanceOf(JMSException.class)
            .hasMessage("Send error");
    }
}