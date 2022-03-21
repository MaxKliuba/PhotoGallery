package com.maxclub.android.photogallery.api

import com.maxclub.android.photogallery.PhotoResponse
import retrofit2.Call
import retrofit2.http.GET

private const val API_KEY = "0485ec8c3aaf3a17ef7ee06898bc0b5a"

interface FlickrApi {
    @GET(
        "services/rest/?method=flickr.interestingness.getList" +
                "&api_key=$API_KEY" +
                "&format=json" +
                "&nojsoncallback=1" +
                "&extras=url_s"
    )
    fun fetchPhotos(): Call<PhotoResponse>
}