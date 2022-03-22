package com.maxclub.android.photogallery

import com.google.gson.annotations.SerializedName

data class PhotoResponse(
    var page: Int = 0,
    var pages: Int = 0,
    @SerializedName("perpage") var perPage: Int = 0,
    var total: Int = 0,
    @SerializedName("photo") var galleryItems: List<GalleryItem>
)