package com.example.ibmmq.adapter;

import jakarta.jms.*;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import com.example.ibmmq.adapter.MessageWrappers.*;
import com.example.ibmmq.adapter.DestinationWrappers.*;

import java.util.Enumeration;

/**
 * Adapter to bridge between Jakarta JMS (jakarta.jms.*) and IBM MQ Client (javax.jms.*)
 * This allows the application to use modern Jakarta JMS while still working with IBM MQ Client
 */
public class JakartaJMSAdapter {

    /**
     * Wraps a javax.jms.ConnectionFactory to provide Jakarta JMS interface
     */
    public static class ConnectionFactoryWrapper implements jakarta.jms.ConnectionFactory {
        private final javax.jms.ConnectionFactory delegate;

        public ConnectionFactoryWrapper(javax.jms.ConnectionFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public jakarta.jms.Connection createConnection() throws JMSException {
            try {
                return new ConnectionWrapper(delegate.createConnection());
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.Connection createConnection(String userName, String password) throws JMSException {
            try {
                return new ConnectionWrapper(delegate.createConnection(userName, password));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.JMSContext createContext() {
            throw new UnsupportedOperationException("JMSContext not supported in IBM MQ Client adapter");
        }

        @Override
        public jakarta.jms.JMSContext createContext(String userName, String password) {
            throw new UnsupportedOperationException("JMSContext not supported in IBM MQ Client adapter");
        }

        @Override
        public jakarta.jms.JMSContext createContext(String userName, String password, int sessionMode) {
            throw new UnsupportedOperationException("JMSContext not supported in IBM MQ Client adapter");
        }

        @Override
        public jakarta.jms.JMSContext createContext(int sessionMode) {
            throw new UnsupportedOperationException("JMSContext not supported in IBM MQ Client adapter");
        }
    }

    /**
     * Wraps a javax.jms.Connection to provide Jakarta JMS interface
     */
    public static class ConnectionWrapper implements jakarta.jms.Connection {
        private final javax.jms.Connection delegate;

        public ConnectionWrapper(javax.jms.Connection delegate) {
            this.delegate = delegate;
        }

        @Override
        public jakarta.jms.Session createSession(boolean transacted, int acknowledgeMode) throws JMSException {
            try {
                return new SessionWrapper(delegate.createSession(transacted, acknowledgeMode));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.Session createSession(int sessionMode) throws JMSException {
            try {
                return new SessionWrapper(delegate.createSession(sessionMode));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.Session createSession() throws JMSException {
            try {
                return new SessionWrapper(delegate.createSession());
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public String getClientID() throws JMSException {
            try {
                return delegate.getClientID();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setClientID(String clientID) throws JMSException {
            try {
                delegate.setClientID(clientID);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.ConnectionMetaData getMetaData() throws JMSException {
            try {
                return new ConnectionMetaDataWrapper(delegate.getMetaData());
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.ExceptionListener getExceptionListener() throws JMSException {
            try {
                javax.jms.ExceptionListener listener = delegate.getExceptionListener();
                return listener != null ? new ExceptionListenerWrapper(listener) : null;
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setExceptionListener(jakarta.jms.ExceptionListener listener) throws JMSException {
            try {
                if (listener != null) {
                    delegate.setExceptionListener(new JavaxExceptionListenerWrapper(listener));
                } else {
                    delegate.setExceptionListener(null);
                }
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void start() throws JMSException {
            try {
                delegate.start();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void stop() throws JMSException {
            try {
                delegate.stop();
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
        public jakarta.jms.ConnectionConsumer createConnectionConsumer(jakarta.jms.Destination destination, String messageSelector, jakarta.jms.ServerSessionPool sessionPool, int maxMessages) throws JMSException {
            throw new UnsupportedOperationException("ConnectionConsumer not supported in adapter");
        }

        @Override
        public jakarta.jms.ConnectionConsumer createSharedConnectionConsumer(jakarta.jms.Topic topic, String subscriptionName, String messageSelector, jakarta.jms.ServerSessionPool sessionPool, int maxMessages) throws JMSException {
            throw new UnsupportedOperationException("ConnectionConsumer not supported in adapter");
        }

        @Override
        public jakarta.jms.ConnectionConsumer createDurableConnectionConsumer(jakarta.jms.Topic topic, String subscriptionName, String messageSelector, jakarta.jms.ServerSessionPool sessionPool, int maxMessages) throws JMSException {
            throw new UnsupportedOperationException("ConnectionConsumer not supported in adapter");
        }

        @Override
        public jakarta.jms.ConnectionConsumer createSharedDurableConnectionConsumer(jakarta.jms.Topic topic, String subscriptionName, String messageSelector, jakarta.jms.ServerSessionPool sessionPool, int maxMessages) throws JMSException {
            throw new UnsupportedOperationException("ConnectionConsumer not supported in adapter");
        }
    }

    /**
     * Wraps a javax.jms.Session to provide Jakarta JMS interface
     */
    public static class SessionWrapper implements jakarta.jms.Session {
        private final javax.jms.Session delegate;

        public SessionWrapper(javax.jms.Session delegate) {
            this.delegate = delegate;
        }

        @Override
        public jakarta.jms.BytesMessage createBytesMessage() throws JMSException {
            try {
                return new BytesMessageWrapper(delegate.createBytesMessage());
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.MapMessage createMapMessage() throws JMSException {
            try {
                return new MapMessageWrapper(delegate.createMapMessage());
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.Message createMessage() throws JMSException {
            try {
                return new MessageWrapper(delegate.createMessage());
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.ObjectMessage createObjectMessage() throws JMSException {
            try {
                return new ObjectMessageWrapper(delegate.createObjectMessage());
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.ObjectMessage createObjectMessage(java.io.Serializable object) throws JMSException {
            try {
                return new ObjectMessageWrapper(delegate.createObjectMessage(object));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.StreamMessage createStreamMessage() throws JMSException {
            try {
                return new StreamMessageWrapper(delegate.createStreamMessage());
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.TextMessage createTextMessage() throws JMSException {
            try {
                return new TextMessageWrapper(delegate.createTextMessage());
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.TextMessage createTextMessage(String text) throws JMSException {
            try {
                return new TextMessageWrapper(delegate.createTextMessage(text));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public boolean getTransacted() throws JMSException {
            try {
                return delegate.getTransacted();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int getAcknowledgeMode() throws JMSException {
            try {
                return delegate.getAcknowledgeMode();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void commit() throws JMSException {
            try {
                delegate.commit();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void rollback() throws JMSException {
            try {
                delegate.rollback();
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
        public void recover() throws JMSException {
            try {
                delegate.recover();
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
        public void run() {
            delegate.run();
        }

        @Override
        public jakarta.jms.MessageProducer createProducer(jakarta.jms.Destination destination) throws JMSException {
            try {
                javax.jms.Destination javaxDest = destination != null ?
                    ((DestinationWrapper) destination).getDelegate() : null;
                return new MessageProducerWrapper(delegate.createProducer(javaxDest));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.MessageConsumer createConsumer(jakarta.jms.Destination destination) throws JMSException {
            try {
                javax.jms.Destination javaxDest = destination != null ? ((DestinationWrapper) destination).getDelegate() : null;
                return new MessageConsumerWrapper(delegate.createConsumer(javaxDest));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.MessageConsumer createConsumer(jakarta.jms.Destination destination, String messageSelector) throws JMSException {
            try {
                javax.jms.Destination javaxDest = destination != null ? ((DestinationWrapper) destination).getDelegate() : null;
                return new MessageConsumerWrapper(delegate.createConsumer(javaxDest, messageSelector));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.MessageConsumer createConsumer(jakarta.jms.Destination destination, String messageSelector, boolean noLocal) throws JMSException {
            try {
                javax.jms.Destination javaxDest = destination != null ? ((DestinationWrapper) destination).getDelegate() : null;
                return new MessageConsumerWrapper(delegate.createConsumer(javaxDest, messageSelector, noLocal));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.MessageConsumer createSharedConsumer(jakarta.jms.Topic topic, String sharedSubscriptionName) throws JMSException {
            try {
                javax.jms.Topic javaxTopic = (javax.jms.Topic) ((DestinationWrapper) topic).getDelegate();
                return new MessageConsumerWrapper(delegate.createSharedConsumer(javaxTopic, sharedSubscriptionName));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.MessageConsumer createSharedConsumer(jakarta.jms.Topic topic, String sharedSubscriptionName, String messageSelector) throws JMSException {
            try {
                javax.jms.Topic javaxTopic = (javax.jms.Topic) ((DestinationWrapper) topic).getDelegate();
                return new MessageConsumerWrapper(delegate.createSharedConsumer(javaxTopic, sharedSubscriptionName, messageSelector));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.Queue createQueue(String queueName) throws JMSException {
            try {
                return new QueueWrapper(delegate.createQueue(queueName));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.Topic createTopic(String topicName) throws JMSException {
            try {
                return new TopicWrapper(delegate.createTopic(topicName));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.TopicSubscriber createDurableSubscriber(jakarta.jms.Topic topic, String name) throws JMSException {
            try {
                javax.jms.Topic javaxTopic = (javax.jms.Topic) ((DestinationWrapper) topic).getDelegate();
                return new TopicSubscriberWrapper(delegate.createDurableSubscriber(javaxTopic, name));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.TopicSubscriber createDurableSubscriber(jakarta.jms.Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
            try {
                javax.jms.Topic javaxTopic = (javax.jms.Topic) ((DestinationWrapper) topic).getDelegate();
                return new TopicSubscriberWrapper(delegate.createDurableSubscriber(javaxTopic, name, messageSelector, noLocal));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.MessageConsumer createDurableConsumer(jakarta.jms.Topic topic, String name) throws JMSException {
            try {
                javax.jms.Topic javaxTopic = (javax.jms.Topic) ((DestinationWrapper) topic).getDelegate();
                return new MessageConsumerWrapper(delegate.createDurableConsumer(javaxTopic, name));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.MessageConsumer createDurableConsumer(jakarta.jms.Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
            try {
                javax.jms.Topic javaxTopic = (javax.jms.Topic) ((DestinationWrapper) topic).getDelegate();
                return new MessageConsumerWrapper(delegate.createDurableConsumer(javaxTopic, name, messageSelector, noLocal));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.MessageConsumer createSharedDurableConsumer(jakarta.jms.Topic topic, String name) throws JMSException {
            try {
                javax.jms.Topic javaxTopic = (javax.jms.Topic) ((DestinationWrapper) topic).getDelegate();
                return new MessageConsumerWrapper(delegate.createSharedDurableConsumer(javaxTopic, name));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.MessageConsumer createSharedDurableConsumer(jakarta.jms.Topic topic, String name, String messageSelector) throws JMSException {
            try {
                javax.jms.Topic javaxTopic = (javax.jms.Topic) ((DestinationWrapper) topic).getDelegate();
                return new MessageConsumerWrapper(delegate.createSharedDurableConsumer(javaxTopic, name, messageSelector));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.QueueBrowser createBrowser(jakarta.jms.Queue queue) throws JMSException {
            try {
                javax.jms.Queue javaxQueue = (javax.jms.Queue) ((DestinationWrapper) queue).getDelegate();
                return new QueueBrowserWrapper(delegate.createBrowser(javaxQueue));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.QueueBrowser createBrowser(jakarta.jms.Queue queue, String messageSelector) throws JMSException {
            try {
                javax.jms.Queue javaxQueue = (javax.jms.Queue) ((DestinationWrapper) queue).getDelegate();
                return new QueueBrowserWrapper(delegate.createBrowser(javaxQueue, messageSelector));
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.TemporaryQueue createTemporaryQueue() throws JMSException {
            try {
                return new TemporaryQueueWrapper(delegate.createTemporaryQueue());
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.TemporaryTopic createTemporaryTopic() throws JMSException {
            try {
                return new TemporaryTopicWrapper(delegate.createTemporaryTopic());
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void unsubscribe(String name) throws JMSException {
            try {
                delegate.unsubscribe(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }
    }

    // Helper wrapper classes for listeners
    private static class ExceptionListenerWrapper implements jakarta.jms.ExceptionListener {
        private final javax.jms.ExceptionListener delegate;

        public ExceptionListenerWrapper(javax.jms.ExceptionListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onException(JMSException exception) {
            delegate.onException(new javax.jms.JMSException(exception.getMessage()));
        }
    }

    private static class JavaxExceptionListenerWrapper implements javax.jms.ExceptionListener {
        private final jakarta.jms.ExceptionListener delegate;

        public JavaxExceptionListenerWrapper(jakarta.jms.ExceptionListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onException(javax.jms.JMSException exception) {
            delegate.onException(new JMSException(exception.getMessage()));
        }
    }

    private static class MessageListenerWrapper implements jakarta.jms.MessageListener {
        private final javax.jms.MessageListener delegate;

        public MessageListenerWrapper(javax.jms.MessageListener delegate) {
            this.delegate = delegate;
        }

        @Override
        public void onMessage(jakarta.jms.Message message) {
            // Convert Jakarta message back to javax message for the delegate
            javax.jms.Message javaxMessage = ((MessageWrapper) message).getDelegate();
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
            // Convert javax message to Jakarta message for the delegate
            delegate.onMessage(new MessageWrapper(message));
        }
    }

    // Utility method to convert javax.jms exceptions to jakarta.jms exceptions
    public static JMSException convertException(javax.jms.JMSException javaxException) {
        JMSException jakartaException = new JMSException(javaxException.getMessage());
        jakartaException.setLinkedException(javaxException.getLinkedException());
        return jakartaException;
    }

    // Utility method to convert jakarta.jms exceptions to javax.jms exceptions
    public static javax.jms.JMSException convertException(JMSException jakartaException) {
        javax.jms.JMSException javaxException = new javax.jms.JMSException(jakartaException.getMessage());
        javaxException.setLinkedException(jakartaException.getLinkedException());
        return javaxException;
    }
}