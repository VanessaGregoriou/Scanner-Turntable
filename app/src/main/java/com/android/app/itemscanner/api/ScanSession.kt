package com.android.app.itemscanner.api

import java.util.Date

data class ScanSession(
    val title: String,
    val image: String? = null,
    val numPhotos: Int = 180,
    val creationTime: Date = Date())