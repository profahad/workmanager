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
import com.versatilogics.apps.workmanagerex.network.*
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
            uploadImage()
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
                val selectedImageUri = result.data?.data
                image_view.setImageURI(selectedImageUri)
                val parcelFileDescriptor =
                    contentResolver.openFileDescriptor(selectedImageUri!!, "r", null)

                val inputStream = FileInputStream(parcelFileDescriptor?.fileDescriptor)
                selectedFile = File(cacheDir, contentResolver.getFileName(selectedImageUri))
                val outputStream = FileOutputStream(selectedFile)
                inputStream.copyTo(outputStream)
            }

        }

    private fun uploadImage() {
        if (selectedFile == null) {
            layout_root.snackbar("Select an Image First")
            return
        }

        selectedFile?.let {
            progress_bar.progress = 0
            val body = UploadRequestBody(it, "image", object : UploadRequestBody.UploadCallback {
                override fun onProgressUpdate(percentage: Int) {
                    progress_bar.progress = percentage
                }
            })
            ApiService().uploadImage(
                MultipartBody.Part.createFormData(
                    "image",
                    it.name,
                    body
                ),
                RequestBody.create(MediaType.parse("multipart/form-data"), "json")
            ).enqueue(object : Callback<UploadResponse> {
                override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                    layout_root.snackbar(t.message!!)
                    progress_bar.progress = 0
                }

                override fun onResponse(
                    call: Call<UploadResponse>,
                    response: Response<UploadResponse>
                ) {
                    response.body()?.let {
                        layout_root.snackbar(it.data?.link ?: "N/A")
                        progress_bar.progress = 100
                    }
                }
            })

        }
    }

    private fun workManager() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val trackRequest = PeriodicWorkRequestBuilder<TrackRequest>(15, TimeUnit.MINUTES)
            .setInputData(workDataOf("input" to "Fahad"))
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