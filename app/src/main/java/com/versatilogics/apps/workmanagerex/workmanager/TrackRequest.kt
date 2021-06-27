package com.versatilogics.apps.workmanagerex.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

class TrackRequest(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val developer = inputData.getString("input")
        delay(3000)
        return Result.success(workDataOf("output" to "$developer is here"))
    }
}
