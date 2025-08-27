package com.example.mforum.ui.teaching

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mforum.R
import com.example.mforum.data.TeachingRepository
import com.example.mforum.databinding.FragmentVideoAiBinding
import kotlinx.coroutines.launch

class VideoAIFragment : Fragment() {

    private var _binding: FragmentVideoAiBinding? = null
    private val binding get() = _binding!!
    private lateinit var teachingRepository: TeachingRepository
    private lateinit var videoAdapter: VideoResourceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)  // Enable options menu

        _binding = FragmentVideoAiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teachingRepository = TeachingRepository(requireContext())

        videoAdapter = VideoResourceAdapter { resource ->
            // 使用Bundle传递参数而不是Directions
            val bundle = Bundle().apply {
                putInt("resourceId", resource.id)
            }
            findNavController().navigate(R.id.action_navigation_video_ai_to_videoDetailFragment, bundle)
        }

        binding.videoRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = videoAdapter
        }

        // 设置下拉刷新
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadVideoResources()
        }

        // 加载数据
        loadVideoResources()
    }

    private fun loadVideoResources() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE

        lifecycleScope.launch {
            val resources = teachingRepository.getVideoResources()

            if (resources.isNotEmpty()) {
                videoAdapter.submitList(resources)
                binding.emptyView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.VISIBLE
            }

            binding.progressBar.visibility = View.GONE
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Handle the back button click here
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}