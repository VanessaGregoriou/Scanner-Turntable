package com.android.app.itemscanner.api

import android.net.Uri
import java.util.Date

data class ScanSession(
    val title: String,
    val numPhotos: Int = 180,
    val creationTime: Date = Date(),
    var image: Uri? = null,
    var zipFile: Uri? = null
)
