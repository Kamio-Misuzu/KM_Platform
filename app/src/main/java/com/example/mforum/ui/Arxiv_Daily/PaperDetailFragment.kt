package com.example.mforum.ui.Arxiv_Daily

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mforum.R
import com.example.mforum.data.ArxivPaper
import com.example.mforum.databinding.FragmentPaperDetailBinding
import com.example.mforum.network.TranslationService
import com.example.mforum.utils.PreferencesManager
import kotlinx.coroutines.launch

class PaperDetailFragment : Fragment() {


    private var _binding: FragmentPaperDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var translationService: TranslationService
    private lateinit var prefs: PreferencesManager
    private var currentPaper: ArxivPaper? = null

    private val args: PaperDetailFragmentArgs by navArgs()

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
        setHasOptionsMenu(true)

        _binding = FragmentPaperDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = PreferencesManager(requireContext())
        translationService = TranslationService(prefs)

        // 获取传递的论文数据
        currentPaper = args.paper
        if (currentPaper == null) {
            Toast.makeText(requireContext(), "论文数据加载失败", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
            return
        }

        displayPaper(currentPaper!!)

        // 设置翻译方法下拉菜单
        val methods = resources.getStringArray(R.array.translation_methods)
        val methodAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            methods
        )
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.translationMethod.adapter = methodAdapter

        // 设置模型选择下拉菜单
        val modelAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            modelNames
        )
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.modelSpinner.adapter = modelAdapter

        // 监听翻译方法变化
        binding.translationMethod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val method = parent.getItemAtPosition(position).toString()
                if (method == "硅基流动API") {
                    binding.apiKeyLayout.visibility = View.VISIBLE
                } else {
                    binding.apiKeyLayout.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // 从偏好设置加载保存的API密钥和模型
        prefs.getSiliconFlowApiKey()?.let {
            binding.apiKeyEditText.setText(it)
        }

        prefs.getSiliconFlowModelId()?.let { modelId ->
            val modelName = models.entries.find { it.value == modelId }?.key
            modelName?.let {
                val position = modelNames.indexOf(it) // 这里使用 modelNames
                if (position >= 0) {
                    binding.modelSpinner.setSelection(position)
                }
            }
        }


        // 设置目标语言下拉菜单
        val targetLanguages = resources.getStringArray(R.array.target_languages)
        val langAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            targetLanguages
        )
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.targetLanguageSpinner.adapter = langAdapter

        // 设置翻译按钮点击事件
        binding.translateButton.setOnClickListener {
            currentPaper?.let { paper ->
                val method = binding.translationMethod.selectedItem.toString()
                val targetLang = binding.targetLanguageSpinner.selectedItem.toString()

                if (method == "硅基流动API") {
                    // 使用保存的API密钥和模型，如果用户没有输入新的
                    val apiKey = if (binding.apiKeyEditText.text.toString().isNotEmpty()) {
                        binding.apiKeyEditText.text.toString().trim()
                    } else {
                        prefs.getSiliconFlowApiKey()
                    }

                    val modelName = if (binding.modelSpinner.selectedItemPosition > 0) {
                        binding.modelSpinner.selectedItem.toString()
                    } else {
                        // 使用保存的模型
                        val savedModelId = prefs.getSiliconFlowModelId()
                        models.entries.find { it.value == savedModelId }?.key ?: "DeepSeek-V3"
                    }

                    val modelId = models[modelName] ?: "deepseek-ai/DeepSeek-V3"

                    if (apiKey.isNullOrEmpty()) {
                        Toast.makeText(requireContext(), "请先设置API密钥", Toast.LENGTH_SHORT).show()
                        // 导航到设置页面
                        try {
                            findNavController().navigate(R.id.action_paperDetailFragment_to_settingsFragment)
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "无法打开设置页面", Toast.LENGTH_SHORT).show()
                        }
                        return@setOnClickListener
                    }

                    // 如果用户输入了新的API密钥，保存它
                    if (binding.apiKeyEditText.text.toString().isNotEmpty()) {
                        prefs.setSiliconFlowApiKey(apiKey)
                        prefs.setSiliconFlowModelId(modelId)
                    }

                    translateAbstract(paper.abstract, targetLang,apiKey, modelId)
                } else {
                    translateAbstract(paper.abstract,targetLang)
                }
            }
        }


        // 在 onViewCreated 方法中添加
        binding.toggleApiKeyVisibility.setOnClickListener {
            val selection = binding.apiKeyEditText.selectionEnd
            if (binding.apiKeyEditText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                binding.apiKeyEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.toggleApiKeyVisibility.setImageResource(R.drawable.ic_visibility_off)
            } else {
                binding.apiKeyEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.toggleApiKeyVisibility.setImageResource(R.drawable.ic_visibility)
            }
            binding.apiKeyEditText.setSelection(selection)
        }

    }

    private fun displayPaper(paper: ArxivPaper) {
        binding.paperTitle.text = paper.title
        binding.paperAuthors.text = paper.authors.joinToString(", ")
        binding.paperDate.text = paper.publishedDate
        binding.paperAbstract.text = paper.abstract
    }

    private fun translateAbstract(abstract: String, targetLang: String, apiKey: String? = null, modelId: String? = null) {
        binding.translationResult.visibility = View.GONE
        binding.translationProgress.visibility = View.VISIBLE

        val method = binding.translationMethod.selectedItem.toString()
        val targetLang = targetLang

        lifecycleScope.launch {
            try {
                val translation = when (method) {
                    "硅基流动API" -> translationService.translateWithSiliconFlow(
                        abstract,
                        targetLang,
                        apiKey,
                        modelId
                    )
                    "有道翻译" -> translationService.translateWithYoudao(abstract, targetLang)
                    else -> "不支持的翻译方法"
                }

                binding.translationResult.text = translation
                binding.translationResult.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.translationResult.text = "翻译失败: ${e.message}"
                binding.translationResult.visibility = View.VISIBLE
            } finally {
                binding.translationProgress.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}