package com.hha.framework

import CItem
import android.util.Log
import com.hha.resources.Global
import com.hha.framework.Types
import com.hha.framework.CTimeFrame
import com.hha.types.CMoney
import com.hha.types.EDeletedStatus
import java.text.SimpleDateFormat
import java.util.*

class CTransaction {
    companion object {
        private const val TAG = "TRANS"
    }

    var id: Long = -1
    var customer_id: Int = 0
    lateinit var table_name: String
    lateinit var start_time: String
    var status: ClientOrdersType = ClientOrdersType.OPEN
    var total = CMoney(0)
    var has_orders: Boolean = false
    private val m_global: Global = Global.getInstance()

    private val m_item: LinkedList<CItem> = LinkedList()
    private val m_timeFrame: LinkedList<CTimeFrame> = LinkedList()

    constructor(id: Long, name: String, time: String, status: ClientOrdersType, customerId: Int,
                total: CMoney) {
        this.id = id
        this.table_name = name
        this.start_time = time
        this.status = status
        this.customer_id = customerId
        this.total = total
    }

    constructor(source: CTransaction) {
        this.id = source.id
        this.table_name = source.table_name
        this.start_time = source.start_time
        this.status = source.status
        this.customer_id = source.customer_id
        this.total = source.total

        //source.m_item.forEach { item ->
        //    m_item.add(CItem(item))
        //}

        source.m_timeFrame.forEach { timeFrame ->
            m_timeFrame.add(CTimeFrame(timeFrame))
        }
    }

    fun getMinutes(): Int {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val date = inputFormat.parse(start_time)
            val millisecond = date.time
            val now = Date().time
            ((now - millisecond + 30000) / (1000 * 60)).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            10
        }
    }

    fun calculateTotal(): CMoney {
        var total = CMoney(0)
        m_item.forEach { item ->
            if (item.deletedStatus == EDeletedStatus.DELETE_NOT) {
                total = total + item.getTotal()
            }
        }
        this.total = total
        return total
    }

    fun size(): Int = m_item.size

    fun plus1() {
        if (size() == 0) return
        var y = m_global.cursor
        var item = get(y) ?: get(--y) ?: return
        if (item.deletedStatus != EDeletedStatus.DELETE_NOT) return
        add(y, 1)
    }

    fun add(cursor: Int, quantity: Int) {

    }

    fun minus1() {

















































        //if (size() == 0) return
        //var y = m_global.cursor
        //var item = get(y) ?: get(--y) ?: return
        //if (item.deleted != CItem.DELETE_NOT) return
        //add(y, timeFrameIndex(), -1)
    }

    fun portion() {
        if (size() == 0) return
        var y = m_global.cursor
        var item = get(y) ?: get(--y) ?: return
        //if (item.deleted != CItem.DELETE_NOT) return

        //m_global.transactionItemDB.deleteSequence(id, CTimeFrameIndex(), item.sequence, CItem.DELETE_CAUSE_CHANGE_PORTION)
        //item.portion = if (item.portion == 2.toByte()) 1 else 2
        //val mi = m_global.itemDB.getItemFromIndex(item.menu_item_index)
        //var price = if (isTakeaway()) mi.takeaway_price else mi.restaurant_price

        //price = if (item.portion == 1.toByte()) {
        //    (price * 60 / 100 + 49).let { it - (it % 50) }
        //} else {
        //    (item.portion * price) / 2
       // }
        //item.unit = price

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = dateFormat.format(Date())

        //m_global.transactionItemDB.createItem(
        //    item.menu_item_id,
        //    item.sequence,
        //    timeFrameIndex(),
        //    0,
        //    id,
        //    item.quantity,
         //   item.level,
        //    item.portion,
        //    item.original,
        //    item.unit,
       //     date
       // )
    }

    // ... [rest of the methods converted similarly] ...

    private fun isTakeaway(): Boolean = table_name.startsWith("T")

    fun get(position: Int): CItem? {
        return try {
            m_item[position]
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            null
        }
    }

    fun start(): Int {
        var deviceId: Short = 3000
        try {
            deviceId = (CConfiguration.getInstance.getValue("handheld_id") + 3000).toShort()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val output = dateFormat.format(Date())
        val tf = CTimeFrame(customer_id, (m_timeFrame.size + 1).toShort(), 100, output, "", id, deviceId)
      //  m_global.timeFrameDB.createTimeFrame(tf.transaction_id, tf.time_frame_index)
        m_timeFrame.add(tf)
        m_global.timeFrame = tf
        return m_timeFrame.size
    }

    fun stop(count: Short) {
        val tf = m_timeFrame.lastOrNull() ?: return
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        tf.end_time = dateFormat.format(Date())
     //   m_global.timeFrameDB.closeTimeFrame(id, tf.time_frame_index, tf.end_time, count)
     //   m_global.transactionDB.updateTotal(m_global.transaction)
    }
}