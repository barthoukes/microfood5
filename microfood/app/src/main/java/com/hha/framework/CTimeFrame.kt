package com.hha.framework

import java.text.SimpleDateFormat
import java.util.Date
import com.hha.exceptions.ConfigNotFoundException
import com.hha.resources.Global

class CTimeFrame {
    var id: Int = 0
    var time_frame_index: Short = 0
    var transaction_id: Long = 0
    var waiter: Int = 0
    var start_time: String = ""
    var end_time: String = ""
    var device_id: Short = 0
    val CFG = Global.getInstance().CFG

    /// @brief constructor.
    constructor(cf: CTimeFrame) {
        id = cf.id
        time_frame_index = cf.time_frame_index
        transaction_id = cf.transaction_id
        waiter = cf.waiter
        start_time = cf.start_time
        end_time = cf.end_time
        device_id = cf.device_id
    }

    constructor(idd: Int, tfi: Short, wtr: Int, st: String, et: String, trx: Long, dev: Short) {
        id = idd
        time_frame_index = tfi
        waiter = wtr
        start_time = st
        end_time = et
        transaction_id = trx
        device_id = dev
    }

    /// @brief create a new time frame.
    constructor(idd: Int, tfi: Short, trx: Long) {
        id = idd
        time_frame_index = tfi
        try {
            device_id = (Global.getInstance().firstTablet +
                    CFG.getValue("handheld_id") + 3000).toShort()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: ConfigNotFoundException) {
            e.printStackTrace()
        }
        waiter = 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val output = dateFormat.format(Date())
        start_time = output
        end_time = "0"
        transaction_id = trx
        try {
            device_id = (Global.getInstance().firstTablet +
                    CFG.getValue("handheld_id") + 3000).toShort()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: ConfigNotFoundException) {
            e.printStackTrace()
        }
    }
}