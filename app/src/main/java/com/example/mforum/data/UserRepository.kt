package com.example.mforum.data

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.mforum.network.RetrofitInstance
import com.example.mforum.network.LoginRequest
import com.example.mforum.network.AuthResponse
import com.example.mforum.utils.PreferencesManager
import retrofit2.HttpException
import android.util.Log
import androidx.lifecycle.MutableLiveData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UserRepository(context: Context) {
    private val apiService = RetrofitInstance.getApiService(context)
    private val prefs = PreferencesManager(context)

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    suspend fun register(username: String, email: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 输入验证
                if (username.length < 3) {
                    Log.e("UserRepository", "用户名太短")
                    return@withContext false
                }

                if (password.length < 6) {
                    Log.e("UserRepository", "密码太短")
                    return@withContext false
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Log.e("UserRepository", "邮箱格式不正确")
                    return@withContext false
                }

                // 创建用户对象
                val user = User(
                    username = username,
                    email = email,
                    password = password
                )

                // 调用API注册用户
                val response = apiService.registerUser(user)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    // 保存token和用户ID
                    prefs.saveToken(authResponse.access_token)
                    prefs.saveUserId(authResponse.user.id)
                    Log.d("UserRepository", "注册成功: ${authResponse.user}")
                    true
                } else {
                    val errorBody = response.errorBody()?.string() ?: "未知错误"
                    Log.e("UserRepository", "注册失败: $errorBody")
                    false
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "注册异常: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun login(username: String, password: String): User? {
        return withContext(Dispatchers.IO) {
            try {
                // 创建登录请求
                val loginRequest = LoginRequest(username, password)

                // 调用API登录
                val response = apiService.loginUser(loginRequest)

                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    // 保存token和用户ID
                    prefs.saveToken(authResponse.access_token)
                    prefs.saveUserId(authResponse.user.id)
                    Log.d("UserRepository", "登录成功: ${authResponse.user}")
                    authResponse.user
                } else {
                    Log.e("UserRepository", "登录失败: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "登录异常: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    // 上传头像
    suspend fun uploadAvatar(userId: Int, imageFile: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!imageFile.exists()) {
                    Log.e("UserRepository", "文件不存在: ${imageFile.absolutePath}")
                    return@withContext false
                }

                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val avatarPart = MultipartBody.Part.createFormData("avatar", imageFile.name, requestFile)

                val response = apiService.uploadAvatar(userId, avatarPart)
                if (response.isSuccessful) {
                    Log.d("UserRepository", "头像上传成功")

                    // 更新本地缓存
                    val userResponse = apiService.getUser(userId)
                    if (userResponse.isSuccessful && userResponse.body() != null) {
                        _currentUser.postValue(userResponse.body()) // 现在可以使用_currentUser了
                        Log.d("UserRepository", "用户信息已更新")
                    } else {
                        Log.e("UserRepository", "更新用户信息失败: ${userResponse.errorBody()?.string()}")
                    }

                    true
                } else {
                    Log.e("UserRepository", "头像上传失败: ${response.errorBody()?.string()}")
                    false
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "头像上传异常", e)
                false
            }
        }
    }

    suspend fun getCurrentUser(userId: Int): User? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUser(userId)
                if (response.isSuccessful && response.body() != null) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "获取用户信息异常: ${e.message}")
                null
            }
        }
    }

    suspend fun getUser(userId: Int): User? {
        return try {
            val response = apiService.getUser(userId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}