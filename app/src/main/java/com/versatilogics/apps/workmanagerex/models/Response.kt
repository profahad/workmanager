package com.versatilogics.apps.workmanagerex.models

import com.google.gson.annotations.SerializedName
import com.versatilogics.apps.workmanagerex.models.ImgData

data class Response(
    @SerializedName("data") val data: ImgData?,
    @SerializedName("success") val success: Boolean,
    @SerializedName("status") val status: Int
)