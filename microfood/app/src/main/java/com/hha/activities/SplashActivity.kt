package com.hha.activities

import com.hha.dialog.MainMenuActivity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hha.dialog.BaseActivity
import com.hha.framework.CMenuCards
import com.hha.resources.MenuRepository
import kotlinx.coroutines.delay
import tech.hha.microfood.databinding.ActivitySplashBinding
import tech.hha.microfood.databinding.ActivityAboutBinding
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val SPLASH_DISPLAY_DURATION_MS = 2000L // 2 seconds minimum display time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Optional: Make it truly full screen (hiding status and navigation bars)
        // window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        //         or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        //         or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        //         or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        //         or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        //         or View.SYSTEM_UI_FLAG_FULLSCREEN)


        // Start loading data (e.g., menu via gRPC) and navigate
        lifecycleScope.launch {
            val startTime = System.currentTimeMillis()

            // --- Simulate gRPC call or actual data fetching ---
            // binding.progressBarLoading.visibility = View.VISIBLE // Show progress
            // try {
            //     val menuData = fetchMenuDataViaGRPC() // Your suspend function
            //     // Store menuData or pass it via Intent if small, or use a ViewModel/Repository
            //     Log.d("SplashActivity", "Menu data fetched: $menuData")
            // } catch (e: Exception) {
            //     Log.e("SplashActivity", "Error fetching menu data", e)
            //     // Handle error, maybe show a message or proceed with default data
            // }
            // binding.progressBarLoading.visibility = View.GONE // Hide progress
            // --- End of data fetching ---

            val timeTaken = System.currentTimeMillis() - startTime
            val remainingDelay = SPLASH_DISPLAY_DURATION_MS - timeTaken

            if (remainingDelay > 0) {
                delay(remainingDelay) // Ensure splash is shown for at least 2 seconds




            }
            CMenuCards.getInstance().loadTakeaway()
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
        //val mainIntent = Intent(this@SplashActivity, MainMenuActivity::class.java)
        val mainIntent = Intent(this@SplashActivity, MainMenuActivity::class.java)
        // Optional: Pass any fetched data to MainActivity
        // mainIntent.putExtra("MENU_DATA", "your_fetched_menu_data_string_or_parcelable")
        startActivity(mainIntent)
        finish() // Close the SplashActivity so it's not in the back stack
    }
}