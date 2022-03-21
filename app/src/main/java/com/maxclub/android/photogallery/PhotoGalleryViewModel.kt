package com.maxclub.android.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class PhotoGalleryViewModel : ViewModel() {
    private val flickrFetcher = FlickrFetcher()
    val galleryItemLiveData: LiveData<List<GalleryItem>> = flickrFetcher.fetchPhotos()

    override fun onCleared() {
        super.onCleared()
        flickrFetcher.cancelFlickrRequest()
    }
}