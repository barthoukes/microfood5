package com.hha.dialog

import MainMenuDialog
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.WindowManager
import android.widget.TextView
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
            delay(2000) // 2 seconds delay
            navigateToMainMenu()
        }
    }

    private fun navigateToMainMenu() {
        startActivity(Intent(this@SplashLayout, MainMenuDialog::class.java))
        finish()
    }
}