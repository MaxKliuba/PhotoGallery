package com.maxclub.android.photogallery

import com.google.gson.annotations.SerializedName

data class PhotoResponse(
    val page: Int,
    val pages: Int,
    @SerializedName("perpage") var perPage: Int,
    val total: Int,
    @SerializedName("photo") val galleryItems: List<GalleryItem>
)