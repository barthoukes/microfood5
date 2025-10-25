package com.hha.resources

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class CTimestamp private constructor(private var dateTime: LocalDateTime, private var allowSimulate: Boolean = true)
{

   companion object
   {
      private val lock = ReentrantLock()
      private var simulationTime: LocalDateTime? = null
      private var useSimulation = false

      private const val MINIMUM_YEAR = 1900

      fun startSimulation()
      {
         lock.withLock {
            useSimulation = true
            simulationTime = LocalDateTime.of(2014, 9, 10, 16, 20, 0)
         }
      }

      fun incrementSimulationTime()
      {
         lock.withLock {
            if (!useSimulation)
            {
               startSimulation()
            }
            simulationTime = simulationTime!!.plusSeconds(5)
         }
      }

      fun getMaxDay(year: Int, month: Int): Int
      {
         return when (month)
         {
            1, 3, 5, 7, 8, 10, 12 -> 31
            2 -> if (year % 100 == 0) 30 else if (year % 4 == 0) 29 else 28
            else -> 30
         }
      }

      fun getYearDay(year: Int, month: Int, day: Int): Int
      {
         var yd = 0
         for (m in 1 until month)
         {
            yd += getMaxDay(year, m)
         }
         yd += day - 1
         return yd
      }

      fun getWeekDay(year: Int, month: Int, day: Int): Int
      {
         val date = LocalDateTime.of(year, month, day, 1, 0)
         return (date.dayOfWeek.value - 1) % 7 // 0 = Monday
      }

      fun getWeek(year: Int, month: Int, day: Int): Int
      {
         val yd = getYearDay(year, month, day)
         val wd = getWeekDay(year, 1, 1)
         return (yd + wd) / 7
      }

      fun getFirstWeekday(year: Int, month: Int): Int
      {
         return getWeekDay(year, month, 1)
      }

      fun tijd(hour: Int = -1, minute: Int = -1, seconds: Int = -1): String
      {
         return if (hour < 0)
         {
            val now = CTimestamp()
            String.format("%d:%02d:%02d", now.hour, now.minute, now.second)
         } else
         {
            String.format("%d:%02d:%02d", hour, minute, seconds)
         }
      }
   }

   constructor() : this(LocalDateTime.now(), true)
   {
      lock.withLock {
         if (useSimulation && allowSimulate && simulationTime != null)
         {
            dateTime = simulationTime!!
         }
      }
   }

   constructor(year: Int, month: Int, day: Int) :
      this(LocalDateTime.of(year, month, day, 0, 0, 0), false)

   constructor(sqlValue: String) : this(LocalDateTime.now(), false)
   {
      val parts = sqlValue.split(" ")
      if (parts.size < 2)
      {
         // Try to parse as time only
         val timeParts = sqlValue.split(":")
         if (timeParts.size >= 2)
         {
            dateTime = LocalDateTime.now().withHour(timeParts[0].toInt())
               .withMinute(timeParts[1].toInt())
               .withSecond(if (timeParts.size >= 3) timeParts[2].toInt() else 0)
         }
         return
      }

      val datePart = parts[0]
      val timePart = parts[1]

      val dateParts = datePart.split("-")
      val timeParts = timePart.split(":")

      if (dateParts.size >= 3 && timeParts.size >= 3)
      {
         dateTime = LocalDateTime.of(
            dateParts[0].toInt(),
            dateParts[1].toInt(),
            dateParts[2].toInt(),
            timeParts[0].toInt(),
            timeParts[1].toInt(),
            timeParts[2].toInt()
         )
      }
   }

   // Comparison operators
   operator fun compareTo(other: CTimestamp): Int
   {
      return dateTime.compareTo(other.dateTime)
   }

   override fun equals(other: Any?): Boolean
   {
      if (this === other) return true
      if (other !is CTimestamp) return false
      return dateTime == other.dateTime
   }

   override fun hashCode(): Int
   {
      return dateTime.hashCode()
   }

   // Property getters
   val year: Int get() = dateTime.year
   val month: Int get() = dateTime.monthValue
   val day: Int get() = dateTime.dayOfMonth
   val hour: Int get() = dateTime.hour
   val minute: Int get() = dateTime.minute
   val second: Int get() = dateTime.second
   val millisecond: Int get() = (dateTime.nano / 1_000_000)

   // Comparison methods
   fun compareSeconds(other: CTimestamp): Long
   {
      val time1 = this.time2seconds()
      val time2 = other.time2seconds()
      return time1 - time2
   }

   fun time2seconds(): Long
   {
      return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond()
   }

   fun isAsap(config: Boolean): Boolean
   {
      val now = CTimestamp()
      return (compareSeconds(now) <= 120 && config)
   }

   fun compareDate(other: CTimestamp): Int
   {
      return when
      {
         other.year > year -> 1
         other.year < year -> -1
         other.month > month -> 1
         other.month < month -> -1
         other.day > day -> 1
         other.day < day -> -1
         else -> 0
      }
   }

   // Time manipulation methods
   fun roundUp(minutes: Int)
   {
      if (minutes <= 1) return

      dateTime = dateTime.withSecond(0).withNano(0)
      var newMinute = dateTime.minute + (minutes - 1)

      if (newMinute >= 60)
      {
         newMinute = 0
         addHours(1)
      } else
      {
         newMinute -= (newMinute % minutes)
      }

      dateTime = dateTime.withMinute(newMinute)
   }

   fun addMonth(nr: Int): Boolean
   {
      val lastDay = getMaxDay(year, month) == day
      var newDateTime = dateTime.plusMonths(nr.toLong())

      // Handle month boundaries
      while (newDateTime.monthValue > 12)
      {
         newDateTime = newDateTime.minusMonths(12).plusYears(1)
      }

      // Check minimum year
      if (newDateTime.year < MINIMUM_YEAR)
      {
         return false
      }

      // Adjust day if necessary
      val mday = getMaxDay(newDateTime.year, newDateTime.monthValue)
      val adjustedDay = if (day > mday || lastDay) mday else newDateTime.dayOfMonth

      dateTime = newDateTime.withDayOfMonth(adjustedDay)
      return true
   }

   fun addDay(nr: Int): Boolean
   {
      dateTime = dateTime.plusDays(nr.toLong())
      return true
   }

   fun addMinutes(nr: Int): Boolean
   {
      dateTime = dateTime.plusMinutes(nr.toLong())
      return true
   }

   fun addSeconds(nr: Int): Boolean
   {
      dateTime = dateTime.plusSeconds(nr.toLong())
      return true
   }

   fun addHours(nr: Int): Boolean
   {
      dateTime = dateTime.plusHours(nr.toLong())
      return true
   }

   fun addYear(nr: Int): Boolean
   {
      if (year + nr < MINIMUM_YEAR)
      {
         return false
      }
      dateTime = dateTime.plusYears(nr.toLong())

      val mday = getMaxDay(dateTime.year, dateTime.monthValue)
      if (dateTime.dayOfMonth > mday)
      {
         dateTime = dateTime.withDayOfMonth(mday)
      }
      return true
   }

   fun decreaseHours(x: Int)
   {
      dateTime = dateTime.minusHours(x.toLong())
   }

   // Getter methods
   val week: Int get() = getWeek(year, month, day)
   val weekday: Int get() = getWeekDay(year, month, day)

   val isToday: Boolean
      get()
      {
         val today = CTimestamp()
         return today.year == year && today.month == month && today.day == day
      }

   // String representation methods
   override fun toString(): String
   {
      return String.format("%02d:%02d:%02d", hour, minute, second)
   }

   fun getDateTime(): String
   {
      val actualYear = if (year < 1980) 1980 else year
      return String.format(
         "%04d-%02d-%02d  %02d:%02d:%02d",
         actualYear, month, day, hour, minute, second
      )
   }

   fun getTime(): String
   {
      return String.format("%02d:%02d:%02d", hour, minute, second)
   }

   fun getSimpleTime(): String
   {
      return String.format("%02d:%02d", hour, minute)
   }

   fun getShortTime(): String
   {
      return String.format("%02d:%02d", hour, minute)
   }

   fun getDate(): String
   {
      return String.format("%02d-%02d-%04d", day, month, year)
   }

   fun getSqlDate(): String
   {
      return String.format("%04d-%02d-%02d", year, month, day)
   }

   fun getSysTime(): String
   {
      getRealTime()
      return String.format(
         "%04d/%02d/%02d %02d:%02d:%02d.%03d: ",
         year, month, day, hour, minute, second, millisecond
      )
   }

   fun timeInRange(startTime: CTimestamp, endTime: CTimestamp): Boolean
   {
      val h = hour
      val m = minute
      val sh = startTime.hour
      val sm = startTime.minute
      val eh = endTime.hour
      val em = endTime.minute

      return !(h < sh || (h == sh && m < sm) || h > eh || (h == eh && m >= em))
   }

   private fun getRealTime()
   {
      if (!useSimulation || !allowSimulate)
      {
         dateTime = LocalDateTime.now()
      }
   }
}

// Extension functions for operator overloading
fun CTimestamp.lessThan(other: CTimestamp): Boolean = this < other
fun CTimestamp.greaterThan(other: CTimestamp): Boolean = this > other
fun CTimestamp.greaterThanOrEqual(other: CTimestamp): Boolean = this >= other