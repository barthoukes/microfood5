package com.hha.framework

import java.text.SimpleDateFormat
import java.util.Date
import com.hha.exceptions.ConfigNotFoundException
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.hha.common.CookingState
import com.hha.resources.CTimestamp
import com.hha.types.ETimeFrameIndex

class CTimeFrame
{
    var id: Int = 0
    var time_frame_index = ETimeFrameIndex(1)
    var transaction_id: Int = 0
    var waiter: Int = 0
    var start_time: String = ""
    var end_time: String = ""
    var device_id: Short = 0
    val global = Global.getInstance()
    val CFG = global.CFG

    /// @brief constructor.
    constructor(cf: CTimeFrame)
    {
        id = cf.id
        this.time_frame_index = cf.time_frame_index
        transaction_id = cf.transaction_id
        waiter = cf.waiter
        start_time = cf.start_time
        end_time = cf.end_time
        device_id = cf.device_id
    }

    constructor(transactionId: Int)
    {
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
        transaction_id = global.transactionId
        device_id = global.pcNumber
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
            transaction_id,
            time_frame_index.index.toInt(),
            device_id, "", false,
            CookingState.COOKING_DONE
        )
    }

    fun closeTimeFrame()
    {
        // With timestamp added
        val service = GrpcServiceFactory.createDailyTimeFrameService()
        val now = CTimestamp()
        service.endTimeFrame(global.transactionId,
            time_frame_index.toInt(),
            device_id, now.getDateTime(), false,
            CookingState.COOKING_DONE)
    }

    /// @brief create a new time frame.
    constructor(idd: Int, tfi: ETimeFrameIndex, transactionId: Int)
    {
        id = idd
        time_frame_index = tfi
        try
        {
            device_id = (Global.getInstance().firstTablet +
               CFG.getValue("handheld_id") + 3000).toShort()
        } catch (e: NumberFormatException)
        {
            e.printStackTrace()
        } catch (e: ConfigNotFoundException)
        {
            e.printStackTrace()
        }
        waiter = 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val output = dateFormat.format(Date())
        start_time = output
        end_time = "0"
        transaction_id = transactionId
        try
        {
            device_id = (Global.getInstance().firstTablet +
               CFG.getValue("handheld_id") + 3000).toShort()
        } catch (e: NumberFormatException)
        {
            e.printStackTrace()
        } catch (e: ConfigNotFoundException)
        {
            e.printStackTrace()
        }
    }

    fun toInt(): Int
    {
        return time_frame_index.toInt()
    }
}