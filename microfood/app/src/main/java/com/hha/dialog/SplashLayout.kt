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
class SplashLayout : androidx.activity.ComponentActivity()
{

    val global = Global.getInstance()
    val CFG = global.CFG
    private lateinit var services: GrpcServiceFactory

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initializeDatabases()
        setupConfiguration()
        configureScreen()
        setupMessageConnection()
        configureSplashText()
        scheduleNavigation()
    }

    private fun initializeDatabases()
    {
        val global = Global.getInstance().apply {
            myDir = applicationContext.filesDir.absoluteFile
        }
    }

    private fun setupConfiguration()
    {
        global.showAllprices = CFG.getOption("entry_show_prices")
        global.showAllTimes = CFG.getOption("entry_show_times")
    }

    private fun configureScreen()
    {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setupMessageConnection()
    {
        try
        {
            val path = CFG.getString("IP")
            val port = 9876 // Default port, could be made configurable
        } catch (e: ConfigNotFoundException)
        {
            e.printStackTrace()
            // Consider showing error to user or fallback behavior
        }
    }

    private fun configureSplashText()
    {
        val bigTextView = findViewById<TextView>(R.id.textBig)
        try
        {
            bigTextView.setTextSize(
                TypedValue.COMPLEX_UNIT_DIP,
                CFG.getValue("font_splash").toFloat()
            )
        } catch (e: Exception)
        {
            e.printStackTrace()
            // Fallback font size could be set here
        }
    }

    private fun scheduleNavigation()
    {
        lifecycleScope.launch {
            // Show the "Searching..." message only ONCE, outside the loop.
            Toast.makeText(this@SplashLayout, "Searching for server...", Toast.LENGTH_SHORT).show()

            val maxRetries = 720 // Try for 1 hour before giving up.
            var attempt = 0

            while (attempt < maxRetries)
            {
                var foundIp: String? = null
                try
                {
                    // --- Safe Network Scan ---
                    Log.d("SplashLayout", "Searching for server, attempt ${attempt + 1}/$maxRetries...")
                    foundIp = NetworkScanner.findFirstOpenPort(50051)

                } catch (e: Exception)
                {
                    // Log the exception if the network scan itself fails.
                    Log.e("SplashLayout", "NetworkScanner failed with an exception", e)
                }

                if (foundIp != null)
                {
                    try
                    {
                        // --- Safe Data Loading ---
                        Log.i("SplashLayout", "Server found at $foundIp. Loading data...")

                        // Save IP and load data. This might throw an exception.
                        Global.getInstance().serverIp = foundIp
                        Global.getInstance().getOptions()
                        CMenuCards.getInstance().loadTakeaway()

                        // If we get here, data loading was successful. Navigate and exit.
                        navigateToMainActivity()
                        return@launch // Stop the coroutine immediately.

                    } catch (e: Exception)
                    {
                        // If data loading fails, log it and try the whole process again.
                        Log.e("SplashLayout", "Found server but failed to load data", e)
                    }
                }

                // --- Retry Logic ---
                attempt++
                if (attempt < maxRetries)
                {
                    // Wait 3 seconds before the next attempt.
                    delay(3000)
                }
            }

            // --- FAILURE (Loop finished after all retries) ---
            // This code only runs if the while loop completes without success.
            Log.e("SplashLayout", "Could not find and connect to server after $maxRetries attempts.")
            Toast.makeText(
                this@SplashLayout,
                "Error: Could not connect to server. Please check the network and restart.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun navigateToMainActivity()
    {
        startActivity(Intent(this, MainMenuActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        //  startActivity(Intent(this@SplashLayout, MainMenuActivity::class.java))
        finish()
    }
}