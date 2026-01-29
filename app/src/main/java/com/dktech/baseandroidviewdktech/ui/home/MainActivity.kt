package com.dktech.baseandroidviewdktech.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.dktech.baseandroidviewdktech.R
import com.dktech.baseandroidviewdktech.base.BaseActivity
import com.dktech.baseandroidviewdktech.base.BaseActivityVM
import com.dktech.baseandroidviewdktech.base.ViewModelFactory
import com.dktech.baseandroidviewdktech.databinding.ActivityMainBinding
import com.dktech.baseandroidviewdktech.ui.home.MainViewModel
import com.dktech.baseandroidviewdktech.ui.home.adapter.VPAdapter
import com.dktech.baseandroidviewdktech.ui.home.model.PaintingCategory
import com.dktech.baseandroidviewdktech.ui.my_collection.MyCollectionActivity
import com.dktech.baseandroidviewdktech.ui.setting.SettingActivity
import com.dktech.baseandroidviewdktech.utils.helper.setSafeOnClickListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : BaseActivity<ActivityMainBinding>() {
    override val onBackPressedCallback: OnBackPressedCallback
        get() =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                }
            }

    private val viewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private val vpAdapter by lazy {
        VPAdapter(
            this,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun getViewBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun initData() {
    }

    override fun initView() {
        binding.vpMain.adapter = vpAdapter
        val tabTiles = PaintingCategory.entries.map { it.categoryName }

        TabLayoutMediator(binding.tabLayoutVP, binding.vpMain) { tab, position ->
            tab.customView =
                LayoutInflater
                    .from(binding.tabLayoutVP.context)
                    .inflate(R.layout.item_tab_text, binding.tabLayoutVP, false)

            val tv = tab.customView!!.findViewById<TextView>(R.id.tvTab)
            tv.text = tabTiles[position]
        }.attach()

        binding.tabLayoutVP.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
//                    viewModel.updateCategory(
//                        this@MainActivity,
//                        PaintingCategory.entries[tab.position]
//                    )
                    val tv = tab.customView?.findViewById<TextView>(R.id.tvTab)
                    tv?.apply {
                        setTextColor(getColor(R.color.tab_selected))
                        textSize = 16f
                    }
                    val line = tab.customView?.findViewById<View>(R.id.line)
                    line?.apply {
                        visibility = View.VISIBLE
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    val tv = tab.customView?.findViewById<TextView>(R.id.tvTab)
                    tv?.apply {
                        setTextColor(getColor(R.color.tab_unselected))
                        textSize = 14f
                    }
                    val line = tab.customView?.findViewById<View>(R.id.line)
                    line?.apply {
                        visibility = View.GONE
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
            },
        )

        binding.tabLayoutVP.getTabAt(0)?.let { tab ->
            binding.tabLayoutVP.selectTab(tab)
            updateTabSelected(tab)
            viewModel.loadColorBookData(mContext = this)
        }
    }

    private fun updateTabSelected(tab: TabLayout.Tab) {
        tab.customView?.findViewById<TextView>(R.id.tvTab)?.apply {
            setTextColor(getColor(R.color.tab_selected))
            textSize = 16f
        }
        tab.customView?.findViewById<View>(R.id.line)?.visibility = View.VISIBLE
    }

    override fun initEvent() {
        binding.icCollection.setSafeOnClickListener {
            Intent(this@MainActivity, MyCollectionActivity::class.java).apply {
                startActivity(this)
            }
        }
        binding.icSetting.setSafeOnClickListener {
            Intent(this@MainActivity, SettingActivity::class.java).apply {
                startActivity(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadColorBookData(this)
    }

    override fun initObserver() {
    }
}
