package com.example.surf_club_android.model

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.policy.GlobalUploadPolicy
import com.cloudinary.android.policy.UploadPolicy
import com.example.surf_club_android.BuildConfig
import com.example.surf_club_android.base.MyApplication
import java.io.File
import java.io.FileOutputStream

class CloudinaryModel {

    companion object {
        private var isInitialized = false
    }

    init {
        if (!isInitialized) {
            val config = mapOf(
                "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
            )
            Log.d("CloudinaryModel", "Initializing Cloudinary: cloud_name=${BuildConfig.CLOUDINARY_CLOUD_NAME}")

            MyApplication.Globals.context?.let { context ->
                MediaManager.init(context, config)
                MediaManager.get().globalUploadPolicy = GlobalUploadPolicy.Builder()
                    .maxConcurrentRequests(3)
                    .networkPolicy(UploadPolicy.NetworkType.UNMETERED)
                    .build()
                isInitialized = true
            } ?: Log.e("CloudinaryModel", "Application context is null!")
        } else {
            Log.d("CloudinaryModel", "MediaManager is already initialized, skipping.")
        }
    }

    fun uploadBitmap(bitmap: Bitmap, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val context = MyApplication.Globals.context
        if (context == null) {
            onError("Application context is null")
            return
        }
        val file = bitmapToFile(bitmap, context)
        Log.d("CloudinaryModel", "Uploading file from path: ${file.absolutePath} with size: ${file.length()} bytes")

        MediaManager.get().upload(file.path)
            .option("folder", "images")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d("CloudinaryModel", "Upload started with requestId: $requestId")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    val progress = (bytes.toDouble() / totalBytes * 100).toInt()
                    Log.d("CloudinaryModel", "Upload progress: $progress%")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val publicUrl = resultData["secure_url"] as? String ?: ""
                    Log.d("CloudinaryModel", "Upload successful. URL: $publicUrl")
                    onSuccess(publicUrl)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    val errorMsg = error?.description ?: "Unknown error"
                    Log.e("CloudinaryModel", "Upload error: $errorMsg")
                    onError(errorMsg)
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    val errorMsg = error?.description ?: "Reschedule error"
                    Log.e("CloudinaryModel", "Upload rescheduled: $errorMsg")
                    onError(errorMsg)
                }
            })
            .dispatch()
    }

    fun bitmapToFile(bitmap: Bitmap, context: Context): File {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        Log.d("CloudinaryModel", "File created at ${file.absolutePath} with size: ${file.length()} bytes")
        return file
    }
}
