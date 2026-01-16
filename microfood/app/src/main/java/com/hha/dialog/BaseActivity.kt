package com.hha.dialog

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.green
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.setMargins
import androidx.lifecycle.observe
import com.hha.grpc.GrpcArcDrawable
import com.hha.grpc.GrpcStateViewModel
import tech.hha.microfood.R

// Make it an open class so it can be inherited
open class BaseActivity : AppCompatActivity()
{
   protected val grpcStateViewModel: GrpcStateViewModel by viewModels()
   private var grpcArcDrawable: GrpcArcDrawable? = null
   // A variable to store the original brightness for restoration
   private var originalBrightness: Float = -1.0f

   private fun addGrpcSpinner(rootView: ViewGroup) {
      val spinner = ImageView(this).apply {
         id = R.id.grpc_spinner // Use a stable ID from ids.xml
         visibility = View.VISIBLE
         elevation = 100f
      }

      val layoutParams = ConstraintLayout.LayoutParams(
         resources.getDimensionPixelSize(R.dimen.spinner_size),
         resources.getDimensionPixelSize(R.dimen.spinner_size)
      ).apply {
         topToTop = ConstraintLayout.LayoutParams.PARENT_ID
         endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
         setMargins(16, 16, 16, 16)
      }

      rootView.addView(spinner, layoutParams)
      setupGrpcSpinner(spinner) // Renamed method call
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

   /**
    * Restores the screen brightness to its original value.
    * This should typically be called in onDestroy() to ensure the user's
    * setting is respected when they leave the activity.
    */
   fun restoreOriginalScreenBrightness() {
      // Only restore if a valid original brightness was saved
      if (originalBrightness != -1.0f) {
         val layoutParams: WindowManager.LayoutParams = window.attributes
         layoutParams.screenBrightness = originalBrightness
         window.attributes = layoutParams
      }
   }

   /**
    * Sets the screen brightness for this activity to maximum.
    * It saves the original brightness setting so it can be restored later.
    */
   fun setMaxScreenBrightness() {
      // Save the original brightness only if it hasn't been saved already
      if (originalBrightness == -1.0f) {
         originalBrightness = window.attributes.screenBrightness
      }
      val layoutParams: WindowManager.LayoutParams = window.attributes
      layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
      window.attributes = layoutParams
   }

   override fun setContentView(layoutResID: Int)
   {
      super.setContentView(layoutResID)
      findViewById<ImageView>(R.id.grpc_spinner)?.let { spinner ->
         setupGrpcSpinner(spinner)
      }
   }

   override fun setContentView(view: View?)
   {
      super.setContentView(view)
      view?.findViewById<ImageView>(R.id.grpc_spinner)?.let { spinner ->
         setupGrpcSpinner(spinner)
      }
   }

   // Renamed method
   private fun setupGrpcSpinner(spinner: ImageView) {
      if (grpcArcDrawable != null) return

      grpcArcDrawable = GrpcArcDrawable().apply {
         setColors(
            sentColor = ContextCompat.getColor(this@BaseActivity, R.color.COLOUR_HEADER_BACKGROUND),
            confirmedColor = ContextCompat.getColor(this@BaseActivity, R.color.COLOUR_BUTTON5_TEXT),
            backgroundColor = ContextCompat.getColor(this@BaseActivity, R.color.ANDROID_BUTTON_BACKGROUND)
         )
      }
      spinner.setImageDrawable(grpcArcDrawable)
      spinner.visibility = View.VISIBLE

      // Observe the renamed LiveData property
      grpcStateViewModel.grpcState.observe(this) { state ->
         state?.let {
            grpcArcDrawable?.updateProgress(
               it.sentAngle, it.confirmedAngle)
         }
      }
   }

   fun toast(message: String)
   {
      Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
   }

   private fun toast(@StringRes messageRes: Int)
   {
      Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
   }

}
