package com.maxclub.android.photogallery

import com.google.gson.annotations.SerializedName

data class PhotoResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("pages") val pages: Int,
    @SerializedName("perpage") val perPage: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("photo") val galleryItems: List<GalleryItem>,
)