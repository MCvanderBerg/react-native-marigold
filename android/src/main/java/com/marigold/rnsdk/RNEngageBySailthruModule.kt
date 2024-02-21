package com.marigold.rnsdk

import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.marigold.sdk.EngageBySailthru
import com.marigold.sdk.Marigold
import com.marigold.sdk.model.AttributeMap
import com.marigold.sdk.model.Purchase
import org.json.JSONException
import org.json.JSONObject
import java.lang.reflect.InvocationTargetException
import java.net.URI
import java.net.URISyntaxException
import java.util.ArrayList
import java.util.Date

class RNEngageBySailthruModule (reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    @VisibleForTesting
    var engage = EngageBySailthru()

    @VisibleForTesting
    var jsonConverter = JsonConverter()

    @ReactMethod
    fun logEvent(value: String) {
        engage.logEvent(value)
    }

    @ReactMethod
    fun logEvent(eventName: String, varsMap: ReadableMap) {
        var varsJson: JSONObject? = null
        try {
            varsJson = jsonConverter.convertMapToJson(varsMap)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        engage.logEvent(eventName, varsJson)
    }

    @ReactMethod
    @Deprecated("use setProfileVars instead")
    fun setAttributes(readableMap: ReadableMap, promise: Promise) {
        val attributeMap = try {
            getAttributeMap(readableMap)
        } catch (e: JSONException) {
            promise.reject(RNMarigoldModule.ERROR_CODE_DEVICE, e.message)
            return
        }
        engage.setAttributes(attributeMap, object : EngageBySailthru.AttributesHandler {
            override fun onSuccess() {
                promise.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise.reject(RNMarigoldModule.ERROR_CODE_DEVICE, error.message)
            }
        })
    }


    @ReactMethod
    fun setUserId(userId: String?, promise: Promise) {
        engage.setUserId(userId, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise.reject(RNMarigoldModule.ERROR_CODE_DEVICE, error.message)
            }
        })
    }

    @ReactMethod
    fun setUserEmail(userEmail: String?, promise: Promise) {
        engage.setUserEmail(userEmail, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise.resolve(null)
            }

            override fun onFailure(error: Error) {
                promise.reject(RNMarigoldModule.ERROR_CODE_DEVICE, error.message)
            }
        })
    }

    @ReactMethod
    fun trackClick(sectionId: String, url: String, promise: Promise) {
        try {
            val uri = URI(url)
            engage.trackClick(sectionId, uri, object : EngageBySailthru.TrackHandler {
                override fun onSuccess() {
                    promise.resolve(true)
                }

                override fun onFailure(error: Error) {
                    promise.reject(RNMarigoldModule.ERROR_CODE_TRACKING, error.message)
                }
            })
        } catch (e: URISyntaxException) {
            promise.reject(RNMarigoldModule.ERROR_CODE_TRACKING, e.message)
        }
    }

    @ReactMethod
    fun trackPageview(url: String?, tags: ReadableArray?, promise: Promise) {
        val uri = try {
            URI(url)
        } catch (e: URISyntaxException) {
            promise.reject(RNMarigoldModule.ERROR_CODE_TRACKING, e.message)
            return
        }
        var convertedTags: List<String?>? = null
        if (tags != null) {
            convertedTags = ArrayList()
            for (i in 0 until tags.size()) {
                convertedTags.add(tags.getString(i))
            }
        }
        engage.trackPageview(uri, convertedTags, object : EngageBySailthru.TrackHandler {
            override fun onSuccess() {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(RNMarigoldModule.ERROR_CODE_TRACKING, error.message)
            }
        })
    }

    @ReactMethod
    fun trackImpression(sectionId: String, urls: ReadableArray?, promise: Promise) {
        var convertedUrls: List<URI>? = null
        if (urls != null) {
            try {
                convertedUrls = ArrayList()
                for (i in 0 until urls.size()) {
                    convertedUrls.add(URI(urls.getString(i)))
                }
            } catch (e: URISyntaxException) {
                promise.reject(RNMarigoldModule.ERROR_CODE_TRACKING, e.message)
                return
            }
        }
        engage.trackImpression(sectionId, convertedUrls, object : EngageBySailthru.TrackHandler {
            override fun onSuccess() {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(RNMarigoldModule.ERROR_CODE_TRACKING, error.message)
            }
        })
    }

    @ReactMethod
    fun setProfileVars(vars: ReadableMap, promise: Promise) {
        val varsJson = try {
            jsonConverter.convertMapToJson(vars)
        } catch (e: JSONException) {
            promise.reject(RNMarigoldModule.ERROR_CODE_VARS, e.message)
            return
        }
        engage.setProfileVars(varsJson, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(value: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(RNMarigoldModule.ERROR_CODE_VARS, error.message)
            }
        })
    }

    @ReactMethod
    fun getProfileVars(promise: Promise) {
        engage.getProfileVars(object : Marigold.MarigoldHandler<JSONObject?> {
            override fun onSuccess(value: JSONObject?) {
                try {
                    val vars = value?.let { jsonConverter.convertJsonToMap(it) }
                    promise.resolve(vars)
                } catch (e: JSONException) {
                    promise.reject(RNMarigoldModule.ERROR_CODE_VARS, e.message)
                }
            }

            override fun onFailure(error: Error) {
                promise.reject(RNMarigoldModule.ERROR_CODE_VARS, error.message)
            }
        })
    }

    @ReactMethod
    fun logPurchase(purchaseMap: ReadableMap, promise: Promise) {
        val purchase = try {
            getPurchaseInstance(purchaseMap)
        } catch (e: JSONException) {
            promise.reject(RNMarigoldModule.ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: NoSuchMethodException) {
            promise.reject(RNMarigoldModule.ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: IllegalAccessException) {
            promise.reject(RNMarigoldModule.ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: InvocationTargetException) {
            promise.reject(RNMarigoldModule.ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: InstantiationException) {
            promise.reject(RNMarigoldModule.ERROR_CODE_PURCHASE, e.message)
            return
        }
        engage.logPurchase(purchase, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(aVoid: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(RNMarigoldModule.ERROR_CODE_PURCHASE, error.message)
            }
        })
    }

    @ReactMethod
    fun logAbandonedCart(purchaseMap: ReadableMap, promise: Promise) {
        val purchase = try {
            getPurchaseInstance(purchaseMap)
        } catch (e: JSONException) {
            promise.reject(RNMarigoldModule.ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: NoSuchMethodException) {
            promise.reject(RNMarigoldModule.ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: IllegalAccessException) {
            promise.reject(RNMarigoldModule.ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: InvocationTargetException) {
            promise.reject(RNMarigoldModule.ERROR_CODE_PURCHASE, e.message)
            return
        } catch (e: InstantiationException) {
            promise.reject(RNMarigoldModule.ERROR_CODE_PURCHASE, e.message)
            return
        }
        engage.logAbandonedCart(purchase, object : Marigold.MarigoldHandler<Void?> {
            override fun onSuccess(aVoid: Void?) {
                promise.resolve(true)
            }

            override fun onFailure(error: Error) {
                promise.reject(RNMarigoldModule.ERROR_CODE_PURCHASE, error.message)
            }
        })
    }

    override fun getName(): String {
        return "RNEngageBySailthru"
    }

    @VisibleForTesting
    @kotlin.Throws(JSONException::class, NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class)
    fun getPurchaseInstance(purchaseMap: ReadableMap): Purchase {
        val purchaseJson = jsonConverter.convertMapToJson(purchaseMap, false)
        val purchaseConstructor = Purchase::class.java.getDeclaredConstructor(JSONObject::class.java)
        purchaseConstructor.isAccessible = true
        return purchaseConstructor.newInstance(purchaseJson)
    }

    @VisibleForTesting
    @kotlin.Throws(JSONException::class)
    fun getAttributeMap(readableMap: ReadableMap): AttributeMap {
        val attributeMapJson = jsonConverter.convertMapToJson(readableMap)
        val attributes = attributeMapJson.getJSONObject("attributes")
        val attributeMap = AttributeMap()
        attributeMap.setMergeRules(attributeMapJson.getInt("mergeRule"))
        val keys = attributes.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val attribute = attributes.getJSONObject(key)
            val attributeType = attribute.getString("type")
            when (attributeType) {
                "string" -> attributeMap.putString(key, attribute.getString("value"))
                "stringArray" -> {
                    val array: ArrayList<String> = ArrayList()
                    val values = attribute.getJSONArray("value")
                    var i = 0
                    while (i < values.length()) {
                        array.add(values.get(i) as String)
                        i++
                    }
                    attributeMap.putStringArray(key, array)
                }

                "integer" -> attributeMap.putInt(key, attribute.getInt("value"))
                "integerArray" -> {
                    val array: ArrayList<Int> = ArrayList()
                    val values = attribute.getJSONArray("value")
                    var i = 0
                    while (i < values.length()) {
                        val j = values.getInt(i)
                        array.add(j)
                        i++
                    }
                    attributeMap.putIntArray(key, array)
                }

                "boolean" -> attributeMap.putBoolean(key, attribute.getBoolean("value"))
                "float" -> attributeMap.putFloat(key, attribute.getDouble("value").toFloat())
                "floatArray" -> {
                    val array: ArrayList<Float> = ArrayList()
                    val values = attribute.getJSONArray("value")
                    var i = 0
                    while (i < values.length()) {
                        val value = (values.get(i).toString()).toFloat()
                        array.add(value)
                        i++
                    }
                    attributeMap.putFloatArray(key, array)
                }

                "date" -> {
                    val value = Date(attribute.getLong("value"))
                    attributeMap.putDate(key, value)
                }

                "dateArray" -> {
                    val array: ArrayList<Date> = ArrayList()
                    val values = attribute.getJSONArray("value")
                    var i = 0
                    while (i < values.length()) {
                        val dateValue = values.getLong(i)
                        val date = Date(dateValue)
                        array.add(date)
                        i++
                    }
                    attributeMap.putDateArray(key, array)
                }
            }
        }
        return attributeMap
    }
}