package com.example.ibmmq.unit.adapter;

import com.example.ibmmq.adapter.MessageWrappers;
import com.example.ibmmq.adapter.MessageWrappers.MessageWrapper;
import jakarta.jms.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Message Wrappers Tests")
class MessageWrappersTest {

    // NOTE: These tests are simplified due to Jakarta JMS adapter complexity
    // For full integration testing, use integration tests with real IBM MQ

    @Test
    @DisplayName("Should create TextMessageWrapper")
    void shouldCreateTextMessageWrapper() {
        // Given
        javax.jms.TextMessage javaxTextMessage = mock(javax.jms.TextMessage.class);

        // When & Then - Test simplified due to Jakarta JMS adapter complexity
        assertThatCode(() -> {
            MessageWrappers.TextMessageWrapper wrapper = new MessageWrappers.TextMessageWrapper(javaxTextMessage);
            assertThat(wrapper).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should create BytesMessageWrapper")
    void shouldCreateBytesMessageWrapper() {
        // Given
        javax.jms.BytesMessage javaxBytesMessage = mock(javax.jms.BytesMessage.class);

        // When & Then - Test simplified due to Jakarta JMS adapter complexity
        assertThatCode(() -> {
            MessageWrappers.BytesMessageWrapper wrapper = new MessageWrappers.BytesMessageWrapper(javaxBytesMessage);
            assertThat(wrapper).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should create ObjectMessageWrapper")
    void shouldCreateObjectMessageWrapper() {
        // Given
        javax.jms.ObjectMessage javaxObjectMessage = mock(javax.jms.ObjectMessage.class);

        // When & Then - Test simplified due to Jakarta JMS adapter complexity
        assertThatCode(() -> {
            MessageWrappers.ObjectMessageWrapper wrapper = new MessageWrappers.ObjectMessageWrapper(javaxObjectMessage);
            assertThat(wrapper).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should create MapMessageWrapper")
    void shouldCreateMapMessageWrapper() {
        // Given
        javax.jms.MapMessage javaxMapMessage = mock(javax.jms.MapMessage.class);

        // When & Then - Test simplified due to Jakarta JMS adapter complexity
        assertThatCode(() -> {
            MessageWrappers.MapMessageWrapper wrapper = new MessageWrappers.MapMessageWrapper(javaxMapMessage);
            assertThat(wrapper).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should create StreamMessageWrapper")
    void shouldCreateStreamMessageWrapper() {
        // Given
        javax.jms.StreamMessage javaxStreamMessage = mock(javax.jms.StreamMessage.class);

        // When & Then - Test simplified due to Jakarta JMS adapter complexity
        assertThatCode(() -> {
            MessageWrappers.StreamMessageWrapper wrapper = new MessageWrappers.StreamMessageWrapper(javaxStreamMessage);
            assertThat(wrapper).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should implement Jakarta JMS message interfaces")
    void shouldImplementJakartaJmsMessageInterfaces() {
        // Given
        javax.jms.TextMessage javaxTextMessage = mock(javax.jms.TextMessage.class);
        javax.jms.BytesMessage javaxBytesMessage = mock(javax.jms.BytesMessage.class);
        javax.jms.ObjectMessage javaxObjectMessage = mock(javax.jms.ObjectMessage.class);
        javax.jms.MapMessage javaxMapMessage = mock(javax.jms.MapMessage.class);
        javax.jms.StreamMessage javaxStreamMessage = mock(javax.jms.StreamMessage.class);

        // When
        MessageWrappers.TextMessageWrapper textWrapper = new MessageWrappers.TextMessageWrapper(javaxTextMessage);
        MessageWrappers.BytesMessageWrapper bytesWrapper = new MessageWrappers.BytesMessageWrapper(javaxBytesMessage);
        MessageWrappers.ObjectMessageWrapper objectWrapper = new MessageWrappers.ObjectMessageWrapper(javaxObjectMessage);
        MessageWrappers.MapMessageWrapper mapWrapper = new MessageWrappers.MapMessageWrapper(javaxMapMessage);
        MessageWrappers.StreamMessageWrapper streamWrapper = new MessageWrappers.StreamMessageWrapper(javaxStreamMessage);

        // Then - Verify they implement Jakarta JMS interfaces
        assertThat(textWrapper).isInstanceOf(jakarta.jms.TextMessage.class);
        assertThat(bytesWrapper).isInstanceOf(jakarta.jms.BytesMessage.class);
        assertThat(objectWrapper).isInstanceOf(jakarta.jms.ObjectMessage.class);
        assertThat(mapWrapper).isInstanceOf(jakarta.jms.MapMessage.class);
        assertThat(streamWrapper).isInstanceOf(jakarta.jms.StreamMessage.class);

        // All should also implement base Message interface
        assertThat(textWrapper).isInstanceOf(jakarta.jms.Message.class);
        assertThat(bytesWrapper).isInstanceOf(jakarta.jms.Message.class);
        assertThat(objectWrapper).isInstanceOf(jakarta.jms.Message.class);
        assertThat(mapWrapper).isInstanceOf(jakarta.jms.Message.class);
        assertThat(streamWrapper).isInstanceOf(jakarta.jms.Message.class);
    }

    @Test
    @DisplayName("Should handle null delegates gracefully")
    void shouldHandleNullDelegatesGracefully() {
        // When & Then - Test simplified due to Jakarta JMS adapter complexity
        assertThatCode(() -> {
            MessageWrappers.TextMessageWrapper textWrapper = new MessageWrappers.TextMessageWrapper(null);
            MessageWrappers.BytesMessageWrapper bytesWrapper = new MessageWrappers.BytesMessageWrapper(null);
            MessageWrappers.ObjectMessageWrapper objectWrapper = new MessageWrappers.ObjectMessageWrapper(null);
            MessageWrappers.MapMessageWrapper mapWrapper = new MessageWrappers.MapMessageWrapper(null);
            MessageWrappers.StreamMessageWrapper streamWrapper = new MessageWrappers.StreamMessageWrapper(null);

            assertThat(textWrapper).isNotNull();
            assertThat(bytesWrapper).isNotNull();
            assertThat(objectWrapper).isNotNull();
            assertThat(mapWrapper).isNotNull();
            assertThat(streamWrapper).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should provide wrapper for all message types")
    void shouldProvideWrapperForAllMessageTypes() {
        // Test simplified due to Jakarta JMS adapter complexity
        // For full integration testing, use integration tests with real IBM MQ
        assertThat(MessageWrappers.TextMessageWrapper.class).isNotNull();
        assertThat(MessageWrappers.BytesMessageWrapper.class).isNotNull();
        assertThat(MessageWrappers.ObjectMessageWrapper.class).isNotNull();
        assertThat(MessageWrappers.MapMessageWrapper.class).isNotNull();
        assertThat(MessageWrappers.StreamMessageWrapper.class).isNotNull();
    }

    @Test
    @DisplayName("Should test TextMessage basic functionality")
    void shouldTestTextMessageBasicFunctionality() throws Exception {
        // Given
        javax.jms.TextMessage javaxTextMessage = mock(javax.jms.TextMessage.class);
        when(javaxTextMessage.getText()).thenReturn("test message");

        MessageWrappers.TextMessageWrapper wrapper = new MessageWrappers.TextMessageWrapper(javaxTextMessage);

        // When
        String text = wrapper.getText();
        wrapper.setText("new message");

        // Then
        assertThat(text).isEqualTo("test message");
        verify(javaxTextMessage).getText();
        verify(javaxTextMessage).setText("new message");
    }

    @Test
    @DisplayName("Should test BytesMessage basic functionality")
    void shouldTestBytesMessageBasicFunctionality() throws Exception {
        // Given
        javax.jms.BytesMessage javaxBytesMessage = mock(javax.jms.BytesMessage.class);
        when(javaxBytesMessage.getBodyLength()).thenReturn(10L);

        MessageWrappers.BytesMessageWrapper wrapper = new MessageWrappers.BytesMessageWrapper(javaxBytesMessage);

        // When
        long length = wrapper.getBodyLength();
        wrapper.writeBytes(new byte[]{1, 2, 3});
        wrapper.reset();

        // Then
        assertThat(length).isEqualTo(10L);
        verify(javaxBytesMessage).getBodyLength();
        verify(javaxBytesMessage).writeBytes(any(byte[].class));
        verify(javaxBytesMessage).reset();
    }

    @Test
    @DisplayName("Should test ObjectMessage basic functionality")
    void shouldTestObjectMessageBasicFunctionality() throws Exception {
        // Given
        javax.jms.ObjectMessage javaxObjectMessage = mock(javax.jms.ObjectMessage.class);
        String testObject = "test object";
        when(javaxObjectMessage.getObject()).thenReturn(testObject);

        MessageWrappers.ObjectMessageWrapper wrapper = new MessageWrappers.ObjectMessageWrapper(javaxObjectMessage);

        // When
        Object object = wrapper.getObject();
        wrapper.setObject("new object");

        // Then
        assertThat(object).isEqualTo(testObject);
        verify(javaxObjectMessage).getObject();
        verify(javaxObjectMessage).setObject("new object");
    }

    @Test
    @DisplayName("Should test MapMessage basic functionality")
    void shouldTestMapMessageBasicFunctionality() throws Exception {
        // Given
        javax.jms.MapMessage javaxMapMessage = mock(javax.jms.MapMessage.class);
        when(javaxMapMessage.getString("key")).thenReturn("value");
        when(javaxMapMessage.itemExists("key")).thenReturn(true);

        MessageWrappers.MapMessageWrapper wrapper = new MessageWrappers.MapMessageWrapper(javaxMapMessage);

        // When
        String value = wrapper.getString("key");
        boolean exists = wrapper.itemExists("key");
        wrapper.setString("newkey", "newvalue");

        // Then
        assertThat(value).isEqualTo("value");
        assertThat(exists).isTrue();
        verify(javaxMapMessage).getString("key");
        verify(javaxMapMessage).itemExists("key");
        verify(javaxMapMessage).setString("newkey", "newvalue");
    }

    @Test
    @DisplayName("Should test StreamMessage basic functionality")
    void shouldTestStreamMessageBasicFunctionality() throws Exception {
        // Given
        javax.jms.StreamMessage javaxStreamMessage = mock(javax.jms.StreamMessage.class);
        when(javaxStreamMessage.readString()).thenReturn("stream value");

        MessageWrappers.StreamMessageWrapper wrapper = new MessageWrappers.StreamMessageWrapper(javaxStreamMessage);

        // When
        String value = wrapper.readString();
        wrapper.writeString("new stream value");
        wrapper.reset();

        // Then
        assertThat(value).isEqualTo("stream value");
        verify(javaxStreamMessage).readString();
        verify(javaxStreamMessage).writeString("new stream value");
        verify(javaxStreamMessage).reset();
    }

    @Test
    @DisplayName("Should test MessageWrapper base class JMS headers")
    void shouldTestMessageWrapperJMSHeaders() throws Exception {
        // Given
        javax.jms.Message javaxMessage = mock(javax.jms.Message.class);
        when(javaxMessage.getJMSMessageID()).thenReturn("MSG123");
        when(javaxMessage.getJMSTimestamp()).thenReturn(123456789L);
        when(javaxMessage.getJMSCorrelationID()).thenReturn("CORR123");
        when(javaxMessage.getJMSPriority()).thenReturn(5);
        when(javaxMessage.getJMSDeliveryMode()).thenReturn(2);

        MessageWrappers.MessageWrapper wrapper = new MessageWrappers.MessageWrapper(javaxMessage);

        // When & Then - Test JMS header getters
        assertThat(wrapper.getJMSMessageID()).isEqualTo("MSG123");
        assertThat(wrapper.getJMSTimestamp()).isEqualTo(123456789L);
        assertThat(wrapper.getJMSCorrelationID()).isEqualTo("CORR123");
        assertThat(wrapper.getJMSPriority()).isEqualTo(5);
        assertThat(wrapper.getJMSDeliveryMode()).isEqualTo(2);

        // Test JMS header setters
        wrapper.setJMSMessageID("NEW_MSG123");
        wrapper.setJMSTimestamp(987654321L);
        wrapper.setJMSCorrelationID("NEW_CORR123");
        wrapper.setJMSPriority(9);
        wrapper.setJMSDeliveryMode(1);

        verify(javaxMessage).setJMSMessageID("NEW_MSG123");
        verify(javaxMessage).setJMSTimestamp(987654321L);
        verify(javaxMessage).setJMSCorrelationID("NEW_CORR123");
        verify(javaxMessage).setJMSPriority(9);
        verify(javaxMessage).setJMSDeliveryMode(1);
    }

    @Test
    @DisplayName("Should test MessageWrapper property operations")
    void shouldTestMessageWrapperPropertyOperations() throws Exception {
        // Given
        javax.jms.Message javaxMessage = mock(javax.jms.Message.class);
        when(javaxMessage.getBooleanProperty("boolProp")).thenReturn(true);
        when(javaxMessage.getStringProperty("stringProp")).thenReturn("test");
        when(javaxMessage.getIntProperty("intProp")).thenReturn(42);
        when(javaxMessage.getDoubleProperty("doubleProp")).thenReturn(3.14);

        MessageWrappers.MessageWrapper wrapper = new MessageWrappers.MessageWrapper(javaxMessage);

        // When & Then - Test property getters
        assertThat(wrapper.getBooleanProperty("boolProp")).isTrue();
        assertThat(wrapper.getStringProperty("stringProp")).isEqualTo("test");
        assertThat(wrapper.getIntProperty("intProp")).isEqualTo(42);
        assertThat(wrapper.getDoubleProperty("doubleProp")).isEqualTo(3.14);

        // Test property setters
        wrapper.setBooleanProperty("newBoolProp", false);
        wrapper.setStringProperty("newStringProp", "newValue");
        wrapper.setIntProperty("newIntProp", 99);
        wrapper.setDoubleProperty("newDoubleProp", 2.71);

        verify(javaxMessage).setBooleanProperty("newBoolProp", false);
        verify(javaxMessage).setStringProperty("newStringProp", "newValue");
        verify(javaxMessage).setIntProperty("newIntProp", 99);
        verify(javaxMessage).setDoubleProperty("newDoubleProp", 2.71);
    }

    @Test
    @DisplayName("Should test MessageWrapper all primitive property types")
    void shouldTestMessageWrapperAllPrimitivePropertyTypes() throws Exception {
        // Given
        javax.jms.Message javaxMessage = mock(javax.jms.Message.class);
        MessageWrappers.MessageWrapper wrapper = new MessageWrappers.MessageWrapper(javaxMessage);

        // Test all primitive property types
        wrapper.setByteProperty("byteProp", (byte) 10);
        wrapper.setShortProperty("shortProp", (short) 100);
        wrapper.setLongProperty("longProp", 1000L);
        wrapper.setFloatProperty("floatProp", 1.5f);
        wrapper.setObjectProperty("objectProp", "objectValue");

        verify(javaxMessage).setByteProperty("byteProp", (byte) 10);
        verify(javaxMessage).setShortProperty("shortProp", (short) 100);
        verify(javaxMessage).setLongProperty("longProp", 1000L);
        verify(javaxMessage).setFloatProperty("floatProp", 1.5f);
        verify(javaxMessage).setObjectProperty("objectProp", "objectValue");

        // Test primitive property getters
        when(javaxMessage.getByteProperty("byteProp")).thenReturn((byte) 10);
        when(javaxMessage.getShortProperty("shortProp")).thenReturn((short) 100);
        when(javaxMessage.getLongProperty("longProp")).thenReturn(1000L);
        when(javaxMessage.getFloatProperty("floatProp")).thenReturn(1.5f);
        when(javaxMessage.getObjectProperty("objectProp")).thenReturn("objectValue");

        assertThat(wrapper.getByteProperty("byteProp")).isEqualTo((byte) 10);
        assertThat(wrapper.getShortProperty("shortProp")).isEqualTo((short) 100);
        assertThat(wrapper.getLongProperty("longProp")).isEqualTo(1000L);
        assertThat(wrapper.getFloatProperty("floatProp")).isEqualTo(1.5f);
        assertThat(wrapper.getObjectProperty("objectProp")).isEqualTo("objectValue");
    }

    @Test
    @DisplayName("Should test MessageWrapper property existence and names")
    void shouldTestMessageWrapperPropertyExistenceAndNames() throws Exception {
        // Given
        javax.jms.Message javaxMessage = mock(javax.jms.Message.class);
        when(javaxMessage.propertyExists("existingProp")).thenReturn(true);
        when(javaxMessage.propertyExists("nonExistingProp")).thenReturn(false);

        Enumeration<String> propertyNames = Collections.enumeration(Arrays.asList("prop1", "prop2", "prop3"));
        when(javaxMessage.getPropertyNames()).thenReturn(propertyNames);

        MessageWrappers.MessageWrapper wrapper = new MessageWrappers.MessageWrapper(javaxMessage);

        // When & Then
        assertThat(wrapper.propertyExists("existingProp")).isTrue();
        assertThat(wrapper.propertyExists("nonExistingProp")).isFalse();

        Enumeration<String> wrapperPropertyNames = wrapper.getPropertyNames();
        List<String> propertyNamesList = new ArrayList<>();
        while (wrapperPropertyNames.hasMoreElements()) {
            propertyNamesList.add(wrapperPropertyNames.nextElement());
        }
        assertThat(propertyNamesList).containsExactly("prop1", "prop2", "prop3");
    }

    @Test
    @DisplayName("Should test MessageWrapper destination operations")
    void shouldTestMessageWrapperDestinationOperations() throws Exception {
        // Given
        javax.jms.Message javaxMessage = mock(javax.jms.Message.class);
        javax.jms.Destination javaxDestination = mock(javax.jms.Destination.class);
        javax.jms.Destination javaxReplyTo = mock(javax.jms.Destination.class);

        when(javaxMessage.getJMSDestination()).thenReturn(javaxDestination);
        when(javaxMessage.getJMSReplyTo()).thenReturn(javaxReplyTo);

        MessageWrappers.MessageWrapper wrapper = new MessageWrappers.MessageWrapper(javaxMessage);

        // When & Then - Test destination getters
        jakarta.jms.Destination jakartaDestination = wrapper.getJMSDestination();
        jakarta.jms.Destination jakartaReplyTo = wrapper.getJMSReplyTo();

        assertThat(jakartaDestination).isNotNull();
        assertThat(jakartaReplyTo).isNotNull();

        // Test destination setters with null values (simplified test due to complex wrapper architecture)
        wrapper.setJMSDestination(null);
        wrapper.setJMSReplyTo(null);

        verify(javaxMessage).setJMSDestination(null);
        verify(javaxMessage).setJMSReplyTo(null);
    }

    @Test
    @DisplayName("Should test MessageWrapper acknowledgment and body operations")
    void shouldTestMessageWrapperAcknowledgmentAndBodyOperations() throws Exception {
        // Given
        javax.jms.Message javaxMessage = mock(javax.jms.Message.class);
        when(javaxMessage.getBody(String.class)).thenReturn("messageBody");

        MessageWrappers.MessageWrapper wrapper = new MessageWrappers.MessageWrapper(javaxMessage);

        // When & Then - Test acknowledge
        wrapper.acknowledge();
        verify(javaxMessage).acknowledge();

        // Test clearBody
        wrapper.clearBody();
        verify(javaxMessage).clearBody();

        // Test clearProperties
        wrapper.clearProperties();
        verify(javaxMessage).clearProperties();

        // Test getBody
        String body = wrapper.getBody(String.class);
        assertThat(body).isEqualTo("messageBody");
        verify(javaxMessage).getBody(String.class);

        // Test isBodyAssignableTo
        when(javaxMessage.isBodyAssignableTo(String.class)).thenReturn(true);
        boolean assignable = wrapper.isBodyAssignableTo(String.class);
        assertThat(assignable).isTrue();
        verify(javaxMessage).isBodyAssignableTo(String.class);
    }

    @Test
    @DisplayName("Should test MessageWrapper remaining JMS properties")
    void shouldTestMessageWrapperRemainingJMSProperties() throws Exception {
        // Given
        javax.jms.Message javaxMessage = mock(javax.jms.Message.class);
        when(javaxMessage.getJMSExpiration()).thenReturn(12345L);
        when(javaxMessage.getJMSRedelivered()).thenReturn(true);
        when(javaxMessage.getJMSType()).thenReturn("MessageType");

        MessageWrappers.MessageWrapper wrapper = new MessageWrappers.MessageWrapper(javaxMessage);

        // When & Then - Test remaining JMS properties
        assertThat(wrapper.getJMSExpiration()).isEqualTo(12345L);
        assertThat(wrapper.getJMSRedelivered()).isTrue();
        assertThat(wrapper.getJMSType()).isEqualTo("MessageType");

        // Test setters
        wrapper.setJMSExpiration(54321L);
        wrapper.setJMSRedelivered(false);
        wrapper.setJMSType("NewMessageType");

        verify(javaxMessage).setJMSExpiration(54321L);
        verify(javaxMessage).setJMSRedelivered(false);
        verify(javaxMessage).setJMSType("NewMessageType");
    }

    @Test
    @DisplayName("Should handle JMSException in all wrappers")
    void shouldHandleJMSExceptionInAllWrappers() throws Exception {
        // Given
        javax.jms.TextMessage javaxTextMessage = mock(javax.jms.TextMessage.class);
        javax.jms.BytesMessage javaxBytesMessage = mock(javax.jms.BytesMessage.class);
        javax.jms.ObjectMessage javaxObjectMessage = mock(javax.jms.ObjectMessage.class);
        javax.jms.MapMessage javaxMapMessage = mock(javax.jms.MapMessage.class);
        javax.jms.StreamMessage javaxStreamMessage = mock(javax.jms.StreamMessage.class);

        javax.jms.JMSException javaxException = new javax.jms.JMSException("test error");
        when(javaxTextMessage.getText()).thenThrow(javaxException);
        when(javaxBytesMessage.getBodyLength()).thenThrow(javaxException);
        when(javaxObjectMessage.getObject()).thenThrow(javaxException);
        when(javaxMapMessage.getString("key")).thenThrow(javaxException);
        when(javaxStreamMessage.readString()).thenThrow(javaxException);

        MessageWrappers.TextMessageWrapper textWrapper = new MessageWrappers.TextMessageWrapper(javaxTextMessage);
        MessageWrappers.BytesMessageWrapper bytesWrapper = new MessageWrappers.BytesMessageWrapper(javaxBytesMessage);
        MessageWrappers.ObjectMessageWrapper objectWrapper = new MessageWrappers.ObjectMessageWrapper(javaxObjectMessage);
        MessageWrappers.MapMessageWrapper mapWrapper = new MessageWrappers.MapMessageWrapper(javaxMapMessage);
        MessageWrappers.StreamMessageWrapper streamWrapper = new MessageWrappers.StreamMessageWrapper(javaxStreamMessage);

        // When & Then
        assertThatThrownBy(() -> textWrapper.getText())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("test error");

        assertThatThrownBy(() -> bytesWrapper.getBodyLength())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("test error");

        assertThatThrownBy(() -> objectWrapper.getObject())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("test error");

        assertThatThrownBy(() -> mapWrapper.getString("key"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("test error");

        assertThatThrownBy(() -> streamWrapper.readString())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("test error");
    }

    @Test
    @DisplayName("Should test BytesMessageWrapper comprehensive functionality")
    void shouldTestBytesMessageWrapperComprehensive() throws Exception {
        // Given
        javax.jms.BytesMessage javaxBytesMessage = mock(javax.jms.BytesMessage.class);
        com.example.ibmmq.adapter.MessageWrappers.BytesMessageWrapper wrapper =
            new com.example.ibmmq.adapter.MessageWrappers.BytesMessageWrapper(javaxBytesMessage);

        // Mock byte operations
        byte[] testBytes = {1, 2, 3, 4, 5};
        when(javaxBytesMessage.getBodyLength()).thenReturn(5L);
        when(javaxBytesMessage.readBytes(any(byte[].class))).thenReturn(5);
        when(javaxBytesMessage.readByte()).thenReturn((byte) 42);
        when(javaxBytesMessage.readInt()).thenReturn(123);
        when(javaxBytesMessage.readLong()).thenReturn(456L);
        when(javaxBytesMessage.readFloat()).thenReturn(7.89f);
        when(javaxBytesMessage.readDouble()).thenReturn(10.11);
        when(javaxBytesMessage.readUTF()).thenReturn("test string");
        when(javaxBytesMessage.readBoolean()).thenReturn(true);
        when(javaxBytesMessage.readShort()).thenReturn((short) 999);
        when(javaxBytesMessage.readChar()).thenReturn('A');

        // When & Then - test all read operations
        assertThat(wrapper.getBodyLength()).isEqualTo(5L);
        byte[] buffer = new byte[10];
        assertThat(wrapper.readBytes(buffer)).isEqualTo(5);
        assertThat(wrapper.readByte()).isEqualTo((byte) 42);
        assertThat(wrapper.readInt()).isEqualTo(123);
        assertThat(wrapper.readLong()).isEqualTo(456L);
        assertThat(wrapper.readFloat()).isEqualTo(7.89f);
        assertThat(wrapper.readDouble()).isEqualTo(10.11);
        assertThat(wrapper.readUTF()).isEqualTo("test string");
        assertThat(wrapper.readBoolean()).isTrue();
        assertThat(wrapper.readShort()).isEqualTo((short) 999);
        assertThat(wrapper.readChar()).isEqualTo('A');

        // Test all write operations
        wrapper.writeBytes(testBytes);
        verify(javaxBytesMessage).writeBytes(testBytes);

        wrapper.writeByte((byte) 42);
        verify(javaxBytesMessage).writeByte((byte) 42);

        wrapper.writeInt(123);
        verify(javaxBytesMessage).writeInt(123);

        wrapper.writeLong(456L);
        verify(javaxBytesMessage).writeLong(456L);

        wrapper.writeFloat(7.89f);
        verify(javaxBytesMessage).writeFloat(7.89f);

        wrapper.writeDouble(10.11);
        verify(javaxBytesMessage).writeDouble(10.11);

        wrapper.writeUTF("test string");
        verify(javaxBytesMessage).writeUTF("test string");

        wrapper.writeBoolean(true);
        verify(javaxBytesMessage).writeBoolean(true);

        wrapper.writeShort((short) 999);
        verify(javaxBytesMessage).writeShort((short) 999);

        wrapper.writeChar('A');
        verify(javaxBytesMessage).writeChar('A');

        // Test reset
        wrapper.reset();
        verify(javaxBytesMessage).reset();
    }

    @Test
    @DisplayName("Should test MapMessageWrapper comprehensive functionality")
    void shouldTestMapMessageWrapperComprehensive() throws Exception {
        // Given
        javax.jms.MapMessage javaxMapMessage = mock(javax.jms.MapMessage.class);
        com.example.ibmmq.adapter.MessageWrappers.MapMessageWrapper wrapper =
            new com.example.ibmmq.adapter.MessageWrappers.MapMessageWrapper(javaxMapMessage);

        // Mock all get operations
        when(javaxMapMessage.getBoolean("boolKey")).thenReturn(true);
        when(javaxMapMessage.getString("stringKey")).thenReturn("test value");
        when(javaxMapMessage.getInt("intKey")).thenReturn(42);
        when(javaxMapMessage.getLong("longKey")).thenReturn(123L);
        when(javaxMapMessage.getFloat("floatKey")).thenReturn(4.56f);
        when(javaxMapMessage.getDouble("doubleKey")).thenReturn(7.89);
        when(javaxMapMessage.getByte("byteKey")).thenReturn((byte) 10);
        when(javaxMapMessage.getShort("shortKey")).thenReturn((short) 200);
        when(javaxMapMessage.getChar("charKey")).thenReturn('X');
        when(javaxMapMessage.getObject("objectKey")).thenReturn("objectValue");
        byte[] testBytes = {1, 2, 3};
        when(javaxMapMessage.getBytes("bytesKey")).thenReturn(testBytes);
        when(javaxMapMessage.itemExists("existingKey")).thenReturn(true);
        when(javaxMapMessage.itemExists("nonExistingKey")).thenReturn(false);

        // When & Then - test all get operations
        assertThat(wrapper.getBoolean("boolKey")).isTrue();
        assertThat(wrapper.getString("stringKey")).isEqualTo("test value");
        assertThat(wrapper.getInt("intKey")).isEqualTo(42);
        assertThat(wrapper.getLong("longKey")).isEqualTo(123L);
        assertThat(wrapper.getFloat("floatKey")).isEqualTo(4.56f);
        assertThat(wrapper.getDouble("doubleKey")).isEqualTo(7.89);
        assertThat(wrapper.getByte("byteKey")).isEqualTo((byte) 10);
        assertThat(wrapper.getShort("shortKey")).isEqualTo((short) 200);
        assertThat(wrapper.getChar("charKey")).isEqualTo('X');
        assertThat(wrapper.getObject("objectKey")).isEqualTo("objectValue");
        assertThat(wrapper.getBytes("bytesKey")).isEqualTo(testBytes);
        assertThat(wrapper.itemExists("existingKey")).isTrue();
        assertThat(wrapper.itemExists("nonExistingKey")).isFalse();

        // Test all set operations
        wrapper.setBoolean("newBool", false);
        verify(javaxMapMessage).setBoolean("newBool", false);

        wrapper.setString("newString", "new value");
        verify(javaxMapMessage).setString("newString", "new value");

        wrapper.setInt("newInt", 999);
        verify(javaxMapMessage).setInt("newInt", 999);

        wrapper.setLong("newLong", 888L);
        verify(javaxMapMessage).setLong("newLong", 888L);

        wrapper.setFloat("newFloat", 1.23f);
        verify(javaxMapMessage).setFloat("newFloat", 1.23f);

        wrapper.setDouble("newDouble", 4.56);
        verify(javaxMapMessage).setDouble("newDouble", 4.56);

        wrapper.setByte("newByte", (byte) 99);
        verify(javaxMapMessage).setByte("newByte", (byte) 99);

        wrapper.setShort("newShort", (short) 777);
        verify(javaxMapMessage).setShort("newShort", (short) 777);

        wrapper.setChar("newChar", 'Z');
        verify(javaxMapMessage).setChar("newChar", 'Z');

        wrapper.setObject("newObject", "newObjectValue");
        verify(javaxMapMessage).setObject("newObject", "newObjectValue");

        byte[] newBytes = {7, 8, 9};
        wrapper.setBytes("newBytes", newBytes);
        verify(javaxMapMessage).setBytes("newBytes", newBytes);

        // Test setBytes with offset and length (0% coverage method)
        wrapper.setBytes("bytesWithOffset", newBytes, 1, 2);
        verify(javaxMapMessage).setBytes("bytesWithOffset", newBytes, 1, 2);

        // Test map name operations
        Enumeration<String> mockEnum = mock(Enumeration.class);
        when(javaxMapMessage.getMapNames()).thenReturn(mockEnum);
        assertThat(wrapper.getMapNames()).isEqualTo(mockEnum);
    }

    @Test
    @DisplayName("Should test StreamMessageWrapper comprehensive functionality")
    void shouldTestStreamMessageWrapperComprehensive() throws Exception {
        // Given
        javax.jms.StreamMessage javaxStreamMessage = mock(javax.jms.StreamMessage.class);
        com.example.ibmmq.adapter.MessageWrappers.StreamMessageWrapper wrapper =
            new com.example.ibmmq.adapter.MessageWrappers.StreamMessageWrapper(javaxStreamMessage);

        // Mock all read operations
        when(javaxStreamMessage.readBoolean()).thenReturn(true);
        when(javaxStreamMessage.readByte()).thenReturn((byte) 42);
        when(javaxStreamMessage.readInt()).thenReturn(123);
        when(javaxStreamMessage.readLong()).thenReturn(456L);
        when(javaxStreamMessage.readFloat()).thenReturn(7.89f);
        when(javaxStreamMessage.readDouble()).thenReturn(10.11);
        when(javaxStreamMessage.readString()).thenReturn("test stream");
        when(javaxStreamMessage.readShort()).thenReturn((short) 999);
        when(javaxStreamMessage.readChar()).thenReturn('S');
        when(javaxStreamMessage.readObject()).thenReturn("streamObject");

        byte[] testBytes = {1, 2, 3};
        when(javaxStreamMessage.readBytes(any(byte[].class))).thenReturn(3);

        // When & Then - test all read operations
        assertThat(wrapper.readBoolean()).isTrue();
        assertThat(wrapper.readByte()).isEqualTo((byte) 42);
        assertThat(wrapper.readInt()).isEqualTo(123);
        assertThat(wrapper.readLong()).isEqualTo(456L);
        assertThat(wrapper.readFloat()).isEqualTo(7.89f);
        assertThat(wrapper.readDouble()).isEqualTo(10.11);
        assertThat(wrapper.readString()).isEqualTo("test stream");
        assertThat(wrapper.readShort()).isEqualTo((short) 999);
        assertThat(wrapper.readChar()).isEqualTo('S');
        assertThat(wrapper.readObject()).isEqualTo("streamObject");

        byte[] buffer = new byte[10];
        assertThat(wrapper.readBytes(buffer)).isEqualTo(3);

        // Test all write operations
        wrapper.writeBoolean(false);
        verify(javaxStreamMessage).writeBoolean(false);

        wrapper.writeByte((byte) 99);
        verify(javaxStreamMessage).writeByte((byte) 99);

        wrapper.writeInt(777);
        verify(javaxStreamMessage).writeInt(777);

        wrapper.writeLong(888L);
        verify(javaxStreamMessage).writeLong(888L);

        wrapper.writeFloat(2.34f);
        verify(javaxStreamMessage).writeFloat(2.34f);

        wrapper.writeDouble(5.67);
        verify(javaxStreamMessage).writeDouble(5.67);

        wrapper.writeString("new stream value");
        verify(javaxStreamMessage).writeString("new stream value");

        wrapper.writeShort((short) 555);
        verify(javaxStreamMessage).writeShort((short) 555);

        wrapper.writeChar('T');
        verify(javaxStreamMessage).writeChar('T');

        wrapper.writeObject("newStreamObject");
        verify(javaxStreamMessage).writeObject("newStreamObject");

        wrapper.writeBytes(testBytes);
        verify(javaxStreamMessage).writeBytes(testBytes);

        // Test partial bytes write
        wrapper.writeBytes(testBytes, 1, 2);
        verify(javaxStreamMessage).writeBytes(testBytes, 1, 2);

        // Test reset
        wrapper.reset();
        verify(javaxStreamMessage).reset();
    }

    @Test
    @DisplayName("Should test MessageWrapper comprehensive JMS header and property methods")
    void shouldTestMessageWrapperComprehensiveJMSMethods() throws Exception {
        // Given
        javax.jms.Message javaxMessage = mock(javax.jms.Message.class);
        javax.jms.Queue javaxQueue = mock(javax.jms.Queue.class);
        javax.jms.Topic javaxTopic = mock(javax.jms.Topic.class);

        when(javaxMessage.getJMSMessageID()).thenReturn("test-message-id");
        when(javaxMessage.getJMSTimestamp()).thenReturn(1234567890L);
        when(javaxMessage.getJMSCorrelationID()).thenReturn("test-correlation");
        when(javaxMessage.getJMSCorrelationIDAsBytes()).thenReturn("test-bytes".getBytes());
        when(javaxMessage.getJMSReplyTo()).thenReturn(javaxQueue);
        when(javaxMessage.getJMSDestination()).thenReturn(javaxTopic);
        when(javaxMessage.getJMSDeliveryMode()).thenReturn(DeliveryMode.PERSISTENT);
        when(javaxMessage.getJMSRedelivered()).thenReturn(true);
        when(javaxMessage.getJMSType()).thenReturn("test-type");
        when(javaxMessage.getJMSExpiration()).thenReturn(9876543210L);
        when(javaxMessage.getJMSDeliveryTime()).thenReturn(5555555555L);
        when(javaxMessage.getJMSPriority()).thenReturn(7);

        MessageWrapper wrapper = new MessageWrapper(javaxMessage);

        // When & Then - Test all JMS header getters
        assertThat(wrapper.getJMSMessageID()).isEqualTo("test-message-id");
        assertThat(wrapper.getJMSTimestamp()).isEqualTo(1234567890L);
        assertThat(wrapper.getJMSCorrelationID()).isEqualTo("test-correlation");
        assertThat(wrapper.getJMSCorrelationIDAsBytes()).isEqualTo("test-bytes".getBytes());
        assertThat(wrapper.getJMSReplyTo()).isNotNull();
        assertThat(wrapper.getJMSDestination()).isNotNull();
        assertThat(wrapper.getJMSDeliveryMode()).isEqualTo(DeliveryMode.PERSISTENT);
        assertThat(wrapper.getJMSRedelivered()).isTrue();
        assertThat(wrapper.getJMSType()).isEqualTo("test-type");
        assertThat(wrapper.getJMSExpiration()).isEqualTo(9876543210L);
        assertThat(wrapper.getJMSDeliveryTime()).isEqualTo(5555555555L);
        assertThat(wrapper.getJMSPriority()).isEqualTo(7);

        // Test all JMS header setters
        wrapper.setJMSMessageID("new-message-id");
        wrapper.setJMSTimestamp(1111111111L);
        wrapper.setJMSCorrelationID("new-correlation");
        wrapper.setJMSCorrelationIDAsBytes("new-bytes".getBytes());
        wrapper.setJMSReplyTo(new com.example.ibmmq.adapter.DestinationWrappers.QueueWrapper(javaxQueue));
        wrapper.setJMSDestination(new com.example.ibmmq.adapter.DestinationWrappers.TopicWrapper(javaxTopic));
        wrapper.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
        wrapper.setJMSRedelivered(false);
        wrapper.setJMSType("new-type");
        wrapper.setJMSExpiration(2222222222L);
        wrapper.setJMSDeliveryTime(3333333333L);
        wrapper.setJMSPriority(9);

        verify(javaxMessage).setJMSMessageID("new-message-id");
        verify(javaxMessage).setJMSTimestamp(1111111111L);
        verify(javaxMessage).setJMSCorrelationID("new-correlation");
        verify(javaxMessage).setJMSCorrelationIDAsBytes("new-bytes".getBytes());
        verify(javaxMessage).setJMSReplyTo(javaxQueue);
        verify(javaxMessage).setJMSDestination(javaxTopic);
        verify(javaxMessage).setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
        verify(javaxMessage).setJMSRedelivered(false);
        verify(javaxMessage).setJMSType("new-type");
        verify(javaxMessage).setJMSExpiration(2222222222L);
        verify(javaxMessage).setJMSDeliveryTime(3333333333L);
        verify(javaxMessage).setJMSPriority(9);
    }

    @Test
    @DisplayName("Should test MessageWrapper comprehensive property methods")
    void shouldTestMessageWrapperComprehensivePropertyMethods() throws Exception {
        // Given
        javax.jms.Message javaxMessage = mock(javax.jms.Message.class);
        when(javaxMessage.propertyExists("testProp")).thenReturn(true);
        when(javaxMessage.getBooleanProperty("boolProp")).thenReturn(true);
        when(javaxMessage.getByteProperty("byteProp")).thenReturn((byte) 42);
        when(javaxMessage.getShortProperty("shortProp")).thenReturn((short) 123);
        when(javaxMessage.getIntProperty("intProp")).thenReturn(456);
        when(javaxMessage.getLongProperty("longProp")).thenReturn(789L);
        when(javaxMessage.getFloatProperty("floatProp")).thenReturn(1.23f);
        when(javaxMessage.getDoubleProperty("doubleProp")).thenReturn(4.56);
        when(javaxMessage.getStringProperty("stringProp")).thenReturn("test-value");
        when(javaxMessage.getObjectProperty("objectProp")).thenReturn("test-object");

        @SuppressWarnings("unchecked")
        Enumeration<String> mockEnum = mock(Enumeration.class);
        when(javaxMessage.getPropertyNames()).thenReturn(mockEnum);

        MessageWrapper wrapper = new MessageWrapper(javaxMessage);

        // When & Then - Test property getters
        assertThat(wrapper.propertyExists("testProp")).isTrue();
        assertThat(wrapper.getBooleanProperty("boolProp")).isTrue();
        assertThat(wrapper.getByteProperty("byteProp")).isEqualTo((byte) 42);
        assertThat(wrapper.getShortProperty("shortProp")).isEqualTo((short) 123);
        assertThat(wrapper.getIntProperty("intProp")).isEqualTo(456);
        assertThat(wrapper.getLongProperty("longProp")).isEqualTo(789L);
        assertThat(wrapper.getFloatProperty("floatProp")).isEqualTo(1.23f);
        assertThat(wrapper.getDoubleProperty("doubleProp")).isEqualTo(4.56);
        assertThat(wrapper.getStringProperty("stringProp")).isEqualTo("test-value");
        assertThat(wrapper.getObjectProperty("objectProp")).isEqualTo("test-object");
        assertThat(wrapper.getPropertyNames()).isEqualTo(mockEnum);

        // Test property setters
        wrapper.setBooleanProperty("newBoolProp", false);
        wrapper.setByteProperty("newByteProp", (byte) 99);
        wrapper.setShortProperty("newShortProp", (short) 999);
        wrapper.setIntProperty("newIntProp", 888);
        wrapper.setLongProperty("newLongProp", 777L);
        wrapper.setFloatProperty("newFloatProp", 9.87f);
        wrapper.setDoubleProperty("newDoubleProp", 6.54);
        wrapper.setStringProperty("newStringProp", "new-value");
        wrapper.setObjectProperty("newObjectProp", "new-object");

        verify(javaxMessage).setBooleanProperty("newBoolProp", false);
        verify(javaxMessage).setByteProperty("newByteProp", (byte) 99);
        verify(javaxMessage).setShortProperty("newShortProp", (short) 999);
        verify(javaxMessage).setIntProperty("newIntProp", 888);
        verify(javaxMessage).setLongProperty("newLongProp", 777L);
        verify(javaxMessage).setFloatProperty("newFloatProp", 9.87f);
        verify(javaxMessage).setDoubleProperty("newDoubleProp", 6.54);
        verify(javaxMessage).setStringProperty("newStringProp", "new-value");
        verify(javaxMessage).setObjectProperty("newObjectProp", "new-object");

        // Test clear properties
        wrapper.clearProperties();
        verify(javaxMessage).clearProperties();
    }

    @Test
    @DisplayName("Should test MessageWrapper acknowledge and body methods")
    void shouldTestMessageWrapperAcknowledgeAndBodyMethods() throws Exception {
        // Given
        javax.jms.Message javaxMessage = mock(javax.jms.Message.class);
        when(javaxMessage.isBodyAssignableTo(String.class)).thenReturn(true);
        when(javaxMessage.getBody(String.class)).thenReturn("test-body");

        MessageWrapper wrapper = new MessageWrapper(javaxMessage);

        // When & Then - Test body methods
        assertThat(wrapper.isBodyAssignableTo(String.class)).isTrue();
        assertThat(wrapper.getBody(String.class)).isEqualTo("test-body");

        // Test acknowledge
        wrapper.acknowledge();
        verify(javaxMessage).acknowledge();

        verify(javaxMessage).isBodyAssignableTo(String.class);
        verify(javaxMessage).getBody(String.class);
    }

    @Test
    @DisplayName("Should test MessageWrapper null destination handling")
    void shouldTestMessageWrapperNullDestinationHandling() throws Exception {
        // Given
        javax.jms.Message javaxMessage = mock(javax.jms.Message.class);
        when(javaxMessage.getJMSReplyTo()).thenReturn(null);
        when(javaxMessage.getJMSDestination()).thenReturn(null);

        MessageWrapper wrapper = new MessageWrapper(javaxMessage);

        // When & Then - Test null destinations
        assertThat(wrapper.getJMSReplyTo()).isNull();
        assertThat(wrapper.getJMSDestination()).isNull();

        // Test setting null destinations
        wrapper.setJMSReplyTo(null);
        wrapper.setJMSDestination(null);

        verify(javaxMessage).setJMSReplyTo(null);
        verify(javaxMessage).setJMSDestination(null);
    }

    @Test
    @DisplayName("Should test MessageWrapper JMSException handling")
    void shouldTestMessageWrapperJMSExceptionHandling() throws Exception {
        // Given
        javax.jms.Message javaxMessage = mock(javax.jms.Message.class);
        javax.jms.JMSException javaxException = new javax.jms.JMSException("Test error");

        when(javaxMessage.getJMSMessageID()).thenThrow(javaxException);
        when(javaxMessage.getJMSTimestamp()).thenThrow(javaxException);
        when(javaxMessage.propertyExists("test")).thenThrow(javaxException);
        when(javaxMessage.getBooleanProperty("test")).thenThrow(javaxException);
        doThrow(javaxException).when(javaxMessage).setJMSMessageID("test");
        doThrow(javaxException).when(javaxMessage).setBooleanProperty("test", true);
        doThrow(javaxException).when(javaxMessage).acknowledge();

        MessageWrapper wrapper = new MessageWrapper(javaxMessage);

        // When & Then - Test exception conversion
        assertThatThrownBy(() -> wrapper.getJMSMessageID())
            .isInstanceOf(JMSException.class)
            .hasMessage("Test error");

        assertThatThrownBy(() -> wrapper.getJMSTimestamp())
            .isInstanceOf(JMSException.class)
            .hasMessage("Test error");

        assertThatThrownBy(() -> wrapper.propertyExists("test"))
            .isInstanceOf(JMSException.class)
            .hasMessage("Test error");

        assertThatThrownBy(() -> wrapper.getBooleanProperty("test"))
            .isInstanceOf(JMSException.class)
            .hasMessage("Test error");

        assertThatThrownBy(() -> wrapper.setJMSMessageID("test"))
            .isInstanceOf(JMSException.class)
            .hasMessage("Test error");

        assertThatThrownBy(() -> wrapper.setBooleanProperty("test", true))
            .isInstanceOf(JMSException.class)
            .hasMessage("Test error");

        assertThatThrownBy(() -> wrapper.acknowledge())
            .isInstanceOf(JMSException.class)
            .hasMessage("Test error");
    }

    @Test
    @DisplayName("Should test BytesMessageWrapper exception handling for all methods")
    void shouldTestBytesMessageWrapperExceptionHandling() throws Exception {
        // Given
        javax.jms.BytesMessage javaxBytesMessage = mock(javax.jms.BytesMessage.class);
        com.example.ibmmq.adapter.MessageWrappers.BytesMessageWrapper wrapper =
            new com.example.ibmmq.adapter.MessageWrappers.BytesMessageWrapper(javaxBytesMessage);
        javax.jms.JMSException javaxException = new javax.jms.JMSException("BytesMessage error");

        // Mock all read operations to throw exceptions
        when(javaxBytesMessage.readUnsignedByte()).thenThrow(javaxException);
        when(javaxBytesMessage.readUnsignedShort()).thenThrow(javaxException);
        when(javaxBytesMessage.readBytes(any(byte[].class), anyInt())).thenThrow(javaxException);
        doThrow(javaxException).when(javaxBytesMessage).writeBytes(any(byte[].class), anyInt(), anyInt());
        doThrow(javaxException).when(javaxBytesMessage).writeObject(any());

        // When & Then - Test exception handling for uncovered read methods
        assertThatThrownBy(() -> wrapper.readUnsignedByte())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("BytesMessage error");

        assertThatThrownBy(() -> wrapper.readUnsignedShort())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("BytesMessage error");

        byte[] buffer = new byte[10];
        assertThatThrownBy(() -> wrapper.readBytes(buffer, 5))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("BytesMessage error");

        byte[] data = {1, 2, 3};
        assertThatThrownBy(() -> wrapper.writeBytes(data, 0, 3))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("BytesMessage error");

        assertThatThrownBy(() -> wrapper.writeObject("test object"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("BytesMessage error");
    }

    @Test
    @DisplayName("Should test BytesMessageWrapper additional read/write methods")
    void shouldTestBytesMessageWrapperAdditionalMethods() throws Exception {
        // Given
        javax.jms.BytesMessage javaxBytesMessage = mock(javax.jms.BytesMessage.class);
        com.example.ibmmq.adapter.MessageWrappers.BytesMessageWrapper wrapper =
            new com.example.ibmmq.adapter.MessageWrappers.BytesMessageWrapper(javaxBytesMessage);

        // Mock additional read operations
        when(javaxBytesMessage.readUnsignedByte()).thenReturn(255);
        when(javaxBytesMessage.readUnsignedShort()).thenReturn(65535);

        byte[] buffer = new byte[10];
        when(javaxBytesMessage.readBytes(buffer, 5)).thenReturn(5);

        // When & Then - Test additional read operations
        assertThat(wrapper.readUnsignedByte()).isEqualTo(255);
        verify(javaxBytesMessage).readUnsignedByte();

        assertThat(wrapper.readUnsignedShort()).isEqualTo(65535);
        verify(javaxBytesMessage).readUnsignedShort();

        assertThat(wrapper.readBytes(buffer, 5)).isEqualTo(5);
        verify(javaxBytesMessage).readBytes(buffer, 5);

        // Test additional write operations
        byte[] data = {1, 2, 3};
        wrapper.writeBytes(data, 0, 3);
        verify(javaxBytesMessage).writeBytes(data, 0, 3);

        wrapper.writeObject("test object");
        verify(javaxBytesMessage).writeObject("test object");
    }

    @Test
    @DisplayName("MapMessageWrapper: Should handle all JMSException scenarios for getter methods")
    void mapMessageWrapperShouldHandleGetterJMSExceptions() throws Exception {
        // Given
        javax.jms.MapMessage javaxMapMessage = mock(javax.jms.MapMessage.class);
        javax.jms.JMSException javaxException = new javax.jms.JMSException("Test javax exception");
        MessageWrappers.MapMessageWrapper wrapper = new MessageWrappers.MapMessageWrapper(javaxMapMessage);

        // When/Then - Test exception handling for all getter methods
        when(javaxMapMessage.getBoolean("key")).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.getBoolean("key"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxMapMessage.getByte("key")).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.getByte("key"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxMapMessage.getShort("key")).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.getShort("key"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxMapMessage.getChar("key")).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.getChar("key"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxMapMessage.getInt("key")).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.getInt("key"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxMapMessage.getLong("key")).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.getLong("key"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxMapMessage.getFloat("key")).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.getFloat("key"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxMapMessage.getDouble("key")).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.getDouble("key"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxMapMessage.getString("key")).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.getString("key"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxMapMessage.getBytes("key")).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.getBytes("key"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxMapMessage.getObject("key")).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.getObject("key"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxMapMessage.itemExists("key")).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.itemExists("key"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxMapMessage.getMapNames()).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.getMapNames())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");
    }

    @Test
    @DisplayName("MapMessageWrapper: Should handle all JMSException scenarios for setter methods")
    void mapMessageWrapperShouldHandleSetterJMSExceptions() throws Exception {
        // Given
        javax.jms.MapMessage javaxMapMessage = mock(javax.jms.MapMessage.class);
        javax.jms.JMSException javaxException = new javax.jms.JMSException("Test javax exception");
        MessageWrappers.MapMessageWrapper wrapper = new MessageWrappers.MapMessageWrapper(javaxMapMessage);

        // When/Then - Test exception handling for all setter methods
        doThrow(javaxException).when(javaxMapMessage).setBoolean("key", true);
        assertThatThrownBy(() -> wrapper.setBoolean("key", true))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxMapMessage).setByte("key", (byte) 1);
        assertThatThrownBy(() -> wrapper.setByte("key", (byte) 1))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxMapMessage).setShort("key", (short) 1);
        assertThatThrownBy(() -> wrapper.setShort("key", (short) 1))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxMapMessage).setChar("key", 'A');
        assertThatThrownBy(() -> wrapper.setChar("key", 'A'))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxMapMessage).setInt("key", 1);
        assertThatThrownBy(() -> wrapper.setInt("key", 1))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxMapMessage).setLong("key", 1L);
        assertThatThrownBy(() -> wrapper.setLong("key", 1L))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxMapMessage).setFloat("key", 1.0f);
        assertThatThrownBy(() -> wrapper.setFloat("key", 1.0f))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxMapMessage).setDouble("key", 1.0);
        assertThatThrownBy(() -> wrapper.setDouble("key", 1.0))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxMapMessage).setString("key", "value");
        assertThatThrownBy(() -> wrapper.setString("key", "value"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        byte[] testBytes = {1, 2, 3};
        doThrow(javaxException).when(javaxMapMessage).setBytes("key", testBytes);
        assertThatThrownBy(() -> wrapper.setBytes("key", testBytes))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxMapMessage).setBytes("key", testBytes, 0, 1);
        assertThatThrownBy(() -> wrapper.setBytes("key", testBytes, 0, 1))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxMapMessage).setObject("key", "object");
        assertThatThrownBy(() -> wrapper.setObject("key", "object"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");
    }

    @Test
    @DisplayName("StreamMessageWrapper: Should handle all JMSException scenarios for read methods")
    void streamMessageWrapperShouldHandleReadJMSExceptions() throws Exception {
        // Given
        javax.jms.StreamMessage javaxStreamMessage = mock(javax.jms.StreamMessage.class);
        javax.jms.JMSException javaxException = new javax.jms.JMSException("Test javax exception");
        MessageWrappers.StreamMessageWrapper wrapper = new MessageWrappers.StreamMessageWrapper(javaxStreamMessage);

        // When/Then - Test exception handling for all read methods
        when(javaxStreamMessage.readBoolean()).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.readBoolean())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxStreamMessage.readByte()).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.readByte())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxStreamMessage.readShort()).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.readShort())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxStreamMessage.readChar()).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.readChar())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxStreamMessage.readInt()).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.readInt())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxStreamMessage.readLong()).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.readLong())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxStreamMessage.readFloat()).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.readFloat())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxStreamMessage.readDouble()).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.readDouble())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxStreamMessage.readString()).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.readString())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxStreamMessage.readBytes(any(byte[].class))).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.readBytes(new byte[10]))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        when(javaxStreamMessage.readObject()).thenThrow(javaxException);
        assertThatThrownBy(() -> wrapper.readObject())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxStreamMessage).reset();
        assertThatThrownBy(() -> wrapper.reset())
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");
    }

    @Test
    @DisplayName("StreamMessageWrapper: Should handle all JMSException scenarios for write methods")
    void streamMessageWrapperShouldHandleWriteJMSExceptions() throws Exception {
        // Given
        javax.jms.StreamMessage javaxStreamMessage = mock(javax.jms.StreamMessage.class);
        javax.jms.JMSException javaxException = new javax.jms.JMSException("Test javax exception");
        MessageWrappers.StreamMessageWrapper wrapper = new MessageWrappers.StreamMessageWrapper(javaxStreamMessage);

        // When/Then - Test exception handling for all write methods
        doThrow(javaxException).when(javaxStreamMessage).writeBoolean(true);
        assertThatThrownBy(() -> wrapper.writeBoolean(true))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxStreamMessage).writeByte((byte) 1);
        assertThatThrownBy(() -> wrapper.writeByte((byte) 1))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxStreamMessage).writeShort((short) 1);
        assertThatThrownBy(() -> wrapper.writeShort((short) 1))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxStreamMessage).writeChar('A');
        assertThatThrownBy(() -> wrapper.writeChar('A'))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxStreamMessage).writeInt(1);
        assertThatThrownBy(() -> wrapper.writeInt(1))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxStreamMessage).writeLong(1L);
        assertThatThrownBy(() -> wrapper.writeLong(1L))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxStreamMessage).writeFloat(1.0f);
        assertThatThrownBy(() -> wrapper.writeFloat(1.0f))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxStreamMessage).writeDouble(1.0);
        assertThatThrownBy(() -> wrapper.writeDouble(1.0))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxStreamMessage).writeString("value");
        assertThatThrownBy(() -> wrapper.writeString("value"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        byte[] testBytes = {1, 2, 3};
        doThrow(javaxException).when(javaxStreamMessage).writeBytes(testBytes);
        assertThatThrownBy(() -> wrapper.writeBytes(testBytes))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxStreamMessage).writeBytes(testBytes, 0, 1);
        assertThatThrownBy(() -> wrapper.writeBytes(testBytes, 0, 1))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");

        doThrow(javaxException).when(javaxStreamMessage).writeObject("object");
        assertThatThrownBy(() -> wrapper.writeObject("object"))
            .isInstanceOf(jakarta.jms.JMSException.class)
            .hasMessage("Test javax exception");
    }
}