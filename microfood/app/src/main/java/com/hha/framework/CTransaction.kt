package com.hha.framework

import android.util.Log
import com.hha.common.TransactionData
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import com.hha.types.CMoney
import com.hha.types.EClientOrdersType
import com.hha.types.EDeletedStatus
import com.hha.types.EPaymentMethod
import com.hha.types.ETransType
import com.hha.types.EPaymentStatus
import java.text.SimpleDateFormat
import java.util.*

class CTransaction : Iterable<CSortedItem> {
    public var m_transactionId: Int = 0
    lateinit var name: String
    lateinit var transType: ETransType
    var rfidKeyId: Int = 0
    lateinit var type: EClientOrdersType
    var deposit: Int  = 0
    lateinit var timeStart: String
    lateinit var timeEnd: String
    lateinit var timeCustomer: String
    var subTotalLow = CMoney(0)
    var subTotalHigh = CMoney(0)
    var discountLow = CMoney(0)
    var discountHigh = CMoney(0)
    var remainsLow = 0f
    var remainsHigh = 0f
    var tipsLow = CMoney(0)
    var tipsHigh = CMoney(0)
    var totalLow = CMoney(0)
    var totalHigh = CMoney(0)
    var taxTotalLow = CMoney(0)
    var taxTotalHigh = CMoney(0)
    var customerId  = 0
    var archived = false
    var message = ""
    var subtotalTaxFree = CMoney(0)
    var totalTaxFree = CMoney(0)
    var discountTaxFree = CMoney(0)
    var tipsTaxFree = CMoney(0)

    companion object {
        private const val TAG = "TRANS"
    }

    var global = Global.getInstance()

    var status: EClientOrdersType = EClientOrdersType.OPEN
    var total: CMoney = CMoney(0)
    var hasOrders: Boolean = false
    private val m_global: Global = Global.getInstance()

    private val m_items = CTransactionItems()
    //private val m_timeFrames = CTimeFrameList()
    private val m_payments = CPaymentList()
    private lateinit var timeFrame: CTimeFrame

    // Add this iterator implementation
    override fun iterator(): Iterator<CSortedItem> = m_items.iterator()

    constructor(transactionId: Int, name: String, time: String,
                status: EClientOrdersType, customerId: Int,
                total: CMoney) {
        this.name = name
        this.status = status
        this.total = total

        this.m_transactionId = transactionId
        m_payments.setTransactionId(transactionId)

        this.name = name
        transType = ETransType.TRANS_TYPE_TAKEAWAY
        rfidKeyId = -1
        type = EClientOrdersType.OPEN
        deposit = 0
        timeStart = ""
        timeEnd = ""
        timeCustomer = ""
        subTotalLow = CMoney(0)
        subTotalHigh = CMoney(0)
        discountLow = CMoney(0)
        discountHigh = CMoney(0)
        remainsLow  = 0.0F
        remainsHigh = 0.0F
        tipsLow = CMoney(0)
        tipsHigh = CMoney(0)
        totalLow = CMoney(0)
        totalHigh = CMoney(0)
        taxTotalLow = CMoney(0)
        taxTotalHigh = CMoney(0)
        this.customerId  = customerId
        archived = false
        message = ""
        subtotalTaxFree = CMoney(0)
        totalTaxFree = CMoney(0)
        discountTaxFree = CMoney(0)
        tipsTaxFree = CMoney(0)
    }

    constructor(transactionId : Int) {
        val service = GrpcServiceFactory.createDailyTransactionService()
        val data: TransactionData? = service.selectTransactionId(transactionId.toInt())

        global.transaction = CTransaction(data)
    }

    constructor(source: CTransaction) {
        customerId = source.customerId
        name = source.name
        m_transactionId = source.m_transactionId
        transType = source.transType;
        rfidKeyId = source.rfidKeyId;
        deposit = source.deposit
        timeStart = source.timeStart
        timeCustomer = source.timeCustomer
        status = source.status;
        total = source.total
        hasOrders = false;
        subTotalLow = source.subTotalLow
        subTotalHigh = source.subTotalHigh
        discountLow = source.discountLow
        discountHigh = source.discountHigh
        remainsLow = source.remainsLow
        remainsHigh = source.remainsHigh
        tipsLow = source.tipsLow
        tipsHigh = source.tipsHigh
        totalLow = source.totalLow
        totalHigh = source.totalHigh
        taxTotalLow = source.taxTotalLow
        taxTotalHigh = source.taxTotalHigh
        archived = source.archived
        message = source.message
        subtotalTaxFree = source.subtotalTaxFree
        totalTaxFree = source.totalTaxFree
        discountTaxFree = source.discountTaxFree
        tipsTaxFree = source.tipsTaxFree
        subtotalTaxFree = source.subtotalTaxFree
        totalTaxFree = source.totalTaxFree
        discountTaxFree = source.discountTaxFree
        tipsTaxFree = source.tipsTaxFree
    }

    constructor(source: TransactionData?) {
        if (source == null) {
            m_transactionId = -1
            name = "-"
            status = EClientOrdersType.OPEN
            return
        }
        m_transactionId = source.transactionId
        customerId = source.customerId
        name = source.name
        transType = ETransType.fromTransType(source.transType)
        rfidKeyId = source.rfidKeyId;
        deposit = source.deposit
        timeStart = source.timeStart
        timeCustomer = source.timeCustomer
        status = EClientOrdersType.fromCLientOrdersType(source.getStatus());
        hasOrders = false
        subTotalLow = CMoney(source.subtotalLow.cents)
        subTotalHigh = CMoney(source.subtotalHigh.cents)
        discountLow = CMoney(source.discountLow.cents)
        discountHigh = CMoney(source.discountHigh.cents)
        remainsLow = source.remainsLow
        remainsHigh = source.remainsHigh
        tipsLow = CMoney(source.tipsLow.cents)
        tipsHigh = CMoney(source.tipsHigh.cents)
        totalLow = CMoney(source.totalLow.cents)
        totalHigh = CMoney(source.totalHigh.cents)
        taxTotalLow = CMoney(source.taxTotalLow.cents)
        taxTotalHigh = CMoney(source.taxTotalHigh.cents)
        archived = source.archived
        message = source.message
        subtotalTaxFree = CMoney(source.subtotalTaxFree.cents)
        totalTaxFree = CMoney(source.totalTaxFree.cents)
        discountTaxFree = CMoney(source.discountTaxFree.cents)
        tipsTaxFree = CMoney(source.tipsTaxFree.cents)
        subtotalTaxFree = CMoney(source.subtotalTaxFree.cents)
        totalTaxFree = CMoney(source.totalTaxFree.cents)
        discountTaxFree = CMoney(source.discountTaxFree.cents)
        tipsTaxFree = CMoney(source.tipsTaxFree.cents)

        m_payments.setTransactionId(m_transactionId)
    }

    fun getMinutes(): Int {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val date = inputFormat.parse(timeStart)
            val millisecond = date?.time ?: 0
            val now = Date().time
            ((now - millisecond + 30000) / (1000 * 60)).toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            10
        }
    }

    val size : Int
        get() = m_items.size

    fun useTakeawayPrices() : Boolean {
        return ETransType.useTakeawayPrices(transType)
    }

    fun addOneToCursorPosition()
    {
        if (size == 0) return
        var y = m_global.cursor.position
        var item = get(y) ?: get(--y) ?: return
        if (item.deletedStatus != EDeletedStatus.DELETE_NOT) return
        m_items.addQuantity(CCursor(y), -1)
    }

    fun minus1()
    {
        if (size == 0) return
        var y = m_global.cursor.position
        var item = get(y) ?: get(--y) ?: return
        if (item.deletedStatus != EDeletedStatus.DELETE_NOT) return
        m_items.addQuantity(CCursor(y), -1)
    }

    val empty : Boolean
        get() = m_items.empty

//    fun addOne()
//    {
//        if (size == 0) return
//        var y = m_global.cursor
//        var item = get(y) ?: get(--y) ?: return
//        m_items.addQuantity(y, 1)
//    }

    fun getCursor(findItem : CItem): Int
    {
        var offset = 0
        for (sortedItem : CSortedItem in m_items)
        {
            for (item : CItem in sortedItem)
            {
                if (findItem.sequence == item.sequence && findItem.subSequence == item.subSequence)
                {
                    return offset
                }
                offset = offset+1
            }
        }
        return offset
    }

    fun portion()
    {
        if (size == 0) return
        var y = m_global.cursor.position
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

        //val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        //val date = dateFormat.format(Date())

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

    private fun isTakeaway(): Boolean = when(transType) {
        ETransType.TRANS_TYPE_TAKEAWAY, ETransType.TRANS_TYPE_EAT_INSIDE,
        ETransType.TRANS_TYPE_RECHAUD, ETransType.TRANS_TYPE_UNDEFINED -> true
            else -> true
    }

    operator fun get(position: Int): CItem? {
        return m_items.getCItem(position)
    }

    fun stopTimeFrame() {
        timeFrame.end()
     //   m_global.timeFrameDB.closeTimeFrame(id, tf.time_frame_index, tf.end_time, count)
     //   m_global.transactionDB.updateTotal(m_global.transaction)
    }

    fun addTransactionItem(selectedMenuItem: CMenuItem, clusterId: Short) : Boolean {
        Log.d("CTransaction", "add transaction item: " +
                "${selectedMenuItem.menuItemId} cursor ${global.cursor}")
        return m_items.touchItem( selectedMenuItem, clusterId)
    }

    fun getTotalAmount(): CMoney
    {
         return m_items.getTotalAmount()
    }

    fun startNextTimeFrame(): CTimeFrame
    {
        Log.i("Ctransaction", "Start next TimeFrame")
        timeFrame = CTimeFrame(m_transactionId)
        global.timeFrame = timeFrame
        return timeFrame
    }

    fun getCashTotal(): CMoney {
        return m_payments.getCashTotal();
    }

    fun getCardTotal(): CMoney {
        return m_payments.getCardTotal();
    }

    fun getPaymentTotal(): CMoney {
        return m_payments.getTotal();
    }

    fun cancelPayment(index : Int, paymentStatus : EPaymentStatus)
    {
        m_payments.cancelPayment(index, paymentStatus)
    }

    fun addPayment(payment: EPaymentMethod, amount: CMoney)
    {
        m_payments.addPayment(payment, customerId, amount)
    }
}