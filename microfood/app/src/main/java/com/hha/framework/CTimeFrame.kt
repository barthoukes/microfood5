package com.hha.framework

import com.hha.callback.TimeFrameOperations
import java.text.SimpleDateFormat
import java.util.Date
import com.hha.grpc.GrpcServiceFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.hha.common.CookingState
import com.hha.resources.CTimestamp
import com.hha.types.ETimeFrameIndex

class CTimeFrame
{
   var id: Int = 0
   var time_frame_index = ETimeFrameIndex(1)
   var waiter: Int = 0
   var start_time: String = ""
   var end_time: String = ""

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
      time_frame_index = cf.time_frame_index
      waiter = cf.waiter
      start_time = cf.start_time
      end_time = cf.end_time
   }

   constructor(transactionId: Int, operations: TimeFrameOperations)
   {
      m_operations = operations
      val service = GrpcServiceFactory.createDailyTimeFrameService()
      val timeFrameId = 1 + service.getLatestTimeFrameIndex(transactionId.toInt())

      val current = LocalDateTime.now()
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
      start_time = current.format(formatter)
      var x = service.insertNewTimeFrame()
      if (x == null) id = 1 else id = x
      time_frame_index = ETimeFrameIndex(timeFrameId.toShort())
      waiter = 0
      end_time = "1980-01-01 00:00:00"
   }

   fun previous()
   {
      time_frame_index = time_frame_index.previous()
   }

   fun end()
   {
      // No timestamp added
      val service = GrpcServiceFactory.createDailyTimeFrameService()
      service.endTimeFrame(
         m_operations.transactionId,
         time_frame_index.index.toInt(),
         m_operations.getDeviceId(),
         "", false,
         CookingState.COOKING_DONE
      )
   }

   fun closeTimeFrame()
   {
      // With timestamp added
      val service = GrpcServiceFactory.createDailyTimeFrameService()
      val now = CTimestamp()
      service.endTimeFrame(
         m_operations.transactionId,
         time_frame_index.toInt(),
         m_operations.getDeviceId(),
         now.getDateTime(), false,
         CookingState.COOKING_DONE
      )
   }

   /// @brief create a new time frame.
   constructor(idd: Int, tfi: ETimeFrameIndex, transactionId: Int)
   {
      id = idd
      time_frame_index = tfi
      waiter = 0

      val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      val output = dateFormat.format(Date())
      start_time = output
      end_time = "0"
   }

   fun toInt(): Int
   {
      return time_frame_index.toInt()
   }

   fun getTimeFrameIndex(): ETimeFrameIndex
   {
      return time_frame_index
   }
}
