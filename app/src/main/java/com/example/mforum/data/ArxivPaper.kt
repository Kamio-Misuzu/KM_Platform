package com.example.mforum.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ArxivPaper(
    val id: String,
    val title: String,
    val authors: List<String>,
    val abstract: String,
    val publishedDate: String,
    val pdfUrl: String
) : Parcelable