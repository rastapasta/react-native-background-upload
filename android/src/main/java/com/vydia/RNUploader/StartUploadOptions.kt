package com.vydia.RNUploader

import androidx.work.Data
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeMap
import java.util.*

class StartUploadOptions(options: ReadableMap) {

  enum class RequestType {
    RAW, MULTIPART
  }

  val id: String
  val url: String
  val path: String
  var method = "POST"
    private set
  var requestType = RequestType.RAW
    private set
  var notificationChannel = "BackgroundUploadChannel"
    private set
  var discretionary = false
    private set
  var maxRetries = 2
    private set
  var headers: Map<String, String> = emptyMap()
    private set

  // For Multipart
  var parameters: Map<String, String> = emptyMap()
    private set
  var field: String = ""
    private set

  companion object {
    val KEY_ID = "id"
    val KEY_URL = "url"
    val KEY_PATH = "path"
    val KEY_METHOD = "method"
    val KEY_HEADERS = "headers"
    val KEY_PARAMETERS = "parameters"
  }

  fun toData(): Data {
    return Data.Builder().run {
      putString(KEY_ID, id)
      putString(KEY_URL, url)
      putString(KEY_METHOD, method)
      putString(KEY_PATH, path)
      val serializedHeaders = mutableListOf<String>()
      val serializedParameters = mutableListOf<String>()
      headers.forEach { (key, value) ->
        serializedHeaders.add(key)
        serializedHeaders.add(value)
      }
      parameters.forEach {(key, value) ->
        serializedParameters.add(key)
        serializedParameters.add(value)
      }
      putStringArray(KEY_HEADERS, serializedHeaders.toTypedArray())
      putStringArray(KEY_PARAMETERS, serializedParameters.toTypedArray())
      build()
    }
  }



  init {
    id = options.getString("customUploadId") ?: UUID.randomUUID().toString()
    url = options.getString("url") ?: throw InvalidUploadOptionException("Missing 'url' field.")
    path = options.getString("path") ?: throw InvalidUploadOptionException("Missing 'path' field.")
    method = options.getString("method") ?: method;

    if (options.hasKey("type")) {
      val requestType =
        options.getString("type") ?: throw InvalidUploadOptionException("type must be string.")

      if (requestType == "raw" && requestType != "multipart")
        throw InvalidUploadOptionException("type should be string: raw or multipart.")

      this.requestType = RequestType.valueOf(requestType.uppercase())
    }

    if (options.hasKey("maxRetries") && options.getType("maxRetries") == ReadableType.Number)
      maxRetries = options.getInt("maxRetries")

    if (options.hasKey("isDiscretionary"))
      discretionary = options.getBoolean("isDiscretionary")


    val parameters = options.getMap("parameters")
    if (parameters != null) {
      if (requestType != RequestType.MULTIPART)
        throw InvalidUploadOptionException("Parameters supported only in multipart type")

      val map = mutableMapOf<String, String>()
      val keys = parameters.keySetIterator()
      while (keys.hasNextKey()) {
        val key = keys.nextKey()
        map[key] = parameters.getString(key) ?: ""
      }

      this.parameters = map
    }

    if (requestType == RequestType.MULTIPART) {
      val field = options.getString("field")
        ?: throw InvalidUploadOptionException("field is required field for multipart type.")
      this.field = field
    }


    val headers = options.getMap("headers")
    if (headers != null) {
      val map = mutableMapOf<String, String>()
      val keys = headers.keySetIterator()
      while (keys.hasNextKey()) {
        val key = keys.nextKey()
        map[key] = headers.getString(key) ?: ""
      }

      this.headers = map
    }
  }
}

class InvalidUploadOptionException(message: String) : IllegalArgumentException(message) {}