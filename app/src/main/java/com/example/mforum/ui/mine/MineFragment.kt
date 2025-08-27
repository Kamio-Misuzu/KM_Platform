package com.example.mforum.ui.mine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.mforum.MainActivity
import com.example.mforum.R
import com.example.mforum.databinding.FragmentMineBinding
import com.example.mforum.ui.auth.LoginDialogFragment
import kotlinx.coroutines.launch
import java.io.File

class MineFragment : Fragment() {

    private var _binding: FragmentMineBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MineViewModel by activityViewModels()

    // 定义登录成功回调接口
    interface OnLoginSuccessListener {
        fun onLoginSuccess()
    }

    private var loginSuccessListener: OnLoginSuccessListener? = null

    // 头像选择器
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // 处理选择的图片
            lifecycleScope.launch {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val file = File(requireContext().cacheDir, "avatar_temp.jpg")
                inputStream?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val success = viewModel.uploadAvatar(file)
                if (success) {
                    Toast.makeText(requireContext(), "头像上传成功", Toast.LENGTH_SHORT).show()
                    // 刷新用户信息
                    viewModel.checkLoginStatus()
                } else {
                    Toast.makeText(requireContext(), "头像上传失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置下拉刷新
//        setupSwipeRefresh()

//        // 观察ViewModel的刷新状态
//        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
//            (activity as? MainActivity)?.stopRefreshing()
//        }

        // 设置登录成功监听器
        loginSuccessListener = object : OnLoginSuccessListener {
            override fun onLoginSuccess() {
                // 登录成功后刷新界面
                viewModel.checkLoginStatus()
            }
        }

        // 观察用户信息
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.username.text = it.username
                // 加载用户头像
                loadAvatar(it.avatarUrl)
            }
        }

        // 观察登录状态
        viewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (isLoggedIn) {
                showUserInfo()
            } else {
                showLoginPrompt()
            }
            updateAuthButtonsVisibility(isLoggedIn)
        }

        // 设置按钮点击事件
        binding.settings.setOnClickListener {
            findNavController().navigate(R.id.action_Mine_to_setting)
        }

        // 点击用户信息区域
        binding.profileContainer.setOnClickListener {
            if (viewModel.isLoggedIn.value == true) {
                // 已登录，可以选择头像
                selectAvatar()
            } else {
                // 未登录，显示登录对话框
                showLoginDialog()
            }
        }

        // 移除长按退出功能，改为使用按钮
        binding.profileContainer.setOnLongClickListener(null)

        // 设置登录按钮点击事件
        binding.btnLogin.setOnClickListener {
            showLoginDialog()
        }

        // 设置退出登录按钮点击事件
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun setupSwipeRefresh() {
//        val swipeRefreshLayout = requireView().findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
//        swipeRefreshLayout.setOnRefreshListener {
//            // 刷新用户信息
//            viewModel.checkLoginStatus()
//            // 停止刷新动画
//            swipeRefreshLayout.isRefreshing = false
//        }
//    }

    private fun updateAuthButtonsVisibility(isLoggedIn: Boolean) {
        if (isLoggedIn) {
            binding.btnLogin.visibility = View.GONE
            binding.btnLogout.visibility = View.VISIBLE
        } else {
            binding.btnLogin.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.GONE
        }
    }

    private fun showUserInfo() {
        binding.progressBar.visibility = View.GONE
        // 可以显示更多用户信息
    }

    private fun showLoginPrompt() {
        binding.progressBar.visibility = View.GONE
        binding.username.text = "点击登录"
        binding.profileImage.setImageResource(R.drawable.ic_default_avatar)
    }

    // 添加加载头像的方法
    private fun loadAvatar(avatarUrl: String) {
        if (avatarUrl.isNotEmpty()) {
            // 构建完整的头像URL
            val fullAvatarUrl = if (avatarUrl.startsWith("http")) {
                avatarUrl
            } else {

                "http://192.168.0.108:5000$avatarUrl"
            }

            // 添加随机参数确保每次请求都是新的
            val random = (Math.random() * 10000).toInt()
            val urlWithRandom = "$fullAvatarUrl?r=$random"

            // 使用Glide加载网络图片，禁用所有缓存
            Glide.with(requireContext())
                .load(urlWithRandom)
                .placeholder(R.drawable.ic_default_avatar)
                .error(R.drawable.ic_default_avatar)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .circleCrop()
                .into(binding.profileImage)
        } else {
            binding.profileImage.setImageResource(R.drawable.ic_default_avatar)
        }
    }

    // 选择头像
    private fun selectAvatar() {
        pickImage.launch("image/*")
    }

    private fun showLoginDialog() {
        val dialog = LoginDialogFragment().apply {
            setLoginSuccessListener(loginSuccessListener)
        }
        dialog.show(parentFragmentManager, LoginDialogFragment.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        loginSuccessListener = null
    }



    // 设置登录成功监听器的方法
    fun setLoginSuccessListener(listener: OnLoginSuccessListener?) {
        this.loginSuccessListener = listener
    }
}