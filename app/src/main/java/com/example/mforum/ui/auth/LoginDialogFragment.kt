package com.example.mforum.ui.auth

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.mforum.databinding.DialogLoginBinding
import com.example.mforum.ui.mine.MineFragment
import com.example.mforum.ui.mine.MineViewModel
import kotlinx.coroutines.launch

class LoginDialogFragment : DialogFragment() {
    private var _binding: DialogLoginBinding? = null
    private val binding get() = _binding!!

    // 使用 activityViewModels 而不是 viewModels 来共享 ViewModel
    private val viewModel: MineViewModel by activityViewModels()

    private var loginSuccessListener: MineFragment.OnLoginSuccessListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogLoginBinding.inflate(LayoutInflater.from(requireContext()))

        val builder = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle("登录/注册")

        // 只设置点击监听器，不观察 LiveData
        setupClickListeners()

        return builder.create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 在 View 创建后设置观察者
        setupObservers()
    }

    private fun setupClickListeners() {
        // 切换登录/注册视图
        binding.switchToRegister.setOnClickListener {
            binding.loginContainer.visibility = View.GONE
            binding.registerContainer.visibility = View.VISIBLE
        }

        binding.switchToLogin.setOnClickListener {
            binding.registerContainer.visibility = View.GONE
            binding.loginContainer.visibility = View.VISIBLE
        }

        // 登录按钮
        binding.btnLogin.setOnClickListener {
            val username = binding.etLoginUsername.text.toString().trim()
            val password = binding.etLoginPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "请填写完整信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = viewModel.login(username, password)
                if (user != null) {
                    Toast.makeText(requireContext(), "登录成功", Toast.LENGTH_SHORT).show()
                    // 通知监听器登录成功
                    loginSuccessListener?.onLoginSuccess()
                    dismiss()
                }
            }
        }

        // 注册按钮
        binding.btnRegister.setOnClickListener {
            val username = binding.etRegisterUsername.text.toString().trim()
            val email = binding.etRegisterEmail.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString()
            val confirmPassword = binding.etRegisterConfirmPassword.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "请填写完整信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "两次输入的密码不一致", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val success = viewModel.register(username, email, password)
                if (success) {
                    Toast.makeText(requireContext(), "注册成功，请登录", Toast.LENGTH_SHORT).show()
                    binding.registerContainer.visibility = View.GONE
                    binding.loginContainer.visibility = View.VISIBLE
                }
                // 错误信息会通过 ViewModel 的 errorMessage LiveData 显示
            }
        }
    }

    private fun setupObservers() {
        // 观察错误消息
        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // 观察加载状态
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
            binding.btnRegister.isEnabled = !isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "LoginDialogFragment"
    }

    // 设置登录成功监听器的方法
    fun setLoginSuccessListener(listener: MineFragment.OnLoginSuccessListener) {
        this.loginSuccessListener = listener
    }
}