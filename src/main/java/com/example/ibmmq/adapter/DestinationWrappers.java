package com.example.ibmmq.adapter;

import jakarta.jms.*;
import java.util.Enumeration;

/**
 * Destination wrapper classes for Jakarta JMS to javax JMS adapter
 */
public class DestinationWrappers {

    /**
     * Base wrapper for destinations
     */
    public static class DestinationWrapper implements jakarta.jms.Destination {
        protected final javax.jms.Destination delegate;

        public DestinationWrapper(javax.jms.Destination delegate) {
            this.delegate = delegate;
        }

        public javax.jms.Destination getDelegate() {
            return delegate;
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof DestinationWrapper)) return false;
            DestinationWrapper other = (DestinationWrapper) obj;
            return delegate.equals(other.delegate);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }
    }

    /**
     * Wrapper for Queue
     */
    public static class QueueWrapper extends DestinationWrapper implements jakarta.jms.Queue {
        private final javax.jms.Queue queueDelegate;

        public QueueWrapper(javax.jms.Queue delegate) {
            super(delegate);
            this.queueDelegate = delegate;
        }

        @Override
        public String getQueueName() throws JMSException {
            try {
                return queueDelegate.getQueueName();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }
    }

    /**
     * Wrapper for Topic
     */
    public static class TopicWrapper extends DestinationWrapper implements jakarta.jms.Topic {
        private final javax.jms.Topic topicDelegate;

        public TopicWrapper(javax.jms.Topic delegate) {
            super(delegate);
            this.topicDelegate = delegate;
        }

        @Override
        public String getTopicName() throws JMSException {
            try {
                return topicDelegate.getTopicName();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }
    }

    /**
     * Wrapper for TemporaryQueue
     */
    public static class TemporaryQueueWrapper extends QueueWrapper implements jakarta.jms.TemporaryQueue {
        private final javax.jms.TemporaryQueue tempQueueDelegate;

        public TemporaryQueueWrapper(javax.jms.TemporaryQueue delegate) {
            super(delegate);
            this.tempQueueDelegate = delegate;
        }

        @Override
        public void delete() throws JMSException {
            try {
                tempQueueDelegate.delete();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }
    }

    /**
     * Wrapper for TemporaryTopic
     */
    public static class TemporaryTopicWrapper extends TopicWrapper implements jakarta.jms.TemporaryTopic {
        private final javax.jms.TemporaryTopic tempTopicDelegate;

        public TemporaryTopicWrapper(javax.jms.TemporaryTopic delegate) {
            super(delegate);
            this.tempTopicDelegate = delegate;
        }

        @Override
        public void delete() throws JMSException {
            try {
                tempTopicDelegate.delete();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }
    }

    /**
     * Wrapper for MessageProducer
     */
    public static class MessageProducerWrapper implements jakarta.jms.MessageProducer {
        private final javax.jms.MessageProducer delegate;

        public MessageProducerWrapper(javax.jms.MessageProducer delegate) {
            this.delegate = delegate;
        }

        @Override
        public void setDisableMessageID(boolean value) throws JMSException {
            try {
                delegate.setDisableMessageID(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public boolean getDisableMessageID() throws JMSException {
            try {
                return delegate.getDisableMessageID();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setDisableMessageTimestamp(boolean value) throws JMSException {
            try {
                delegate.setDisableMessageTimestamp(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public boolean getDisableMessageTimestamp() throws JMSException {
            try {
                return delegate.getDisableMessageTimestamp();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setDeliveryMode(int deliveryMode) throws JMSException {
            try {
                delegate.setDeliveryMode(deliveryMode);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int getDeliveryMode() throws JMSException {
            try {
                return delegate.getDeliveryMode();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setPriority(int defaultPriority) throws JMSException {
            try {
                delegate.setPriority(defaultPriority);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int getPriority() throws JMSException {
            try {
                return delegate.getPriority();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setTimeToLive(long timeToLive) throws JMSException {
            try {
                delegate.setTimeToLive(timeToLive);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public long getTimeToLive() throws JMSException {
            try {
                return delegate.getTimeToLive();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setDeliveryDelay(long deliveryDelay) throws JMSException {
            try {
                delegate.setDeliveryDelay(deliveryDelay);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public long getDeliveryDelay() throws JMSException {
            try {
                return delegate.getDeliveryDelay();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.Destination getDestination() throws JMSException {
            try {
                javax.jms.Destination javaxDest = delegate.getDestination();
                if (javaxDest == null) return null;

                if (javaxDest instanceof javax.jms.Queue) {
                    return new QueueWrapper((javax.jms.Queue) javaxDest);
                } else if (javaxDest instanceof javax.jms.Topic) {
                    return new TopicWrapper((javax.jms.Topic) javaxDest);
                } else {
                    return new DestinationWrapper(javaxDest);
                }
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void close() throws JMSException {
            try {
                delegate.close();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void send(jakarta.jms.Message message) throws JMSException {
            try {
                javax.jms.Message javaxMessage = ((MessageWrappers.MessageWrapper) message).getDelegate();
                delegate.send(javaxMessage);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void send(jakarta.jms.Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
            try {
                javax.jms.Message javaxMessage = ((MessageWrappers.MessageWrapper) message).getDelegate();
                delegate.send(javaxMessage, deliveryMode, priority, timeToLive);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void send(jakarta.jms.Destination destination, jakarta.jms.Message message) throws JMSException {
            try {
                javax.jms.Destination javaxDest = ((DestinationWrapper) destination).getDelegate();
                javax.jms.Message javaxMessage = ((MessageWrappers.MessageWrapper) message).getDelegate();
                delegate.send(javaxDest, javaxMessage);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void send(jakarta.jms.Destination destination, jakarta.jms.Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
            try {
                javax.jms.Destination javaxDest = ((DestinationWrapper) destination).getDelegate();
                javax.jms.Message javaxMessage = ((MessageWrappers.MessageWrapper) message).getDelegate();
                delegate.send(javaxDest, javaxMessage, deliveryMode, priority, timeToLive);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void send(jakarta.jms.Message message, CompletionListener completionListener) throws JMSException {
            try {
                javax.jms.Message javaxMessage = ((MessageWrappers.MessageWrapper) message).getDelegate();
                delegate.send(javaxMessage, new CompletionListenerWrapper(completionListener));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void send(jakarta.jms.Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener) throws JMSException {
            try {
                javax.jms.Message javaxMessage = ((MessageWrappers.MessageWrapper) message).getDelegate();
                delegate.send(javaxMessage, deliveryMode, priority, timeToLive, new CompletionListenerWrapper(completionListener));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void send(jakarta.jms.Destination destination, jakarta.jms.Message message, CompletionListener completionListener) throws JMSException {
            try {
                javax.jms.Destination javaxDest = ((DestinationWrapper) destination).getDelegate();
                javax.jms.Message javaxMessage = ((MessageWrappers.MessageWrapper) message).getDelegate();
                delegate.send(javaxDest, javaxMessage, new CompletionListenerWrapper(completionListener));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void send(jakarta.jms.Destination destination, jakarta.jms.Message message, int deliveryMode, int priority, long timeToLive, CompletionListener completionListener) throws JMSException {
            try {
                javax.jms.Destination javaxDest = ((DestinationWrapper) destination).getDelegate();
                javax.jms.Message javaxMessage = ((MessageWrappers.MessageWrapper) message).getDelegate();
                delegate.send(javaxDest, javaxMessage, deliveryMode, priority, timeToLive, new CompletionListenerWrapper(completionListener));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }
    }

    /**
     * Wrapper for MessageConsumer
     */
    public static class MessageConsumerWrapper implements jakarta.jms.MessageConsumer {
        private final javax.jms.MessageConsumer delegate;

        public MessageConsumerWrapper(javax.jms.MessageConsumer delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getMessageSelector() throws JMSException {
            try {
                return delegate.getMessageSelector();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.MessageListener getMessageListener() throws JMSException {
            try {
                javax.jms.MessageListener listener = delegate.getMessageListener();
                return listener != null ? new MessageListenerWrapper(listener) : null;
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setMessageListener(jakarta.jms.MessageListener listener) throws JMSException {
            try {
                if (listener != null) {
                    delegate.setMessageListener(new JavaxMessageListenerWrapper(listener));
                } else {
                    delegate.setMessageListener(null);
                }
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.Message receive() throws JMSException {
            try {
                javax.jms.Message javaxMessage = delegate.receive();
                return javaxMessage != null ? wrapMessage(javaxMessage) : null;
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.Message receive(long timeout) throws JMSException {
            try {
                javax.jms.Message javaxMessage = delegate.receive(timeout);
                return javaxMessage != null ? wrapMessage(javaxMessage) : null;
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.Message receiveNoWait() throws JMSException {
            try {
                javax.jms.Message javaxMessage = delegate.receiveNoWait();
                return javaxMessage != null ? wrapMessage(javaxMessage) : null;
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void close() throws JMSException {
            try {
                delegate.close();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        private jakarta.jms.Message wrapMessage(javax.jms.Message javaxMessage) {
            if (javaxMessage instanceof javax.jms.TextMessage) {
                return new MessageWrappers.TextMessageWrapper((javax.jms.TextMessage) javaxMessage);
            } else if (javaxMessage instanceof javax.jms.BytesMessage) {
                return new MessageWrappers.BytesMessageWrapper((javax.jms.BytesMessage) javaxMessage);
            } else if (javaxMessage instanceof javax.jms.ObjectMessage) {
                return new MessageWrappers.ObjectMessageWrapper((javax.jms.ObjectMessage) javaxMessage);
            } else if (javaxMessage instanceof javax.jms.MapMessage) {
                return new MessageWrappers.MapMessageWrapper((javax.jms.MapMessage) javaxMessage);
            } else if (javaxMessage instanceof javax.jms.StreamMessage) {
                return new MessageWrappers.StreamMessageWrapper((javax.jms.StreamMessage) javaxMessage);
            } else {
                return new MessageWrappers.MessageWrapper(javaxMessage);
            }
        }
    }

    /**
     * Wrapper for TopicSubscriber
     */
    public static class TopicSubscriberWrapper extends MessageConsumerWrapper implements jakarta.jms.TopicSubscriber {
        private final javax.jms.TopicSubscriber topicDelegate;

        public TopicSubscriberWrapper(javax.jms.TopicSubscriber delegate) {
            super(delegate);
            this.topicDelegate = delegate;
        }

        @Override
        public jakarta.jms.Topic getTopic() throws JMSException {
            try {
                return new TopicWrapper(topicDelegate.getTopic());
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public boolean getNoLocal() throws JMSException {
            try {
                return topicDelegate.getNoLocal();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }
    }

    /**
     * Wrapper for QueueBrowser
     */
    public static class QueueBrowserWrapper implements jakarta.jms.QueueBrowser {
        private final javax.jms.QueueBrowser delegate;

        public QueueBrowserWrapper(javax.jms.QueueBrowser delegate) {
            this.delegate = delegate;
        }

        @Override
        public jakarta.jms.Queue getQueue() throws JMSException {
            try {
                return new QueueWrapper(delegate.getQueue());
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public String getMessageSelector() throws JMSException {
            try {
                return delegate.getMessageSelector();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public Enumeration getEnumeration() throws JMSException {
            try {
                return delegate.getEnumeration();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void close() throws JMSException {
            try {
                delegate.close();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }
    }

    /**
     * Wrapper for ConnectionMetaData
     */
    public static class ConnectionMetaDataWrapper implements jakarta.jms.ConnectionMetaData {
        private final javax.jms.ConnectionMetaData delegate;

        public ConnectionMetaDataWrapper(javax.jms.ConnectionMetaData delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getJMSVersion() throws JMSException {
            try {
                return delegate.getJMSVersion();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int getJMSMajorVersion() throws JMSException {
            try {
                return delegate.getJMSMajorVersion();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int getJMSMinorVersion() throws JMSException {
            try {
                return delegate.getJMSMinorVersion();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public String getJMSProviderName() throws JMSException {
            try {
                return delegate.getJMSProviderName();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public String getProviderVersion() throws JMSException {
            try {
                return delegate.getProviderVersion();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int getProviderMajorVersion() throws JMSException {
            try {
                return delegate.getProviderMajorVersion();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int getProviderMinorVersion() throws JMSException {
            try {
                return delegate.getProviderMinorVersion();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public Enumeration getJMSXPropertyNames() throws JMSException {
            try {
                return delegate.getJMSXPropertyNames();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }
    }

    // Helper wrapper classes for listeners
    private static class MessageListenerWrapper implements jakarta.jms.MessageListener {
        private final javax.jms.MessageListener delegate;

        public MessageListenerWrapper(javax.jms.MessageListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onMessage(jakarta.jms.Message message) {
            javax.jms.Message javaxMessage = ((MessageWrappers.MessageWrapper) message).getDelegate();
            delegate.onMessage(javaxMessage);
        }
    }

    private static class JavaxMessageListenerWrapper implements javax.jms.MessageListener {
        private final jakarta.jms.MessageListener delegate;

        public JavaxMessageListenerWrapper(jakarta.jms.MessageListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onMessage(javax.jms.Message message) {
            if (message instanceof javax.jms.TextMessage) {
                delegate.onMessage(new MessageWrappers.TextMessageWrapper((javax.jms.TextMessage) message));
            } else if (message instanceof javax.jms.BytesMessage) {
                delegate.onMessage(new MessageWrappers.BytesMessageWrapper((javax.jms.BytesMessage) message));
            } else if (message instanceof javax.jms.ObjectMessage) {
                delegate.onMessage(new MessageWrappers.ObjectMessageWrapper((javax.jms.ObjectMessage) message));
            } else if (message instanceof javax.jms.MapMessage) {
                delegate.onMessage(new MessageWrappers.MapMessageWrapper((javax.jms.MapMessage) message));
            } else if (message instanceof javax.jms.StreamMessage) {
                delegate.onMessage(new MessageWrappers.StreamMessageWrapper((javax.jms.StreamMessage) message));
            } else {
                delegate.onMessage(new MessageWrappers.MessageWrapper(message));
            }
        }
    }

    private static class CompletionListenerWrapper implements javax.jms.CompletionListener {
        private final jakarta.jms.CompletionListener delegate;

        public CompletionListenerWrapper(jakarta.jms.CompletionListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onCompletion(javax.jms.Message message) {
            if (message instanceof javax.jms.TextMessage) {
                delegate.onCompletion(new MessageWrappers.TextMessageWrapper((javax.jms.TextMessage) message));
            } else if (message instanceof javax.jms.BytesMessage) {
                delegate.onCompletion(new MessageWrappers.BytesMessageWrapper((javax.jms.BytesMessage) message));
            } else if (message instanceof javax.jms.ObjectMessage) {
                delegate.onCompletion(new MessageWrappers.ObjectMessageWrapper((javax.jms.ObjectMessage) message));
            } else if (message instanceof javax.jms.MapMessage) {
                delegate.onCompletion(new MessageWrappers.MapMessageWrapper((javax.jms.MapMessage) message));
            } else if (message instanceof javax.jms.StreamMessage) {
                delegate.onCompletion(new MessageWrappers.StreamMessageWrapper((javax.jms.StreamMessage) message));
            } else {
                delegate.onCompletion(new MessageWrappers.MessageWrapper(message));
            }
        }

        @Override
        public void onException(javax.jms.Message message, Exception exception) {
            if (message instanceof javax.jms.TextMessage) {
                delegate.onException(new MessageWrappers.TextMessageWrapper((javax.jms.TextMessage) message), exception);
            } else if (message instanceof javax.jms.BytesMessage) {
                delegate.onException(new MessageWrappers.BytesMessageWrapper((javax.jms.BytesMessage) message), exception);
            } else if (message instanceof javax.jms.ObjectMessage) {
                delegate.onException(new MessageWrappers.ObjectMessageWrapper((javax.jms.ObjectMessage) message), exception);
            } else if (message instanceof javax.jms.MapMessage) {
                delegate.onException(new MessageWrappers.MapMessageWrapper((javax.jms.MapMessage) message), exception);
            } else if (message instanceof javax.jms.StreamMessage) {
                delegate.onException(new MessageWrappers.StreamMessageWrapper((javax.jms.StreamMessage) message), exception);
            } else {
                delegate.onException(new MessageWrappers.MessageWrapper(message), exception);
            }
        }
    }
}