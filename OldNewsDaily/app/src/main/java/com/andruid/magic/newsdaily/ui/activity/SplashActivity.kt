package com.andruid.magic.newsdaily.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.andruid.magic.newsdaily.util.PrefUtil

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(PrefUtil.isFirstTime(this))
            startActivity(Intent(this, IntroActivity::class.java))
        else
            startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}