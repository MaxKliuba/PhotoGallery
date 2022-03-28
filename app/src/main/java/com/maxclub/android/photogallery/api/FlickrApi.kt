package com.maxclub.android.photogallery.api

import com.google.gson.GsonBuilder
import com.maxclub.android.photogallery.PhotoResponseDeserializer
import com.maxclub.android.photogallery.PhotoResponse
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApi {
    @GET("services/rest/?method=flickr.interestingness.getList")
    suspend fun fetchPhotos(
        @Query("page") page: Int = DEFAULT_PAGE,
        @Query("per_page") perPage: Int = DEFAULT_PAGE_SIZE,
    ): Response<PhotoResponse>

    @GET("services/rest?method=flickr.photos.search")
    suspend fun searchPhotos(
        @Query("text") query: String = "",
        @Query("page") page: Int = DEFAULT_PAGE,
        @Query("per_page") perPage: Int = DEFAULT_PAGE_SIZE,
    ): Response<PhotoResponse>

    companion object {
        const val API_KEY = "0485ec8c3aaf3a17ef7ee06898bc0b5a"
        const val DEFAULT_PAGE = 1
        const val DEFAULT_PAGE_SIZE = 100
        const val MAX_PAGE_SIZE = 500

        fun create(): FlickrApi {
            val gsonPhotoResponseDeserializer = GsonBuilder()
                .registerTypeAdapter(PhotoResponse::class.java, PhotoResponseDeserializer())
                .create()

            val client = OkHttpClient.Builder()
                .addInterceptor(PhotoInterceptor())
                .build()

            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("https://api.flickr.com/")
                .addConverterFactory(GsonConverterFactory.create(gsonPhotoResponseDeserializer))
                .client(client)
                .build()

            return retrofit.create(FlickrApi::class.java)
        }
    }
}