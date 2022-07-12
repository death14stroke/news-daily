package com.death14stroke.newsdaily.ui.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.death14stroke.newsdaily.R
import com.death14stroke.newsdaily.data.ACTION_NOTI_CLICK
import com.death14stroke.newsdaily.data.EXTRA_CATEGORY
import com.death14stroke.newsdaily.databinding.ActivityHomeBinding
import com.death14stroke.newsdaily.ui.custom.DebouncingQueryTextListener
import com.death14stroke.newsdaily.ui.fragment.NewsFragmentDirections
import com.death14stroke.newsdaily.ui.util.color
import com.death14stroke.newsdaily.ui.util.getColorFromAttr
import com.death14stroke.newsdaily.ui.viewbinding.viewBinding
import com.death14stroke.newsdaily.ui.viewmodel.SearchViewModel
import com.death14stroke.newsloader.data.model.Category
import com.miguelcatalan.materialsearchview.MaterialSearchView
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityHomeBinding::inflate)
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val appBarConfiguration by lazy {
        AppBarConfiguration(
            setOf(
                R.id.nav_general, R.id.nav_business, R.id.nav_entertainment,
                R.id.nav_health, R.id.nav_science, R.id.nav_sports, R.id.nav_tech
            ), binding.drawerLayout
        )
    }
    private val searchViewModel by viewModel<SearchViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolBar)
        initDrawer()
        initSearchView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        MenuInflater(this).inflate(R.menu.menu_news, menu)
        val item = menu.findItem(R.id.action_search)
        binding.searchView.setMenuItem(item)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == ACTION_NOTI_CLICK)
            handleNotiClick(intent)
    }

    private fun handleNotiClick(intent: Intent) {
        val destination = when (intent.getSerializableExtra(EXTRA_CATEGORY) as Category?) {
            Category.BUSINESS -> R.id.nav_business
            Category.ENTERTAINMENT -> R.id.nav_entertainment
            Category.HEALTH -> R.id.nav_health
            Category.SCIENCE -> R.id.nav_science
            Category.SPORTS -> R.id.nav_sports
            Category.TECHNOLOGY -> R.id.nav_tech
            else -> R.id.nav_general
        }
        navController.navigate(destination)
    }

    private fun initDrawer() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_search, R.id.nav_webview, R.id.nav_show_image, R.id.action_settings, R.id.action_intro ->
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                else -> binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }

            if (destination.id == R.id.nav_show_image) {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(color(R.color.dark_grey)))
                window.statusBarColor = color(R.color.dark_grey)
                closeSearchView()
            } else {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(getColorFromAttr(android.R.attr.colorPrimaryDark)))
                window.statusBarColor = Color.TRANSPARENT
            }

            if (destination.id == R.id.nav_webview)
                closeSearchView()
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    private fun initSearchView() {
        binding.searchView.setOnQueryTextListener(DebouncingQueryTextListener(lifecycleScope) { query ->
            searchViewModel.queryFlow.value = query ?: ""
        })

        binding.searchView.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewShown() {
                if (navController.currentDestination!!.id != R.id.nav_search)
                    navController.navigate(NewsFragmentDirections.actionNewsToSearch())
            }

            override fun onSearchViewClosed() {
                if (navController.currentDestination!!.id == R.id.nav_search)
                    navController.navigateUp()
            }
        })
    }

    fun closeSearchView() {
        binding.searchView.closeSearch()
    }
}