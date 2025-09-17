package com.example.ibmmq.adapter;

import jakarta.jms.*;

import java.util.Enumeration;

/**
 * Message wrapper classes for Jakarta JMS to javax JMS adapter
 */
public class MessageWrappers {

    /**
     * Base wrapper for all message types
     */
    public static class MessageWrapper implements jakarta.jms.Message {
        protected final javax.jms.Message delegate;

        public MessageWrapper(javax.jms.Message delegate) {
            this.delegate = delegate;
        }

        public javax.jms.Message getDelegate() {
            return delegate;
        }

        @Override
        public String getJMSMessageID() throws JMSException {
            try {
                return delegate.getJMSMessageID();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setJMSMessageID(String id) throws JMSException {
            try {
                delegate.setJMSMessageID(id);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public long getJMSTimestamp() throws JMSException {
            try {
                return delegate.getJMSTimestamp();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setJMSTimestamp(long timestamp) throws JMSException {
            try {
                delegate.setJMSTimestamp(timestamp);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
            try {
                return delegate.getJMSCorrelationIDAsBytes();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setJMSCorrelationIDAsBytes(byte[] correlationID) throws JMSException {
            try {
                delegate.setJMSCorrelationIDAsBytes(correlationID);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setJMSCorrelationID(String correlationID) throws JMSException {
            try {
                delegate.setJMSCorrelationID(correlationID);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public String getJMSCorrelationID() throws JMSException {
            try {
                return delegate.getJMSCorrelationID();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.Destination getJMSReplyTo() throws JMSException {
            try {
                javax.jms.Destination javaxDest = delegate.getJMSReplyTo();
                if (javaxDest == null) return null;

                if (javaxDest instanceof javax.jms.Queue) {
                    return new DestinationWrappers.QueueWrapper((javax.jms.Queue) javaxDest);
                } else if (javaxDest instanceof javax.jms.Topic) {
                    return new DestinationWrappers.TopicWrapper((javax.jms.Topic) javaxDest);
                } else {
                    return new DestinationWrappers.DestinationWrapper(javaxDest);
                }
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setJMSReplyTo(jakarta.jms.Destination replyTo) throws JMSException {
            try {
                if (replyTo != null) {
                    javax.jms.Destination javaxDest = ((DestinationWrappers.DestinationWrapper) replyTo).getDelegate();
                    delegate.setJMSReplyTo(javaxDest);
                } else {
                    delegate.setJMSReplyTo(null);
                }
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public jakarta.jms.Destination getJMSDestination() throws JMSException {
            try {
                javax.jms.Destination javaxDest = delegate.getJMSDestination();
                if (javaxDest == null) return null;

                if (javaxDest instanceof javax.jms.Queue) {
                    return new DestinationWrappers.QueueWrapper((javax.jms.Queue) javaxDest);
                } else if (javaxDest instanceof javax.jms.Topic) {
                    return new DestinationWrappers.TopicWrapper((javax.jms.Topic) javaxDest);
                } else {
                    return new DestinationWrappers.DestinationWrapper(javaxDest);
                }
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setJMSDestination(jakarta.jms.Destination destination) throws JMSException {
            try {
                if (destination != null) {
                    javax.jms.Destination javaxDest = ((DestinationWrappers.DestinationWrapper) destination).getDelegate();
                    delegate.setJMSDestination(javaxDest);
                } else {
                    delegate.setJMSDestination(null);
                }
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int getJMSDeliveryMode() throws JMSException {
            try {
                return delegate.getJMSDeliveryMode();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setJMSDeliveryMode(int deliveryMode) throws JMSException {
            try {
                delegate.setJMSDeliveryMode(deliveryMode);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public boolean getJMSRedelivered() throws JMSException {
            try {
                return delegate.getJMSRedelivered();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setJMSRedelivered(boolean redelivered) throws JMSException {
            try {
                delegate.setJMSRedelivered(redelivered);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public String getJMSType() throws JMSException {
            try {
                return delegate.getJMSType();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setJMSType(String type) throws JMSException {
            try {
                delegate.setJMSType(type);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public long getJMSExpiration() throws JMSException {
            try {
                return delegate.getJMSExpiration();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setJMSExpiration(long expiration) throws JMSException {
            try {
                delegate.setJMSExpiration(expiration);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public long getJMSDeliveryTime() throws JMSException {
            try {
                return delegate.getJMSDeliveryTime();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setJMSDeliveryTime(long deliveryTime) throws JMSException {
            try {
                delegate.setJMSDeliveryTime(deliveryTime);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int getJMSPriority() throws JMSException {
            try {
                return delegate.getJMSPriority();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setJMSPriority(int priority) throws JMSException {
            try {
                delegate.setJMSPriority(priority);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void clearProperties() throws JMSException {
            try {
                delegate.clearProperties();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public boolean propertyExists(String name) throws JMSException {
            try {
                return delegate.propertyExists(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public boolean getBooleanProperty(String name) throws JMSException {
            try {
                return delegate.getBooleanProperty(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public byte getByteProperty(String name) throws JMSException {
            try {
                return delegate.getByteProperty(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public short getShortProperty(String name) throws JMSException {
            try {
                return delegate.getShortProperty(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int getIntProperty(String name) throws JMSException {
            try {
                return delegate.getIntProperty(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public long getLongProperty(String name) throws JMSException {
            try {
                return delegate.getLongProperty(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public float getFloatProperty(String name) throws JMSException {
            try {
                return delegate.getFloatProperty(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public double getDoubleProperty(String name) throws JMSException {
            try {
                return delegate.getDoubleProperty(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public String getStringProperty(String name) throws JMSException {
            try {
                return delegate.getStringProperty(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public Object getObjectProperty(String name) throws JMSException {
            try {
                return delegate.getObjectProperty(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public Enumeration getPropertyNames() throws JMSException {
            try {
                return delegate.getPropertyNames();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setBooleanProperty(String name, boolean value) throws JMSException {
            try {
                delegate.setBooleanProperty(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setByteProperty(String name, byte value) throws JMSException {
            try {
                delegate.setByteProperty(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setShortProperty(String name, short value) throws JMSException {
            try {
                delegate.setShortProperty(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setIntProperty(String name, int value) throws JMSException {
            try {
                delegate.setIntProperty(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setLongProperty(String name, long value) throws JMSException {
            try {
                delegate.setLongProperty(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setFloatProperty(String name, float value) throws JMSException {
            try {
                delegate.setFloatProperty(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setDoubleProperty(String name, double value) throws JMSException {
            try {
                delegate.setDoubleProperty(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setStringProperty(String name, String value) throws JMSException {
            try {
                delegate.setStringProperty(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setObjectProperty(String name, Object value) throws JMSException {
            try {
                delegate.setObjectProperty(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void acknowledge() throws JMSException {
            try {
                delegate.acknowledge();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void clearBody() throws JMSException {
            try {
                delegate.clearBody();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public <T> T getBody(Class<T> c) throws JMSException {
            try {
                return delegate.getBody(c);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public boolean isBodyAssignableTo(Class c) throws JMSException {
            try {
                return delegate.isBodyAssignableTo(c);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }
    }

    /**
     * Wrapper for TextMessage
     */
    public static class TextMessageWrapper extends MessageWrapper implements jakarta.jms.TextMessage {
        private final javax.jms.TextMessage textDelegate;

        public TextMessageWrapper(javax.jms.TextMessage delegate) {
            super(delegate);
            this.textDelegate = delegate;
        }

        @Override
        public void setText(String string) throws JMSException {
            try {
                textDelegate.setText(string);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public String getText() throws JMSException {
            try {
                return textDelegate.getText();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }
    }

    /**
     * Wrapper for BytesMessage
     */
    public static class BytesMessageWrapper extends MessageWrapper implements jakarta.jms.BytesMessage {
        private final javax.jms.BytesMessage bytesDelegate;

        public BytesMessageWrapper(javax.jms.BytesMessage delegate) {
            super(delegate);
            this.bytesDelegate = delegate;
        }

        @Override
        public long getBodyLength() throws JMSException {
            try {
                return bytesDelegate.getBodyLength();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public boolean readBoolean() throws JMSException {
            try {
                return bytesDelegate.readBoolean();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public byte readByte() throws JMSException {
            try {
                return bytesDelegate.readByte();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int readUnsignedByte() throws JMSException {
            try {
                return bytesDelegate.readUnsignedByte();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public short readShort() throws JMSException {
            try {
                return bytesDelegate.readShort();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int readUnsignedShort() throws JMSException {
            try {
                return bytesDelegate.readUnsignedShort();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public char readChar() throws JMSException {
            try {
                return bytesDelegate.readChar();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int readInt() throws JMSException {
            try {
                return bytesDelegate.readInt();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public long readLong() throws JMSException {
            try {
                return bytesDelegate.readLong();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public float readFloat() throws JMSException {
            try {
                return bytesDelegate.readFloat();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public double readDouble() throws JMSException {
            try {
                return bytesDelegate.readDouble();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public String readUTF() throws JMSException {
            try {
                return bytesDelegate.readUTF();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int readBytes(byte[] value) throws JMSException {
            try {
                return bytesDelegate.readBytes(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int readBytes(byte[] value, int length) throws JMSException {
            try {
                return bytesDelegate.readBytes(value, length);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeBoolean(boolean value) throws JMSException {
            try {
                bytesDelegate.writeBoolean(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeByte(byte value) throws JMSException {
            try {
                bytesDelegate.writeByte(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeShort(short value) throws JMSException {
            try {
                bytesDelegate.writeShort(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeChar(char value) throws JMSException {
            try {
                bytesDelegate.writeChar(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeInt(int value) throws JMSException {
            try {
                bytesDelegate.writeInt(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeLong(long value) throws JMSException {
            try {
                bytesDelegate.writeLong(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeFloat(float value) throws JMSException {
            try {
                bytesDelegate.writeFloat(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeDouble(double value) throws JMSException {
            try {
                bytesDelegate.writeDouble(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeUTF(String value) throws JMSException {
            try {
                bytesDelegate.writeUTF(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeBytes(byte[] value) throws JMSException {
            try {
                bytesDelegate.writeBytes(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeBytes(byte[] value, int offset, int length) throws JMSException {
            try {
                bytesDelegate.writeBytes(value, offset, length);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeObject(Object value) throws JMSException {
            try {
                bytesDelegate.writeObject(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void reset() throws JMSException {
            try {
                bytesDelegate.reset();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }
    }

    /**
     * Wrapper for ObjectMessage
     */
    public static class ObjectMessageWrapper extends MessageWrapper implements jakarta.jms.ObjectMessage {
        private final javax.jms.ObjectMessage objectDelegate;

        public ObjectMessageWrapper(javax.jms.ObjectMessage delegate) {
            super(delegate);
            this.objectDelegate = delegate;
        }

        @Override
        public void setObject(java.io.Serializable object) throws JMSException {
            try {
                objectDelegate.setObject(object);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public java.io.Serializable getObject() throws JMSException {
            try {
                return objectDelegate.getObject();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }
    }

    /**
     * Wrapper for MapMessage
     */
    public static class MapMessageWrapper extends MessageWrapper implements jakarta.jms.MapMessage {
        private final javax.jms.MapMessage mapDelegate;

        public MapMessageWrapper(javax.jms.MapMessage delegate) {
            super(delegate);
            this.mapDelegate = delegate;
        }

        @Override
        public boolean getBoolean(String name) throws JMSException {
            try {
                return mapDelegate.getBoolean(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public byte getByte(String name) throws JMSException {
            try {
                return mapDelegate.getByte(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public short getShort(String name) throws JMSException {
            try {
                return mapDelegate.getShort(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public char getChar(String name) throws JMSException {
            try {
                return mapDelegate.getChar(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int getInt(String name) throws JMSException {
            try {
                return mapDelegate.getInt(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public long getLong(String name) throws JMSException {
            try {
                return mapDelegate.getLong(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public float getFloat(String name) throws JMSException {
            try {
                return mapDelegate.getFloat(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public double getDouble(String name) throws JMSException {
            try {
                return mapDelegate.getDouble(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public String getString(String name) throws JMSException {
            try {
                return mapDelegate.getString(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public byte[] getBytes(String name) throws JMSException {
            try {
                return mapDelegate.getBytes(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public Object getObject(String name) throws JMSException {
            try {
                return mapDelegate.getObject(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public Enumeration getMapNames() throws JMSException {
            try {
                return mapDelegate.getMapNames();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setBoolean(String name, boolean value) throws JMSException {
            try {
                mapDelegate.setBoolean(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setByte(String name, byte value) throws JMSException {
            try {
                mapDelegate.setByte(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setShort(String name, short value) throws JMSException {
            try {
                mapDelegate.setShort(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setChar(String name, char value) throws JMSException {
            try {
                mapDelegate.setChar(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setInt(String name, int value) throws JMSException {
            try {
                mapDelegate.setInt(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setLong(String name, long value) throws JMSException {
            try {
                mapDelegate.setLong(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setFloat(String name, float value) throws JMSException {
            try {
                mapDelegate.setFloat(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setDouble(String name, double value) throws JMSException {
            try {
                mapDelegate.setDouble(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setString(String name, String value) throws JMSException {
            try {
                mapDelegate.setString(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setBytes(String name, byte[] value) throws JMSException {
            try {
                mapDelegate.setBytes(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setBytes(String name, byte[] value, int offset, int length) throws JMSException {
            try {
                mapDelegate.setBytes(name, value, offset, length);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void setObject(String name, Object value) throws JMSException {
            try {
                mapDelegate.setObject(name, value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public boolean itemExists(String name) throws JMSException {
            try {
                return mapDelegate.itemExists(name);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }
    }

    /**
     * Wrapper for StreamMessage
     */
    public static class StreamMessageWrapper extends MessageWrapper implements jakarta.jms.StreamMessage {
        private final javax.jms.StreamMessage streamDelegate;

        public StreamMessageWrapper(javax.jms.StreamMessage delegate) {
            super(delegate);
            this.streamDelegate = delegate;
        }

        @Override
        public boolean readBoolean() throws JMSException {
            try {
                return streamDelegate.readBoolean();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public byte readByte() throws JMSException {
            try {
                return streamDelegate.readByte();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public short readShort() throws JMSException {
            try {
                return streamDelegate.readShort();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public char readChar() throws JMSException {
            try {
                return streamDelegate.readChar();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int readInt() throws JMSException {
            try {
                return streamDelegate.readInt();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public long readLong() throws JMSException {
            try {
                return streamDelegate.readLong();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public float readFloat() throws JMSException {
            try {
                return streamDelegate.readFloat();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public double readDouble() throws JMSException {
            try {
                return streamDelegate.readDouble();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public String readString() throws JMSException {
            try {
                return streamDelegate.readString();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public int readBytes(byte[] value) throws JMSException {
            try {
                return streamDelegate.readBytes(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public Object readObject() throws JMSException {
            try {
                return streamDelegate.readObject();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeBoolean(boolean value) throws JMSException {
            try {
                streamDelegate.writeBoolean(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeByte(byte value) throws JMSException {
            try {
                streamDelegate.writeByte(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeShort(short value) throws JMSException {
            try {
                streamDelegate.writeShort(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeChar(char value) throws JMSException {
            try {
                streamDelegate.writeChar(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeInt(int value) throws JMSException {
            try {
                streamDelegate.writeInt(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeLong(long value) throws JMSException {
            try {
                streamDelegate.writeLong(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeFloat(float value) throws JMSException {
            try {
                streamDelegate.writeFloat(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeDouble(double value) throws JMSException {
            try {
                streamDelegate.writeDouble(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeString(String value) throws JMSException {
            try {
                streamDelegate.writeString(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeBytes(byte[] value) throws JMSException {
            try {
                streamDelegate.writeBytes(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeBytes(byte[] value, int offset, int length) throws JMSException {
            try {
                streamDelegate.writeBytes(value, offset, length);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void writeObject(Object value) throws JMSException {
            try {
                streamDelegate.writeObject(value);
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }

        @Override
        public void reset() throws JMSException {
            try {
                streamDelegate.reset();
            } catch (javax.jms.JMSException e) {
                throw new JMSException(e.getMessage());
            }
        }
    }
}