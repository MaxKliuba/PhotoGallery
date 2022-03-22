package com.maxclub.android.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.maxclub.android.photogallery.api.FlickrApi

class PhotoGalleryViewModel : ViewModel() {
    private val flickrApi = FlickrApi.create()

    val galleryItemLiveData: LiveData<PagingData<GalleryItem>> =
        Pager(PagingConfig(pageSize = 100, maxSize = 1000)) {
            PhotoPagingSource(flickrApi)
        }.liveData
            .cachedIn(viewModelScope)
}