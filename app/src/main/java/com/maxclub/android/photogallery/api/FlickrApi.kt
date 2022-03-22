package com.maxclub.android.photogallery.api

import com.google.gson.GsonBuilder
import com.maxclub.android.photogallery.PhotoResponseDeserializer
import com.maxclub.android.photogallery.PhotoResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val API_KEY = "0485ec8c3aaf3a17ef7ee06898bc0b5a"

interface FlickrApi {
    @GET(
        "services/rest/?method=flickr.interestingness.getList" +
                "&api_key=$API_KEY" +
                "&format=json" +
                "&nojsoncallback=1" +
                "&extras=url_s"
    )
    suspend fun fetchPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
    ): Response<PhotoResponse>

    companion object {
        const val DEFAULT_PAGE = 1
        const val MAX_PAGE_SIZE = 500

        fun create(): FlickrApi {
            val gsonPhotoResponseDeserializer = GsonBuilder()
                .registerTypeAdapter(PhotoResponse::class.java, PhotoResponseDeserializer())
                .create()

            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("https://api.flickr.com/")
                .addConverterFactory(GsonConverterFactory.create(gsonPhotoResponseDeserializer))
                .build()

            return retrofit.create(FlickrApi::class.java)
        }
    }
}