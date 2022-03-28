package com.maxclub.android.photogallery

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.*
import com.maxclub.android.photogallery.api.FlickrApi

class PhotoGalleryViewModel(private val app: Application) : AndroidViewModel(app) {
    private val flickrApi = FlickrApi.create()
    private val mutableSearchTerm = MutableLiveData(QueryPreferences.getStoredQuery(app))

    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""

    val galleryItemLiveData: LiveData<PagingData<GalleryItem>> =
        Transformations.switchMap(mutableSearchTerm) { searchTerm ->
            Pager(
                PagingConfig(
                    pageSize = FlickrApi.DEFAULT_PAGE_SIZE,
                    maxSize = 1000
                )
            ) {
                PhotoPagingSource(flickrApi, searchTerm)
            }.liveData
        }.cachedIn(viewModelScope)

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }
}