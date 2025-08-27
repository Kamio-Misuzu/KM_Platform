package com.example.mforum.ui.forum

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mforum.R
import com.example.mforum.data.Post
import com.example.mforum.databinding.FragmentForumBinding
import com.example.mforum.databinding.ItemPostBinding
import com.example.mforum.ui.mine.MineViewModel
import com.bumptech.glide.Glide
import com.example.mforum.data.User

class ForumFragment : Fragment() {
    private var _binding: FragmentForumBinding? = null
    private val binding get() = _binding!!

    private val forumViewModel: ForumViewModel by viewModels()

    private val mineViewModel: MineViewModel by activityViewModels()
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化适配器
        postAdapter = PostAdapter(emptyList(), { userId -> forumViewModel.getUserInfo(userId) }) { post ->
            val bundle = Bundle().apply {
                putInt("postId", post.id)
            }
            findNavController().navigate(R.id.action_forumFragment_to_postDetailFragment, bundle)
        }

        binding.postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.postsRecyclerView.adapter = postAdapter

        // 观察帖子数据
        forumViewModel.posts.observe(viewLifecycleOwner, Observer { posts ->
            postAdapter.updatePosts(posts)
        })

        // 观察当前用户信息
        mineViewModel.currentUser.observe(viewLifecycleOwner, Observer { user ->
            forumViewModel.setCurrentUser(user)
        })

        // 加载帖子
        forumViewModel.loadPosts()

        // 新建帖子按钮点击事件
        binding.fabAddPost.setOnClickListener {
            if (mineViewModel.isLoggedIn.value == true) {
                findNavController().navigate(R.id.action_forumFragment_to_createPostFragment)
            } else {
                Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

// 适配器
class PostAdapter(
    private var posts: List<Post>,
    private val getUserInfo: (Int) -> User?,
    private val onItemClick: (Post) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.titleTextView.text = post.title
            binding.categoryTextView.text = post.category

            // 获取用户信息
            val user = getUserInfo(post.authorId)
            binding.usernameTextView.text = user?.username ?: "加载中..."

            // 加载用户头像
            if (user?.avatarUrl != null && user.avatarUrl.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(user.avatarUrl)
                    .placeholder(R.drawable.ic_default_avatar)
                    .error(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(binding.avatarImageView)
            } else {
                binding.avatarImageView.setImageResource(R.drawable.ic_default_avatar)
            }

            itemView.setOnClickListener {
                onItemClick(post)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount(): Int = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}