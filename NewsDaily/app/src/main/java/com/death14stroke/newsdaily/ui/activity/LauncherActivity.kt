package com.death14stroke.newsdaily.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.death14stroke.newsdaily.data.repository.MainRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LauncherActivity: AppCompatActivity() {
    private val repository: MainRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { true }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                repository.isFirstTime().collectLatest { isFirstTime ->
                    val intent = when (isFirstTime) {
                        true -> Intent(this@LauncherActivity, IntroActivity::class.java)
                        false -> Intent(this@LauncherActivity, HomeActivity::class.java)
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}