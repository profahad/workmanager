package com.versatilogics.apps.workmanagerex

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import java.lang.Exception

class TrackRequest(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val developer = inputData.getString("input")
        delay(3000)
        Log.d("TrackRequest", "doWork: output $developer is here")
        return Result.success(workDataOf("output" to "$developer is here"))
    }
}
