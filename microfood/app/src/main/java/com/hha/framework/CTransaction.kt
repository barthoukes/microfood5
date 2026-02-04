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
import com.hha.resources.CTimestamp
import com.hha.resources.Global
import com.hha.service.StorePartService
import com.hha.service.StoreSmurfService
import com.hha.types.C3Moneys
import com.hha.types.CMoney
import com.hha.types.EClientOrdersType
import com.hha.types.ECookingState
import com.hha.types.EDeletedStatus
import com.hha.types.EItemLocation
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
        var tag = "CTransaction"

        suspend fun findOpenTable(name: String): Int
        {
            try {
                val service = GrpcServiceFactory.createDailyTransactionService()
                // Assume getOpenTransactionByName returns a simple response with an ID.
                val transactionId = service.findOpenTable(name)
                return transactionId ?: -1 // Return ID or -1 if not found
            } catch (e: Exception) {
                Log.e(tag, "gRPC error finding open table for $name", e)
                return -1 // Return -1 on error
            }
        }

        suspend fun createAndLoad(
            transactionId: Int,
            sort: EItemSort = EItemSort.SORT_ORDER,
            timeFrame: ETimeFrameIndex = ETimeFrameIndex(ETimeFrameIndex.TIME_FRAME_ALL)
        ): CTransaction {
            Log.i(tag, "createAndLoad")
            // Step 1: Call the private, synchronous constructor to create the object
            val transaction = CTransaction(transactionId, sort, timeFrame)

            // Step 2: Call the suspend function to load the data asynchronously
            transaction.selectTransactionId(transactionId, sort, timeFrame)

            // Step 3: Return the fully initialized object
            return transaction
        }
    }

    var global = Global.getInstance()
    var CFG = global.CFG
    var userCFG = global.userCFG

    private var mCustomer: CCustomer? = null
    private var mFactorHigh: Double = 1.0
    private var mFactorLow: Double = 1.0
    var hasOrders: Boolean = false
    private lateinit var mItems : CTransactionItems
    private val mListeners = mutableListOf<TransactionListener>()

    private var mPartial: CMoney = CMoney(0)
    //private val m_timeFrames = CTimeFrameList()
    private val mPayments = CPaymentList(this)
    private var mSizeAtStart : Int = 0
    private lateinit var mTimeFrame: CTimeFrame
    val customerTime : String
        get() = data.timeCustomer

    val deliverTime : String
        get() = data.timeCustomer

    val itemSize: Int
        get() = mItems.itemLines()

    val itemSort: EItemSort
        get() = mSort

    var rfidKeyId: Short = 0
       get() = data.rfidKeyId

    val size: Int
        get() = mItems.itemLines()

    val empty: Boolean
        get() = mItems.empty

    override var transactionId: Int = 0
        get() = data.transactionId

    val transType : ETransType
        get() = data.transType

    val displayName : String
        get() = data.name

    var mSort = EItemSort.SORT_BILL

    constructor(
        transactionId: Int, name: String, time: String,
        status: EClientOrdersType, customerId: Int,
        total: CMoney
    )
    {
        Log.d(tag, "constructor ${transactionId} ${name} ${status} ${total}")
        mItems.addListener(this)
        mPayments.addListener(this)

        if (transactionId != data.transactionId)
        {
            mTimeFrame = CTimeFrame(this)
        }
        data = CTransactionData()
        data.name = name
        data.status = status
        data.transactionId = transactionId
        mPayments.setTransactionId()
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

    private constructor(
        transactionId: Int, sort: EItemSort, timeFrame: ETimeFrameIndex)
    {
        Log.d(tag, "constructor $transactionId")
        mTimeFrame = CTimeFrame(this)
        mTimeFrame.timeFrameIndex = ETimeFrameIndex(timeFrame.index)
        mSort = sort
        mPayments.addListener(this)
        // Afterwards still need to call suspend fun loadItems
    }

    suspend fun loadItems()
    {
        Log.i(tag, "loadItems")
        mItems = CTransactionItems.createAndLoad(
            this, transactionId, mSort,
            mTimeFrame.timeFrameIndex)
        mItems.addListener(this)
    }

    private constructor(transactionId: Int)
    {
        Log.d(tag, "constructor(int) $transactionId")
        mItems.addListener(this)
        mPayments.addListener(this)
    }

    constructor(source: TransactionData?)
    {
        Log.d(tag, "constructor data")
        //m_items.addListener(this)
        mPayments.addListener(this)
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
        mPayments.setTransactionId()
        mSizeAtStart = mItems.itemLines()
        mCustomer = CCustomer(data.customerId)
    }

    fun addItemListener(listener: TransactionItemListener)
    {
        mItems.addListener(listener)
    }

    fun addListener(listener: TransactionListener)
    {
        if (!mListeners.contains(listener))
        {
            mListeners.add(listener)
        }
    }

    fun addPayment(payment: EPaymentMethod, amount: CMoney)
    {
        mPayments.addPayment(payment, amount)
    }

    suspend fun addOneToCursorPosition(): Boolean
    {
        Log.d(tag, "addOneToCursorPosition")

        if (size == 0) return false
        var y = global.cursor.position
        var item = get(y) ?: get(--y) ?: return false
        if (item.deletedStatus != EDeletedStatus.DELETE_NOT) return false
        return mItems.addQuantity(CCursor(y)
            , 1)
    }

    fun addPaymentListener(listener: PaymentsListener)
    {
        Log.d(tag, "addPaymentListener")
        mPayments.addListener(listener)
    }

    fun addReturnMoney()
    {
        Log.d(tag, "addReturnMoney")
        mPayments.addReturnMoney()
    }

    /** @brief Add money to a waiter
     *  @param waiter [in] Waiter amount
     *  @todo For change after bill: Only the items that were added.
     */
    fun addToEmployee(payStatus: EPaymentStatus)
    {
        Log.d(tag, "addToEmployee")
        val cash = getCashTotal(payStatus)
        val card = getCardTotal(payStatus)
        val returns = getReturnTotal(payStatus)
        val allKitchen = getAllKitchenTotal(payStatus,true)
        val employee = global.personnel.getPerson(global.rfidKeyId)
        employee.add( allKitchen,
            mItems.getItemTotal( EItemLocation.ITEM_KITCHEN2, payStatus, true),
            mItems.getItemTotal( EItemLocation.ITEM_DRINKS, payStatus, true),
            mItems.getItemTotal( EItemLocation.ITEM_BAR, payStatus, true),
            mItems.getItemTotal( EItemLocation.ITEM_SUSHI, payStatus, true),
            mItems.getItemTotal( EItemLocation.ITEM_NONFOOD, payStatus, true),
            mItems.getItemTotal( EItemLocation.ITEM_OTHERS, payStatus, true),
        cash,
        card,
        returns)
        employee.update(false);
    }

    suspend fun addTransactionItem(selectedMenuItem: CMenuItem, clusterId: Short): Boolean
    {
        Log.d(
            tag, "addTransactionItem " +
               "${selectedMenuItem.menuItemId} cursor ${global.cursor.position}"
        )
        return mItems.touchItem(selectedMenuItem, clusterId)
    }

    fun calculateTotalTransaction(): CMoney
    {
        Log.d(tag, "calculateTotalTransaction")
        val previousTotal = data.total.toMoney()
        data.subTotal = mItems.calculateTotalItems()
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

    fun cancelNewPayments()
    {
        Log.d(tag, "cancelNewPayments")
        mPayments.cancelNewPayments()
    }

    fun changeDeliverTime(timer: CTimestamp)
    {
        Log.d(tag, "changeDeliverTime")
        val timeFrameIndex = mTimeFrame.getValidTimeFrame()
        mTimeFrame.changeDeliverTime(
            transactionId, global.deviceId, timeFrameIndex, timer)
    }

    suspend fun cleanCurrentTransaction()
    {
        Log.d(tag, "cleanCurrentTransaction")
        data.rfidKeyId = global.rfidKeyId
        val service = GrpcServiceFactory.createDailyTransactionService()
        service.updateTotal(data.transactionId)
        closeTransaction(EClientOrdersType.EMPTY)
        cleanTransaction()
    }

    fun cleanTransaction()
    {
        Log.d(tag, "cleanTransaction")
        GrpcServiceFactory.createDailyTransactionService().cleanTransaction(transactionId)
        data.cleanTransaction()
    }

    fun closeTimeFrame()
    {
        Log.d(tag, "closeTimeFrame")
        mTimeFrame.closeTimeFrame()
        mTimeFrame.previous()
    }

    suspend fun closeTransaction(why: EClientOrdersType)
    {
        Log.d(tag, "closeTransaction")
        val service = GrpcServiceFactory.createDailyTransactionService()
        //service.closeTransaction(transactionId, why.value)
        val paymentServices = GrpcServiceFactory.createDailyTransactionPaymentService()

        val totals = paymentServices.getTransactionPaymentTotals(transactionId,
            false)
        val payments = CPaymentTransaction(totals)

        val totalPayments =payments.getTotal(EPaymentStatus.PAY_STATUS_ANY)

        // Update storage
        if ( why == EClientOrdersType.CLOSED && CFG.getBoolean("store_enable"))
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
                why.toClientOrdersType())
            val servicePayments = GrpcServiceFactory.createDailyTransactionPaymentService()
            servicePayments.finishPayments(data.transactionId)
            val serviceItems = GrpcServiceFactory.createDailyTransactionItemService()
            serviceItems.finishPayments(data.transactionId)
            val serviceChecksum = GrpcServiceFactory.createDailyTransactionChecksumService()
            serviceChecksum.Checksum(data.transactionId)

            if ( why == EClientOrdersType.CLOSED && mCustomer != null)
            {
                mCustomer!!.addEaten(totalPayments)
            }
        }
        else
        {
            Log.e("CclientOrdersHandler", "close: Totals are not correct! Payment " +
               "Total: ${totalPayments.cents()} " +
               "!= Transaction Total: ${getItemTotal().cents()}")
        }
        if ( why == EClientOrdersType.CLOSED || why == EClientOrdersType.EMPTY)
        {
            val tableService = GrpcServiceFactory.createFloorTableService()


            tableService.closeTransaction(transactionId);
        }
        // Update memory
        selectTransactionId(data.transactionId, mSort,
            ETimeFrameIndex(ETimeFrameIndex.TIME_FRAME_ALL))
        mItems.setNegativeQuantityToRemovedItems();
    }

    fun cancelPayments(status: EPaymentStatus)
    {
        Log.d(tag, "cancelPayments")
        mPayments.cancelPayments(status)
    }

    fun cancelPayments(index: Int, status: EPaymentStatus)
    {
        Log.d(tag, "cancelPayments")
        mPayments.cancelPayments(index, status)
    }

    fun endTimeFrame(
        transactionId: Int,
        pcNumber: Int,
        newTime: CTimestamp,
        timeChanged: Boolean,
        newState: ECookingState
    )
    {
        Log.d(tag, "endTimeFrame")
        val strTime = newTime.getDateTime()
        mTimeFrame.endTimeFrame(
            transactionId,
            CFG.getShort("pc_number"),
            strTime,
            timeChanged,
            newState
        )
    }

    fun get(position: Int): CItem?
    {
        return mItems.get(position)
    }

    fun getBillPrinterQuantity(): Int
    {
        Log.d(tag, "getBillPrinterQuantity")
        var quantity = userCFG.getValue("user_print_bills")
        if ( quantity == 0 && CFG.getBoolean("bill_always_print_delivery"))
        {
            val str = getBillRemarks()
            if (!str.isEmpty())
            {
                quantity =1
            }
        }
        return quantity
    }

    fun getBillRemarks(): String
    {
        return data.message
    }

    fun getCursor(selectedTransactionItem: CItem): Int
    {
        return mItems.getCursor(selectedTransactionItem)
    }

    fun getEmployeeName(): String
    {
        Log.d(tag, "getEmployeeName")
        val key = data.rfidKeyId
        return CPersonnel().getEmployeeName(key)
    }

    fun cancelPayment(index: Int, paymentStatus: EPaymentStatus)
    {
        mPayments.cancelPayments(index, paymentStatus)
    }

    suspend fun emptyTransaction(reason: String)
    {
        Log.d(tag, "emptyTransaction")
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
        return mPayments.getCashTotal();
    }

    fun getCashTotal(payStatus: EPaymentStatus) : CMoney
    {
        return mPayments.getCashTotal(payStatus)
    }

    fun getCardTotal(): CMoney
    {
        return mPayments.getCardTotal();
    }

    fun getCardTotal(payStatus: EPaymentStatus) : CMoney
    {
        return mPayments.getCardTotal(payStatus)
    }

    fun getClient(): String
    {
        // @todo get the customer
        return mCustomer?.name ?: ""
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
        return mItems.getDrinksTotal()
    }

    fun getAllKitchenTotal(
        payStatus: EPaymentStatus, isWithStatiegeld: Boolean): CMoney
    {
        Log.d(tag, "getAllKitchenTotal")
        return mItems.getAllKitchenTotal(payStatus, isWithStatiegeld);
    }

    fun getEmployeeId(): Short = data.rfidKeyId

    fun getItemTotal(location: EItemLocation,
        payStatus: EPaymentStatus,
        withStatiegeld: Boolean) =
            mItems.getItemTotal(location, payStatus, withStatiegeld)

    fun getItemTotal(): CMoney
    {
        return mItems.getItemsTotal()
    }

    fun getKitchenTotal(): CMoney
    {
        return mItems.getKitchenTotal()
    }

    fun getHighestPaymentIndex(): Int
    {
        return mPayments.getHighestPaymentIndex()
    }

    fun getMinutes(): Int
    {
        Log.d(tag, "getMinutes")
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

    fun getName(): String = data.name

    fun getPartialTotal(partialIndex: Short): CMoney
    {
        return mPayments.getPartialTotal(partialIndex)
    }

    fun getPaidTotal(payStatus: EPaymentStatus): CMoney =
        mPayments.getPaidTotal(payStatus)

    fun getPayments() : List<CPayment> = mPayments.getPayments()

    fun getPaymentTotal(): CMoney = mPayments.getTotal();

    fun getReturnTotal(paymentStatus: EPaymentStatus):
       CMoney = mPayments.getReturnTotal(paymentStatus)

    fun getStatus(): EClientOrdersType = data.status

    override fun getTimeFrame(): CTimeFrame
    {
        return mTimeFrame
    }

    fun getTransactionPaymentTotals(
        includeCancelledPayments: Boolean): CPaymentTransaction
    {
        return mPayments.getTransactionPaymentTotals(includeCancelledPayments)
    }

    override fun getTimeFrameIndex(): ETimeFrameIndex
    {
        return mTimeFrame.timeFrameIndex
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

    /*----------------------------------------------------------------------------*/
    fun getTransactionPayments(): CPaymentTransaction
    {
        return mPayments.getTransactionPaymentTotals(false)
    }

    fun getTotalAlreadyPaid(): CMoney
    {
        return mPayments.getTotalAlreadyPaid()
    }

    fun getTotalFromIndex(line: Int): CMoney = mItems.getTotalFromIndex(line)

    override fun getTotalTransaction(): CMoney
    {
        return data.total.toMoney()
    }

    fun hasAnyChanges() : Boolean = mItems.hasAnyChanges()

    fun isRechaud(): Boolean
    {
        return data.transType == ETransType.TRANS_TYPE_RECHAUD
    }

    fun itemSize() = mItems.itemLines()

    fun itemLines(): Int = mItems.itemLines()

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

    fun isValid(): Boolean
    {
        return data.transactionId > 0
    }

    fun itemSum(): Int
    {
        return mItems.itemSum()
    }

    // Add this iterator implementation
    override fun iterator(): Iterator<CSortedItem> = mItems.iterator()

    suspend fun minus1()
    {
        Log.d(tag, "minus1")
        if (size == 0) return
        var y = global.cursor.position
        var item = get(y) ?: get(--y) ?: return
        if (item.deletedStatus != EDeletedStatus.DELETE_NOT) return
        mItems.addQuantity(CCursor(y), -1)
        mItems.checkCursor(global.cursor)
    }

    suspend fun nextPortion()
    {
        Log.d(tag, "nextPortion")
        if (size == 0) return
        var y = global.cursor.position
        var item = get(y) ?: get(--y) ?: return
        if (item.deletedStatus != EDeletedStatus.DELETE_NOT) return
        mItems.nextPortion(CCursor(y))
    }

    // A single method to notify any external listeners that something has changed.
    private fun notifyListeners()
    {
        Log.d(tag, "notifyListeners")
        for (listener in mListeners)
        {
            listener.onTransactionChanged(this)
        }
    }

    fun numberKitchenItems(): Int
    {
        return mItems.numberKitchenItems(
            data.transactionId,
            mTimeFrame.timeFrameIndex)
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

    fun open()
    {
        setStatus(EClientOrdersType.OPEN)
    }

    override fun onTransactionCleared()
    {
        notifyListeners();
    }

    override fun onBeginLoading()
    {
        TODO("Not yet implemented")
    }

    override fun onEndLoading()
    {
        TODO("Not yet implemented")
    }

    suspend fun remove()
    {
        Log.d(tag, "remove")
        if (size == 0) return
        var y = global.cursor.position
        var item = get(y) ?: get(--y) ?: return
        if (item.deletedStatus != EDeletedStatus.DELETE_NOT) return
        mItems.addQuantity(CCursor(y), -item.getQuantity())
        mItems.checkCursor(global.cursor)
    }

    fun removeItemListener(listener: TransactionItemListener)
    {
        mItems.removeListener(listener)
    }

    fun removeListener(listener: TransactionListener)
    {
        mListeners.remove(listener)
    }

    fun removePaymentListener(listener: PaymentsListener)
    {
        mPayments.removeListener(listener)
    }

    fun removeTimeFrame()
    {
        mItems.undoTimeFrame( mTimeFrame.timeFrameIndex,
            global.CFG.getShort("pc_number"))
        closeTimeFrame()
    }

    fun setMessage(message: String)
    {
        Log.d(tag, "setMessage")
        data.message = message
        val service = GrpcServiceFactory.createDailyTransactionService()
        service.setMessage(data.transactionId, message)
    }

    suspend fun startNewTimeFrame()
    {
        Log.d(tag, "startTimeFrame")
        val waiter = global.rfidKeyId
        if (data.transactionId > 1E6)
        {
            mTimeFrame.getLatestTimeFrameIndex(data.transactionId)
        }
        mTimeFrame.next()
        val deviceId = global.pcNumber
        mTimeFrame.startTimeFrame(deviceId, data.transactionId, waiter)
    }

    suspend fun selectTransactionId(transactionId: Int, sort: EItemSort, timeFrame: ETimeFrameIndex)
    {
        Log.i(tag, "selectTransactionId $transactionId")
        try
        {
            val service = GrpcServiceFactory.createDailyTransactionService()
            val transactionData: TransactionData? = service.selectTransactionId(transactionId)
            // @todo load the items from the transaction
            val allItems = true

            if (transactionData != null)
            {
                data.setTransactionData(transactionData)
                mSort = sort
                val tfi = when (allItems)
                {
                    true -> ETimeFrameIndex(ETimeFrameIndex.TIME_FRAME_ALL)
                    else -> timeFrame
                }
                loadItems()
                mTimeFrame = CTimeFrame(this)
                mPayments.setTransactionId()
                mSizeAtStart = mItems.itemLines()
                mCustomer = CCustomer(data.customerId)
            } else
            {
                Log.e(tag, "Failed to select transaction with ID: $transactionId")
                // Handle error case
            }
        } catch (e: Exception) {
            Log.e(tag, "Error selecting transaction $transactionId: ${e.message}")
        }
    }

    fun setDiscount(discount: CMoney)
    {
        Log.d(tag, "setDiscount")
        val high = data.subTotal.taxHigh.cents()
        val low = data.subTotal.taxLow.cents()

        if ( high+low > 0)
        {
            var h =0
            var l =0
            var z =0
            var factorLow =1.0
            var factorHigh =1.0
            if ( discount.cents() < high && high > 0)
            {
                // Discount less than high tax amount
                h =discount.cents()
                factorHigh -=(h.toDouble()/high.toDouble())
            }
            else
            {
                h =high
                if (high!=0) factorHigh = 0.0 else factorHigh = 1.0
                val rest =discount.cents() - high
                if (rest < low && low > 0)
                {
                    l =rest
                    factorLow -=(rest.toDouble()/low.toDouble())
                }
                else
                {
                    // Rest more than amount or amount zero
                    l =low
                    if (low!=0) factorLow = 0.0 else factorLow = 1.0
                }
            }
            setDiscount( h, l, 0, factorHigh, factorLow)
        }
        mPayments.invalidate()
        updateTotal()
    }

    fun setDiscount(discountHigh: Int, discountLow: Int, discountTaxFree: Int,
                    factorHigh: Double, factorLow: Double)
    {
        Log.d(tag, "setDiscount")
        val service = GrpcServiceFactory.createDailyTransactionService()
        val iter = service.setDiscount(
            data.transactionId, discountHigh, discountLow, discountTaxFree,
            factorHigh, factorLow)
        data.discount.taxLow = CMoney(discountLow)
        data.discount.taxHigh = CMoney(discountHigh)
        data.discount.taxFree = CMoney(discountTaxFree)
        mFactorHigh = factorHigh
        mFactorLow = factorLow
    }

    fun setTips(tips: CMoney)
    {
        Log.d(tag, "setTips")
        data.tips.taxLow = CMoney(0)
        data.tips.taxHigh = CMoney(0)
        data.tips.taxFree = CMoney(0)
        if (data.total.taxHigh.empty())
        {
            data.tips.taxLow = tips
        }
        else
        {
            data.tips.taxHigh = tips
        }
    }

    fun setPartialAmount(amount: CMoney)
    {
        mPartial =amount
    }

    fun setEmployeeId(rfidKeyId: Short)
    {
        data.rfidKeyId = rfidKeyId
        val service = GrpcServiceFactory.createDailyTransactionService()
        service.setRfidKeyId(
            data.transactionId, rfidKeyId)
    }

    fun setPayingState()
    {
        setStatus(EClientOrdersType.PAYING)
    }

    fun setStatus(status : EClientOrdersType)
    {
        Log.d(tag, "setStatus")
        data.status = status
        val pType = status.toClientOrdersType()
        val service = GrpcServiceFactory.createDailyTransactionService()
        service.setStatus(data.transactionId, pType)
    }

    fun stopTimeFrame()
    {
        mTimeFrame.end()
    }

    fun transactionEmptyAtStartAndAtEnd(): Boolean
    {
        return mSizeAtStart == 0 && mItems.itemLines() == 0
    }

    fun TAcount() : Int
    {
        return data.name.toIntOrNull() ?:0
    }

    // @tpdp make suspend
    fun updateTotal()
    {
        Log.d(tag, "updateTotal")
        val service = GrpcServiceFactory.createDailyTransactionService()
        service.updateTotal(data.transactionId)
        // Local update prices
        data.subTotal = mItems.calculateTotalItems()
        data.total = data.subTotal - data.discount + data.tips
        data.taxTotal.calculateTax(data.total,
            Global.getInstance().taxPercentageLow,
            Global.getInstance().taxPercentageHigh)
    }

    fun useTakeawayPrices(): Boolean
    {
        return ETransType.useTakeawayPrices(data.transType)
    }

}