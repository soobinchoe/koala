package com.traydcorp.koala

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.traydcorp.koala.ui.home.HomeActivity

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intentHome = Intent(this, HomeActivity::class.java)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(intentHome)
            finish()
        }, 2000)

    }
}