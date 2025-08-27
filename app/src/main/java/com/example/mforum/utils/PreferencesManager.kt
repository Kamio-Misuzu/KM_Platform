package com.example.mforum.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("mforum_prefs", Context.MODE_PRIVATE)


    fun saveUserId(userId: Int) {
        sharedPreferences.edit().putInt("user_id", userId).apply()
    }

    fun getUserId(): Int {
        return sharedPreferences.getInt("user_id", -1)
    }

    // 添加Token管理方法
    fun saveToken(token: String) {
        sharedPreferences.edit().putString("access_token", token).apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    fun clearToken() {
        sharedPreferences.edit().remove("access_token").apply()
    }

    fun clearUserData() {
        sharedPreferences.edit().remove("user_id").remove("access_token").apply()
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != -1 && getToken() != null
    }

    fun getSiliconFlowApiKey(): String? {
        return sharedPreferences.getString("silicon_flow_api_key", null)
    }

    fun setSiliconFlowApiKey(apiKey: String) {
        sharedPreferences.edit().putString("silicon_flow_api_key", apiKey).apply()
    }

    fun getSiliconFlowModelId(): String? {
        return sharedPreferences.getString("silicon_flow_model_id", "deepseek-ai/DeepSeek-V3")
    }

    fun setSiliconFlowModelId(modelId: String) {
        sharedPreferences.edit().putString("silicon_flow_model_id", modelId).apply()
    }

    fun clearSiliconFlowSettings() {
        sharedPreferences.edit().remove("silicon_flow_api_key").remove("silicon_flow_model_id").apply()
    }

    // 保存默认翻译目标语言
    fun setDefaultTargetLanguage(language: String) {
        sharedPreferences.edit().putString(KEY_DEFAULT_TARGET_LANGUAGE, language).apply()
    }

    // 获取默认翻译目标语言
    fun getDefaultTargetLanguage(): String? {
        return sharedPreferences.getString(KEY_DEFAULT_TARGET_LANGUAGE, "中文") // 默认中文
    }

    companion object {
        const val PREFS_NAME = "mforum_prefs"
        const val KEY_DEFAULT_TARGET_LANGUAGE = "default_target_language"
    }
}

