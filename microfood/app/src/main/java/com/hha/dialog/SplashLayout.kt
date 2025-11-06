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
import com.hha.framework.CMenuCards
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.hha.resources.Global
import com.hha.grpc.GrpcServiceFactory
import com.hha.network.NetworkScanner
import tech.hha.microfood.R

@SuppressLint("CustomSplashScreen")
class SplashLayout : androidx.activity.ComponentActivity() {

    val global = Global.getInstance()
    val CFG = global.CFG
    private lateinit var services: GrpcServiceFactory

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
        global.showAllprices = CFG.getOption("entry_show_prices")
        global.showAllTimes = CFG.getOption("entry_show_times")
    }

    private fun configureScreen() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setupMessageConnection() {
        try {
            val path = CFG.getString("IP")
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
                CFG.getValue("font_splash").toFloat()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback font size could be set here
        }
    }

    private fun scheduleNavigation() {
        lifecycleScope.launch {
            delay(100) // 2 seconds delay

            Toast.makeText(this@SplashLayout, "Searching for server...", Toast.LENGTH_SHORT).show()

            val foundIp = NetworkScanner.findFirstOpenPort(50051)

            if (foundIp != null) {
                // Save the found IP to the global instance
                Global.getInstance().serverIp = foundIp
                // Proceed to the main part of the app
                Global.getInstance().getOptions()
                CMenuCards.getInstance().loadTakeaway()
                navigateToMainActivity()
            } else {
                // Show an error message
                Toast.makeText(this@SplashLayout, "Error: Could not find server.", Toast.LENGTH_LONG).show()
                // Optionally, you could add a "Retry" button or close the app
            }
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
        startActivity(Intent(this, MainMenuActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
      //  startActivity(Intent(this@SplashLayout, MainMenuActivity::class.java))
        finish()
    }
}