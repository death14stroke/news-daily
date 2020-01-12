package com.andruid.magic.newsdaily.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.databinding.ActivityHomeBinding
import com.andruid.magic.newsdaily.ui.util.NavArgsUtil.buildNavArgs
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.apply {
            setSupportActionBar(toolbar)

            fab.setOnClickListener { view ->
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
            val navController = findNavController(R.id.nav_host_fragment)
            navController.setGraph(
                R.navigation.navigation_home,
                buildNavArgs(getString(R.string.general))
            )
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_general, R.id.nav_business, R.id.nav_entertainment,
                    R.id.nav_health, R.id.nav_science, R.id.nav_sports, R.id.nav_tech
                ), drawerLayout
            )
            navController.addOnDestinationChangedListener { _, destination, _ ->
                if(destination.id == R.id.nav_webview)
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
            navView.setNavigationItemSelectedListener(this@HomeActivity)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        navController.navigate(
            item.itemId, buildNavArgs(
                getString(
                    when (item.itemId) {
                        R.id.nav_general -> R.string.general
                        R.id.nav_business -> R.string.business
                        R.id.nav_entertainment -> R.string.entertainment
                        R.id.nav_health -> R.string.health
                        R.id.nav_science -> R.string.science
                        R.id.nav_sports -> R.string.sports
                        R.id.nav_tech -> R.string.technology
                        else -> R.string.general
                    }
                )
            )
        )
        binding.drawerLayout.closeDrawers()
        return true
    }
}