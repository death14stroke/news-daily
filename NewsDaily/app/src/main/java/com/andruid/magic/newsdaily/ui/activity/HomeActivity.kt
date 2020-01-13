package com.andruid.magic.newsdaily.ui.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.andruid.magic.newsdaily.R
import com.andruid.magic.newsdaily.databinding.ActivityHomeBinding
import com.andruid.magic.newsdaily.eventbus.SearchEvent
import com.andruid.magic.newsdaily.ui.fragment.NewsFragmentDirections
import com.andruid.magic.newsdaily.ui.util.RxSearchObservable.fromView
import com.miguelcatalan.materialsearchview.MaterialSearchView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var disposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        binding.apply {
            setSupportActionBar(toolBar)

            disposable = fromView(binding.searchView)
                .debounce(1000, TimeUnit.MILLISECONDS)
                .filter { text: String -> text.isNotEmpty() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { query: String -> loadArticles(query) }

            val navController = findNavController(R.id.nav_host_fragment)
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_general, R.id.nav_business, R.id.nav_entertainment,
                    R.id.nav_health, R.id.nav_science, R.id.nav_sports, R.id.nav_tech
                ), drawerLayout
            )
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.nav_webview, R.id.nav_search, R.id.action_settings ->
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    else -> drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
            }
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)

            searchView.setOnSearchViewListener(object: MaterialSearchView.SearchViewListener {
                override fun onSearchViewShown() {
                    if(navController.currentDestination!!.id != R.id.nav_search)
                        navController.navigate(NewsFragmentDirections.actionNewsToSearch())
                }

                override fun onSearchViewClosed() {
                    navController.navigateUp()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.unbind()
        disposable.dispose()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (binding.searchView.isSearchOpen)
            binding.searchView.closeSearch()
        else
            super.onBackPressed()
    }

    private fun loadArticles(query: String) {
        EventBus.getDefault().post(SearchEvent(query))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(R.menu.menu_news, menu)

        val item = menu!!.findItem(R.id.action_search)
        binding.searchView.setMenuItem(item)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return NavigationUI.onNavDestinationSelected(item, navController) ||
                super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}