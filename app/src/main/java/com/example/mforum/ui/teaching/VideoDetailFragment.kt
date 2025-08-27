package com.example.mforum.ui.teaching

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mforum.R
import com.example.mforum.data.TeachingRepository
import com.example.mforum.databinding.FragmentVideoDetailBinding
import kotlinx.coroutines.launch

class VideoDetailFragment : Fragment() {

    private var _binding: FragmentVideoDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var teachingRepository: TeachingRepository
    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // 启用选项菜单
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teachingRepository = TeachingRepository(requireContext())

        // 设置工具栏
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // 获取传递的参数
        val resourceId = arguments?.getInt("resourceId") ?: 0

        if (resourceId > 0) {
            loadVideoDetail(resourceId)
        }

        // 设置播放按钮点击事件
        binding.playButton.setOnClickListener {
            playVideo(resourceId)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_video_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val favoriteItem = menu.findItem(R.id.action_favorite)
        if (isFavorite) {
            favoriteItem.setIcon(R.drawable.ic_favorite)
        } else {
            favoriteItem.setIcon(R.drawable.ic_favorite_border)
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share -> {
                shareVideo()
                true
            }
            R.id.action_favorite -> {
                toggleFavorite()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadVideoDetail(resourceId: Int) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val detail = teachingRepository.getVideoDetail(resourceId)

            if (detail != null) {
                binding.titleTextView.text = detail.title
                binding.descriptionTextView.text = detail.content

                // 加载相关视频
                if (detail.relatedResources.isNotEmpty()) {
                    // 设置相关视频适配器
                    val adapter = VideoResourceAdapter { resource ->
                        // 导航到选中的相关视频
                        val bundle = Bundle().apply {
                            putInt("resourceId", resource.id)
                        }
                        findNavController().navigate(R.id.action_videoDetailFragment_self, bundle)
                    }
                    binding.relatedVideosRecyclerView.adapter = adapter
                    adapter.submitList(detail.relatedResources)
                }
            }

            binding.progressBar.visibility = View.GONE
        }
    }

    private fun playVideo(resourceId: Int) {
        // 实现视频播放逻辑
        // 可以使用ExoPlayer或其他视频播放库
    }

    private fun shareVideo() {
        // 实现分享功能
    }

    private fun toggleFavorite() {
        isFavorite = !isFavorite
        requireActivity().invalidateOptionsMenu() // 刷新菜单
        // 保存收藏状态到数据库或API
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}