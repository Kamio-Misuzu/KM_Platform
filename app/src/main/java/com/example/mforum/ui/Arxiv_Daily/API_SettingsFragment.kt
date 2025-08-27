package com.example.mforum.ui.Arxiv_Daily

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mforum.R
import com.example.mforum.databinding.FragmentApiSettingsBinding
import com.example.mforum.utils.PreferencesManager

class API_SettingsFragment : Fragment() {

    private var _binding: FragmentApiSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PreferencesManager

    // 模型列表
    private val models = mapOf(
        "DeepSeek-V3" to "deepseek-ai/DeepSeek-V3",
        "DeepSeek-R1" to "deepseek-ai/DeepSeek-R1",
        "Kimi" to "moonshotai/Kimi-K2-Instruct"
    )

    // 将 modelNames 定义为普通的 List 而不是 lateinit
    private val modelNames by lazy { models.keys.toList() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApiSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = PreferencesManager(requireContext())

        // 设置模型选择下拉菜单
        val modelAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            modelNames // 这里使用 modelNames
        )
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.siliconFlowModel.adapter = modelAdapter

        // 设置默认翻译目标语言下拉菜单
        val targetLanguages = resources.getStringArray(R.array.target_languages)
        val languageAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            targetLanguages
        )
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.defaultTargetLanguage.adapter = languageAdapter

        // 加载保存的设置
        loadSavedSettings()

        // 保存设置按钮点击事件
        binding.saveSettingsButton.setOnClickListener {
            saveSettings()
        }

        // 清除设置按钮点击事件
        binding.clearSettingsButton.setOnClickListener {
            clearSettings()
        }

        // 在 onViewCreated 方法中添加
        binding.toggleApiKeyVisibility.setOnClickListener {
            val selection = binding.siliconFlowApiKey.selectionEnd
            if (binding.siliconFlowApiKey.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                binding.siliconFlowApiKey.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.toggleApiKeyVisibility.setImageResource(R.drawable.ic_visibility_off)
            } else {
                binding.siliconFlowApiKey.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.toggleApiKeyVisibility.setImageResource(R.drawable.ic_visibility)
            }
            binding.siliconFlowApiKey.setSelection(selection)
        }
    }

    private fun loadSavedSettings() {
        // 加载API密钥
        prefs.getSiliconFlowApiKey()?.let {
            binding.siliconFlowApiKey.setText(it)
        }

        // 加载模型选择
        prefs.getSiliconFlowModelId()?.let { modelId ->
            val modelName = models.entries.find { it.value == modelId }?.key
            modelName?.let {
                val position = modelNames.indexOf(it) // 这里使用 modelNames
                if (position >= 0) {
                    binding.siliconFlowModel.setSelection(position)
                }
            }
        }

        // 加载默认翻译目标语言
        prefs.getDefaultTargetLanguage()?.let { language ->
            val targetLanguages = resources.getStringArray(R.array.target_languages)
            val position = targetLanguages.indexOf(language)
            if (position >= 0) {
                binding.defaultTargetLanguage.setSelection(position)
            }
        }
    }

    private fun saveSettings() {
        val apiKey = binding.siliconFlowApiKey.text.toString().trim()
        val modelName = binding.siliconFlowModel.selectedItem.toString()
        val modelId = models[modelName] ?: "deepseek-ai/DeepSeek-V3"
        val targetLanguage = binding.defaultTargetLanguage.selectedItem.toString()

        if (apiKey.isEmpty()) {
            Toast.makeText(requireContext(), "请输入API密钥", Toast.LENGTH_SHORT).show()
            return
        }

        prefs.setSiliconFlowApiKey(apiKey)
        prefs.setSiliconFlowModelId(modelId)
        prefs.setDefaultTargetLanguage(targetLanguage)

        Toast.makeText(requireContext(), "设置已保存", Toast.LENGTH_SHORT).show()
    }

    private fun clearSettings() {
        prefs.clearSiliconFlowSettings()
        binding.siliconFlowApiKey.setText("")
        binding.siliconFlowModel.setSelection(0)
        binding.defaultTargetLanguage.setSelection(0) // 重置为第一个选项

        Toast.makeText(requireContext(), "设置已清除", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}