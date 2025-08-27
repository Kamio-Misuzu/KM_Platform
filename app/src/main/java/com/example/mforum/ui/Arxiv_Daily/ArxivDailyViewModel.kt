package com.example.mforum.ui.Arxiv_Daily

import androidx.lifecycle.ViewModel
import com.example.mforum.data.ArxivPaper

class ArxivDailyViewModel : ViewModel() {
    var papers: List<ArxivPaper> = emptyList()
    var selectedCategoryIndex = 0
    var selectedNumberIndex = 9 // 默认选择10篇
    var listViewScrollPosition = 0
}