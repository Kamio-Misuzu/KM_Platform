package com.example.mforum.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.mforum.network.RetrofitInstance
import retrofit2.HttpException
import android.util.Log

class PostRepository(context: Context) {
    private val apiService = RetrofitInstance.getApiService(context)

    suspend fun getPosts(): List<Post> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPosts()
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!.posts // 提取 posts 数组
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("PostRepository", "获取帖子列表异常: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun getPost(postId: Int): Post? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPost(postId)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("PostRepository", "获取帖子异常: ${e.message}")
                null
            }
        }
    }

    suspend fun createPost(post: Post): Post? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createPost(post)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("PostRepository", "创建帖子异常: ${e.message}")
                null
            }
        }
    }

    suspend fun getComments(postId: Int): List<Comment> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getComments(postId)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("PostRepository", "获取评论异常: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun createComment(comment: Comment): Comment? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createComment(comment.postId, comment)
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("PostRepository", "创建评论异常: ${e.message}")
                null
            }
        }
    }
}