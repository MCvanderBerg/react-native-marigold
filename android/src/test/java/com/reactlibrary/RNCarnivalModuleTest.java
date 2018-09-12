package com.reactlibrary;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;

import com.carnival.sdk.AttributeMap;
import com.carnival.sdk.Carnival;
import com.carnival.sdk.CarnivalImpressionType;
import com.carnival.sdk.Message;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Carnival.class, RNCarnivalModule.class})
public class RNCarnivalModuleTest {

    @Mock
    private ReactApplicationContext mockContext;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Carnival.class);
        PowerMockito.mockStatic(RNCarnivalModule.class);
        PowerMockito.doNothing().when(RNCarnivalModule.class, "setWrapperInfo");
    }

    @Test
    public void testConstructor() {
        new RNCarnivalModule(mockContext);

        PowerMockito.verifyStatic();
        RNCarnivalModule.setWrapperInfo();
    }

    @Test
    public void testUpdateLocation() throws Exception {
        double latitude = 10, longitude = 10;

        PowerMockito.doNothing().when(Carnival.class, "updateLocation", anyObject());
        Location location = mock(Location.class);
        PowerMockito.whenNew(Location.class).withAnyArguments().thenReturn(location);
        doNothing().when(location).setLatitude(anyDouble());
        doNothing().when(location).setLongitude(anyDouble());

        RNCarnivalModule rnCarnivalModule = new RNCarnivalModule(mockContext);
        rnCarnivalModule.updateLocation(latitude, longitude);

        verify(location).setLatitude(latitude);
        verify(location).setLongitude(longitude);
        PowerMockito.verifyStatic();
        Carnival.updateLocation(location);
    }

    @Test
    public void testGetDeviceID() throws Exception {
        // Setup variables
        String deviceID = "device ID";
        String errorMessage = "error message";
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Mock methods
        PowerMockito.doNothing().when(Carnival.class, "getDeviceId", anyObject());
        doReturn(errorMessage).when(error).getMessage();

        // Start test
        RNCarnivalModule rnCarnivalModule = new RNCarnivalModule(mockContext);
        rnCarnivalModule.getDeviceID(promise);

        // Capture handler for verification
        ArgumentCaptor<Carnival.CarnivalHandler> argumentCaptor = ArgumentCaptor.forClass(Carnival.CarnivalHandler.class);
        PowerMockito.verifyStatic();
        Carnival.getDeviceId(argumentCaptor.capture());
        Carnival.CarnivalHandler carnivalHandler = argumentCaptor.getValue();

        // Test success
        carnivalHandler.onSuccess(deviceID);
        verify(promise).resolve(deviceID);

        // Test failure
        carnivalHandler.onFailure(error);
        verify(promise).reject(RNCarnivalModule.ERROR_CODE_DEVICE, errorMessage);
    }

    @Test
    public void testLogEvent() throws Exception {
        String event = "event string";

        PowerMockito.doNothing().when(Carnival.class, "logEvent", event);

        RNCarnivalModule rnCarnivalModule = new RNCarnivalModule(mockContext);
        rnCarnivalModule.logEvent(event);

        PowerMockito.verifyStatic();
        Carnival.logEvent(event);
    }

    @Test
    public void testSetAttributes() throws Exception {
        // setup mocks
        ReadableMap readableMap = mock(ReadableMap.class);
        JSONObject attributeMapJson = mock(JSONObject.class);
        JSONObject attributeJson = mock(JSONObject.class);
        AttributeMap attributeMap = mock(AttributeMap.class);
        Iterator<String> keys = mock(Iterator.class);

        // setup mocking for conversion from ReadableMap to JSON
        PowerMockito.when(RNCarnivalModule.class, "convertMapToJson", readableMap).thenReturn(attributeMapJson);
        when(attributeMapJson.getJSONObject("attributes")).thenReturn(attributeJson);

        // Mock attribute map
        PowerMockito.whenNew(AttributeMap.class).withNoArguments().thenReturn(attributeMap);
        doNothing().when(attributeMap).setMergeRules(anyInt());

        // Setup JSON objects
        when(attributeJson.getInt("mergeRule")).thenReturn(0);
        when(attributeJson.keys()).thenReturn(keys);
        when(keys.hasNext()).thenReturn(false);

        // Mock Carnival method
        PowerMockito.doNothing().when(Carnival.class, "setAttributes", eq(attributeMap), any(Carnival.AttributesHandler.class));

        // Initiate test
        RNCarnivalModule rnCarnivalModule = new RNCarnivalModule(mockContext);
        rnCarnivalModule.setAttributes(readableMap, null);

        // Verify results
        PowerMockito.verifyStatic();
        Carnival.setAttributes(eq(attributeMap), any(Carnival.AttributesHandler.class));
    }

    @Test
    public void testGetMessages() throws Exception {
        // Setup mocks
        Promise promise = mock(Promise.class);
        WritableArray writableArray = mock(WritableArray.class);
        Error error = mock(Error.class);

        // Mock Carnival method
        PowerMockito.doNothing().when(Carnival.class, "getMessages", any(Carnival.MessagesHandler.class));

        // Initiate test
        RNCarnivalModule rnCarnivalModule = new RNCarnivalModule(mockContext);
        rnCarnivalModule.getMessages(promise);

        // Capture MessagesHandler to verify behaviour
        ArgumentCaptor<Carnival.MessagesHandler> argumentCaptor = ArgumentCaptor.forClass(Carnival.MessagesHandler.class);
        PowerMockito.verifyStatic();
        Carnival.getMessages(argumentCaptor.capture());
        Carnival.MessagesHandler messagesHandler = argumentCaptor.getValue();

        // Replace native array with mock
        PowerMockito.when(RNCarnivalModule.class, "getWritableArray").thenReturn(writableArray);

        // Setup message array
        ArrayList<Message> messages = new ArrayList<>();

        // Test success handler
        messagesHandler.onSuccess(messages);
        verify(promise).resolve(writableArray);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        messagesHandler.onFailure(error);
        verify(promise).reject(RNCarnivalModule.ERROR_CODE_MESSAGES, errorMessage);
    }

    @Test
    public void testSetUserId() throws Exception {
        String userID = "user ID";

        PowerMockito.doNothing().when(Carnival.class, "setUserId", userID, null);

        RNCarnivalModule rnCarnivalModule = new RNCarnivalModule(mockContext);
        rnCarnivalModule.setUserId(userID);

        PowerMockito.verifyStatic();
        Carnival.setUserId(userID, null);
    }

    @Test
    public void testSetUserEmail() throws Exception {
        String userEmail = "user email";

        PowerMockito.doNothing().when(Carnival.class, "setUserEmail", userEmail, null);

        RNCarnivalModule rnCarnivalModule = new RNCarnivalModule(mockContext);
        rnCarnivalModule.setUserEmail(userEmail);

        PowerMockito.verifyStatic();
        Carnival.setUserEmail(userEmail, null);
    }

    @Test
    public void testGetUnreadCount() throws Exception {
        int unreadCount = 4;
        Promise promise = mock(Promise.class);

        PowerMockito.when(Carnival.class, "getUnreadMessageCount").thenReturn(unreadCount);

        RNCarnivalModule rnCarnivalModule = new RNCarnivalModule(mockContext);
        rnCarnivalModule.getUnreadCount(promise);

        PowerMockito.verifyStatic();
        Carnival.getUnreadMessageCount();

        verify(promise).resolve(unreadCount);
    }

    @Test
    public void testRemoveMessage() throws Exception {
        // Create mocks
        ReadableMap readableMap = mock(ReadableMap.class);

        // Create message to remove
        RNCarnivalModule rnCarnivalModule = spy(new RNCarnivalModule(mockContext));
        Constructor<Message> constructor = Message.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Message message = constructor.newInstance();
        doReturn(message).when(rnCarnivalModule).getMessage(readableMap);

        // Mock Carnival method
        PowerMockito.doNothing().when(Carnival.class, "deleteMessage", message, null);

        // Initiate test
        rnCarnivalModule.removeMessage(readableMap);

        // Verify result
        PowerMockito.verifyStatic();
        Carnival.deleteMessage(message, null);
    }

    @Test
    public void testRegisterMessageImpression() throws Exception {
        // Create input
        ReadableMap readableMap = mock(ReadableMap.class);
        int typeCode = 0;

        // Create message to remove
        RNCarnivalModule rnCarnivalModule = spy(new RNCarnivalModule(mockContext));
        Constructor<Message> constructor = Message.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Message message = constructor.newInstance();
        doReturn(message).when(rnCarnivalModule).getMessage(readableMap);

        // Mock Carnival method
        PowerMockito.doNothing().when(Carnival.class, "registerMessageImpression", CarnivalImpressionType.IMPRESSION_TYPE_IN_APP_VIEW, message);

        // Initiate test
        rnCarnivalModule.registerMessageImpression(typeCode, readableMap);

        // Verify result
        PowerMockito.verifyStatic();
        Carnival.registerMessageImpression(CarnivalImpressionType.IMPRESSION_TYPE_IN_APP_VIEW, message);
    }

    @Test
    public void testMarkMessageAsRead() throws Exception {
        // Create mocks
        ReadableMap readableMap = mock(ReadableMap.class);
        Promise promise = mock(Promise.class);

        // Create message to remove
        RNCarnivalModule rnCarnivalModule = spy(new RNCarnivalModule(mockContext));
        Constructor<Message> constructor = Message.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Message message = constructor.newInstance();
        doReturn(message).when(rnCarnivalModule).getMessage(readableMap);

        // Mock Carnival method
        PowerMockito.doNothing().when(Carnival.class, "setMessageRead", eq(message), any(Carnival.MessagesReadHandler.class));

        // Initiate test
        rnCarnivalModule.markMessageAsRead(readableMap, promise);

        // Verify result
        PowerMockito.verifyStatic();
        Carnival.setMessageRead(eq(message), any(Carnival.MessagesReadHandler.class));
    }

    @Test
    public void testSetDisplayInAppNotificationsTrue() throws Exception {
        // Mock Carnival method
        PowerMockito.doNothing().when(Carnival.class, "setOnInAppNotificationDisplayListener", any(Carnival.OnInAppNotificationDisplayListener.class));

        // Initiate true test
        RNCarnivalModule rnCarnivalModule = new RNCarnivalModule(mockContext);
        rnCarnivalModule.setDisplayInAppNotifications(true);

        // Capture listener to verify behaviour
        ArgumentCaptor<Carnival.OnInAppNotificationDisplayListener> argumentCaptor = ArgumentCaptor.forClass(Carnival.OnInAppNotificationDisplayListener.class);
        PowerMockito.verifyStatic();
        Carnival.setOnInAppNotificationDisplayListener(argumentCaptor.capture());
        Carnival.OnInAppNotificationDisplayListener listener = argumentCaptor.getValue();

        // Verify result
        assertNull(listener);
    }

    @Test
    public void testSetDisplayInAppNotificationsFalse() throws Exception {
        // Mock Carnival method
        PowerMockito.doNothing().when(Carnival.class, "setOnInAppNotificationDisplayListener", any(Carnival.OnInAppNotificationDisplayListener.class));

        // Initiate true test
        RNCarnivalModule rnCarnivalModule = new RNCarnivalModule(mockContext);
        rnCarnivalModule.setDisplayInAppNotifications(false);

        // Capture listener to verify behaviour
        ArgumentCaptor<Carnival.OnInAppNotificationDisplayListener> argumentCaptor = ArgumentCaptor.forClass(Carnival.OnInAppNotificationDisplayListener.class);
        PowerMockito.verifyStatic();
        Carnival.setOnInAppNotificationDisplayListener(argumentCaptor.capture());
        Carnival.OnInAppNotificationDisplayListener listener = argumentCaptor.getValue();

        // Verify result
        assertNotNull(listener);
    }

    @Test
    public void testPresentMessageDetail() throws Exception {
        // Setup input
        String messageID = "message ID";

        // Mock activity
        Activity activity = mock(Activity.class);
        when(mockContext.getCurrentActivity()).thenReturn(activity);

        // Mock Intent
        Intent intent = mock(Intent.class);
        PowerMockito.whenNew(Intent.class).withAnyArguments().thenReturn(intent);
        doReturn(intent).when(intent).putExtra(Carnival.EXTRA_MESSAGE_ID, messageID);

        // Initiate test
        RNCarnivalModule rnCarnivalModule = new RNCarnivalModule(mockContext);
        rnCarnivalModule.presentMessageDetail(messageID);

        // Verify result
        verify(activity).startActivity(intent);
    }

}
