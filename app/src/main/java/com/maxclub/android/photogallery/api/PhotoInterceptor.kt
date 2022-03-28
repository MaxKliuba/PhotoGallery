package com.maxclub.android.photogallery.api

import android.util.Log
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val LOG_TAG = "PhotoInterceptor"

class PhotoInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()

        val newUrl: HttpUrl = originalRequest.url().newBuilder()
            .addQueryParameter("api_key", FlickrApi.API_KEY)
            .addQueryParameter("format", "json")
            .addQueryParameter("nojsoncallback", "1")
            .addQueryParameter("extras", "url_s")
            .addQueryParameter("safe_search", "1")
            .build()

        Log.d(LOG_TAG, "url: $newUrl")

        val newRequest: Request = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}