package com.hha.dialog

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

// Make it an open class so it can be inherited
open class BaseActivity : AppCompatActivity()
{

   override fun onCreate(savedInstanceState: Bundle?)
   {
      super.onCreate(savedInstanceState)
      // This ensures that the window can draw behind system bars,
      // which is necessary for the theme attributes to work correctly.
      WindowCompat.setDecorFitsSystemWindows(window, false)
   }

   override fun onWindowFocusChanged(hasFocus: Boolean)
   {
      super.onWindowFocusChanged(hasFocus)
      // Hide the bars whenever the activity gains focus
      if (hasFocus)
      {
         hideSystemBars()
      }
   }

   private fun hideSystemBars()
   {
      val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
      // Show bars temporarily when swiping from the screen edge
      windowInsetsController.systemBarsBehavior =
         WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      // Hide both status and navigation bars
      windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
   }

   private fun toast(@StringRes messageRes: Int)
   {
      Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
   }

   fun toast(message: String)
   {
      Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
   }
}
