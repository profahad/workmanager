package com.versatilogics.apps.workmanagerex.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface ApiService {

    @Multipart
    @Headers(
        "Authorization: Client-ID 546c25a59c58ad7"
    )
    @POST("image")
    fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("title") title: RequestBody
    ): Call<UploadResponse>

    companion object {
        operator fun invoke(): ApiService {
            return Retrofit.Builder()
                .baseUrl("https://api.imgur.com/3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}