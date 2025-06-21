package com.hha.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hha.dialog.MainMenuActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.hha.microfood.databinding.ActivityAboutBinding

class AboutActivity  : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding
    private val ABOUT_DISPLAY_DURATION_MS = 20000L // 2 seconds minimum display time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Optional: Make it truly full screen (hiding status and navigation bars)
        // window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        //         or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        //         or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        //         or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        //         or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        //         or View.SYSTEM_UI_FLAG_FULLSCREEN)

        // Set up the back button click listener
        binding.continueButton.setOnClickListener {
            navigateToMain()
        }

        // Start loading data (e.g., menu via gRPC) and navigate
        lifecycleScope.launch {
            delay(ABOUT_DISPLAY_DURATION_MS) // Ensure splash is shown for at least 2 seconds
            navigateToMain()
        }
    }

    // Example suspend function for gRPC call (replace with your actual implementation)
    // private suspend fun fetchMenuDataViaGRPC(): String {
    //     delay(1500) // Simulate network delay
    //     return "Menu Item 1, Menu Item 2"
    // }

    private fun navigateToMain() {
        // Intent to start MainActivity which will host your MainMenuDialog
        val mainIntent = Intent(this@AboutActivity, MainMenuActivity::class.java)
        // Optional: Pass any fetched data to MainActivity
        // mainIntent.putExtra("MENU_DATA", "your_fetched_menu_data_string_or_parcelable")
        startActivity(mainIntent)
        finish() // Close the SplashActivity so it's not in the back stack
    }
}
