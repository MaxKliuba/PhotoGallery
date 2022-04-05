package com.maxclub.android.photogallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment

class PhotoPageActivity : SingleFragmentActivity() {
    override fun createFragment(): Fragment =
        PhotoPageFragment.newInstance(intent.data ?: Uri.EMPTY)

    companion object {
        fun newIntent(context: Context, photoPageUri: Uri): Intent =
            Intent(context, PhotoPageActivity::class.java).apply {
                data = photoPageUri
            }
    }
}