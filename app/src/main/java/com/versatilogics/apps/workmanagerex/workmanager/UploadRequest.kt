package com.versatilogics.apps.workmanagerex.workmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.versatilogics.apps.workmanagerex.network.ApiService
import com.versatilogics.apps.workmanagerex.network.ProgressRequestBody
import com.versatilogics.apps.workmanagerex.models.Response
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UploadRequest(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val channelId = "FILES_MANAGER"
    private val notificationId = 1

    override suspend fun doWork(): Result {
        val path = inputData.getString("image")
            ?: return Result.failure(workDataOf("error" to "path not found"))
        val response = uploadImage(File(path))
        return Result.success(workDataOf("output" to "${response.data?.link}"))
    }

    suspend fun uploadImage(file: File): Response {

        createNotificationChannel()

        val body = ProgressRequestBody(file, "image", object : ProgressRequestBody.UploadCallback {
            override fun onProgressUpdate(percentage: Int) {
                Log.d("UploadRequest", "onProgressUpdate: $percentage")
                createNotification(progressPercentage = percentage)
            }
        })
        val response = ApiService().coroutineUploadRequest(
            MultipartBody.Part.createFormData(
                "image",
                file.name,
                body
            ),
            RequestBody.create(MediaType.parse("multipart/form-data"), "json")
        )
        if (response.success)
            createNotification(isFinished = true)
        return response
    }

    private fun createNotification(
        maxPercentage: Int = 100,
        progressPercentage: Int = 0,
        isFinished: Boolean = false
    ) {
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Uploading")
            .setContentText("Uploading file...")
            .setSmallIcon(
                if (!isFinished) {
                    android.R.drawable.stat_sys_download
                } else {
                    android.R.drawable.stat_sys_download_done
                }
            )
            .setSilent(true)
            .setOngoing(
                !isFinished
            )

        with(NotificationManagerCompat.from(applicationContext)) {

            if (isFinished) {
                builder.setContentText("Upload complete.")
                builder.setProgress(0, 0, false)
            } else {
                builder.setContentText("$progressPercentage%")
                builder.setProgress(maxPercentage, progressPercentage, false)
            }

            notify(notificationId, builder.build())
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ (Android 8.0) because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Files Manager"
            val channelDescription = "Gate to download and upload files"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, name, importance)
            channel.apply {
                description = channelDescription
            }

            // Finally register the channel with system
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
