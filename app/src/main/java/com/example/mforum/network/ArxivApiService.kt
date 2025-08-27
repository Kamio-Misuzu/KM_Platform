// ArxivApiService.kt
package com.example.mforum.network

import com.example.mforum.data.ArxivPaper
import retrofit2.http.GET
import retrofit2.http.Query

interface ArxivApiService {
    @GET("query")
    suspend fun searchPapers(
        @Query("search_query") query: String,
        @Query("start") start: Int = 0,
        @Query("max_results") maxResults: Int = 25
    ): io.ktor.client.statement.HttpResponse
}