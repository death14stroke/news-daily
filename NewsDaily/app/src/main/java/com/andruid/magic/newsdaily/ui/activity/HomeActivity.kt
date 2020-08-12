package com.andruid.magic.newsdaily.ui.activity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.data.ACTION_NOTI_CLICK
import com.andruid.magic.newsdaily.data.ACTION_SEARCH_ARTICLES
import com.andruid.magic.newsdaily.data.EXTRA_CATEGORY
import com.andruid.magic.newsdaily.data.EXTRA_QUERY
import com.andruid.magic.newsdaily.database.repository.DbRepository
import com.andruid.magic.newsdaily.databinding.ActivityHomeBinding
import com.andruid.magic.newsdaily.ui.custom.DebouncingQueryTextListener
import com.andruid.magic.newsdaily.ui.fragment.NewsFragmentDirections
import com.andruid.magic.newsdaily.util.color
import com.andruid.magic.newsdaily.util.getColorFromAttr
import com.google.android.material.textview.MaterialTextView
import com.miguelcatalan.materialsearchview.MaterialSearchView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    private lateinit var binding: ActivityHomeBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        initDrawer()
        initSearchView()

        if (intent.action == ACTION_NOTI_CLICK)
            handleNotiClick(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(R.menu.menu_news, menu)

        val item = menu!!.findItem(R.id.action_search)
        binding.searchView.setMenuItem(item)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(item, navController) ||
                super.onOptionsItemSelected(item)
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
        val categories = resources.getStringArray(R.array.categories)
        val destination = when (intent.getStringExtra(EXTRA_CATEGORY)) {
            categories[1] -> R.id.nav_business
            categories[2] -> R.id.nav_entertainment
            categories[3] -> R.id.nav_health
            categories[4] -> R.id.nav_science
            categories[5] -> R.id.nav_sports
            categories[6] -> R.id.nav_tech
            else -> R.id.nav_general
        }

        navController.navigate(destination)
    }

    private fun initDrawer() {
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_general, R.id.nav_business, R.id.nav_entertainment,
                R.id.nav_health, R.id.nav_science, R.id.nav_sports, R.id.nav_tech
            ), binding.drawerLayout
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_search, R.id.nav_webview, R.id.nav_show_image, R.id.action_settings, R.id.action_intro ->
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                else -> binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }

            if (destination.id == R.id.nav_show_image) {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(color(R.color.dark_grey)))
                window.statusBarColor = color(R.color.dark_grey)
            } else {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(getColorFromAttr(R.attr.colorPrimaryDark)))
                window.statusBarColor = Color.TRANSPARENT
            }

            if (destination.id == R.id.nav_webview && binding.searchView.isSearchOpen)
                binding.searchView.closeSearch()
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        val categories = resources.getStringArray(R.array.categories)
        val itemIds = intArrayOf(
            R.id.nav_general,
            R.id.nav_business,
            R.id.nav_entertainment,
            R.id.nav_health,
            R.id.nav_science,
            R.id.nav_sports,
            R.id.nav_tech
        )

        val flows = mutableListOf<Flow<Int>>()

        lifecycleScope.launch {
            categories.forEach { category ->
                flows.add(DbRepository.countUnread(category))
            }

            flows.forEach { flow ->
                flow.collect { unread ->
                /*(binding.navView.menu.findItem(itemId).actionView as MaterialTextView).text =
                    unread.toString()*/
                    Log.d("unreadLog", "unread = $unread")
            }
            }
        }
    }

    private fun initSearchView() {
        binding.searchView.setOnQueryTextListener(DebouncingQueryTextListener(lifecycleScope) { newText ->
            val intent = Intent(ACTION_SEARCH_ARTICLES)
                .putExtra(EXTRA_QUERY, newText)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
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