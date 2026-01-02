package com.hha.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class TimeSliderView @JvmOverloads constructor(
   context: Context,
   attrs: AttributeSet? = null,
   defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)
{

   // --- State Variables ---
   private var hour: Int = 10
   private var minute: Int = 10
   private var isAm: Boolean = true

   // --- Drawing Properties ---
   private var centerX: Float = 0f
   private var centerY: Float = 0f
   private var amPmCircleRadius: Float = 0f
   private var hourCircleRadius: Float = 0f
   private var minuteCircleRadius: Float = 0f

   // --- Paint Objects ---
   private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = 4f
      color = Color.DKGRAY
   }

   private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.BLACK
      textAlign = Paint.Align.CENTER
      textSize = 40f
   }

   private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.FILL
      color = Color.BLUE // Example color for the selector
   }

   override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)
   {
      super.onSizeChanged(w, h, oldw, oldh)
      // This is called when the view's size is known.
      // We calculate all our dimensions here.
      val size = min(w, h)
      centerX = w / 2f
      centerY = h / 2f

      // Define the radii for the three concentric circles
      minuteCircleRadius = size * 0.45f
      hourCircleRadius = size * 0.30f
      amPmCircleRadius = size * 0.15f

      textPaint.textSize = amPmCircleRadius * 0.5f
   }

   override fun onDraw(canvas: Canvas)
   {
      super.onDraw(canvas)

      // 1. Draw the three concentric circles
      canvas.drawCircle(centerX, centerY, amPmCircleRadius, circlePaint)
      canvas.drawCircle(centerX, centerY, hourCircleRadius, circlePaint)
      canvas.drawCircle(centerX, centerY, minuteCircleRadius, circlePaint)

      // 2. Draw AM/PM text
      canvas.drawText(if (isAm) "AM" else "PM", centerX, centerY + textPaint.textSize / 3, textPaint)

      // 3. Draw Hour Indicator
      val hourAngle = Math.toRadians((hour / 12.0 * 360.0) - 90) // -90 to start at the top
      val hourIndicatorX = centerX + hourCircleRadius * cos(hourAngle).toFloat()
      val hourIndicatorY = centerY + hourCircleRadius * sin(hourAngle).toFloat()
      canvas.drawCircle(hourIndicatorX, hourIndicatorY, 15f, indicatorPaint)

      // 4. Draw Minute Indicator
      val minuteAngle = Math.toRadians((minute / 60.0 * 360.0) - 90)
      val minuteIndicatorX = centerX + minuteCircleRadius * cos(minuteAngle).toFloat()
      val minuteIndicatorY = centerY + minuteCircleRadius * sin(minuteAngle).toFloat()
      canvas.drawCircle(minuteIndicatorX, minuteIndicatorY, 15f, indicatorPaint)
   }

   override fun onTouchEvent(event: MotionEvent): Boolean
   {
      when (event.action)
      {
         MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
         {
            val dx = event.x - centerX
            val dy = event.y - centerY
            val distance = sqrt(dx * dx + dy * dy)

            // Determine which circle was touched
            when
            {
               distance <= amPmCircleRadius && event.action == MotionEvent.ACTION_DOWN ->
               {
                  // Inner circle (AM/PM)
                  isAm = !isAm
               }

               distance <= hourCircleRadius ->
               {
                  // Middle circle (Hours)
                  hour = angleToHour(atan2(dy, dx))
               }

               distance <= minuteCircleRadius ->
               {
                  // Outer circle (Minutes)
                  minute = angleToMinute(atan2(dy, dx))
               }
            }

            // Trigger a redraw to show the new state
            invalidate()
            return true
         }
      }
      return super.onTouchEvent(event)
   }

   private fun angleToHour(angleRad: Float): Int
   {
      val angleDeg = Math.toDegrees(angleRad.toDouble()) + 90 // Adjust for top-start
      var hour = (angleDeg / 30).toInt() // 360 / 12 hours = 30 degrees per hour
      if (hour < 0) hour += 12
      return hour
   }

   private fun angleToMinute(angleRad: Float): Int
   {
      val angleDeg = Math.toDegrees(angleRad.toDouble()) + 90
      var minute = (angleDeg / 6).toInt() // 360 / 60 minutes = 6 degrees per minute
      if (minute < 0) minute += 60
      return minute
   }
}
