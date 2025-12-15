package com.hha.util

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange

/** * A utility object for performing common color operations.
 */
object ColourUtils
{

   /**
    * Creates a brighter version of a given color by a specified factor.
    *
    * This function converts the color to the HSV (Hue, Saturation, Value) model,
    * increases the 'Value' (brightness), and then converts it back to an RGB color Int.
    *
    * @param color The original color as a @ColorInt.
    * @param factor The factor by which to brighten the color (e.g., 0.15 for 15% brighter).
    *               The value should be between 0.0 and 1.0.
    * @return The new, brighter color as a @ColorInt.
    */
   @ColorInt
   fun brighten(@ColorInt color: Int, @FloatRange(from = 0.0, to = 1.0) factor: Float): Int
   {
      // Android's Color class uses a float array to hold HSV values [Hue, Saturation, Value].
      val hsv = FloatArray(3)

      // Convert the original RGB color to its HSV representation.
      Color.colorToHSV(color, hsv)

      // Increase the 'Value' component (hsv[2]), which controls brightness.
      // We multiply the current brightness by (1 + factor) and ensure it doesn't exceed 1.0f.
      hsv[2] = (hsv[2] * (1f + factor)).coerceAtMost(1.0f)

      // Convert the modified HSV values back to an RGB color Int and return it.
      val colour = Color.HSVToColor(hsv) and 0xffffff
      return colour
   }

   fun createGradient(color1: Int, factor: Float = 0.15f): GradientDrawable
   {
      val color2 = brighten(color1, factor)

      val gradientDrawable = GradientDrawable(
         GradientDrawable.Orientation.BOTTOM_TOP,
         intArrayOf(color1, color2)
      )
      return gradientDrawable
   }

   fun createPyramidGradient(color1: Int, factor: Float = 0.15f): GradientDrawable
   {
      val color2 = brighten(color1, factor) or 0xff000000.toInt()
      val col1 = (color1 or 0xff000000.toInt())

      val gradientDrawable = GradientDrawable(
         GradientDrawable.Orientation.BOTTOM_TOP,
         intArrayOf(col1, color2, col1)
      )
      return gradientDrawable
   }

}
