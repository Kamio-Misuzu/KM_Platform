package com.example.mforum.ui.forum

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mforum.databinding.FragmentCreatePostBinding
import com.example.mforum.ui.mine.MineViewModel
import kotlinx.coroutines.CancellationException

class CreatePostFragment : Fragment() {
    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    // 定义可用类别
    private val categories = arrayOf("CV", "LLM", "NLP", "强化学习", "其他")

    private val forumViewModel: ForumViewModel by viewModels()
    private val mineViewModel: MineViewModel by activityViewModels()

    // 添加加载状态变量
    private var isSubmitting = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)  // Enable options menu

        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置类别选择器
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter

        binding.btnSubmit.setOnClickListener {
            if (isSubmitting) return@setOnClickListener

            val title = binding.etTitle.text.toString().trim()
            val content = binding.etContent.text.toString().trim()
            val category = binding.categorySpinner.selectedItem.toString()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "请填写标题和内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 检查用户是否登录
            if (mineViewModel.isLoggedIn.value != true) {
                Toast.makeText(requireContext(), "发布失败，请先登录", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 获取当前用户
            val currentUser = mineViewModel.currentUser.value
            if (currentUser == null) {
                Toast.makeText(requireContext(), "发布失败，用户信息不存在", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 设置加载状态
            isSubmitting = true
            binding.btnSubmit.isEnabled = false
            binding.btnCancel.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            // 创建帖子（添加category参数）
            forumViewModel.createPost(title, content, category, currentUser.id) { success ->
                // 确保Fragment仍然附加到Activity
                if (!isAdded) return@createPost

                isSubmitting = false
                binding.btnSubmit.isEnabled = true
                binding.btnCancel.isEnabled = true
                binding.progressBar.visibility = View.GONE

                if (success) {
                    Toast.makeText(requireContext(), "发布成功", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "发布失败", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            if (!isSubmitting) {
                findNavController().popBackStack()
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
                // 如果正在提交，阻止返回操作
                if (!isSubmitting) {
                    activity?.onBackPressed()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}