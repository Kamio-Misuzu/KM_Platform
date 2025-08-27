package com.example.mforum.ui.Word

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mforum.R
import com.example.mforum.databinding.FragmentWordLearningBinding
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

// 定义数据类
data class WordData(
    val code: Int,
    val msg: String,
    val data: WordDetail
)

data class WordDetail(
    val bookId: String,
    val phrases: List<Phrase>,
    val relWords: List<RelatedWord>,
    val sentences: List<Sentence>,
    val synonyms: List<Synonym>,
    val translations: List<Translation>,
    val ukphone: String,
    val ukspeech: String,
    val usphone: String,
    val usspeech: String,
    val word: String
)

data class Phrase(
    val p_cn: String,
    val p_content: String
)

data class RelatedWord(
    val Hwds: List<Hwd>,
    val Pos: String
)

data class Hwd(
    val hwd: String,
    val tran: String
)

data class Sentence(
    val s_cn: String,
    val s_content: String
)

data class Synonym(
    val Hwds: List<SynonymWord>,
    val pos: String,
    val tran: String
)

data class SynonymWord(
    val word: String
)

data class Translation(
    val pos: String,
    val tran_cn: String
)

class WordLearningFragment : Fragment() {

    private var _binding: FragmentWordLearningBinding? = null
    private val binding get() = _binding!!
    private lateinit var mediaPlayer: MediaPlayer
    private var currentWord: WordDetail? = null

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWordLearningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaPlayer = MediaPlayer()

        // 获取单词
        fetchRandomWord()

        // 美式发音按钮点击事件
        binding.btnSoundUs.setOnClickListener {
            currentWord?.usspeech?.let { url ->
                playSound(url)
            }
        }

        // 英式发音按钮点击事件
        binding.btnSoundUk.setOnClickListener {
            currentWord?.ukspeech?.let { url ->
                playSound(url)
            }
        }

        // 保存单词按钮点击事件
        binding.btnSaveWord.setOnClickListener {
            currentWord?.let { word ->
                saveWordToLocal(word)
            }
        }

        // 下一个单词按钮点击事件
        binding.btnNextWord.setOnClickListener {
            fetchRandomWord()
        }

        // 打开保存目录按钮点击事件
        binding.btnOpenFolder.setOnClickListener {
            openSavedWordsDirectory()
        }
    }

    private fun fetchRandomWord() {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val wordData = withContext(Dispatchers.IO) {
                    getWordFromApi()
                }

                if (wordData != null && wordData.code == 200) {
                    currentWord = wordData.data
                    displayWordData(wordData.data)
                } else {
                    Toast.makeText(requireContext(), "获取单词失败", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("WordLearningFragment", "获取单词失败: ${e.message}")
                Toast.makeText(requireContext(), "网络请求失败", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun getWordFromApi(): WordData? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://v2.xxapi.cn/api/randomenglishwords")
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Gson().fromJson(responseBody, WordData::class.java)
            } else {
                null
            }
        } catch (e: IOException) {
            null
        }
    }

    private fun displayWordData(wordData: WordDetail) {
        binding.tvWord.text = wordData.word

        // 显示音标
        val phonetic = "美: [${wordData.usphone}] 英: [${wordData.ukphone}]"
        binding.tvPhonetic.text = phonetic

        // 显示翻译
        val translationText = wordData.translations.joinToString("\n") { translation ->
            "${translation.pos}: ${translation.tran_cn}"
        }
        binding.tvTranslation.text = translationText

        // 显示相关词汇
        if (wordData.relWords.isNotEmpty()) {
            binding.tvRelatedWords.visibility = View.VISIBLE
            val relatedWordsText = wordData.relWords.joinToString("\n") { relatedWord ->
                "${relatedWord.Pos}: ${relatedWord.Hwds.joinToString { hwd -> "${hwd.hwd} (${hwd.tran})" }}"
            }
            binding.tvRelatedWords.text = "相关词汇:\n$relatedWordsText"
        } else {
            binding.tvRelatedWords.visibility = View.GONE
        }

        // 显示短语
        if (wordData.phrases.isNotEmpty()) {
            binding.tvPhrases.visibility = View.VISIBLE
            val phrasesText = wordData.phrases.joinToString("\n") { phrase ->
                "${phrase.p_content} - ${phrase.p_cn}"
            }
            binding.tvPhrases.text = "短语:\n$phrasesText"
        } else {
            binding.tvPhrases.visibility = View.GONE
        }

        // 显示例句
        if (wordData.sentences.isNotEmpty()) {
            binding.tvExamples.visibility = View.VISIBLE
            val examplesText = wordData.sentences.joinToString("\n\n") { sentence ->
                "${sentence.s_content}\n${sentence.s_cn}"
            }
            binding.tvExamples.text = "例句:\n$examplesText"
        } else {
            binding.tvExamples.visibility = View.GONE
        }
    }

    private fun playSound(url: String) {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { mp ->
                mp.start()
            }
        } catch (e: Exception) {
            Log.e("WordLearningFragment", "播放发音失败: ${e.message}")
        }
    }

    private fun saveWordToLocal(word: WordDetail) {
        try {
            // 检查是否有存储权限
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // 请求权限
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
                return
            }

            // 使用 MediaStore API 保存到公共 Documents 目录
            val resolver = requireContext().contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "${word.word}.txt")
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/km_word")
            }

            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

            uri?.let { uri ->
                val outputStream = resolver.openOutputStream(uri)
                outputStream?.use { stream ->
                    val content = buildString {
                        appendLine("单词: ${word.word}")
                        appendLine("美式音标: ${word.usphone}")
                        appendLine("英式音标: ${word.ukphone}")
                        appendLine()

                        // 添加翻译
                        appendLine("翻译:")
                        word.translations.forEach { translation ->
                            appendLine("${translation.pos}: ${translation.tran_cn}")
                        }
                        appendLine()

                        // 添加相关词汇
                        if (word.relWords.isNotEmpty()) {
                            appendLine("相关词汇:")
                            word.relWords.forEach { relatedWord ->
                                relatedWord.Hwds.forEach { hwd ->
                                    appendLine("${hwd.hwd} (${relatedWord.Pos}): ${hwd.tran}")
                                }
                            }
                            appendLine()
                        }

                        // 添加短语
                        if (word.phrases.isNotEmpty()) {
                            appendLine("短语:")
                            word.phrases.forEach { phrase ->
                                appendLine("${phrase.p_content} - ${phrase.p_cn}")
                            }
                            appendLine()
                        }

                        // 添加例句
                        if (word.sentences.isNotEmpty()) {
                            appendLine("例句:")
                            word.sentences.forEach { sentence ->
                                appendLine("${sentence.s_content}")
                                appendLine("${sentence.s_cn}")
                                appendLine()
                            }
                        }
                    }

                    stream.write(content.toByteArray())
                    Toast.makeText(requireContext(), "单词已保存到 Documents/km_word 目录", Toast.LENGTH_LONG).show()
                }
            } ?: run {
                Toast.makeText(requireContext(), "创建文件失败", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("WordLearningFragment", "保存单词失败: ${e.message}")
            Toast.makeText(requireContext(), "保存单词失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予，可以保存文件
                currentWord?.let { saveWordToLocal(it) }
            } else {
                Toast.makeText(requireContext(), "存储权限被拒绝，无法保存单词", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openSavedWordsDirectory() {
        try {
            // 使用Intent打开Documents目录
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/km_word")
            intent.setDataAndType(uri, "resource/folder")

            // 验证是否有应用可以处理这个Intent
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                // 如果没有默认的文件管理器，显示目录路径
                val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/km_word"
                Toast.makeText(
                    requireContext(),
                    "目录位置: $path",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e("WordLearningFragment", "打开目录失败: ${e.message}")
            Toast.makeText(requireContext(), "无法打开目录", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer.release()
        _binding = null
    }
}