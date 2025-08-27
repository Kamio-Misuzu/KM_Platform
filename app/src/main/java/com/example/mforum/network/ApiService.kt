package com.example.mforum.network

import com.example.mforum.data.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
//     AI教学相关接口
    @GET("ai/teaching/videos")
    suspend fun getVideoTeachingResources(): Response<List<TeachingResource>>

    @GET("ai/teaching/texts")
    suspend fun getTextTeachingResources(): Response<List<TeachingResource>>

    @GET("ai/teaching/video/{id}")
    suspend fun getVideoTeachingDetail(@Path("id") id: Int): Response<TeachingDetail>

    @GET("ai/teaching/text/{id}")
    suspend fun getTextTeachingDetail(@Path("id") id: Int): Response<TeachingDetail>

//    用户注册接口
    @POST("auth/register")
    suspend fun registerUser(@Body user: User): Response<AuthResponse>

    @POST("auth/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<AuthResponse>

    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: Int): Response<User>

    @Multipart
    @POST("users/{userId}/avatar")
    suspend fun uploadAvatar(
        @Path("userId") userId: Int,
        @Part avatar: MultipartBody.Part
    ): Response<ResponseBody>

    @GET("users/{userId}/avatar")
    suspend fun getAvatar(@Path("userId") userId: Int): Response<ResponseBody>

    @GET("posts/{postId}")
    suspend fun getPost(@Path("postId") postId: Int): Response<Post>

    @POST("posts")
    suspend fun createPost(@Body post: Post): Response<Post>

    @GET("posts/{postId}/comments")
    suspend fun getComments(@Path("postId") postId: Int): Response<List<Comment>>

    @POST("posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: Int,
        @Body comment: Comment
    ): Response<Comment>

    // 修改 getPosts 方法的返回类型
    @GET("posts")
    suspend fun getPosts(): Response<PaginatedResponse<Post>>



}

data class LoginRequest(val username: String, val password: String)

// 添加AuthResponse数据类以匹配后端响应格式
data class AuthResponse(
    val message: String,
    val user: User,
    val access_token: String
)

// 添加分页响应数据类
data class PaginatedResponse<T>(
    val current_page: Int,
    val pages: Int,
    val posts: List<T>,
    val total: Int
)

data class TeachingResource(
    val id: Int,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val duration: String, // 视频时长或文本阅读时长
    val level: String // 难度级别
)

data class TeachingDetail(
    val id: Int,
    val title: String,
    val content: String, // 对于视频，可能是视频URL；对于文本，可能是HTML内容
    val type: String, // "video" 或 "text"
    val relatedResources: List<TeachingResource>
)

