package com.hha.dialog

import com.hha.dialog.MainMenuActivity
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.hha.exceptions.ConfigNotFoundException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.hha.framework.CConfiguration
import com.hha.resources.Global
import tech.hha.microfood.R

@SuppressLint("CustomSplashScreen")
class SplashLayout : androidx.activity.ComponentActivity() {

    private lateinit var config: CConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initializeDatabases()
        setupConfiguration()
        configureScreen()
        setupMessageConnection()
        configureSplashText()
        scheduleNavigation()
    }

    private fun initializeDatabases() {
        val global = Global.getInstance().apply {
            myDir = applicationContext.filesDir.absoluteFile
        }
    }

    private fun setupConfiguration() {
        config = CConfiguration.getInstance
    }

    private fun configureScreen() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setupMessageConnection() {
        try {
            val path = config.getString("IP")
            val port = 9876 // Default port, could be made configurable
        } catch (e: ConfigNotFoundException) {
            e.printStackTrace()
            // Consider showing error to user or fallback behavior
        }
    }

    private fun configureSplashText() {
        val bigTextView = findViewById<TextView>(R.id.textBig)
        try {
            bigTextView.setTextSize(
                TypedValue.COMPLEX_UNIT_DIP,
                config.getValue("font_splash").toFloat()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback font size could be set here
        }
    }

    private fun scheduleNavigation() {
        lifecycleScope.launch {
            delay(5000) // 2 seconds delay
            navigateToMainMenu()
        }
    }

    private fun navigateToMainMenu() {
        try {
            val intent = Intent(this@SplashLayout, MainMenuActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("SplashScreen", "Navigation failed", e)
            runOnUiThread {
                Toast.makeText(
                    this@SplashLayout,
                    "Failed to open menu: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
            // Attempt safe fallback
            // startActivity(Intent(this@SplashLayout, FallbackActivity::class.java))
            finish()
        }
    }
    private fun navigateToMainActivity() {
        startActivity(Intent(this@SplashLayout, MainMenuActivity::class.java))
        finish()
    }
}