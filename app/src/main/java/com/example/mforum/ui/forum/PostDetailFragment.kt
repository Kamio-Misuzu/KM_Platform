package com.example.mforum.ui.forum

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mforum.R
import com.example.mforum.data.Comment
import com.example.mforum.data.User
import com.example.mforum.databinding.FragmentPostDetailBinding
import com.example.mforum.ui.mine.MineViewModel
import androidx.fragment.app.activityViewModels
import com.example.mforum.utils.TimeUtils

class PostDetailFragment : Fragment() {
    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var commentAdapter: CommentAdapter

    private val viewModel: PostDetailViewModel by viewModels()
    private val mineViewModel: MineViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始化评论适配器
        commentAdapter = CommentAdapter(emptyList(), emptyMap())
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.commentsRecyclerView.adapter = commentAdapter

        // 获取帖子ID
        val postId = arguments?.getInt("postId", -1) ?: -1
        if (postId == -1) {
            Toast.makeText(requireContext(), "帖子不存在", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
            return
        }

        // 观察帖子数据
        viewModel.post.observe(viewLifecycleOwner) { post ->
            post?.let {
                binding.postTitle.text = it.title
                binding.postContent.text = it.content
                binding.postTime.text = TimeUtils.formatTime(it.createdAt, requireContext())
                binding.postCategory.text = it.category

                // 加载作者信息
                viewModel.loadAuthor(it.authorId)
            }
        }

        // 观察作者信息
        viewModel.author.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.username.text = it.username

                // 加载作者头像
                if (it.avatarUrl.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(it.avatarUrl)
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .circleCrop()
                        .into(binding.userAvatar)
                }
            }
        }

        // 观察评论数据
        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            commentAdapter.updateComments(comments)

            // 加载评论者信息
            comments.forEach { comment ->
                if (!viewModel.hasUserInfo(comment.authorId)) {
                    viewModel.loadCommentAuthor(comment.authorId)
                }
            }
        }

        // 观察评论者信息
        viewModel.commentAuthors.observe(viewLifecycleOwner) { authors ->
            commentAdapter.updateUsers(authors)
        }

        // 发送评论按钮点击事件 - 修复空指针异常
        binding.submitComment.setOnClickListener {
            try {
                val commentText = binding.commentInput.text.toString().trim()
                if (commentText.isEmpty()) {
                    Toast.makeText(requireContext(), "评论内容不能为空", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (mineViewModel.isLoggedIn.value != true) {
                    Toast.makeText(requireContext(), "请先登录", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // 添加空值检查
                val currentUser = mineViewModel.currentUser.value
                if (currentUser == null) {
                    Toast.makeText(requireContext(), "用户信息不存在，请重新登录", Toast.LENGTH_SHORT).show()
                    Log.e("PostDetailFragment", "currentUser is null")
                    return@setOnClickListener
                }

                viewModel.createComment(commentText, currentUser.id)
                binding.commentInput.text.clear()
            } catch (e: Exception) {
                Log.e("PostDetailFragment", "Error submitting comment", e)
                Toast.makeText(requireContext(), "提交评论时发生错误", Toast.LENGTH_SHORT).show()
            }
        }

        // 加载帖子和评论
        viewModel.loadPost(postId)
        viewModel.loadComments(postId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// 修改CommentAdapter以支持用户信息
class CommentAdapter(
    private var comments: List<Comment>,
    private var users: Map<Int, User>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    fun updateComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }

    fun updateUsers(newUsers: Map<Int, User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.comment_avatar)
        val username: TextView = itemView.findViewById(R.id.comment_username)
        val content: TextView = itemView.findViewById(R.id.comment_content)
        val time: TextView = itemView.findViewById(R.id.comment_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        val user = users[comment.authorId]
        val username = user?.username ?: "未知用户"

        holder.username.text = username
        holder.content.text = comment.content
        holder.time.text = TimeUtils.formatTime(comment.createdAt, holder.itemView.context)

        // 加载用户头像
        if (user?.avatarUrl != null && user.avatarUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(user.avatarUrl)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .circleCrop()
                .into(holder.avatar)
        } else {
            holder.avatar.setImageResource(R.drawable.ic_default_avatar)
        }
    }

    override fun getItemCount(): Int = comments.size
}