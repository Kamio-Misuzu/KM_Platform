package com.example.mforum.ui.forum

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mforum.data.Comment
import com.example.mforum.data.Post
import com.example.mforum.data.PostRepository
import com.example.mforum.data.User
import com.example.mforum.data.UserRepository
import kotlinx.coroutines.launch

class PostDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val postRepository = PostRepository(application)
    private val userRepository = UserRepository(application)

    val commentAuthors = MutableLiveData<Map<Int, User>>()
    private val authorsCache = mutableMapOf<Int, User>()

    val post = MutableLiveData<Post?>()
    val author = MutableLiveData<User?>()
    val comments = MutableLiveData<List<Comment>>()

    fun loadPost(postId: Int) {
        viewModelScope.launch {
            val postData = postRepository.getPost(postId)
            post.value = postData
        }
    }

    fun loadAuthor(authorId: Int) {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser(authorId)
            author.value = user
        }
    }

    fun loadComments(postId: Int) {
        viewModelScope.launch {
            val commentList = postRepository.getComments(postId)
            comments.value = commentList
        }
    }

    fun createComment(content: String, authorId: Int) {
        post.value?.let { currentPost ->
            val comment = Comment(
                postId = currentPost.id,
                authorId = authorId,
                content = content
            )

            viewModelScope.launch {
                val createdComment = postRepository.createComment(comment)
                if (createdComment != null) {
                    // 刷新评论列表
                    loadComments(currentPost.id)
                }
            }
        }
    }

    fun loadCommentAuthor(authorId: Int) {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser(authorId)
            user?.let {
                authorsCache[authorId] = it
                commentAuthors.value = authorsCache.toMap()
            }
        }
    }

    fun hasUserInfo(userId: Int): Boolean {
        return authorsCache.containsKey(userId)
    }
}