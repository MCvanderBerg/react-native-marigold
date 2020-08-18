package com.sailthru.mobile.rnsdk;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;

import com.sailthru.mobile.sdk.MessageActivity;
import com.sailthru.mobile.sdk.MessageStream;
import com.sailthru.mobile.sdk.model.AttributeMap;
import com.sailthru.mobile.sdk.SailthruMobile;
import com.sailthru.mobile.sdk.enums.ImpressionType;
import com.sailthru.mobile.sdk.model.ContentItem;
import com.sailthru.mobile.sdk.model.Message;
import com.sailthru.mobile.sdk.model.Purchase;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({SailthruMobile.class, MessageStream.class, AttributeMap.class, MessageActivity.class, Purchase.class, RNSailthruMobileModule.class})
public class RNSailthruMobileModuleTest {

    @Mock
    private ReactApplicationContext mockContext;

    private SailthruMobile sailthruMobile;
    private MessageStream messageStream;

    private RNSailthruMobileModule rnSailthruMobileModule;
    private RNSailthruMobileModule rnSailthruMobileModuleSpy;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        sailthruMobile = PowerMockito.mock(SailthruMobile.class);
        messageStream = PowerMockito.mock(MessageStream.class);
        PowerMockito.mockStatic(RNSailthruMobileModule.class);
        PowerMockito.mockStatic(MessageActivity.class);

        PowerMockito.whenNew(SailthruMobile.class).withAnyArguments().thenReturn(sailthruMobile);
        PowerMockito.whenNew(MessageStream.class).withAnyArguments().thenReturn(messageStream);
        PowerMockito.doNothing().when(RNSailthruMobileModule.class, "setWrapperInfo", any(SailthruMobile.class));

        rnSailthruMobileModule = new RNSailthruMobileModule(mockContext, true);
        rnSailthruMobileModuleSpy = spy(rnSailthruMobileModule);
    }

    @Test
    public void testConstructor() {
        verify(messageStream).setOnInAppNotificationDisplayListener(rnSailthruMobileModule);

        PowerMockito.verifyStatic(RNSailthruMobileModule.class);
        RNSailthruMobileModule.setWrapperInfo(any(SailthruMobile.class));
    }

    @Test
    public void testUpdateLocation() throws Exception {
        double latitude = 10, longitude = 10;

        Location location = mock(Location.class);
        PowerMockito.whenNew(Location.class).withAnyArguments().thenReturn(location);

        rnSailthruMobileModule.updateLocation(latitude, longitude);

        verify(location).setLatitude(latitude);
        verify(location).setLongitude(longitude);
        verify(sailthruMobile).updateLocation(location);
    }

    @Test
    public void testGetDeviceID() {
        // Setup variables
        String deviceID = "device ID";
        String errorMessage = "error message";
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Mock methods
        doReturn(errorMessage).when(error).getMessage();

        // Start test
        rnSailthruMobileModule.getDeviceID(promise);

        // Capture handler for verification
        @SuppressWarnings("unchecked")
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler<String>> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);
        verify(sailthruMobile).getDeviceId(argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler<String> sailthruMobileHandler = argumentCaptor.getValue();

        // Test success
        sailthruMobileHandler.onSuccess(deviceID);
        verify(promise).resolve(deviceID);

        // Test failure
        sailthruMobileHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_DEVICE, errorMessage);
    }

    @Test
    public void testLogEvent() {
        String event = "event string";

        rnSailthruMobileModule.logEvent(event);

        verify(sailthruMobile).logEvent(event);
    }

    @Test
    public void testLogEventWithVars() throws Exception {
        String event = "event string";
        JSONObject varsJson = new JSONObject().put("varKey", "varValue");

        // setup mocks
        ReadableMap readableMap = mock(ReadableMap.class);

        // setup mocking
        PowerMockito.doReturn(varsJson).when(rnSailthruMobileModuleSpy).convertMapToJson(readableMap);

        rnSailthruMobileModuleSpy.logEvent(event, readableMap);

        verify(sailthruMobile).logEvent(event, varsJson);
    }

    @Test
    public void testSetAttributes() throws Exception {
        // setup mocks
        ReadableMap readableMap = mock(ReadableMap.class);
        JSONObject attributeMapJson = mock(JSONObject.class);
        JSONObject attributeJson = mock(JSONObject.class);
        AttributeMap attributeMap = PowerMockito.mock(AttributeMap.class);
        @SuppressWarnings("unchecked")
        Iterator<String> keys = mock(Iterator.class);

        // setup mocking for conversion from ReadableMap to JSON
        PowerMockito.doReturn(attributeMapJson).when(rnSailthruMobileModuleSpy).convertMapToJson(readableMap);
        when(attributeMapJson.getJSONObject("attributes")).thenReturn(attributeJson);

        // Mock attribute map
        PowerMockito.whenNew(AttributeMap.class).withNoArguments().thenReturn(attributeMap);

        // Setup JSON objects
        when(attributeJson.getInt("mergeRule")).thenReturn(0);
        when(attributeJson.keys()).thenReturn(keys);
        when(keys.hasNext()).thenReturn(false);

        // Initiate test
        rnSailthruMobileModuleSpy.setAttributes(readableMap, null);

        // Verify results
        verify(sailthruMobile).setAttributes(eq(attributeMap), any(SailthruMobile.AttributesHandler.class));
    }

    @Test
    public void testGetMessages() {
        // Setup mocks
        Promise promise = mock(Promise.class);
        WritableArray writableArray = mock(WritableArray.class);
        Error error = mock(Error.class);

        // Initiate test
        rnSailthruMobileModuleSpy.getMessages(promise);

        // Capture MessagesHandler to verify behaviour
        ArgumentCaptor<MessageStream.MessagesHandler> argumentCaptor = ArgumentCaptor.forClass(MessageStream.MessagesHandler.class);
        verify(messageStream).getMessages(argumentCaptor.capture());
        MessageStream.MessagesHandler messagesHandler = argumentCaptor.getValue();

        // Replace native array with mock
        PowerMockito.doReturn(writableArray).when(rnSailthruMobileModuleSpy).getWritableArray();

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
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_MESSAGES, errorMessage);
    }

    @Test
    public void testSetUserId() {
        // Setup mocks
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);
        String userID = "user ID";

        rnSailthruMobileModule.setUserId(userID, promise);

        // Capture SailthruMobileHandler to verify behaviour
        @SuppressWarnings("unchecked")
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler<Void>> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);
        verify(sailthruMobile).setUserId(eq(userID), argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler<Void> handler = argumentCaptor.getValue();

        // Test success handler
        handler.onSuccess(null);
        verify(promise).resolve(null);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        handler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_DEVICE, errorMessage);
    }

    @Test
    public void testSetUserEmail() {
        // Setup mocks
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);
        String userEmail = "user email";

        rnSailthruMobileModule.setUserEmail(userEmail, promise);

        // Capture SailthruMobileHandler to verify behaviour
        @SuppressWarnings("unchecked")
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler<Void>> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);
        verify(sailthruMobile).setUserEmail(eq(userEmail), argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler<Void> handler = argumentCaptor.getValue();

        // Test success handler
        handler.onSuccess(null);
        verify(promise).resolve(null);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        handler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_DEVICE, errorMessage);
    }

    @Test
    public void testGetUnreadCount() {
        Integer unreadCount = 4;

        // Setup mocks
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Initiate test
        rnSailthruMobileModule.getUnreadCount(promise);

        // Capture MessagesHandler to verify behaviour
        @SuppressWarnings("unchecked")
        ArgumentCaptor<MessageStream.MessageStreamHandler<Integer>> argumentCaptor = ArgumentCaptor.forClass(MessageStream.MessageStreamHandler.class);
        verify(messageStream).getUnreadMessageCount(argumentCaptor.capture());
        MessageStream.MessageStreamHandler<Integer> countHandler = argumentCaptor.getValue();

        // Test success handler
        countHandler.onSuccess(unreadCount);
        verify(promise).resolve(unreadCount);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        countHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_MESSAGES, errorMessage);
    }

    @Test
    public void testRemoveMessage() throws Exception {
        // Create mocks
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);
        ReadableMap readableMap = mock(ReadableMap.class);

        // Create message to remove
        Constructor<Message> constructor = Message.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Message message = constructor.newInstance();

        RNSailthruMobileModule moduleSpy = spy(rnSailthruMobileModule);
        doReturn(message).when(moduleSpy).getMessage(readableMap);

        // Initiate test
        moduleSpy.removeMessage(readableMap, promise);

        // Capture SailthruMobileHandler to verify behaviour
        @SuppressWarnings("unchecked")
        ArgumentCaptor<MessageStream.MessageDeletedHandler> argumentCaptor = ArgumentCaptor.forClass(MessageStream.MessageDeletedHandler.class);
        verify(messageStream).deleteMessage(eq(message), argumentCaptor.capture());
        MessageStream.MessageDeletedHandler handler = argumentCaptor.getValue();

        // Test success handler
        handler.onSuccess();
        verify(promise).resolve(null);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        handler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_MESSAGES, errorMessage);
    }

    @Test
    public void testRegisterMessageImpression() throws Exception {
        // Create input
        ReadableMap readableMap = mock(ReadableMap.class);
        int typeCode = 0;

        // Create message to remove
        Constructor<Message> constructor = Message.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Message message = constructor.newInstance();

        doReturn(message).when(rnSailthruMobileModuleSpy).getMessage(readableMap);

        // Initiate test
        rnSailthruMobileModuleSpy.registerMessageImpression(typeCode, readableMap);

        // Verify result
        verify(messageStream).registerMessageImpression(ImpressionType.IMPRESSION_TYPE_IN_APP_VIEW, message);
    }

    @Test
    public void testMarkMessageAsRead() throws Exception {
        // Create mocks
        ReadableMap readableMap = mock(ReadableMap.class);
        Promise promise = mock(Promise.class);

        // Create message to remove
        Constructor<Message> constructor = Message.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Message message = constructor.newInstance();

        RNSailthruMobileModule moduleSpy = spy(rnSailthruMobileModule);
        doReturn(message).when(moduleSpy).getMessage(readableMap);

        // Initiate test
        moduleSpy.markMessageAsRead(readableMap, promise);

        // Verify result
        verify(messageStream).setMessageRead(eq(message), any(MessageStream.MessagesReadHandler.class));
    }

    @Test
    public void testPresentMessageDetail() throws Exception {
        // Setup input
        String messageID = "message ID";

        // Setup mocks
        ReadableMap message = mock(ReadableMap.class);
        Activity activity = mock(Activity.class);
        Intent intent = mock(Intent.class);

        // Mock behaviour
        when(message.getString(RNSailthruMobileModule.MESSAGE_ID)).thenReturn(messageID);
        when(rnSailthruMobileModule.currentActivity()).thenReturn(activity);
        PowerMockito.when(MessageActivity.class, "intentForMessage", activity, null, messageID).thenReturn(intent);

        // Initiate test
        rnSailthruMobileModule.presentMessageDetail(message);

        // Verify result
        PowerMockito.verifyStatic(MessageActivity.class);
        MessageActivity.intentForMessage(activity, null, messageID);
        verify(activity).startActivity(intent);
    }

    @Test
    public void testGetRecommendations() {
        // Setup mocks
        Promise promise = mock(Promise.class);
        WritableArray writableArray = mock(WritableArray.class);
        Error error = mock(Error.class);
        String sectionID = "Section ID";

        // Initiate test
        rnSailthruMobileModuleSpy.getRecommendations(sectionID, promise);

        // Capture MessagesHandler to verify behaviour
        ArgumentCaptor<SailthruMobile.RecommendationsHandler> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.RecommendationsHandler.class);
        verify(sailthruMobile).getRecommendations(eq(sectionID), argumentCaptor.capture());
        SailthruMobile.RecommendationsHandler recommendationsHandler = argumentCaptor.getValue();

        // Replace native array with mock
        PowerMockito.doReturn(writableArray).when(rnSailthruMobileModuleSpy).getWritableArray();

        // Setup message array
        ArrayList<ContentItem> contentItems = new ArrayList<>();

        // Test success handler
        recommendationsHandler.onSuccess(contentItems);
        verify(promise).resolve(writableArray);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        recommendationsHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_RECOMMENDATIONS, errorMessage);
    }

    @Test
    public void testTrackClick() {
        // Create input
        Promise promise = mock(Promise.class);
        String sectionID = "Section ID";
        String urlString = "www.notarealurl.com";

        // Initiate test
        rnSailthruMobileModule.trackClick(sectionID, urlString, promise);

        // Verify result
        verify(sailthruMobile).trackClick(eq(sectionID), any(URI.class), any(SailthruMobile.TrackHandler.class));
    }

    @Test
    public void testTrackClickException() {
        // Create input
        Promise promise = mock(Promise.class);
        String sectionID = "Section ID";
        String urlString = "Wrong URL Format";

        // Initiate test
        rnSailthruMobileModule.trackClick(sectionID, urlString, promise);

        // Verify result
        verify(promise).reject(eq(RNSailthruMobileModule.ERROR_CODE_TRACKING), anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTrackPageview() {
        // Create input
        Promise promise = mock(Promise.class);
        String urlString = "www.notarealurl.com";

        // Initiate test
        rnSailthruMobileModule.trackPageview(urlString, null, promise);

        // Verify result
        verify(sailthruMobile).trackPageview(any(URI.class), (List<String>) isNull(), any(SailthruMobile.TrackHandler.class));
    }

    @Test
    public void testTrackPageviewException() {
        // Create input
        Promise promise = mock(Promise.class);
        String urlString = "Wrong URL Format";

        // Initiate test
        rnSailthruMobileModule.trackPageview(urlString, null, promise);

        // Verify result
        verify(promise).reject(eq(RNSailthruMobileModule.ERROR_CODE_TRACKING), anyString());
    }

    @Test
    public void testTrackImpression() {
        // Create input
        Promise promise = mock(Promise.class);
        String sectionID = "Section ID";
        String urlString = "www.notarealurl.com";
        ReadableArray readableArray = mock(ReadableArray.class);

        // Mock methods
        doReturn(1).when(readableArray).size();
        doReturn(urlString).when(readableArray).getString(anyInt());

        // Initiate test
        rnSailthruMobileModule.trackImpression(sectionID, readableArray, promise);

        // Verify result
        verify(sailthruMobile).trackImpression(eq(sectionID), ArgumentMatchers.<URI>anyList(), any(SailthruMobile.TrackHandler.class));
    }

    @Test
    public void testTrackImpressionException() {
        // Create input
        Promise promise = mock(Promise.class);
        String sectionID = "Section ID";
        String urlString = "Wrong URL Format";
        ReadableArray readableArray = mock(ReadableArray.class);

        // Mock methods
        doReturn(1).when(readableArray).size();
        doReturn(urlString).when(readableArray).getString(anyInt());

        // Initiate test
        rnSailthruMobileModule.trackImpression(sectionID, readableArray, promise);

        // Verify result
        verify(promise).reject(eq(RNSailthruMobileModule.ERROR_CODE_TRACKING), anyString());
    }

    @Test
    public void testSetGeoIPTrackingEnabled() {
        // Initiate test
        rnSailthruMobileModule.setGeoIPTrackingEnabled(true);

        // Verify result
        verify(sailthruMobile).setGeoIpTrackingEnabled(true);
    }

    @Test
    public void testSeGeoIPTrackingEnabledWithPromise() {
        // Create input
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Initiate test
        rnSailthruMobileModule.setGeoIPTrackingEnabled(false, promise);

        // Verify result
        @SuppressWarnings("unchecked")
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler<Void>> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);
        verify(sailthruMobile).setGeoIpTrackingEnabled(eq(false), argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler<Void> clearHandler = argumentCaptor.getValue();

        // Test success handler
        clearHandler.onSuccess(null);
        verify(promise).resolve(true);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        clearHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_DEVICE, errorMessage);
    }

    @Test
    public void testClearDevice() {
        // Create input
        int clearValue = SailthruMobile.ATTRIBUTES;
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Initiate test
        rnSailthruMobileModule.clearDevice(clearValue, promise);

        // Verify result
        @SuppressWarnings("unchecked")
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler<Void>> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);
        verify(sailthruMobile).clearDevice(eq(clearValue), argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler<Void> clearHandler = argumentCaptor.getValue();

        // Test success handler
        clearHandler.onSuccess(null);
        verify(promise).resolve(true);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        clearHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_DEVICE, errorMessage);
    }

    @Test
    public void testSetProfileVars() throws Exception {
        // Create input
        ReadableMap vars = mock(ReadableMap.class);
        JSONObject varsJson = mock(JSONObject.class);
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Mock methods
        PowerMockito.doReturn(varsJson).when(rnSailthruMobileModuleSpy).convertMapToJson(vars);

        // Initiate test
        rnSailthruMobileModuleSpy.setProfileVars(vars, promise);

        // Verify result
        @SuppressWarnings("unchecked")
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler<Void>> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);
        verify(sailthruMobile).setProfileVars(eq(varsJson), argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler<Void> setVarsHandler = argumentCaptor.getValue();

        // Test success handler
        setVarsHandler.onSuccess(null);
        verify(promise).resolve(true);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        setVarsHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_VARS, errorMessage);
    }

    @Test
    public void testGetProfileVars() throws Exception {
        // Create input
        JSONObject varsJson = new JSONObject();
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);
        WritableMap mockMap = mock(WritableMap.class);

        // Mock methods
        PowerMockito.doReturn(mockMap).when(rnSailthruMobileModuleSpy).convertJsonToMap(any(JSONObject.class));

        // Initiate test
        rnSailthruMobileModuleSpy.getProfileVars(promise);

        // Verify result
        @SuppressWarnings("unchecked")
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler<JSONObject>> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);
        verify(sailthruMobile).getProfileVars(argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler<JSONObject> getVarsHandler = spy(argumentCaptor.getValue());

        // Test success handler
        getVarsHandler.onSuccess(varsJson);
        verify(promise).resolve(mockMap);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        getVarsHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_VARS, errorMessage);
    }

    @Test
    public void testLogPurchase() throws Exception {
        // Create input
        ReadableMap purchaseMap = mock(ReadableMap.class);
        Purchase purchase = PowerMockito.mock(Purchase.class);
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Mock methods
        PowerMockito.doReturn(purchase).when(rnSailthruMobileModuleSpy).getPurchaseInstance(purchaseMap, promise);

        // Initiate test
        rnSailthruMobileModuleSpy.logPurchase(purchaseMap, promise);

        // Verify result
        @SuppressWarnings("unchecked")
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler<Void>> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);
        verify(sailthruMobile).logPurchase(eq(purchase), argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler<Void> purchaseHandler = argumentCaptor.getValue();

        // Test success handler
        purchaseHandler.onSuccess(null);
        verify(promise).resolve(true);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        purchaseHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_PURCHASE, errorMessage);
    }

    @Test
    public void testLogAbandonedCart() throws Exception {
        // Create input
        ReadableMap purchaseMap = mock(ReadableMap.class);
        Purchase purchase = mock(Purchase.class);
        Promise promise = mock(Promise.class);
        Error error = mock(Error.class);

        // Mock methods
        PowerMockito.doReturn(purchase).when(rnSailthruMobileModuleSpy).getPurchaseInstance(purchaseMap, promise);

        // Initiate test
        rnSailthruMobileModuleSpy.logAbandonedCart(purchaseMap, promise);

        // Verify result
        @SuppressWarnings("unchecked")
        ArgumentCaptor<SailthruMobile.SailthruMobileHandler<Void>> argumentCaptor = ArgumentCaptor.forClass(SailthruMobile.SailthruMobileHandler.class);
        verify(sailthruMobile).logAbandonedCart(eq(purchase), argumentCaptor.capture());
        SailthruMobile.SailthruMobileHandler<Void> purchaseHandler = argumentCaptor.getValue();

        // Test success handler
        purchaseHandler.onSuccess(null);
        verify(promise).resolve(true);

        // Setup error
        String errorMessage = "error message";
        when(error.getMessage()).thenReturn(errorMessage);

        // Test error handler
        purchaseHandler.onFailure(error);
        verify(promise).reject(RNSailthruMobileModule.ERROR_CODE_PURCHASE, errorMessage);
    }
}
