package com.hha.framework

import com.hha.callback.TimeFrameOperations
import java.text.SimpleDateFormat
import java.util.Date
import com.hha.grpc.GrpcServiceFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.hha.common.CookingState
import com.hha.resources.CTimestamp
import com.hha.types.ECookingState
import com.hha.types.ETimeFrameIndex

class CTimeFrame
{
   var id: Int = 0
   var timeFrameIndex = ETimeFrameIndex(1) // @todo add getter
   var waiter: Int = 0
   var startTime: String = ""
   var endTime: String = ""

   lateinit var m_operations: TimeFrameOperations

   /// @brief constructor.
   constructor(operations: TimeFrameOperations)
   {
      m_operations = operations
   }

   constructor(cf: CTimeFrame)
   {
      id = cf.id
      m_operations = cf.m_operations
      timeFrameIndex = cf.timeFrameIndex
      waiter = cf.waiter
      startTime = cf.startTime
      endTime = cf.endTime
   }

   constructor(transactionId: Int, operations: TimeFrameOperations)
   {
      m_operations = operations
      val service = GrpcServiceFactory.createDailyTimeFrameService()
      val timeFrameId = 1 + service.getLatestTimeFrameIndex(transactionId.toInt())

      val current = LocalDateTime.now()
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      startTime = current.format(formatter)
      var x = service.insertNewTimeFrame()
      if (x == null) id = 1 else id = x
      timeFrameIndex = ETimeFrameIndex(timeFrameId.toShort())
      waiter = 0
      endTime = "1980-01-01 00:00:00"
   }

   /// @brief create a new time frame.
   constructor(idd: Int, tfi: ETimeFrameIndex, transactionId: Int)
   {
      id = idd
      timeFrameIndex = tfi
      waiter = 0

      val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      val output = dateFormat.format(Date())
      startTime = output
      endTime = "0"
   }

   fun changeDeliverTime(transactionId: Int, pcNumber: Short,
                         timeFrameIndex: ETimeFrameIndex, timer: CTimestamp)
   {
      val service = GrpcServiceFactory.createDailyTimeFrameService()
      service.changeDeliverTime(
         transactionId, timeFrameIndex.toInt(),
         pcNumber,
         timer.getDateTime())
   }

   fun endTimeFrame()
   {
      // With timestamp added
      val service = GrpcServiceFactory.createDailyTimeFrameService()
      val now = CTimestamp()
      service.endTimeFrame(
         m_operations.transactionId,
         timeFrameIndex.index,
         m_operations.getDeviceId(),
         now.getDateTime(), false,
         CookingState.COOKING_DONE
      )
   }

   fun end()
   {
      // No timestamp added
      val service = GrpcServiceFactory.createDailyTimeFrameService()
      service.endTimeFrame(
         m_operations.transactionId,
         timeFrameIndex.index.toShort(),
         m_operations.getDeviceId(),
         "", false,
         CookingState.COOKING_DONE
      )
   }

   fun endTimeFrame(
      transactionId: Int,
      pcNumber: Short,
      newTime: String,
      timeChanged: Boolean,
      newState: ECookingState
   )
   {
      val service = GrpcServiceFactory.createDailyTimeFrameService()

      val state = newState.toCookingState()
      service.endTimeFrame(
         transactionId, timeFrameIndex.index,
         pcNumber, newTime,
         timeChanged, state)
   }

   fun getLatestTimeFrameIndex(transactionId: Int)
   {
      val service = GrpcServiceFactory.createDailyTimeFrameService()
      this.timeFrameIndex = ETimeFrameIndex(
         service.getLatestTimeFrameIndex(transactionId))
   }

//   fun getTimeFrameIndex(): ETimeFrameIndex
//   {
//      return timeFrameIndex
//   }

   fun getValidTimeFrame(): ETimeFrameIndex
   {
      if (timeFrameIndex.toInt() <= 1)
         return ETimeFrameIndex(ETimeFrameIndex.TIME_FRAME1)
      return timeFrameIndex
   }

   fun next()
   {
      timeFrameIndex = timeFrameIndex.next()
   }

   fun previous()
   {
      timeFrameIndex = timeFrameIndex.previous()
   }

   suspend fun startTimeFrame(deviceId: Short, transactionId: Int, personId: Short)
   {
      val service = GrpcServiceFactory.createDailyTimeFrameService()
      service.startTimeFrame(deviceId, transactionId, personId, timeFrameIndex.index)
   }

   fun toInt(): Int
   {
      return timeFrameIndex.toInt()
   }
}
