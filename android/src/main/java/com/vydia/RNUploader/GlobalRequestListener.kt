package com.vydia.RNUploader

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import net.gotev.uploadservice.data.UploadInfo
import okhttp3.Response

class UploadCancelledException : Throwable("Upload Cancelled")

class GlobalRequestListener(var reactContext: ReactApplicationContext) {
  private val TAG = "UploadReceiver"

  companion object {
    var instance: GlobalRequestListener? = null
    fun initialize(reactContext: ReactApplicationContext) {
      val instance = this.instance
      if (instance == null)
        this.instance = GlobalRequestListener(reactContext)
      else
        instance.reactContext = reactContext
    }
  }

  fun onError(uploadId: String, exception: Throwable) {
    val params = Arguments.createMap()
    params.putString("id", uploadId)

    if (exception is UploadCancelledException) {
      sendEvent("cancelled", params);
      return;
    }

    // Make sure we do not try to call getMessage() on a null object
    if (exception.message != null) {
      params.putString("error", exception.message)
    } else {
      params.putString("error", "Unknown exception")
    }

    sendEvent("error", params)
  }

  fun onProgress(uploadInfo: UploadInfo) {
    val params = Arguments.createMap()
    params.putString("id", uploadInfo.uploadId)
    params.putInt("progress", uploadInfo.progressPercent) //0-100

    sendEvent("progress", params)
  }

  fun onSuccess(uploadId: String, response: Response) {
    val params = Arguments.createMap().apply {
      putString("id", uploadId)
      putInt("responseCode", response.code)
      putString("responseBody", response.body?.string())
      putMap("responseHeaders", Arguments.createMap().apply {
        response.headers.forEach { (key, value) ->
          putString(key, value)
        }
      })
    }
    sendEvent("completed", params)
  }

  /**
   * Sends an event to the JS module.
   */
  private fun sendEvent(eventName: String, params: WritableMap?) {
    // Right after JS reloads, react instance might not be available yet
    if (!reactContext.hasActiveCatalystInstance()) return

    try {
      val jsModule = reactContext.getJSModule(RCTDeviceEventEmitter::class.java)
      jsModule.emit("RNFileUploader-$eventName", params)
    } catch (exc: Throwable) {
      Log.e(TAG, "sendEvent() failed", exc);
    }
  }
}
