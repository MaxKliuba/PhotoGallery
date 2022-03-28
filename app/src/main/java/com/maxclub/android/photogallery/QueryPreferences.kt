package com.maxclub.android.photogallery

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

private const val PREF_SEARCH_QUERY = "searchQuery"

object QueryPreferences {
    fun getStoredQuery(context: Context): String =
        PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PREF_SEARCH_QUERY, "")!!

    fun setStoredQuery(context: Context, query: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {
                putString(PREF_SEARCH_QUERY, query)
            }
    }
}