package com.example.mforum.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.mforum.network.RetrofitInstance
import android.util.Log
import com.example.mforum.network.TeachingDetail
import com.example.mforum.network.TeachingResource

class TeachingRepository(context: Context) {
    private val apiService = RetrofitInstance.getApiService(context)

    suspend fun getVideoResources(): List<TeachingResource> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getVideoTeachingResources()
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("TeachingRepository", "获取视频教学资源异常: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun getTextResources(): List<TeachingResource> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTextTeachingResources()
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("TeachingRepository", "获取文本教学资源异常: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun getVideoDetail(id: Int): TeachingDetail? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getVideoTeachingDetail(id)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("TeachingRepository", "获取视频详情异常: ${e.message}")
                null
            }
        }
    }

    suspend fun getTextDetail(id: Int): TeachingDetail? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTextTeachingDetail(id)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("TeachingRepository", "获取文本详情异常: ${e.message}")
                null
            }
        }
    }
}