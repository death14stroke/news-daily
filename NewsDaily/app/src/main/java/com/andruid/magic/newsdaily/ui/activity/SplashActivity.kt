package com.andruid.magic.newsdaily.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.andruid.magic.newsdaily.util.isFirstTime
import com.andruid.magic.newsdaily.util.updateFirstTimePref

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isFirstTime()) {
            startActivity(Intent(this, IntroActivity::class.java))
            updateFirstTimePref()
        }
        else
            startActivity(Intent(this, HomeActivity::class.java))

        finish()
    }
}