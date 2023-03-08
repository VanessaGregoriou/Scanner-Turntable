package com.android.app.itemscanner.api

import android.graphics.drawable.Drawable
import android.view.View.OnClickListener

interface SessionItemViewModel {
    fun getTitle(): String
    fun getThumbnail(): Drawable?
    fun getCreationData(): String
    fun getNumPhotos(): Int

    fun showExtraDetails(): Boolean

    fun onToggleDetailsClick(): OnClickListener
    fun onRenameClick(): OnClickListener
    fun onShareClick(): OnClickListener
    fun onDeleteClick(): OnClickListener
}