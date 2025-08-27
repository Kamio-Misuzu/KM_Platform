package com.example.mforum.ui.mine

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mforum.data.User
import com.example.mforum.data.UserRepository
import com.example.mforum.utils.PreferencesManager
import kotlinx.coroutines.launch
import java.io.File

class MineViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application)
    private val prefs = PreferencesManager(application)
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        checkLoginStatus()

    }

    fun checkLoginStatus() {
        val userId = prefs.getUserId()
        _isLoggedIn.value = userId != -1

        if (userId != -1) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val user = repository.getCurrentUser(userId)
                    _currentUser.value = user
                } catch (e: Exception) {
                    _errorMessage.value = "获取用户信息失败: ${e.message}"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    suspend fun login(username: String, password: String): User? {
        _isLoading.value = true
        _errorMessage.value = null

        return try {
            val user = repository.login(username, password)
            if (user != null) {
                prefs.saveUserId(user.id)
                _currentUser.value = user
                _isLoggedIn.value = true
                user
            } else {
                _errorMessage.value = "用户名或密码错误"
                null
            }
        } catch (e: Exception) {
            _errorMessage.value = "登录失败: ${e.message}"
            null
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun register(username: String, email: String, password: String): Boolean {
        _isLoading.value = true
        _errorMessage.value = null

        return try {
            val success = repository.register(username, email, password)
            if (!success) {
                _errorMessage.value = "注册失败，用户名或邮箱可能已存在"
            }
            success
        } catch (e: Exception) {
            _errorMessage.value = "注册失败: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun uploadAvatar(imageFile: File): Boolean {
        val userId = prefs.getUserId()
        if (userId == -1) return false

        return try {
            val success = repository.uploadAvatar(userId, imageFile)
            if (success) {
                val user = repository.getCurrentUser(userId)
                user?.let {
                    _currentUser.postValue(it) // 使用postValue确保在主线程更新
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun logout() {
        prefs.clearUserData()
        _currentUser.value = null
        _isLoggedIn.value = false
    }


}