package com.vydia.RNUploader

import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType

class StartUploadOptions(options: ReadableMap) {

  enum class RequestType {
    RAW, MULTIPART
  }

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

  init {
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