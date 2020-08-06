package com.andruid.magic.newsdaily.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.databinding.ActivityHomeBinding

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

    private fun initDrawer() {
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_general, R.id.nav_business, R.id.nav_entertainment,
                R.id.nav_health, R.id.nav_science, R.id.nav_sports, R.id.nav_tech
            ), binding.drawerLayout
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_webview -> {
                    if (binding.searchView.isSearchOpen)
                        binding.searchView.closeSearch()
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }
                R.id.nav_search, R.id.action_settings, R.id.action_intro ->
                    binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                else -> binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }
}