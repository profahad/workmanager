package com.versatilogics.apps.workmanagerex.models

import com.google.gson.annotations.SerializedName

data class ImgData(
    @SerializedName("id") var id: String?,
    @SerializedName("title") var title: String?,
    @SerializedName("description") var description: String?,
    @SerializedName("datetime") var datetime: Int?,
    @SerializedName("type") var type: String?,
    @SerializedName("animated") var animated: Boolean?,
    @SerializedName("width") var width: Int?,
    @SerializedName("height") var height: Int?,
    @SerializedName("size") var size: Int?,
    @SerializedName("views") var views: Int?,
    @SerializedName("bandwidth") var bandwidth: Int?,
    @SerializedName("vote") var vote: String?,
    @SerializedName("favorite") var favorite: Boolean?,
    @SerializedName("nsfw") var nsfw: String?,
    @SerializedName("section") var section: String?,
    @SerializedName("account_url") var accountUrl: String?,
    @SerializedName("account_id") var accountId: Int?,
    @SerializedName("is_ad") var isAd: Boolean?,
    @SerializedName("in_most_viral") var inMostViral: Boolean?,
    @SerializedName("has_sound") var hasSound: Boolean?,
    @SerializedName("tags") var tags: List<String>?,
    @SerializedName("ad_type") var adType: Int?,
    @SerializedName("ad_url") var adUrl: String?,
    @SerializedName("edited") var edited: String?,
    @SerializedName("in_gallery") var inGallery: Boolean?,
    @SerializedName("deletehash") var deletehash: String?,
    @SerializedName("name") var name: String?,
    @SerializedName("link") var link: String?
)