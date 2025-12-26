package com.hha.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable

/**
 * A custom Drawable that fills a background with two colors in an arc.
 * One color represents the elapsed time, and the other represents the remaining time.
 * The arc starts from the top (270 degrees) and sweeps clockwise.
 */
class TimeArcDrawable : Drawable() {

   // Paint for the "elapsed time" color
   private val elapsedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.FILL
   }

   // Paint for the "remaining time" or base color
   private val remainingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.FILL
   }

   // The angle (in degrees) to sweep for the elapsed time color.
   // 0 means fully the remaining color, 360 means fully the elapsed color.
   var progressAngle: Float = 0f
      set(value) {
         field = value.coerceIn(0f, 360f) // Ensure angle is within 0-360
         invalidateSelf() // Redraw the drawable when the angle changes
      }

   // Bounding box for the arc/circle
   private val arcBounds = RectF()

   /**
    * Sets the two colors for the drawable.
    * @param elapsedColor The color for the part of the arc that is "filled".
    * @param remainingColor The color for the rest of the arc.
    */
   fun setColors(elapsedColor: Int, remainingColor: Int) {
      elapsedPaint.color = elapsedColor
      remainingPaint.color = remainingColor
      invalidateSelf()
   }

   override fun draw(canvas: Canvas) {
      // The bounds of the drawable (the view it's applied to)
      arcBounds.set(bounds)

      // First, draw the remaining color to fill the entire circle/oval
      canvas.drawOval(arcBounds, remainingPaint)

      // If there's any progress, draw the elapsed color arc on top.
      // We start from -90 degrees (the top of the circle) and sweep clockwise.
      if (progressAngle > 0) {
         canvas.drawArc(arcBounds, -90f, progressAngle, true, elapsedPaint)
      }
   }

   // --- Boilerplate for Drawable ---

   override fun setAlpha(alpha: Int) {
      elapsedPaint.alpha = alpha
      remainingPaint.alpha = alpha
      invalidateSelf()
   }

   override fun setColorFilter(colorFilter: ColorFilter?) {
      elapsedPaint.colorFilter = colorFilter
      remainingPaint.colorFilter = colorFilter
      invalidateSelf()
   }

   @Deprecated("Deprecated in Java")
   override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
