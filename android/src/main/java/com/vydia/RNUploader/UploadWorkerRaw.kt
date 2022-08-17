package com.vydia.RNUploader

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vydia.RNUploader.StartUploadOptions.Companion.KEY_HEADERS
import com.vydia.RNUploader.StartUploadOptions.Companion.KEY_ID
import com.vydia.RNUploader.StartUploadOptions.Companion.KEY_PATH
import com.vydia.RNUploader.StartUploadOptions.Companion.KEY_URL
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


class UploadWorkerRaw(appContext: Context, params: WorkerParameters) :
  CoroutineWorker(appContext, params) {
  companion object {
    private val client = OkHttpClient()
  }

  override suspend fun doWork(): Result {
    val id =
      inputData.getString(KEY_ID)
        ?: throw IllegalArgumentException("`id` cannot be null")
    val url =
      inputData.getString(KEY_URL)
        ?: throw IllegalArgumentException("`url` cannot be null")
    val path =
      inputData.getString(KEY_PATH)
        ?: throw IllegalArgumentException("`path` cannot be null")
    val headers = inputData.getStringArray(KEY_HEADERS) ?: emptyArray()


    val request = Request.Builder().run {
      url(url)
      post(File(path).asRequestBody())
      for (i in headers.indices step 2) {
        addHeader(headers[i], headers[i + 1])
      }
      build()
    }

    return try {
      val response = client.newCall(request).execute()
      GlobalRequestListener.instance?.onSuccess(id, response)
      Result.success()
    } catch (error: Throwable) {
      Result.retry()
    }
  }
}