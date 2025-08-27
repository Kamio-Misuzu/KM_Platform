package com.example.mforum.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mforum.R
import com.example.mforum.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        // 设置视频AI教学卡片点击事件
        binding.cardVideoAi.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_homeFragment_to_navigation_video_ai)
            } catch (e: Exception) {
                Log.e("HomeFragment", "导航到视频AI失败: ${e.message}")
            }
        }

        // 设置文本AI教学卡片点击事件
        binding.cardTextAi.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_homeFragment_to_navigation_text_ai)
            } catch (e: Exception) {
                Log.e("HomeFragment", "导航到文本AI失败: ${e.message}")
            }
        }

        // 设置Arxiv Daily卡片点击事件
        binding.cardArxivDaily.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_homeFragment_to_arxivDailyFragment)
            } catch (e: Exception) {
                Log.e("HomeFragment", "导航到Arxiv Daily失败: ${e.message}")
            }
        }

        // 设置API设置卡片点击事件
        binding.cardSettings.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
            } catch (e: Exception) {
                Log.e("HomeFragment", "导航到设置页面失败: ${e.message}")
                Toast.makeText(requireContext(), "无法打开设置页面", Toast.LENGTH_SHORT).show()
            }
        }

        // 设置背单词卡片点击事件
        binding.cardWordLearning.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_homeFragment_to_wordLearningFragment)
            } catch (e: Exception) {
                Log.e("HomeFragment", "导航到背单词页面失败: ${e.message}")
                Toast.makeText(requireContext(), "无法打开背单词页面", Toast.LENGTH_SHORT).show()
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}