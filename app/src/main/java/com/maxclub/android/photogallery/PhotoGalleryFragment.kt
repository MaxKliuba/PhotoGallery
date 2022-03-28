package com.maxclub.android.photogallery

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.squareup.picasso.Picasso
import kotlin.math.max

private const val LOG_TAG = "PhotoGalleryFragment"
private const val MIN_RECYCLER_VIEW_ITEM_WIDTH_DP = 180

class PhotoGalleryFragment : Fragment() {
    private val photoGalleryViewModel: PhotoGalleryViewModel by lazy {
        ViewModelProvider(this)[PhotoGalleryViewModel::class.java]
    }
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var floatingScrollButton: FloatingActionButton
    private lateinit var pagingPhotoAdapter: PagingPhotoAdapter
    private lateinit var photoRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

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

        floatingScrollButton = view.findViewById(R.id.floating_scroll_button)
        floatingScrollButton.apply {
            setOnClickListener {
                photoRecyclerView.smoothScrollToPosition(0)
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
            val gridLayoutManager = GridLayoutManager(context, 3).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (pagingPhotoAdapter.getItemViewType(position) == PagingPhotoAdapter.GALLERY_ITEM) 1
                        else getSpanCount(photoRecyclerView)
                    }
                }
            }
            layoutManager = gridLayoutManager
            adapter =
                pagingPhotoAdapter.withLoadStateFooter(LoadStatePhotoAdapter(pagingPhotoAdapter::retry))
            viewTreeObserver.addOnGlobalLayoutListener {
                gridLayoutManager.spanCount = getSpanCount(this)
            }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(-1)) {
                        floatingScrollButton.visibility = View.GONE
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    floatingScrollButton.visibility = if (dy >= 0) View.GONE else View.VISIBLE
                }
            })
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoGalleryViewModel.galleryItemLiveData.observe(viewLifecycleOwner) {
            pagingPhotoAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView
        searchView.apply {
            if (photoGalleryViewModel.searchTerm.isNotBlank()) {
                setQuery(photoGalleryViewModel.searchTerm, false)
                isIconified = false
                clearFocus()
            }
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.d(LOG_TAG, "QueryTextSubmit: $query")
                    photoGalleryViewModel.fetchPhotos(query)
                    clearFocus()
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    Log.d(LOG_TAG, "QueryTextChange: $newText")
                    return false
                }
            })

            setOnCloseListener {
                photoGalleryViewModel.fetchPhotos("")
                false
            }
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
            Picasso.get()
                .load(galleryItem.url)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.ic_placeholder)
                .into(itemView as ImageView)
        }
    }

    private class PagingPhotoAdapter(diffUtil: DiffUtil.ItemCallback<GalleryItem>) :
        PagingDataAdapter<GalleryItem, PhotoHolder>(diffUtil) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.list_item_gallery, parent, false)
            return PhotoHolder(view)
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = getItem(position)
            galleryItem?.let {
                holder.bind(it)
            }
        }

        override fun getItemViewType(position: Int): Int =
            if (position >= itemCount) LOAD_STATE_ITEM else GALLERY_ITEM

        companion object {
            const val GALLERY_ITEM = 0
            const val LOAD_STATE_ITEM = 1
        }
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