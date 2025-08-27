package com.example.mforum.ui.forum

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mforum.data.Post
import com.example.mforum.data.PostRepository
import com.example.mforum.data.User
import com.example.mforum.data.UserRepository
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ForumViewModel(application: Application) : AndroidViewModel(application) {
    private val postRepository = PostRepository(application)
    private val userRepository = UserRepository(application)

    val posts = MutableLiveData<List<Post>>()
    private var currentUser: User? = null
    private val userCache = mutableMapOf<Int, User>()

    fun setCurrentUser(user: User?) {
        currentUser = user
    }

    fun loadPosts() {
        viewModelScope.launch {
            val postList = postRepository.getPosts()
            posts.value = postList

            // 预加载用户信息
            postList.forEach { post ->
                if (!userCache.containsKey(post.authorId)) {
                    loadUserInfo(post.authorId)
                }
            }
        }
    }

    private fun loadUserInfo(userId: Int) {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser(userId)
            user?.let {
                userCache[userId] = it
                // 通知适配器更新相关帖子
                posts.value = posts.value // 触发观察者更新
            }
        }
    }

    fun getUserInfo(userId: Int): User? {
        return userCache[userId]
    }

    fun createPost(title: String, content: String, category: String, authorId: Int, callback: (Boolean) -> Unit) {
        val post = Post(
            authorId = authorId,
            title = title,
            content = content,
            category = category
        )

        viewModelScope.launch {
            try {
                val createdPost = postRepository.createPost(post)
                if (createdPost != null) {
                    // 刷新帖子列表
                    loadPosts()
                    callback(true)
                } else {
                    callback(false)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ForumViewModel", "创建帖子异常: ${e.message}")
                callback(false)
            }
        }
    }
}