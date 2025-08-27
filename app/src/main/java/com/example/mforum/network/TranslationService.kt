package com.example.mforum.network

import com.example.mforum.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class TranslationService(private val prefs: PreferencesManager) {

    // 创建一个带有超时时间的 OkHttpClient
//    `lazy` 是一个延迟初始化属性委托（delegate）的函数。它用于实现惰性初始化：
    //    即第一次访问该属性时才会进行初始化，并且之后再次访问该属性时会直接返回之前初始化好的值。
    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS) // 连接时长, 以下同
            .readTimeout(120, TimeUnit.SECONDS)    // 读取
            .writeTimeout(120, TimeUnit.SECONDS)   // 写入
            .build()
    }

    suspend fun translateWithSiliconFlow(
        text: String,
        targetLang: String,
        apiKey: String? = null,
        modelId: String? = null
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                val finalApiKey = apiKey ?: prefs.getSiliconFlowApiKey() ?: throw Exception("API密钥未设置")
                val finalModelId = modelId ?: prefs.getSiliconFlowModelId() ?: "deepseek-ai/DeepSeek-V3"

                // 如果文本过长，进行截断
                val processedText = if (text.length > 6000) {
                    text.substring(0, 6000) + "...[文本过长已截断]"
                } else {
                    text
                }

                val langPrompt = when (targetLang) {
                    "中文" -> "请将以下内容翻译成专业、流畅、自然的中文："
                    "中文繁体" -> "请将以下内容翻译成专业、流畅、自然的繁体中文："
                    "日文" -> "请将以下内容翻译成专业、流畅、自然的日语："
                    else -> "请将以下内容翻译成专业、流畅、自然的中文："
                }

                val fullPrompt = "$langPrompt\n\n$processedText"

                // 构建请求体
                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", fullPrompt)
                    })
                }

                val json = JSONObject().apply {
                    put("model", finalModelId)
                    put("messages", messages)
                    put("temperature", 0.3)
                    put("max_tokens", 2048)
                    put("stream", false)
                }

                val requestBody = okhttp3.RequestBody.create(
                    "application/json; charset=utf-8".toMediaTypeOrNull(),
                    json.toString()
                )

                val request = Request.Builder()
                    .url("https://api.siliconflow.cn/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $finalApiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        if (jsonResponse.has("choices")) {
                            val choices = jsonResponse.getJSONArray("choices")
                            if (choices.length() > 0) {
                                val message = choices.getJSONObject(0).getJSONObject("message")
                                val translation = message.getString("content")
                                "[使用 硅基流动API ($finalModelId) 翻译]\n\n$translation"
                            } else {
                                "[翻译错误]\n\n响应中没有翻译结果\n\n响应体: $responseBody\n\n原始文本:\n$processedText"
                            }
                        } else if (jsonResponse.has("error")) {
                            val error = jsonResponse.getJSONObject("error")
                            val errorMessage = error.optString("message", "未知错误")
                            "[翻译错误]\n\nAPI错误: $errorMessage\n\n响应体: $responseBody\n\n原始文本:\n$processedText"
                        } else {
                            "[翻译错误]\n\n响应格式不正确\n\n响应体: $responseBody\n\n原始文本:\n$processedText"
                        }
                    } catch (e: Exception) {
                        "[解析响应失败]\n\n错误: ${e.message}\n\n响应体: $responseBody\n\n原始文本:\n$processedText"
                    }
                } else {
                    val errorMsg = if (responseBody != null) {
                        try {
                            val errorJson = JSONObject(responseBody)
                            if (errorJson.has("error")) {
                                val error = errorJson.getJSONObject("error")
                                error.optString("message", "未知错误")
                            } else {
                                "HTTP ${response.code}: $responseBody"
                            }
                        } catch (e: Exception) {
                            "HTTP ${response.code}: $responseBody"
                        }
                    } else {
                        "HTTP ${response.code}: 无响应体"
                    }
                    "[翻译错误]\n\n$errorMsg\n\n原始文本:\n$processedText"
                }
            } catch (e: Exception) {
                if (e is java.net.SocketTimeoutException) {
                    "[翻译超时]\n\n请求超时，请检查网络连接或稍后重试\n\n原始文本:\n$text"
                } else {
                    "[翻译错误]\n\n异常: ${e.message}\n\n原始文本:\n$text"
                }
            }
        }
    }

    suspend fun translateWithYoudao(text: String, targetLang: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // 如果文本过长，进行截断
                val processedText = if (text.length > 6000) {
                    text.substring(0, 6000) + "...[文本过长已截断]"
                } else {
                    text
                }

                val encodedText = URLEncoder.encode(processedText, "UTF-8")
                val langCode = when (targetLang) {
                    "中文" -> "zh-CHS"
                    "中文繁体" -> "zh-CHT"
                    "日文" -> "ja"
                    else -> "zh-CHS"
                }

                val url = "https://60s.viki.moe/v2/fanyi?text=$encodedText&to=$langCode"

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        if (jsonResponse.has("data")) {
                            val data = jsonResponse.getJSONObject("data")
                            if (data.has("target")) {
                                val target = data.getJSONObject("target")
                                val translation = target.getString("text")
                                "[使用 有道翻译 翻译]\n\n$translation"
                            } else {
                                "[翻译错误]\n\n响应中没有翻译结果\n\n响应体: $responseBody\n\n原始文本:\n$processedText"
                            }
                        } else {
                            "[翻译错误]\n\n响应格式不正确\n\n响应体: $responseBody\n\n原始文本:\n$processedText"
                        }
                    } catch (e: Exception) {
                        "[解析响应失败]\n\n错误: ${e.message}\n\n响应体: $responseBody\n\n原始文本:\n$processedText"
                    }
                } else {
                    val errorMsg = if (responseBody != null) {
                        try {
                            val errorJson = JSONObject(responseBody)
                            errorJson.optString("message", "未知错误")
                        } catch (e: Exception) {
                            "HTTP ${response.code}: $responseBody"
                        }
                    } else {
                        "HTTP ${response.code}: 无响应体"
                    }
                    "[翻译错误]\n\n$errorMsg\n\n原始文本:\n$processedText"
                }
            } catch (e: Exception) {
                if (e is java.net.SocketTimeoutException) {
                    "[翻译超时]\n\n请求超时，请检查网络连接或稍后重试\n\n原始文本:\n$text"
                } else {
                    "[翻译错误]\n\n异常: ${e.message}\n\n原始文本:\n$text"
                }
            }
        }
    }
}