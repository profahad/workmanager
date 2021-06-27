package com.versatilogics.apps.workmanagerex

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.work.*
import com.bumptech.glide.Glide
import com.versatilogics.apps.workmanagerex.models.ApiResponse
import com.versatilogics.apps.workmanagerex.network.*
import com.versatilogics.apps.workmanagerex.workmanager.TrackRequest
import com.versatilogics.apps.workmanagerex.workmanager.UploadRequest
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private var selectedFile: File? = null
    private var selectedFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupListeners()
    }

    private fun setupListeners() {
        image_view.setOnClickListener {
            openImageChooser()
        }

        button_upload.setOnClickListener {
            uploaded_view.setImageResource(R.drawable.ic_view)
            if (workMangerThread.isChecked) {
                uploaderTask()
            } else {
                simpleUploader()
            }
        }
    }

    private fun openImageChooser() {
        Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startForResult.launch(it)
        }
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //  you will get result here in result.data
                selectedFileUri = result.data?.data
                image_view.setImageURI(selectedFileUri)
                val parcelFileDescriptor =
                    contentResolver.openFileDescriptor(selectedFileUri!!, "r", null)

                val inputStream = FileInputStream(parcelFileDescriptor?.fileDescriptor)
                selectedFile = File(cacheDir, contentResolver.getFileName(selectedFileUri!!))
                val outputStream = FileOutputStream(selectedFile)
                inputStream.copyTo(outputStream)
            }

        }

    private fun uploaderTask() {

        if (selectedFile == null) {
            layout_root.snackbar("Select an Image First")
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val uploadRequest = OneTimeWorkRequestBuilder<UploadRequest>()
            .setInputData(workDataOf("image" to selectedFile?.path))
            .setConstraints(constraints)
            .addTag("trackLog")
            .build()

        WorkManager.getInstance(applicationContext).enqueue(
            uploadRequest
        )

        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(uploadRequest.id)
            .observe(this, {
                if (it.state.isFinished) {
                    Log.d("UploadRequest", "workManager: ${it.outputData.getString("output")}")
                    layout_root.snackbar(it.outputData.getString("output") ?: "N/A")
                    Glide.with(this@MainActivity).load(it.outputData.getString("output"))
                        .into(uploaded_view)
                }
            })
    }

    private fun simpleUploader() {
        if (selectedFile == null) {
            layout_root.snackbar("Select an Image First")
            return
        }

        selectedFile?.let {
            progress_bar.progress = 0
            val body =
                ProgressRequestBody(it, "image", object : ProgressRequestBody.UploadCallback {
                    override fun onProgressUpdate(percentage: Int) {
                        progress_bar.progress = percentage
                    }
                })
            ApiService().simpleUploadRequest(
                MultipartBody.Part.createFormData(
                    "image",
                    it.name,
                    body
                ),
                RequestBody.create(MediaType.parse("multipart/form-data"), "json")
            ).enqueue(object : Callback<ApiResponse> {
                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    layout_root.snackbar(t.message!!)
                    progress_bar.progress = 0
                }

                override fun onResponse(
                    call: Call<ApiResponse>,
                    response: Response<ApiResponse>
                ) {
                    response.body()?.let {
                        layout_root.snackbar(it.data?.link ?: "N/A")
                        Glide.with(this@MainActivity).load(it.data?.link).into(uploaded_view)
                        progress_bar.progress = 100
                    }
                }
            })

        }
    }

    private fun repeatedTask() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val trackRequest = PeriodicWorkRequestBuilder<TrackRequest>(15, TimeUnit.MINUTES)
            .setInputData(workDataOf("image" to selectedFileUri?.path))
            .setConstraints(constraints)
            .addTag("trackLog")
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "trackLogs",
            ExistingPeriodicWorkPolicy.KEEP,
            trackRequest
        )

        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(trackRequest.id)
            .observe(this, {
                if (it.state.isFinished) {
                    Log.d("TrackRequest", "workManager: ${it.outputData.getString("output")}")
                }
            })
    }

}