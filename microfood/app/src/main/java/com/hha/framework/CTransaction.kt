package com.hha.framework

import android.util.Log
import com.hha.callback.TransactionListener
import com.hha.common.CookingState
import com.hha.common.TransactionData
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import com.hha.resources.CTimestamp
import com.hha.types.CMoney
import com.hha.types.EClientOrdersType
import com.hha.types.EDeletedStatus
import com.hha.types.EPaymentMethod
import com.hha.types.ETransType
import com.hha.types.EPaymentStatus
import java.text.SimpleDateFormat
import java.util.*

class CTransaction : Iterable<CSortedItem>
{
    public var m_transactionId: Int = 0
    lateinit var name: String
    lateinit var m_transType: ETransType
    var rfidKeyId: Int = 0
    lateinit var type: EClientOrdersType
    var deposit: Int = 0
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
    var customerId = 0
    var archived = false
    var message = ""
    var subtotalTaxFree = CMoney(0)
    var totalTaxFree = CMoney(0)
    var discountTaxFree = CMoney(0)
    var tipsTaxFree = CMoney(0)

    companion object
    {
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
    private var m_sizeAtStart : Int = 0

    // Add this iterator implementation
    override fun iterator(): Iterator<CSortedItem> = m_items.iterator()

    constructor(
        transactionId: Int, name: String, time: String,
        status: EClientOrdersType, customerId: Int,
        total: CMoney
    )
    {
        this.name = name
        this.status = status
        this.total = total

        this.m_transactionId = transactionId
        m_payments.setTransactionId(transactionId)

        this.name = name
        m_transType = ETransType.TRANS_TYPE_TAKEAWAY
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
        remainsLow = 0.0F
        remainsHigh = 0.0F
        tipsLow = CMoney(0)
        tipsHigh = CMoney(0)
        totalLow = CMoney(0)
        totalHigh = CMoney(0)
        taxTotalLow = CMoney(0)
        taxTotalHigh = CMoney(0)
        this.customerId = customerId
        archived = false
        message = ""
        subtotalTaxFree = CMoney(0)
        totalTaxFree = CMoney(0)
        discountTaxFree = CMoney(0)
        tipsTaxFree = CMoney(0)
    }

    constructor(transactionId: Int)
    {
        val service = GrpcServiceFactory.createDailyTransactionService()
        val data: TransactionData? = service.selectTransactionId(transactionId.toInt())

        global.transaction = CTransaction(data)
        m_sizeAtStart = m_items.itemLines()
    }

    constructor(source: CTransaction)
    {
        customerId = source.customerId
        name = source.name
        m_transactionId = source.m_transactionId
        m_transType = source.m_transType;
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

    constructor(source: TransactionData?)
    {
        if (source == null)
        {
            m_transactionId = -1
            name = "-"
            status = EClientOrdersType.OPEN
            return
        }
        m_transactionId = source.transactionId
        customerId = source.customerId
        name = source.name
        m_transType = ETransType.fromTransType(source.transType)
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
        m_sizeAtStart = m_items.itemLines()
    }

    fun getTotalTransaction(): CMoney
    {
        return totalHigh+totalLow+totalTaxFree
    }

    fun hasAnyChanges() : Boolean
    {
        return m_items.hasAnyChanges()
    }

    fun addListener(listener: TransactionListener)
    {
        m_items.addListener(listener)
    }

    fun removeListener(listener: TransactionListener)
    {
        m_items.removeListener(listener)
    }

    fun transactionEmptyAtStartAndAtEnd(): Boolean
    {
        return m_sizeAtStart == 0 && m_items.itemLines() == 0
    }

    fun getCursor(selectedTransactionItem: CItem): Int
    {
        return m_items.getCursor(selectedTransactionItem)
    }

    fun get(position: Int): CItem?
    {
        return m_items.get(position)
    }

    fun itemSize() = m_items.itemLines()

    fun closeTimeFrame()
    {
        global.timeFrame.closeTimeFrame()
        global.timeFrame.previous()
    }

    fun removeTimeFrame()
    {
        m_items.undoTimeFrame( global.timeFrame.time_frame_index,
            global.CFG.getShort("pc_number"))
        closeTimeFrame()
    }

    fun itemLines(): Int
    {
        return m_items.itemLines()
    }

    fun itemSum(): Int
    {
        return m_items.itemSum()
    }

    fun emptyTransaction(reason: String)
    {
        // No printing of kitchen, just go to billing...
        if (global.transaction != null)
        {
            val itemSum = itemSum()
            if (itemSum != 0)
            {
                removeTimeFrame()
            } else
            {
                closeTimeFrame()
            }
            addReturnMoney()
            if (reason.length() > 0)
                { m_clientOrdersHandler ->
                    setMessage(reason);
                }
            m_clientOrdersHandler->updateTotal()
            m_clientOrdersHandler->cleanCurrentTransaction()
        }
    }

    fun addReturnMoney(total: CMoney)
    {
        // Only close if all payments together are bigger or equal to the total.
        val total = getTotalTransaction()
        m_payments.addReturnMoney(total)
    }

    fun getMinutes(): Int
    {
        return try
        {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val date = inputFormat.parse(timeStart)
            val millisecond = date?.time ?: 0
            val now = Date().time
            ((now - millisecond + 30000) / (1000 * 60)).toInt()
        } catch (e: Exception)
        {
            e.printStackTrace()
            10
        }
    }

    val size: Int
        get() = m_items.itemLines()

    val itemSize: Int
        get() = m_items.itemLines()

    var transactionId: Int = 0
        get() = m_transactionId

    fun useTakeawayPrices(): Boolean
    {
        return ETransType.useTakeawayPrices(m_transType)
    }

    fun addOneToCursorPosition()
    {
        if (size == 0) return
        var y = m_global.cursor.position
        var item = get(y) ?: get(--y) ?: return
        if (item.deletedStatus != EDeletedStatus.DELETE_NOT) return
        m_items.addQuantity(CCursor(y), 1)
    }

    fun minus1()
    {
        if (size == 0) return
        var y = m_global.cursor.position
        var item = get(y) ?: get(--y) ?: return
        if (item.deletedStatus != EDeletedStatus.DELETE_NOT) return
        m_items.addQuantity(CCursor(y), -1)
    }

    val empty: Boolean
        get() = m_items.empty

    fun nextPortion()
    {
        if (size == 0) return
        var y = m_global.cursor.position
        var item = get(y) ?: get(--y) ?: return
        if (item.deletedStatus != EDeletedStatus.DELETE_NOT) return
        m_items.nextPortion(CCursor(y))
    }

    public fun remove()
    {
        if (size == 0) return
        var y = m_global.cursor.position
        var item = get(y) ?: get(--y) ?: return
        if (item.deletedStatus != EDeletedStatus.DELETE_NOT) return
        m_items.addQuantity(CCursor(y), -item.getQuantity())
    }

    fun isTakeaway(): Boolean = when (m_transType)
    {
        ETransType.TRANS_TYPE_TAKEAWAY, ETransType.TRANS_TYPE_EAT_INSIDE,
        ETransType.TRANS_TYPE_RECHAUD, ETransType.TRANS_TYPE_UNDEFINED
            -> true

        else -> false
    }

    fun stopTimeFrame()
    {
        timeFrame.end()
    }

    fun addTransactionItem(selectedMenuItem: CMenuItem, clusterId: Short): Boolean
    {
        Log.d(
            "CTransaction", "add transaction item: " +
               "${selectedMenuItem.menuItemId} cursor ${global.cursor}"
        )
        return m_items.touchItem(selectedMenuItem, clusterId)
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

    fun getCashTotal(): CMoney
    {
        return m_payments.getCashTotal();
    }

    fun getCardTotal(): CMoney
    {
        return m_payments.getCardTotal();
    }

    fun getPaymentTotal(): CMoney
    {
        return m_payments.getTotal();
    }

    fun cancelPayment(index: Int, paymentStatus: EPaymentStatus)
    {
        m_payments.cancelPayment(index, paymentStatus)
    }

    fun addPayment(payment: EPaymentMethod, amount: CMoney)
    {
        m_payments.addPayment(payment, customerId, amount)
    }
}