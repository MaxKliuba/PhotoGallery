package com.maxclub.android.photogallery

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

class PhotoGalleryActivity : SingleFragmentActivity() {
    override fun createFragment(): Fragment = PhotoGalleryFragment.newInstance()

    companion object {
        fun newIntent(context: Context): Intent = Intent(context, PhotoGalleryActivity::class.java)
    }
}