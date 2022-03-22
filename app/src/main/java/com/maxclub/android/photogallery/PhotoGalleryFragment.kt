package com.maxclub.android.photogallery

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlin.math.max

private const val LOG_TAG = "PhotoGalleryFragment"
private const val MIN_RECYCLER_VIEW_ITEM_WIDTH_DP = 180

class PhotoGalleryFragment : Fragment() {
    private val photoGalleryViewModel: PhotoGalleryViewModel by lazy {
        ViewModelProvider(this)[PhotoGalleryViewModel::class.java]
    }
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var pagingPhotoAdapter: PagingPhotoAdapter
    private lateinit var photoRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.apply {
            setColorSchemeResources(R.color.color_scheme_swipe_refresh_layout)
            setProgressBackgroundColorSchemeResource(R.color.color_background_swipe_refresh_layout)
            setOnRefreshListener {
                pagingPhotoAdapter.refresh()
            }
        }
        val loadStateContainer: View = view.findViewById(R.id.load_state_container)
        view.findViewById<Button>(R.id.retry_button).apply {
            setOnClickListener {
                pagingPhotoAdapter.retry()
            }
        }

        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        pagingPhotoAdapter = PagingPhotoAdapter(DiffUtilCallback())
        pagingPhotoAdapter.apply {
            addLoadStateListener {
                photoRecyclerView.isVisible =
                    it.refresh !is LoadState.Loading && it.refresh !is LoadState.Error
                swipeRefreshLayout.isRefreshing = it.refresh is LoadState.Loading
                loadStateContainer.isVisible = it.refresh is LoadState.Error
            }
        }
        photoRecyclerView.apply {
            val gridLayoutManager = GridLayoutManager(context, 1).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (pagingPhotoAdapter.getItemViewType(position) == 0) 1
                        else getSpanCount(photoRecyclerView)
                    }
                }
            }
            layoutManager = gridLayoutManager
            adapter = pagingPhotoAdapter.withLoadStateHeaderAndFooter(
                header = LoadStatePhotoAdapter(pagingPhotoAdapter::retry),
                footer = LoadStatePhotoAdapter(pagingPhotoAdapter::retry),
            )
            viewTreeObserver.addOnGlobalLayoutListener {
                gridLayoutManager.spanCount = getSpanCount(this)
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoGalleryViewModel.galleryItemLiveData.observe(viewLifecycleOwner) {
            pagingPhotoAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    private fun getSpanCount(view: View): Int {
        val minWidthPx =
            (MIN_RECYCLER_VIEW_ITEM_WIDTH_DP * Resources.getSystem().displayMetrics.density).toInt()
        return max(view.width / minWidthPx, 1)
    }

    private class PhotoHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private lateinit var galleryItem: GalleryItem

        fun bind(galleryItem: GalleryItem) {
            this.galleryItem = galleryItem
            (itemView as TextView).text = galleryItem.title
        }

        fun bindPlaceholder() {
            (itemView as TextView).text = ""
        }
    }

    private class PagingPhotoAdapter(diffUtil: DiffUtil.ItemCallback<GalleryItem>) :
        PagingDataAdapter<GalleryItem, PhotoHolder>(diffUtil) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val textView = TextView(parent.context)
            return PhotoHolder(textView)
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = getItem(position)
            galleryItem?.let {
                holder.bind(it)
            } ?: holder.bindPlaceholder()
        }

        override fun getItemViewType(position: Int): Int =
            if (position == itemCount) 1 else 0
    }

    private class DiffUtilCallback : DiffUtil.ItemCallback<GalleryItem>() {
        override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean =
            oldItem == newItem
    }

    private class LoadStatePhotoHolder(
        itemView: View,
        retry: () -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val linearProgressIndicator: LinearProgressIndicator =
            itemView.findViewById(R.id.linear_progress_indicator)
        private val errorTextView: TextView = itemView.findViewById(R.id.error_text_view)
        private val retry: Button = itemView.findViewById<Button>(R.id.retry_button)
            .also {
                it.setOnClickListener { retry() }
            }

        fun bind(loadState: LoadState) {
            linearProgressIndicator.visibility =
                if (loadState is LoadState.Loading) View.VISIBLE else View.GONE
            retry.visibility = if (loadState !is LoadState.Loading) View.VISIBLE else View.GONE
            errorTextView.visibility =
                if (loadState !is LoadState.Loading) View.VISIBLE else View.GONE
        }
    }

    private class LoadStatePhotoAdapter(
        private val retry: () -> Unit
    ) : LoadStateAdapter<LoadStatePhotoHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            loadState: LoadState
        ): LoadStatePhotoHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.load_state_item, parent, false)
            return LoadStatePhotoHolder(view, retry)
        }

        override fun onBindViewHolder(holder: LoadStatePhotoHolder, loadState: LoadState) =
            holder.bind(loadState)
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }
}