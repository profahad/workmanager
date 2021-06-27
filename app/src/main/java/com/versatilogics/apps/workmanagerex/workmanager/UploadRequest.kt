package com.versatilogics.apps.workmanagerex.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.versatilogics.apps.workmanagerex.network.ApiService
import com.versatilogics.apps.workmanagerex.network.UploadRequestBody
import com.versatilogics.apps.workmanagerex.network.UploadResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UploadRequest(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val path = inputData.getString("image")
            ?: return Result.failure(workDataOf("error" to "path not found"))
        val response = uploadImage(File(path))
        return Result.success(workDataOf("output" to "${response.data?.link}"))
    }

    suspend fun uploadImage(file: File): UploadResponse {
        val body = UploadRequestBody(file, "image", object : UploadRequestBody.UploadCallback {
            override fun onProgressUpdate(percentage: Int) {
                Log.d("UploadRequest", "onProgressUpdate: $percentage")
            }
        })
        val response = ApiService().uploadImageThread(
            MultipartBody.Part.createFormData(
                "image",
                file.name,
                body
            ),
            RequestBody.create(MediaType.parse("multipart/form-data"), "json")
        )
        return response
    }
}
