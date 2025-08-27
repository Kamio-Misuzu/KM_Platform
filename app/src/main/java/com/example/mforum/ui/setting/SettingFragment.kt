package com.example.mforum.ui.setting

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mforum.R
import com.example.mforum.ui.mine.MineViewModel

class SettingFragment : Fragment() {

    private lateinit var nightModeSwitch: Switch

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)  // Enable options menu

//        // 在 onCreateView 方法中添加退出登录按钮
//        val logoutButton = view?.findViewById<Button>(R.id.logout_button)
//        if (logoutButton != null) {
//            logoutButton.setOnClickListener {
//                val mineViewModel = ViewModelProvider(requireActivity()).get(MineViewModel::class.java)
//                mineViewModel.logout()
//                findNavController().popBackStack() // 返回上一页
//            }
//        }

        val view = inflater.inflate(R.layout.setting, container, false)

        // 查找Switch控件
        nightModeSwitch = view.findViewById(R.id.color_mode)

        // 设置开关初始状态
        nightModeSwitch.isChecked = isNightModeEnabled()

        // 开关监听器
        nightModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            setNightMode(isChecked)
            requireActivity().recreate()
        }

        return view


    }

    private fun isNightModeEnabled(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    private fun setNightMode(isNightMode: Boolean) {
        val mode = if (isNightMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.title_setting)
    }
}
