package com.versatilogics.apps.workmanagerex.workmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.versatilogics.apps.workmanagerex.MainActivity
import com.versatilogics.apps.workmanagerex.models.ApiResponse
import com.versatilogics.apps.workmanagerex.network.ApiService
import com.versatilogics.apps.workmanagerex.network.ProgressRequestBody
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

    private suspend fun uploadImage(file: File): ApiResponse {

        createNotificationChannel()

        launchForegroundNotification()

        val body = ProgressRequestBody(file, "image", object : ProgressRequestBody.UploadCallback {
            override fun onProgressUpdate(percentage: Int) {
                Log.d("UploadRequest", "onProgressUpdate: $percentage")
                launchForegroundNotification(progressPercentage = percentage, isUpdating = true)
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
            stopForegroundNotification()

        return response
    }

    private fun launchForegroundNotification(
        maxPercentage: Int = 100,
        progressPercentage: Int = 0,
        isUpdating: Boolean = false
    ) {

        val pendingIntent: PendingIntent =
            Intent(applicationContext, MainActivity::class.java).let { notificationIntent ->
                notificationIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                PendingIntent.getActivity(
                    applicationContext,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("Uploading")
            .setContentText("0%")
            .setContentIntent(pendingIntent)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setSilent(true)
            .setProgress(100, 0, false)

        if (isUpdating) {
            with(NotificationManagerCompat.from(applicationContext)) {
                builder.setContentText("$progressPercentage%")
                builder.setProgress(maxPercentage, progressPercentage, false)
                notify(notificationId, builder.build())
            }
        } else {
            setForegroundAsync(ForegroundInfo(notificationId, builder.build()))
        }
    }

    private fun stopForegroundNotification() {
        with(NotificationManagerCompat.from(applicationContext)) {
            cancel(notificationId)
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
