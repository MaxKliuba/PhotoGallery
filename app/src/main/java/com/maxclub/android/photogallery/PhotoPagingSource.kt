package com.maxclub.android.photogallery

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.maxclub.android.photogallery.api.FlickrApi
import retrofit2.HttpException

class PhotoPagingSource(private val flickrApi: FlickrApi) :
    PagingSource<Int, GalleryItem>() {
    override fun getRefreshKey(state: PagingState<Int, GalleryItem>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryItem> =
        try {
            val page = params.key ?: FlickrApi.DEFAULT_PAGE
            val perPage = params.loadSize.coerceAtMost(FlickrApi.MAX_PAGE_SIZE)

            val response = flickrApi.fetchPhotos(page, perPage)
            if (response.isSuccessful) {
                val photoResponse: PhotoResponse? = response.body()
                photoResponse?.let {
                    val galleryItems = it.galleryItems.filterNot { galleryItem ->
                        galleryItem.url.isBlank()
                    }
                    val prevPage = if (page <= FlickrApi.DEFAULT_PAGE) null else page - 1
                    val nextPage = if (it.pages >= page + 1) null else page + 1

                    LoadResult.Page(
                        data = galleryItems,
                        prevKey = prevPage,
                        nextKey = nextPage,
                    )
                } ?: LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null,
                )
            } else {
                LoadResult.Error(HttpException(response))
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
}
