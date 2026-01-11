package com.hha.grpc

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable

/**
 * A custom Drawable to visualize the state of asynchronous communication (e.g., gRPC).
 * It shows a "sent" arc for outgoing requests and a "confirmed" arc for successful responses.
 * When both arcs are equal, the drawable is clear, indicating no pending communication.
 * The arcs start from the top (270 degrees) and sweep clockwise.
 */
class GrpcArcDrawable : Drawable()
{

   // Paint for the "sent requests" arc
   private val sentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.FILL
   }

   // Paint for the "confirmed responses" arc
   private val confirmedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.FILL
   }

   // Paint for the background when there is pending communication
   private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.FILL
   }

   // The angle (in degrees) for the sent requests arc.
   var sentAngle: Float = 0f
      private set

   // The angle (in degrees) for the confirmed responses arc.
   var confirmedAngle: Float = 0f
      private set

   // Bounding box for the arc/circle
   private val arcBounds = RectF()

   /**
    * Initializes the colors for the drawable.
    * @param sentColor The color for the arc representing sent requests.
    * @param confirmedColor The color for the arc representing confirmed responses.
    * @param backgroundColor The background color when communication is active.
    */
   fun setColors(sentColor: Int, confirmedColor: Int, backgroundColor: Int)
   {
      sentPaint.color = sentColor
      confirmedPaint.color = confirmedColor
      backgroundPaint.color = backgroundColor
      invalidateSelf()
   }

   /**
    * Updates the angles for the sent and confirmed progress.
    * Angles are automatically wrapped around 360 degrees.
    * @param newSentAngle The new total angle for sent requests.
    * @param newConfirmedAngle The new total angle for confirmed responses.
    */
   fun updateProgress(newSentAngle: Float, newConfirmedAngle: Float)
   {
      this.sentAngle = newSentAngle % 360
      this.confirmedAngle = newConfirmedAngle % 360
      invalidateSelf() // Redraw the drawable
   }

   override fun draw(canvas: Canvas)
   {
      // If sent and confirmed angles are the same, there's nothing to draw. We are idle.
      if (sentAngle == confirmedAngle)
      {
         return
      }

      // The bounds of the drawable (the view it's applied to)
      arcBounds.set(bounds)

      // 1. Draw the background color to fill the entire circle/oval
      canvas.drawOval(arcBounds, backgroundPaint)

      // 2. Draw the "sent" arc on top of the background.
      //    We draw from the confirmed angle to the sent angle.
      var sweepAngle = (sentAngle - confirmedAngle + 360) % 360
      val minAngle = 5f
      if (sweepAngle > 0 && sweepAngle < minAngle)
      {
         sweepAngle = minAngle
      }
      if (sweepAngle > 0)
      {
         // Start drawing from -90 (top) plus the confirmed angle
         canvas.drawArc(arcBounds, -90f + confirmedAngle, sweepAngle, true, sentPaint)
      }
   }

   // --- Boilerplate for Drawable ---

   override fun setAlpha(alpha: Int)
   {
      sentPaint.alpha = alpha
      confirmedPaint.alpha = alpha
      backgroundPaint.alpha = alpha
      invalidateSelf()
   }

   override fun setColorFilter(colorFilter: ColorFilter?)
   {
      sentPaint.colorFilter = colorFilter
      confirmedPaint.colorFilter = colorFilter
      backgroundPaint.colorFilter = colorFilter
      invalidateSelf()
   }

   @Deprecated("Deprecated in Java")
   override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
