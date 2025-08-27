package com.example.mforum.ui.Arxiv_Daily

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mforum.R
import com.example.mforum.data.ArxivPaper
import com.example.mforum.databinding.FragmentArxivDailyBinding
import com.example.mforum.utils.ArxivParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class ArxivDailyFragment : Fragment() {

    private var _binding: FragmentArxivDailyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ArxivDailyViewModel by viewModels()

    // 定义论文类别
    private val categories = mapOf(
        "计算机视觉" to "cs.CV",
        "机器学习" to "cs.LG",
        "人工智能" to "cs.AI",
        "自然语言处理" to "cs.CL",
        "机器人技术" to "cs.RO",
        "计算生物学" to "q-bio.QM",
        "物理学" to "physics",
        "数学" to "math",
        "统计学" to "stat"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        _binding = FragmentArxivDailyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 恢复状态
        binding.categorySpinner.setSelection(viewModel.selectedCategoryIndex)
        binding.numberSpinner.setSelection(viewModel.selectedNumberIndex)

        // 如果ViewModel中有论文，则显示
        if (viewModel.papers.isNotEmpty()) {
            displayPapers(viewModel.papers)
            binding.papersListView.setSelection(viewModel.listViewScrollPosition)
        }

        // 设置类别下拉菜单
        val categoryNames = categories.keys.toList()
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categoryNames)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = categoryAdapter

        // 设置数量下拉菜单 (1-100)
        val numbers = (1..100).map { it.toString() }
        val numberAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, numbers)
        numberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.numberSpinner.adapter = numberAdapter

        // 获取论文按钮点击事件
        binding.btnFetchPapers.setOnClickListener {
            val categoryName = binding.categorySpinner.selectedItem.toString()
            val categoryCode = categories[categoryName] ?: "cs.CV"
            val paperCount = binding.numberSpinner.selectedItem.toString().toInt()

            // 保存当前选择
            viewModel.selectedCategoryIndex = binding.categorySpinner.selectedItemPosition
            viewModel.selectedNumberIndex = binding.numberSpinner.selectedItemPosition

            fetchPapers(categoryCode, paperCount)
        }

        // 论文列表点击事件
        binding.papersListView.setOnItemClickListener { _, _, position, _ ->
            if (viewModel.papers.isNotEmpty() && position < viewModel.papers.size) {
                val paper = viewModel.papers[position]
                val bundle = Bundle().apply {
                    putParcelable("paper", paper)
                }
                findNavController().navigate(
                    R.id.action_arxivDailyFragment_to_paperDetailFragment,
                    bundle
                )
            }
        }

        // 保存滚动位置
        binding.papersListView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}
            override fun onScroll(
                view: AbsListView,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                viewModel.listViewScrollPosition = firstVisibleItem
            }
        })
    }

    private fun fetchPapers(category: String, count: Int) {
        binding.progressBar.visibility = View.VISIBLE
        binding.papersListView.visibility = View.GONE

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val client = OkHttpClient()
                    val url = "https://export.arxiv.org/api/query?search_query=cat:$category&sortBy=submittedDate&sortOrder=descending&max_results=$count"
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    val xml = response.body?.string()

                    if (xml != null) {
                        val papers = ArxivParser.parseXmlResponse(xml)
                        withContext(Dispatchers.Main) {
                            if (papers.isNotEmpty()) {
                                viewModel.papers = papers
                                displayPapers(papers)
                            } else {
                                Toast.makeText(requireContext(), "未找到相关论文", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "获取论文失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("ArxivDaily", "获取论文失败", e)
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun displayPapers(papers: List<ArxivPaper>) {
        val paperTitles = papers.map { it.title }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, paperTitles)
        binding.papersListView.adapter = adapter
        binding.papersListView.visibility = View.VISIBLE
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