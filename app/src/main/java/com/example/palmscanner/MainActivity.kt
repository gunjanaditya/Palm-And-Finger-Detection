package com.example.palmscanner

import android.os.Bundle
import android.view.WindowManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.palmscanner.databinding.ActivityMainBinding
import com.example.palmscanner.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single Activity — acts purely as a NavHost container.
 * All business logic lives in Fragments + ViewModels.
 * No ViewModel here — MainActivity knows nothing about app state.
 */
@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private lateinit var navController: NavController

    override fun inflateBinding(): ActivityMainBinding = ActivityMainBinding.inflate(layoutInflater)

    override fun initView(savedInstanceState: Bundle?) {
        // Keep screen ON during camera scanning sessions
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
    }

    /**
     * Ensures hardware back button works correctly with Navigation Component.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
