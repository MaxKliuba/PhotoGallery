package com.maxclub.android.photogallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import com.maxclub.android.photogallery.api.FlickrApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val LOG_TAG = "FlickrFetcher"

class FlickrFetcher {
    private val flickrApi: FlickrApi
    private lateinit var flickrRequest: Call<PhotoResponse>

    init {
        val gsonPhotoDeserializer = GsonBuilder()
            .registerTypeAdapter(PhotoResponse::class.java, PhotoDeserializer())
            .create()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create(gsonPhotoDeserializer))
            .build()

        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        flickrRequest = flickrApi.fetchPhotos()

        flickrRequest.enqueue(object : Callback<PhotoResponse> {
            override fun onFailure(call: Call<PhotoResponse>, t: Throwable) {
                Log.e(LOG_TAG, "Failed to fetch photos", t)
            }

            override fun onResponse(
                call: Call<PhotoResponse>,
                response: Response<PhotoResponse>
            ) {
                Log.d(LOG_TAG, "Response received")
                val photoResponse: PhotoResponse? = response.body()
                val galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?: mutableListOf()
                responseLiveData.value = galleryItems.filterNot { it.url.isBlank() }
            }
        })

        return responseLiveData
    }

    fun cancelFlickrRequest() {
        if (::flickrRequest.isInitialized) {
            flickrRequest.cancel()
        }
    }
}