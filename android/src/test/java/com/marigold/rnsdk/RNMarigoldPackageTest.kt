package com.marigold.rnsdk

import android.content.Context
import com.facebook.react.bridge.JavaScriptModule
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.marigold.sdk.Marigold
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.junit.MockitoJUnitRunner
import java.util.List
import org.mockito.Mockito.verify

@RunWith(MockitoJUnitRunner::class)
class RNMarigoldPackageTest {
    @Mock
    private val mockContext: Context? = null

    @Mock
    private val reactApplicationContext: ReactApplicationContext? = null

    @Mock
    private val staticRnMarigoldModule: MockedConstruction<RNMarigoldModule>? = null

    @Mock
    private val staticMarigold: MockedConstruction<Marigold>? = null
    private var marigold: Marigold? = null
    private val appKey = "App Key"
    private var builder: RNMarigoldPackage.Builder? = null
    private var rnSTPackage: RNMarigoldPackage? = null

    @Before
    fun setup() {
        builder = RNMarigoldPackage.Builder.createInstance(mockContext, appKey).setDisplayInAppNotifications(false).setGeoIPTrackingDefault(false)
        rnSTPackage = RNMarigoldPackage(mockContext, appKey)
        marigold = staticMarigold.constructed().get(0)
    }

    @Test
    fun testConstructor() {
        verify(marigold).startEngine(mockContext, appKey)
    }

    @Test
    fun testBuilderConstructor() {
        val rnSTPackageFromBuilderConstructor = RNMarigoldPackage(builder)
        marigold = staticMarigold.constructed().get(1)
        verify(marigold).startEngine(mockContext, appKey)
        verify(marigold).setGeoIpTrackingDefault(false)
        verify(marigold).setGeoIpTrackingDefault(false)
        Assert.assertFalse(rnSTPackageFromBuilderConstructor.displayInAppNotifications)
    }

    @Test
    fun testCreateNativeModules() {
        val nativeModules: List<NativeModule> = rnSTPackage.createNativeModules(reactApplicationContext)
        Assert.assertEquals(1, nativeModules.size())
        val nativeModule: NativeModule = nativeModules[0]
        Assert.assertEquals(staticRnMarigoldModule.constructed().get(0), nativeModule)
    }

    @Test
    fun testCreateJSModules() {
        val jsModules: List<Class<out JavaScriptModule?>> = rnSTPackage.createJSModules()
        Assert.assertTrue(jsModules.isEmpty())
    }

    @Test
    fun testCreateViewManagers() {
        @SuppressWarnings("rawtypes") val viewManagers: List<com.facebook.react.uimanager.ViewManager> = rnSTPackage.createViewManagers(reactApplicationContext)
        Assert.assertTrue(viewManagers.isEmpty())
    }

    // Builder Tests
    @Test
    fun testBuilderCreateInstance() {
        val builder: RNMarigoldPackage.Builder = RNMarigoldPackage.Builder.createInstance(mockContext, appKey)
        Assert.assertEquals(mockContext, builder.context)
        Assert.assertEquals(appKey, builder.appKey)
    }

    @Test
    fun testBuilderSetDisplayInAppNotifications() {
        val builder: RNMarigoldPackage.Builder = RNMarigoldPackage.Builder.createInstance(mockContext, appKey)
                .setDisplayInAppNotifications(false)
        Assert.assertFalse(builder.displayInAppNotifications)
    }

    @Test
    fun testBuilderSetGeoIPTrackingDefault() {
        val builder: RNMarigoldPackage.Builder = RNMarigoldPackage.Builder.createInstance(mockContext, appKey)
                .setGeoIPTrackingDefault(false)
        Assert.assertFalse(builder.geoIPTrackingDefault)
    }

    @Test
    fun testBuilderBuild() {
        val rnSTPackageFromBuilderBuild: RNMarigoldPackage = builder.build()
        marigold = staticMarigold.constructed().get(1)
        verify(marigold).startEngine(mockContext, appKey)
        verify(marigold).setGeoIpTrackingDefault(false)
        Assert.assertFalse(rnSTPackageFromBuilderBuild.displayInAppNotifications)
    }
}
