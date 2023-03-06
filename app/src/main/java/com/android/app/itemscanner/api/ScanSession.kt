package com.android.app.itemscanner.api

import android.graphics.Bitmap
import android.net.Uri
import java.util.Date

data class ScanSession(
    val title: String,
    val numPhotos: Int = 180,
    val creationTime: Date = Date(),
    var thumbnail: Bitmap? = null,
    var zipFile: Uri? = null
)
