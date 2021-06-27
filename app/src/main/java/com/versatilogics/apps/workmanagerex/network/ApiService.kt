package com.versatilogics.apps.workmanagerex.network

import com.versatilogics.apps.workmanagerex.configs.CONSTANTS
import com.versatilogics.apps.workmanagerex.models.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {

    @Multipart
    @Headers(
        "Authorization: Client-ID ${CONSTANTS.CREDENTIALS.CLIENT_ID}"
    )
    @POST(CONSTANTS.API.UPLOAD_IMAGE)
    fun simpleUploadRequest(
        @Part image: MultipartBody.Part,
        @Part(CONSTANTS.KEYS.TITLE) title: RequestBody
    ): Call<ApiResponse>


    @Multipart
    @Headers(
        "Authorization: Client-ID ${CONSTANTS.CREDENTIALS.CLIENT_ID}"
    )
    @POST(CONSTANTS.API.UPLOAD_IMAGE)
    suspend fun coroutineUploadRequest(
        @Part image: MultipartBody.Part,
        @Part(CONSTANTS.KEYS.TITLE) title: RequestBody
    ): ApiResponse

    companion object {
        operator fun invoke(): ApiService {
            return Retrofit.Builder()
                .baseUrl(CONSTANTS.CONFIG.SERVER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}