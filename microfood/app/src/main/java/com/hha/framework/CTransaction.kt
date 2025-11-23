package com.hha.framework

import android.util.Log
import com.hha.callback.TimeFrameOperations
import com.hha.callback.ItemOperations
import com.hha.callback.PaymentsListener
import com.hha.callback.TransactionItemListener
import com.hha.callback.TransactionListener
import com.hha.callback.TransactionOperations
import com.hha.common.TransactionData
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import com.hha.service.StorePartService
import com.hha.service.StoreSmurfService
import com.hha.types.C3Moneys
import com.hha.types.CMoney
import com.hha.types.EClientOrdersType
import com.hha.types.EDeletedStatus
import com.hha.types.EItemSort
import com.hha.types.EPaymentMethod
import com.hha.types.ETransType
import com.hha.types.EPaymentStatus
import com.hha.types.ETaxType
import com.hha.types.ETimeFrameIndex
import java.text.SimpleDateFormat
import java.util.*

class CTransaction : Iterable<CSortedItem>,
    TransactionOperations, TimeFrameOperations, ItemOperations,
    PaymentsListener, TransactionItemListener
{
    var data = CTransactionData()

    companion object
    {
        private const val TAG = "TRANS"
    }

    var global = Global.getInstance()
    var CFG = global.CFG

    var hasOrders: Boolean = false
    private val m_global: Global = Global.getInstance()
    private var m_customer: CCustomer? = null
    private val m_items = CTransactionItems(this)

    private val m_listeners = mutableListOf<TransactionListener>()
    //private val m_timeFrames = CTimeFrameList()
    private val m_payments = CPaymentList(this)
    private lateinit var m_timeFrame: CTimeFrame
    private var m_sizeAtStart : Int = 0

    val transactionType : ETransType
        get() = data.transType

    val transType : ETransType
        get() = data.transType

    val customerTime : String
        get() = data.timeCustomer

    val deliverTime : String
        get() = data.timeCustomer

    val displayName : String
        get() = data.name

    constructor(
        transactionId: Int, name: String, time: String,
        status: EClientOrdersType, customerId: Int,
        total: CMoney
    )
    {
        m_items.addListener(this)
        m_payments.addListener(this)

        if (transactionId != data.transactionId)
        {
            m_timeFrame = CTimeFrame(this)
        }
        data = CTransactionData()
        data.name = name
        data.status = status
        data.transactionId = transactionId
        m_payments.setTransactionId(transactionId)
        data.transType = ETransType.TRANS_TYPE_TAKEAWAY
        data.rfidKeyId = -1
        data.status = EClientOrdersType.OPEN
        data.deposit = 0
        data.timeStart = ""
        data.timeEnd = ""
        data.timeCustomer = ""
        data.subTotal = C3Moneys(0, 0, 0)
        data.discount= C3Moneys(0,0,0)
        data.tips = C3Moneys(0,0,0)
        data.total = C3Moneys(0,0,0)
        data.taxTotal = C3Moneys(0,0,0)
        data.customerId = customerId
        data.archived = false
        data.remainsLow = 0.0F
        data.remainsHigh = 0.0F
        data.message = ""
    }

    constructor(transactionId: Int)
    {
        selectTransactionId(transactionId)

        m_items.addListener(this)
        m_payments.addListener(this)
    }

    constructor(transactionId: Int, itemSort: EItemSort, timeFrameIndex: ETimeFrameIndex)
    {
        selectTransactionId(transactionId)

    }

    constructor(source: TransactionData?)
    {
        //m_items.addListener(this)
        m_payments.addListener(this)
        hasOrders = false
        if (source == null)
        {
            data.transactionId = -1
            data = CTransactionData()
            data.name = "-"
            data.status = EClientOrdersType.OPEN
            return
        }
        data.setTransactionData(source)
        m_payments.setTransactionId(data.transactionId)
        m_sizeAtStart = m_items.itemLines()
        m_customer = CCustomer(data.customerId)
    }

    fun addItemListener(listener: TransactionItemListener)
    {
        m_items.addListener(listener)
    }

    fun addReturnMoney()
    {
        m_payments.addReturnMoney()
    }

    fun calculateTotalTransaction(): CMoney
    {
        val previousTotal = data.total.toMoney()
        data.subTotal = m_items.calculateTotalItems()
        data.total = data.subTotal - data.discount + data.tips
        data.taxTotal.calculateTax(data.total,
            Global.getInstance().taxPercentageLow,
            Global.getInstance().taxPercentageHigh)

        val newTotal = data.total.toMoney()
        if (newTotal != previousTotal)
        {
            notifyListeners()
        }
        return newTotal
    }

    fun addPaymentListener(listener: PaymentsListener)
    {
        m_payments.addListener(listener)
    }

    fun getBillRemarks(): String
    {
        return data.message
    }

    fun getCursor(selectedTransactionItem: CItem): Int
    {
        return m_items.getCursor(selectedTransactionItem)
    }

    fun get(position: Int): CItem?
    {
        return m_items.get(position)
    }

    fun getEmployeeName(): String
    {
        val key = data.rfidKeyId
        return CPersonnel().getEmployeeName(key)
    }

    fun cleanCurrentTransaction()
    {
        data.rfidKeyId = global.rfidKeyId
        val service = GrpcServiceFactory.createDailyTransactionService()
        service.updateTotal(data.transactionId)
        closeTransaction(EClientOrdersType.EMPTY)
        cleanTransaction()
    }

    fun cleanTransaction()
    {
        GrpcServiceFactory.createDailyTransactionService().cleanTransaction(transactionId)
        data.cleanTransaction()
    }

    fun closeTimeFrame()
    {
        m_timeFrame.closeTimeFrame()
        m_timeFrame.previous()
    }

    fun closeTransaction(why: EClientOrdersType)
    {
        val service = GrpcServiceFactory.createDailyTransactionService()
        //service.closeTransaction(transactionId, why.value)
        val paymentServices = GrpcServiceFactory.createDailyTransactionPaymentService()

        val totals = paymentServices.getTransactionPaymentTotals(transactionId,
                false)
        val payments = CPaymentTransaction(totals)

        val totalPayments =payments.getTotal(EPaymentStatus.PAY_STATUS_ANY)

        // Update storage
        if ( why == EClientOrdersType.CLOSED && global.CFG.getBoolean("store_enable"))
        {
            val storePart: StorePartService = GrpcServiceFactory.createStorePartService()
            storePart.reduceStorage(transactionId)
            val storeSmurf: StoreSmurfService = GrpcServiceFactory.createStoreSmurfService()
            storeSmurf.calculateTotalSold(-1)
        }
        val trxName: String =data.name
        if (CFG.getBoolean("restaurant_map"))
        {
            val fts = GrpcServiceFactory.createFloorTableService()
            fts.removeBeerDrinker(trxName);
        }
        if ( why == EClientOrdersType.CLOSED && CFG.getBoolean("prepare_display_enable"))
        {
            val prepareService = GrpcServiceFactory.createPrepareTransactionService()
            prepareService.addPrepare(transactionId);
        }
        // Transaction may have another total, much higher.
        if (getTotalTransaction() ==totalPayments || why != EClientOrdersType.CLOSED)
        {
            val service = GrpcServiceFactory.createDailyTransactionService()
            service.closeTransaction(data.transactionId,
                EClientOrdersType.toClientOrdersType(why))
            val servicePayments = GrpcServiceFactory.createDailyTransactionPaymentService()
            servicePayments.finishPayments(data.transactionId)
            val serviceItems = GrpcServiceFactory.createDailyTransactionItemService()
            serviceItems.finishPayments(data.transactionId)
            val serviceChecksum = GrpcServiceFactory.createDailyTransactionChecksumService()
            serviceChecksum.Checksum(data.transactionId)

            if ( why == EClientOrdersType.CLOSED && m_customer != null)
            {
                m_customer!!.addEaten(totalPayments)
            }
        }
        else
        {
            Log.e("CclientOrdersHandler", "close: Totals are not correct! Payment " +
               "Total: ${totalPayments.cents()} " +
               "!= Transaction Total: ${getItemsTotal().cents()}")
        }
        if ( why == EClientOrdersType.CLOSED || why == EClientOrdersType.EMPTY)
        {
            val tableService = GrpcServiceFactory.createFloorTableService()


            tableService.closeTransaction(transactionId);
        }
        // Update memory
        selectTransactionId(data.transactionId)
        m_items.setNegativeQuantityToRemovedItems();
    }

    val size: Int
        get() = m_items.itemLines()

    val itemSize: Int
        get() = m_items.itemLines()

    override var transactionId: Int = 0
        get() = data.transactionId


    fun addOneToCursorPosition(): Boolean
    {
        if (size == 0) return false
        var y = m_global.cursor.position
        var item = get(y) ?: get(--y) ?: return false
        if (item.deletedStatus != EDeletedStatus.DELETE_NOT) return false
        return m_items.addQuantity(CCursor(y)
        , 1)
    }

    fun addListener(listener: TransactionListener)
    {
        if (!m_listeners.contains(listener))
        {
            m_listeners.add(listener)
        }
    }

    fun addPayment(payment: EPaymentMethod, amount: CMoney)
    {
        m_payments.addPayment(payment, amount)
    }

    fun addTransactionItem(selectedMenuItem: CMenuItem, clusterId: Short): Boolean
    {
        Log.d(
            "CTransaction", "add transaction item: " +
               "${selectedMenuItem.menuItemId} cursor ${global.cursor}"
        )
        return m_items.touchItem(selectedMenuItem, clusterId)
    }

    fun cancelPayment(index: Int, paymentStatus: EPaymentStatus)
    {
        m_payments.cancelPayment(index, paymentStatus)
    }

    val empty: Boolean
        get() = m_items.empty

    fun emptyTransaction(reason: String)
    {
        // No printing of kitchen, just go to billing...
        val itemSum = itemSum()
        if (itemSum != 0)
        {
            removeTimeFrame()
        } else
        {
            closeTimeFrame()
        }
        addReturnMoney()
        if (!reason.isEmpty())
        {
            setMessage(reason)
        }
        updateTotal()
        cleanCurrentTransaction()
    }

    fun getCashTotal(): CMoney
    {
        return m_payments.getCashTotal();
    }

    fun getCardTotal(): CMoney
    {
        return m_payments.getCardTotal();
    }

    fun getClient(): String
    {
        // @todo get the customer
        return m_customer?.name ?: ""
    }

    override fun getCustomerId(): Int
    {
        return data.customerId
    }

    override fun getDeviceId(): Short
    {
        return global.deviceId
    }

    fun getDiscount(): CMoney
    {
        return data.discount.toMoney()
    }

    fun getDrinksTotal(): CMoney
    {
        return m_items.getDrinksTotal()
    }

    fun getItemsTotal(): CMoney
    {
        return m_items.getItemsTotal()
    }

    fun getKitchenTotal(): CMoney
    {
        return m_items.getKitchenTotal()
    }

    fun getMinutes(): Int
    {
        return try
        {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val date = inputFormat.parse(data.timeStart)
            val millisecond = date?.time ?: 0
            val now = Date().time
            ((now - millisecond + 30000) / (1000 * 60)).toInt()
        } catch (e: Exception)
        {
            e.printStackTrace()
            10
        }
    }

    fun getPayments() : List<CPayment>
    {
        return m_payments.getPayments()
    }

    fun getPaymentTotal(): CMoney
    {
        return m_payments.getTotal();
    }

    override fun getTimeFrame(): CTimeFrame
    {
        return m_timeFrame
    }

    override fun getTimeFrameIndex(): ETimeFrameIndex
    {
        return m_timeFrame.getTimeFrameIndex()
    }

    fun getTotal(taxType: ETaxType): CMoney
    {
        return when (taxType)
        {
           ETaxType.BTW_TYPE_LOW -> data.total.taxLow
           ETaxType.BTW_TYPE_HIGH -> data.total.taxHigh
           else -> data.total.taxFree
        }
    }

    fun getTotalAlreadyPaid(): CMoney
    {
        return m_payments.getTotalAlreadyPaid()
    }

    override fun getTotalTransaction(): CMoney
    {
        return data.total.toMoney()
    }

    fun hasAnyChanges() : Boolean
    {
        return m_items.hasAnyChanges()
    }

    fun isRechaud(): Boolean
    {
        return data.transType == ETransType.TRANS_TYPE_RECHAUD
    }

    fun itemSize() = m_items.itemLines()

    fun itemLines(): Int
    {
        return m_items.itemLines()
    }

    fun isShop(): Boolean = when (data.transType)
    {
        ETransType.TRANS_TYPE_SHOP -> true
        else -> false
    }

    fun isTakeaway(): Boolean = when (data.transType)
    {
        ETransType.TRANS_TYPE_TAKEAWAY, ETransType.TRANS_TYPE_EAT_INSIDE,
        ETransType.TRANS_TYPE_RECHAUD, ETransType.TRANS_TYPE_UNDEFINED
            -> true

        else -> false
    }

    fun isTelephone(): Boolean = when (data.transType)
    {
        ETransType.TRANS_TYPE_DELIVERY -> true
        else -> false
    }

    fun itemSum(): Int
    {
        return m_items.itemSum()
    }

    // Add this iterator implementation
    override fun iterator(): Iterator<CSortedItem> = m_items.iterator()

    fun minus1()
    {
        if (size == 0) return
        var y = m_global.cursor.position
        var item = get(y) ?: get(--y) ?: return
        if (item.deletedStatus != EDeletedStatus.DELETE_NOT) return
        m_items.addQuantity(CCursor(y), -1)
    }

    fun nextPortion()
    {
        if (size == 0) return
        var y = m_global.cursor.position
        var item = get(y) ?: get(--y) ?: return
        if (item.deletedStatus != EDeletedStatus.DELETE_NOT) return
        m_items.nextPortion(CCursor(y))
    }

    // A single method to notify any external listeners that something has changed.
    private fun notifyListeners()
    {
        for (listener in m_listeners)
        {
            listener.onTransactionChanged(this)
        }
    }

    override fun onPaymentAdded(position: Int, item: CPayment)
    {
        notifyListeners()
    }

    override fun onPaymentRemoved(position: Int)
    {
        notifyListeners();
    }

    override fun onPaymentUpdated(position: Int, item: CPayment)
    {
        notifyListeners();
    }

    override fun onPaymentsCleared()
    {
        notifyListeners();
    }

    override fun onItemAdded(position: Int, item: CItem)
    {
        notifyListeners();
    }

    override fun onItemRemoved(position: Int, newSize: Int)
    {
        notifyListeners();
    }

    override fun onItemUpdated(position: Int, item: CItem)
    {
        notifyListeners();
    }

    override fun onTransactionCleared()
    {
        notifyListeners();
    }

    public fun remove()
    {
        if (size == 0) return
        var y = m_global.cursor.position
        var item = get(y) ?: get(--y) ?: return
        if (item.deletedStatus != EDeletedStatus.DELETE_NOT) return
        m_items.addQuantity(CCursor(y), -item.getQuantity())
    }

    fun removeItemListener(listener: TransactionItemListener)
    {
        m_items.removeListener(listener)
    }

    fun removeListener(listener: TransactionListener)
    {
        m_listeners.remove(listener)
    }

    fun removePaymentListener(listener: PaymentsListener)
    {
        m_payments.removeListener(listener)
    }

    fun removeTimeFrame()
    {
        m_items.undoTimeFrame( m_timeFrame.time_frame_index,
            global.CFG.getShort("pc_number"))
        closeTimeFrame()
    }

    fun setMessage(message: String)
    {
        data.message = message
        val service = GrpcServiceFactory.createDailyTransactionService()
        service.setMessage(global.transactionId, message)
    }

    fun startNextTimeFrame(): CTimeFrame
    {
        Log.i("Ctransaction", "Start next TimeFrame")
        m_timeFrame = CTimeFrame(data.transactionId, this)
        return m_timeFrame
    }

    fun selectTransactionId(transactionId: Int)
    {
        data.transactionId = transactionId
        m_payments.setTransactionId(transactionId)
        val service = GrpcServiceFactory.createDailyTransactionService()
        val inputData: TransactionData? = service.selectTransactionId(transactionId.toInt())
        data.setTransactionData(inputData)
        m_sizeAtStart = m_items.itemLines()
    }

    fun stopTimeFrame()
    {
        m_timeFrame.end()
    }

    fun transactionEmptyAtStartAndAtEnd(): Boolean
    {
        return m_sizeAtStart == 0 && m_items.itemLines() == 0
    }

    fun TAcount() : Int
    {
        return data.name.toIntOrNull() ?:0
    }

    fun updateTotal()
    {
        val service = GrpcServiceFactory.createDailyTransactionService()
        service.updateTotal(data.transactionId)
        selectTransactionId(data.transactionId)
    }

    fun useTakeawayPrices(): Boolean
    {
        return ETransType.useTakeawayPrices(data.transType)
    }

}