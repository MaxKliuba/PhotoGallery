package com.maxclub.android.photogallery

import android.net.Uri
import com.google.gson.annotations.SerializedName

data class GalleryItem(
    @SerializedName("id") val id: String = "",
    @SerializedName("owner") val owner: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("url_s") val url: String = "",
) {
    val photoPageUri: Uri
        get() = Uri.parse("https://www.flickr.com/photos/")
            .buildUpon()
            .appendPath(owner)
            .appendPath(id)
            .build()
}