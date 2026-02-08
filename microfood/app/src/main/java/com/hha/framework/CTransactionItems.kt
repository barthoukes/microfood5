package com.hha.framework

import EViewMode
import android.util.Log
import com.hha.callback.ItemOperations
import com.hha.callback.TransactionItemListener
import com.hha.common.DeletedStatus
import com.hha.common.ItemVisible
import com.hha.common.Payed
import com.hha.grpc.GrpcServiceFactory
import com.hha.grpc.LoadingState
import com.hha.resources.Global
import com.hha.types.C3Moneys
import com.hha.types.CMoney
import com.hha.types.EAccess
import com.hha.types.EDeletedStatus
import com.hha.types.EEnterState
import com.hha.types.EItemLocation
import com.hha.types.EItemLocation.Companion.location2Locations
import com.hha.types.EItemSort
import com.hha.types.ENameType
import com.hha.types.EOrderLevel
import com.hha.types.EPayed
import com.hha.types.EPaymentStatus
import com.hha.types.ETaal
import com.hha.types.ETimeFrameIndex
import com.hha.types.ETreeRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

@Suppress("VariableNaming")
class CTransactionItems : Iterable<CSortedItem>
{
    var m_state: EEnterState = EEnterState.ENTER_ITEM_STATE
    val global = Global.getInstance()
    val CFG = global.CFG
    var mItems = CSortedItemList()
    var change2spice = false
    var m_clusterId: Short = -1
    var m_twinItemId = -1
    var mAllowChanges = true
    var mChanged = false
    val mClusterIsRunning = false
    val mViewMode: EViewMode = EViewMode.VIEW_MODE_TRANSACTION ///< Are we in split or preview mode?
    private val mListeners = mutableListOf<TransactionItemListener>()
    val mPortionRound = CFG.getValue("portion_round")
    private val mPortionDefinePrice = CFG.getBoolean("portion_define_price")
    private val mPortionHalfPrice = CFG.getValue("portion_half_price")
    lateinit var mItemOperations: ItemOperations

    // Background task management
    private val loadingState = LoadingState()
    private val scope = CoroutineScope(Dispatchers.Main)
    private var currentTask: Job? = null

    companion object
    {
        var tag = "CTransactionItems"

        suspend fun createAndLoad(
            itemOperations : ItemOperations,
            transactionId: Int,
            sort: EItemSort = EItemSort.SORT_ORDER,
            timeFrame: ETimeFrameIndex = ETimeFrameIndex(ETimeFrameIndex.TIME_FRAME_ALL)
        ): CTransactionItems {
            // Step 1: Call the private, synchronous constructor to create the object
            val transactionItems = CTransactionItems(itemOperations)
            // Step 2: Call the suspend function to load the data asynchronously
            transactionItems.selectTransactionId(transactionId, sort, timeFrame)

            // Step 3: Return the fully initialized object
            return transactionItems
        }
    }


    private constructor(itemOperations : ItemOperations)
    {
        mItemOperations = itemOperations
        m_state = EEnterState.ENTER_ITEM_STATE
    }

    /*----------------------------------------------------------------------------*/
    /** @brief Add item at end in the list or for descriptive at some location.
     *  @param cursor [in] Location of the cursor in the list vertical.
     *  @param item_id [in] Id for the item to select.
     *  @param  prices [in] New prices for the item.
     *  @param cluster_id [in] What clusters to use.
     *  @param twin_item_id [in] Which twin item to use.
     *  @post  Cursor at the end.
     *  @return true on succes.
     */
    suspend fun addItem(
        cursor: CCursor, menuItem: CMenuItem, prices: CPriceAndHalfPrice,
        clusterId: Short, twinItem: CMenuItem?
    ): Boolean
    {
        var retVal = false
        Log.i("CTransactionItems", "addItem ${menuItem.localName}")
        if (insertItem(
                cursor.next(), menuItem, 1, prices,
                clusterId, twinItem
            )
        )
        {
            global.cursor.set(mItems.size)
            retVal = true
        }
        return retVal
    }

    /*============================================================================*/
///  @brief     Plus button pressed, to increase the quantity.
///  @post      Quantity increased.
    /*============================================================================*/
    private suspend fun addOne(cursor: CCursor): Boolean
    {
        if (mAllowChanges == false || mViewMode != EViewMode.VIEW_MODE_TRANSACTION)
        {
            return false;
        }
        if (empty)
        {
            return false;
        }
        val y = getValidCursorPosition(cursor);
        if (y.position < 0)
        {
            return false;
        }
        val retVal = addQuantity(y, 1)
        return retVal
    }

    /*============================================================================*/
    ///  @brief     Plus button pressed, to increase the quantity.
    ///  @post      Quantity increased.
    ///  @return    Cursor
    /*============================================================================*/
    suspend fun addOneToCursorPosition(cursor: CCursor)
    {
        if (addOne(cursor))
        {
            val size = getNrItemsAndSubItems()
            if (cursor.position > size)
            {
                cursor.position = size
            }
//            if ( m_transactionItemControl.hasClustersEnabled() && !CFG( "entry_cluster_dialog"))
//            {
//                CitemPtr item =(*m_clientOrdersHandler)[getValidCursorPosition(m_transactionItemControl.getCursor())];
//                m_transactionItemControl.showCluster(item->id);
//            }
        }
//        else
//        {
//            m_transactionItemControl.errorSound();
//        }
    }

    suspend fun addQuantity(cursor: CCursor, quantity: Int): Boolean
    {
        val tfi = mItemOperations.getTimeFrameIndex()
        return addQuantity(cursor, tfi, quantity);
    }

    suspend fun addQuantity(cursor: CCursor, timeFrameId: ETimeFrameIndex, quantity: Int): Boolean
    {
        // Increase in items.
        var mutableQuantity = quantity
        val item: CItem = mItems.getItem(cursor) ?: return false

        var why = EDeletedStatus.DELETE_NOT
        var oldTimeFrame = ETimeFrameIndex(ETimeFrameIndex.TIME_FRAME_UNDEFINED)

        if (mutableQuantity < 0)
        {
            why = when
            {
                timeFrameId == item.timeFrameId -> EDeletedStatus.DELETE_REMOVE_IMMEDIATE
                else -> EDeletedStatus.DELETE_REMOVE_AFTER_PRINTING
            }
            oldTimeFrame = item.timeFrameId
        } else if (item.level == EOrderLevel.LEVEL_SEPARATOR)
        {
            return false
        }
        val maxq = 9999
        if (item.getQuantity() + mutableQuantity > maxq)
        {
            mutableQuantity = maxq - item.getQuantity()
        }
        if (item.getQuantity() + mutableQuantity < -maxq)
        {
            mutableQuantity = -maxq - item.getQuantity()
        }
        if (item.getQuantity() + mutableQuantity == 0)
        {
            val level = CFG.getValue("entry_negative_quantity")
            if (level == 0 || (level == 1 && global.access == EAccess.ACCESS_EMPLOYEE_KEY)
                || (item.level != EOrderLevel.LEVEL_ITEM
                   && item.level != EOrderLevel.LEVEL_COMBINE_ALL
                   && item.level != EOrderLevel.LEVEL_FREE
                   && item.level != EOrderLevel.LEVEL_MINUTES_PRICE
                   && item.level != EOrderLevel.LEVEL_PERSON)
            )
            {
                return deleteItem(cursor, timeFrameId, why);
            }
        }
        mChanged = true
        item.addQuantity(quantity)
        if (item.getQuantity() > quantity)
            mListeners.forEach { it.onItemUpdated(cursor.position, item) }
        else mListeners.forEach { it.onItemAdded(cursor.position, item) }

        // Increase in database.
        var unitPrice = item.getUnitPrice()
        var statiegeld = item.getStatiegeldPerPiece();

        val itemsDb = GrpcServiceFactory.createDailyTransactionItemService()
        val whys = EDeletedStatus.toDeletedStatus(why)
        var level = EOrderLevel.toOrderLevel(item.level)
        itemsDb.createItem(
            item.menuItemId,
            mItemOperations.transactionId, item.sequence,
            item.subSequence, item.subSubSequence,
            quantity, level,
            item.group, item.page, item.parts,
            unitPrice.cents(), item.originalAmount.cents(),
            item.originalHalfAmount.cents(), item.tax,
            item.locations, timeFrameId.toInt(), item.deviceId,
            item.clusterId, Payed.PAID_NO, statiegeld,
            whys, oldTimeFrame.toInt()
        );
        if (item.twinItemId > 0)
        {
            val quantity = item.getQuantity()
            itemsDb.setTwinQuantity(
                mItemOperations.transactionId,
                item.sequence, item.subSequence,
                item.subSubSequence,
                quantity, item.parts
            );
        }

        // Remove extras and spices for normal items, except when we remove a spice.
        if ((item.level == EOrderLevel.LEVEL_ITEM
               || item.level == EOrderLevel.LEVEL_FREE)
            && CFG.getValue("transaction_item_tree") == 0
            && CFG.getValue("entry_extra_main_quantity") > 0
        )
        {
            var idx = CCursor(0)
            // Add all the Spices and Extras also the same amount, remove them if 0!
            for (subItem: CItem in mItems.flatMap { it.items })
            {
                if (subItem.sequence == item.sequence &&
                    (subItem.level == EOrderLevel.LEVEL_EXTRA
                       || subItem.level == EOrderLevel.LEVEL_SPICES)
                )
                {
                    if (why != EDeletedStatus.DELETE_REMOVE_IMMEDIATE)
                    {
                        oldTimeFrame = subItem.timeFrameId
                    }
                    subItem.addQuantity(quantity);
                    if (subItem.getQuantity() == 0)
                    {
                        deleteItem(idx, timeFrameId, why);
                        continue;
                    }
                    statiegeld = subItem.getStatiegeldPerPiece()
                    unitPrice = subItem.getUnitPrice()
                    level = EOrderLevel.toOrderLevel(subItem.level)
                    itemsDb.createItem(
                        subItem.menuItemId,
                        mItemOperations.transactionId,
                        subItem.sequence, subItem.subSequence,
                        subItem.subSubSequence,
                        quantity, level, subItem.group, subItem.page,
                        subItem.parts, unitPrice.cents(),
                        subItem.originalAmount.cents(),
                        subItem.originalHalfAmount.cents(), subItem.tax,
                        subItem.locations, timeFrameId.toInt(),
                        subItem.deviceId, subItem.clusterId,
                        Payed.PAID_NO, statiegeld,
                        whys, oldTimeFrame.toInt()
                    );
                }
                idx.position++;
            }
        }
        return true;
    }

    fun addListener(listener: TransactionItemListener)
    {
        if (!mListeners.contains(listener))
        {
            mListeners.add(listener)
        }
    }

    // Add this iterator implementation
    //override fun iterator(): Iterator<CSortedItem> = m_items.iterator()
    fun getSortedItem(index: Int): CSortedItem
    {
        return mItems.getSortedItem(index)
    }

    fun calculateTotalItems() : C3Moneys
    {
        var total = C3Moneys()
        for (item in mItems)
        {
            total = total + item.calculateTotalItems()
        }
        return total
    }

    val empty: Boolean
        get() = mItems.empty

    fun get(position: Int): CItem?
    {
        var pos = position
        for (item in mItems)
        {
            if (pos < item.size)
            {
                return item[pos]
            }
            pos -= item.size
        }
        return null
    }

    fun cancelCurrentTask()
    {
        currentTask?.cancel()
        currentTask = null
    }

    fun checkCursor(cursor : CCursor)
    {
        if (cursor.position > mItems.size)
        {
            cursor.set(mItems.size)
        }
    }

    fun getAllKitchenTotal(
        payStatus: EPaymentStatus, isWithStatiegeld: Boolean): CMoney
    {
        val transactionId = mItemOperations.transactionId

        val service = GrpcServiceFactory.createDailyTransactionItemService()
        val status = payStatus.toPaymentStatus()


        val total = service.getAllKitchenTotal(
            transactionId, status, isWithStatiegeld)

        return CMoney(total?.cents ?: 0)
    }

    fun getItemTotal(location: EItemLocation,
                     payStatus: EPaymentStatus,
                     withStatiegeld: Boolean) =
        mItems.getItemTotal(location, payStatus, withStatiegeld)

    fun getTotalFromIndex(line: Int): CMoney = mItems.getTotalFromIndex(line)

    fun hasAnyChanges() : Boolean
    {
        return mChanged
    }

    fun itemLines() = mItems.itemLines

    fun itemSum() = mItems.itemSum

    // Add this iterator implementation
    override fun iterator(): Iterator<CSortedItem> = mItems.iterator()

    private suspend fun insertItem(
        cursor: CCursor, menuItem: CMenuItem, quantity: Int, prices: CPriceAndHalfPrice,
        clusterId: Short, twinItem: CMenuItem?
    ): Boolean
    {
        Log.i("CTransactionItems", "insertItem item_id=${menuItem.menuItemId}")

        var taxClusterId = 0
        var page = 0
        var clusters = 0
        if (mAllowChanges == false ||
            mViewMode != EViewMode.VIEW_MODE_TRANSACTION)
        {
            return false;
        }

        val menuCardId = global.menuCardId
        if (menuItem.isVisible == ItemVisible.ITEM_INVISIBLE && !mClusterIsRunning)
        {
            // only when visible
            Log.i(
                "CTransactionItems",
                "insertItem item not visible ${menuItem.menuItemId}"
            )
            return false;
        }
        if (twinItem != null)
        {
            if (twinItem.isVisible == ItemVisible.ITEM_INVISIBLE)
            {
                // only when visible
                Log.i(
                    "CTransactionItems",
                    "insertItem twinItem ${twinItem.menuItemId} not visible"
                )
                return false;
            }
        }
        var level = menuItem.level;
        if (m_state == EEnterState.ENTER_SUBITEM_STATE)
        {
            prices.clear();
            if (level != EOrderLevel.LEVEL_SPICES)
            {
                level = EOrderLevel.LEVEL_EXTRA;
            }
        }
        // Add the food.
        val original = prices.fullPrice
        val original_half = prices.halfPrice
        val taxPercentage = menuItem.getTaxPercentage(false)
        val loc = menuItem.locations
        if (global.clusterNoItems &&
            (level == EOrderLevel.LEVEL_ITEM ||
               level == EOrderLevel.LEVEL_FREE ||
               level == EOrderLevel.LEVEL_PERSON ||
               level == EOrderLevel.LEVEL_MINUTES_PRICE)
        )
        {
            level = EOrderLevel.LEVEL_EXTRA
        }
        if (level == EOrderLevel.LEVEL_EXTRA || level == EOrderLevel.LEVEL_SPICES)
        {
            assert(false)
//            val cursorItem: CSortedItem? = m_items.mainItem(cursor)
//            val mainItem: CItem? = cursorItem?.let { it.mainItem() }
//            if (mainItem != null) {
//                taxClusterId = menuItem.taxClusterId
//                page = mainItem.page
//                clusters = menuItem->clusters
//                loc = mainItem.locations
//                val new_cursor = insertExtra(
//                    item_id, twin_item_id,
//                    main_cursor, quantity, 2,
//                    prices.full_price.Long(), prices.half_price.Long(),
//                    taxPercentage, loc, cluster_id,
//                    PAID_NO, product->statiegeld.Long());
//                m_transactionItemControl.moveCursorTo(new_cursor);
//                m_transactionItemControl.invalidateDisplayAndSetOrderChanged();
            return true;
        } else
        {
            level = EOrderLevel.LEVEL_ITEM
        }

        taxClusterId = menuItem.taxClusterId
        page = menuItem.page
        clusters = menuItem.clusters
        if (level == EOrderLevel.LEVEL_COMBINE_ALL || prices.fullPrice.cents() == 1)
        {
            Log.i("CTransactionItems", "insertItem ask combination price")
            assert(false)
//            prices = m_transactionItemControl.askCombinationPrice();
//            if (!prices.valid)
//            {
//                LOG_FUNC_EXIT << " ask combination price cancelled" << Endl;
//                return false;
//            }
//            if ( level == EOrderLevel.LEVEL_COMBINE_ALL)
//            {
//                m_state =EEnterState.ENTER_SUBITEM_STATE;
//            }
        }
        if (level == EOrderLevel.LEVEL_ASK_CLUSTER)
        {
            assert(false)
            //            val mainCursor =m_clientOrdersHandler->mainItem( cursor);
//            if ( main_cursor >= 0 && main_cursor < (int) rows())
//            {
//                CitemPtr main_item =(*m_clientOrdersHandler)[main_cursor];
//                item_id = main_item->menuItemId;
//            }
//            else
//            {
//                item_id = -1;
//            }
        } else
        {
            Log.i("CtransactionItems", "insertItem insertItem")
            val tfi = mItemOperations.getTimeFrameIndex()
            insertItem(
                menuItem, cursor, quantity, level, taxClusterId, page, 2,
                prices.fullPrice, original, original_half,
                taxPercentage, loc, tfi, global.pcNumber,
                clusterId, EPayed.PAID_NO, menuItem.statiegeld
            );

            if (twinItem != null)
            {
                assert(false)
                // m_clientOrdersHandler ->
                //    insertTwinItem(twin_item_id, cursor, quantity, 2, taxPercentage);
            }
            global.cursor.set(mItems.itemLines)
        }
        if (m_state == EEnterState.ENTER_ITEM_STATE)
        {
            //assert(false)
            //m_transactionItemControl.askOrDisplayClusters(clusters, item_id);
        }
        return true;
    }

    fun numberKitchenItems(transactionId: Int, timeFrameId: ETimeFrameIndex): Int
    {
        val service = GrpcServiceFactory.createDailyTransactionItemService()
        val number = service.numberKitchenItems(transactionId, timeFrameId.index)
        return number
    }

    suspend fun touchItem(menuItem: CMenuItem, clusterId: Short): Boolean
    {
        Log.i("CTransactionItems", "touchItem")
//        /** @brief Ask the twin item */
//        int twin_item_id = 0;
//        int menuCardId =clientOrdersHandler->getMenuCardId();
//        CproductPtr product =CmenuCards::Instance()->getProductFromProductId(menuCardId, -1, item_id);
//        if (!product.get())
//        {
//            return false;
//        }
//
//        if (product->is_twin)
//        {
//            bool isTakeaway(clientOrdersHandler->isTakeawayPrice());
//            CchooseItemDialog dialog( menuCardId, isTakeaway, _CHOOSE_TWIN, SKIP_INVISIBLE_FALSE);
//            dialog.onExecute(this);
//            if (m_parent)
//                {
//                        m_parent->invalidate();
//                }
//            if (dialog.m_validItemSelected)
//            {
//                twin_item_id = dialog.m_itemId;
//            }
//            else
//            {
//                return false;
//            }
//        }
        val twinItem: CMenuItem? = null
        val retVal = touchItem(menuItem, clusterId, twinItem);
        return retVal
    }

    /*============================================================================*/
    /// @brief Touch or type any item.
    /// @param item_id What item is pressed
    /// @param cluster_id What cluster is pressed
    /// @param twin_item_id The twin item to use.
    /// @post Item handled. Screen to be updated.
    /// @return true when item added.
    // bool CtransactionItemModel::touchItem(int item_id, short cluster_id, int twin_item_id)
    /*============================================================================*/
    suspend fun touchItem(menuItem: CMenuItem, clusterId: Short, twinItem: CMenuItem?): Boolean
    {
        var retVal = false
        var change2spice = false
        val prices = menuItem.getPriceAndHalfPrice(0, false)
        if (!prices.isValid)
        {
            Log.i(
                "framework", "CtransactionItemDialog::touchItem cannot find cluster" +
                   " $global.cluster_id"
            )
            return false
        }
        val menuCardId = global.menuCardId
        var level = menuItem.level
        if (level == EOrderLevel.LEVEL_NOTHING)
        {
            Log.i("framework", "LEVEL_NOTHING will not be added")
            return false
        }
        if (m_state == EEnterState.ENTER_SUBITEM_STATE)
        {
            change2spice = true
        }
        do
        {
            if (change2spice)
            {
                prices.clear()
                if (level != EOrderLevel.LEVEL_SPICES)
                {
                    level = EOrderLevel.LEVEL_EXTRA
                }
            }
            // Check if we touch an extra/descriptive item.
            when (level)
            {
                EOrderLevel.LEVEL_EXTRA, EOrderLevel.LEVEL_SPICES,
                EOrderLevel.LEVEL_SUB_EXTRA, EOrderLevel.LEVEL_SUB_SPICES
                    ->
                    return touchExtraOrSpice(menuItem, prices, clusterId, twinItem)

                else ->
                {
                }
            }

            // Check if the item is at the end.
            if (global.cursor.position >= mItems.size)
            {
                // If so: Call another function.
                retVal = touchEnd(menuItem, 1, prices, clusterId, twinItem)
                return retVal
            }
            assert(false)
//            val cursorItem = m_clientOrdersHandler[m_transactionItemControl.cursor]
//            change2spice = cursorItem.level == LEVEL_COMBINE_ALL
        } while (change2spice)

        // If same as below cursor, then add one.

//        val cursor_item = m_clientOrdersHandler[m_transactionItemControl.cursor]
//        if (cursor_item.menuItemId == item_id && cursor_item.twinItemId == twin_item_id && CFG("entry_merge_similar_items")) {
//            addOneToCursorPosition()
//            return true
//        // Just insert this one at the cursor.
//        retVal = touchEnd(item_id, 1, prices, cluster_id, twin_item_id)
        return retVal
    }

    suspend fun nextPortion(cursor: CCursor): Boolean
    {
        val timeFrame = mItemOperations.getTimeFrame()
        val tfi = timeFrame.timeFrameIndex
        return nextPortion(cursor, tfi)
    }

    suspend fun nextPortion(cursor: CCursor, timeFrameId: ETimeFrameIndex): Boolean
    {
        Log.i("CTransactionItems", "portion cursor=${cursor.position}")

        val item: CItem = mItems.getItem(cursor)!!
        val nextPortion = when
        {
            (item.parts == 2) -> 1
            else -> 2
        }
        var price = item.originalAmount
        val halfPrice = item.originalHalfAmount
        val sequence = item.sequence
        val subSequence = item.subSequence
        val subSubSequence = item.subSubSequence
        val statiegeld = item.getStatiegeldPerPiece()
        var quantity = item.getQuantity()
        val level = EOrderLevel.toOrderLevel(item.level)
        mChanged = true
        val why = when
        {
            item.timeFrameId != timeFrameId -> DeletedStatus.DELETE_PORTION_AFTER_PRINTING
            else -> DeletedStatus.DELETE_PORTION_IMMEDIATE
        }
        var unitPrice = item.getUnitPrice()

        val itemsDb = GrpcServiceFactory.createDailyTransactionItemService()
        mListeners.forEach { it.onBeginLoading() }

        withContext(Dispatchers.IO) {
            itemsDb.createItem(
                item.menuItemId, mItemOperations.transactionId,
                sequence, subSequence, subSubSequence, -quantity,
                level, item.group, item.page, item.parts,
                unitPrice.cents(), item.originalAmount.cents(),
                item.originalHalfAmount.cents(), item.tax,
                item.locations, timeFrameId.toInt(),
                item.deviceId, item.clusterId,
                Payed.PAID_NO, statiegeld,
                why, item.timeFrameId.toInt()
            )
            item.parts = nextPortion
            item.updateName()
            if (item.parts == 1)
            {
                var price2 = (price.cents() * mPortionHalfPrice) /
                   100 + mPortionRound
                price = price - CMoney(price.cents() % mPortionRound)

                if (mPortionDefinePrice && !halfPrice.empty())
                {
                    price = halfPrice;
                }
                item.setUnitPrice(price);
            } else
            {
                item.setUnitPrice(CMoney((item.parts * price.cents())) / 2)
            }
            mListeners.forEach { it.onItemUpdated(cursor.position, item) }
            unitPrice = item.getUnitPrice()
            quantity = item.getQuantity()

            itemsDb.createItem(
                item.menuItemId, mItemOperations.transactionId, sequence,
                subSequence, subSubSequence, quantity,
                level, item.group, item.page, item.parts,
                unitPrice.cents(), item.originalAmount.cents(),
                item.originalHalfAmount.cents(),
                item.tax, item.locations, timeFrameId.toInt(),
                item.deviceId, item.clusterId, Payed.PAID_NO, statiegeld
            )
        }
        mListeners.forEach { it.onEndLoading() }

        return true
    }

    fun removeListener(listener: TransactionItemListener)
    {
        mListeners.remove(listener)
    }

    fun setNegativeQuantityToRemovedItems()
    {
        val service = GrpcServiceFactory.createDailyTransactionItemService()
        service.setNegativeQuantityToRemovedItems(mItemOperations.transactionId)
    }

    fun touchExtraOrSpice(
        menuItem: CMenuItem, prices: CPriceAndHalfPrice,
        clusterId: Short, twinItem: CMenuItem?
    ): Boolean
    {
        assert(false)
        return false
    }
//    if ( m_clientOrdersHandler->empty())
//    {
//        m_transactionItemControl.errorSound();
//        return false;
//    }
//    int menuCardId =  m_clientOrdersHandler->getMenuCardId();
//    CproductPtr product =CmenuCards::Instance()->getProductFromProductId(menuCardId, -1, item_id);
//    if ( product.get() ==nullptr)
//    {
//        Log.error( "CproductToTransaction::touchExtraOrSpice wrong item pressed =%d", item_id);
//        return false;
//    }
//    if (!m_clusterIsRunning && product->visible_state == ITEM_INVISIBLE)
//    {
//        Log.write("CproductToTransaction::touchExtraOrSpice  product invisible");
//        return false;
//    }
//
//    int main_cursor =m_clientOrdersHandler->mainItem(m_transactionItemControl.getCursor());
//    if ( main_cursor < 0 && main_cursor >= (int) rows())
//    {
//        m_transactionItemControl.errorSound();
//        return false;
//    }
//    // 4 if so: Is it listed.
//    int main_quantity =1;
//    CitemPtr main_item =(*m_clientOrdersHandler)[main_cursor];
//    if ( !CFG( "transaction_item_tree") && CFG("entry_extra_main_quantity"))
//    {
//        main_quantity =main_item->getQuantity();
//    }
//    if ( main_quantity == 0)
//    {
//        m_transactionItemControl.errorSound();
//        return false;
//    }
//    double tax = CtransactionItemModel::getTaxPercentage(item_id);
//
//    int newCursor =m_clientOrdersHandler->insertExtra( item_id, twin_item_id, main_cursor,
//    main_quantity, main_item->parts, prices.full_price.Long(),
//    prices.half_price.Long(), tax, main_item->locations, cluster_id,
//    PAID_NO, product->statiegeld.Long());
//    m_transactionItemControl.moveCursorTo( newCursor);
//    if ( checkNextCluster( main_cursor, cluster_id))
//    {
//        newCursor =m_clientOrdersHandler->size();
//        m_transactionItemControl.moveCursorTo( newCursor);
//    }
//    m_transactionItemControl.invalidateDisplayAndSetOrderChanged();
//    m_transactionItemControl.customerDisplay(m_transactionItemControl.getCursor());
//        return true;
//    }/

    fun getCursor(selectedTransactionItem: CItem): Int
    {
        return mItems.getCursor(selectedTransactionItem)
    }

    /*----------------------------------------------------------------------------*/
    // @brief Touch item at the end of the list.
    // @post Screen to be updated.
    suspend fun touchEnd(
        menuItem: CMenuItem, quantity: Int,
        prices: CPriceAndHalfPrice, clusterId: Short, twinItem: CMenuItem?
    ): Boolean
    {
        Log.i("framework", "CtransactionItemModel::touchEnd")
        var retVal = false
        if (menuItem.isOutOfStock() || (twinItem != null && twinItem.isOutOfStock()))
        {
            return false
        }
        retVal = true
        global.cursor.set(mItems.size)

        // No items yet, then add the food?
        if (global.cursor.position == 0)
        {
            retVal = insertItem(global.cursor, menuItem, quantity, prices, clusterId, twinItem);
            invalidateDisplayAndSetOrderChanged();
            Log.i("CTransactionItems", "First item added");
            return retVal
        }
        val cursor = CCursor(global.cursor.position-1)
        val previousOrder = mItems.getItem(cursor)
        if (previousOrder == null)
        {
            Log.e(tag, "Prevous order not found!!")
            return false
        }
        //val cursor = mItems.*m_clientOrdersHandler)[cursor - 1]
        val unitPrice = previousOrder.getUnitPrice()

        if ( previousOrder.menuItemId == menuItem.menuItemId
        && (twinItem == null || previousOrder.twinItemId == twinItem.menuItemId)
        && unitPrice == prices.fullPrice
            && previousOrder.deletedStatus == EDeletedStatus.DELETE_NOT
        && previousOrder.parts == 2
        && CFG.getBoolean("entry_merge_similar_items"))
        {
            // Same as previous item.
            addQuantity(cursor, 1)
            Log.d(tag, " Same item added")
            return true
        }
        // Just add the item at the end.
        retVal = addItem(global.cursor, menuItem, prices, clusterId, twinItem)
        Log.i("CTransactionItems", "Item added")
        //assert(false)
        return retVal
    }

    /*----------------------------------------------------------------------------*/
    /** Insert extra or spice */
    fun insertExtra(
        menuItem: CMenuItem, twinItem: CMenuItem?, mainCursor: Int, quantity: Int,
        parts: Int, price: CMoney, halfPrice: CMoney, tax: Double, locations: Int,
        timeFrameId: ETimeFrameIndex, deviceId: Short, clusterId: Short,
        isPaid: EPayed, statiegeld: CMoney
    )
    {
        assert(false)
//        int idx;
//        EorderLevel level;
//        CitemPtr mainItem =m_itemList[ mainCursor];
//        short subSequence=0, subSubSequence=0;
//        CproductPtr product =CmenuCards::Instance()->getProductFromProductId( itemId);
//
//        switch (mainItem->level)
//        {
//            case LEVEL_EXTRA:
//            case LEVEL_SPICES:
//            case LEVEL_SUB_EXTRA:
//            case LEVEL_SUB_SPICES:
//            // Behavior for 2 levels deep
//            level=(product->level == LEVEL_SPICES)?LEVEL_SUB_SPICES:LEVEL_SUB_EXTRA;
//            subSequence = mainItem->subSequence;
//            subSubSequence = getNewSubSubSequence(mainItem->sequence, mainItem->subSequence);
//            break;
//            default:
//            // Behavior for 1 level deep
//            level=(product->level == LEVEL_SPICES) ? LEVEL_SPICES:LEVEL_EXTRA;
//            subSequence = getNewSubSequence( mainItem->sequence);
//            break;
//        }
//
//        // Find same item and add 1.
//        for ( idx=0; idx<static_cast<int>( m_itemList.size()); idx++)
//        {
//            CitemPtr subItem =m_itemList[idx];
//            int unitPrice = subItem->getUnitPrice().Long();
//            // Same as new item?
//            if (  subItem->sequence ==mainItem->sequence
//            && (subItem->subSequence ==mainItem->subSequence || mainItem->subSequence==0)
//            && subItem->menuItemId ==itemId
//            && subItem->twinItemId ==twinItemId
//            && subItem->level ==level
//            && subItem->originalAmount ==price
//            && unitPrice ==price)
//            {
//                    subItem->addQuantity(1);
//                m_pItemsDb->createItem( itemId, m_transactionId, mainItem->sequence,
//                subItem->subSequence, subItem->subSubSequence,
//                1, subItem->level,
//                product->tax_cluster_id, product->page_id, parts, price,
//                price, half_price, tax, locations, timeFrameId, deviceId,
//                clusterId, is_paid, statiegeld);
//                if (twinItemId >0)
//                {
//                    int subQuantity = subItem->getQuantity();
//                    m_pItemsDb->setTwinQuantity( m_transactionId, mainItem->sequence,
//                    subItem->subSequence, subItem->subSubSequence,
//                    subQuantity, subItem->parts);
//                }
//                customerDisplay( idx);
//                return idx; // New cursor.
//            }
//        }
//        // Not in spices list for this item, so add it.
//        std::string local =product->Name( LANG_DUTCH, (int)parts, NAME_PRINTER, false);
//        std::string chinese =product->Name( LANG_SIMPLIFIED, (int)parts, NAME_PRINTER, false);
//        bool cut =product->paper_cut_per_item;
//        std::string shortTime = Ctimestamp().getShortTime();
//        CitemPtr xtra(std::make_shared<Citem>(
//            itemId, product->alias, mainItem->sequence, subSequence, subSubSequence,
//        local, chinese, parts, quantity, Cmoney(price), Cmoney(price), Cmoney(half_price),
//        statiegeld, product->locations,
//        product->tax_cluster_id, product->page_id, timeFrameId, tax, level, -1,
//        deviceId, clusterId, cut, is_paid, shortTime, DELETE_NOT));
//
//        xtra->id = 0;
//        m_pItemsDb->createItem( itemId, m_transactionId, mainItem->sequence, subSequence, subSubSequence, quantity, level,
//        product->tax_cluster_id, product->page_id, parts, price, price, half_price,
//        tax, locations, timeFrameId, deviceId, clusterId, is_paid, statiegeld);
//
//        if (twinItemId >0)
//        {
//            CproductPtr product2 =CmenuCards::Instance()->getProductFromProductId(twinItemId);
//            if (product2.get() != NULL)
//                {
//                        xtra->twinItemId = twinItemId;
//                    xtra->updateName();
//                    m_pItemsDb->createItem( twinItemId, m_transactionId,
//                    mainItem->sequence, subSequence, subSubSequence, quantity, LEVEL_TWIN_ITEM,
//                    product2->tax_cluster_id, product2->page_id, parts, 0, 0, 0,
//                    tax, locations, timeFrameId, deviceId, clusterId,
//                    is_paid, statiegeld);
//                }
//        }
//
//
//        // Find location to insert.
//        switch (level)
//        {
//            case LEVEL_SUB_EXTRA:
//            case LEVEL_SUB_ITEM:
//            case LEVEL_SUB_SPICES:
//            for ( idx=m_itemList.size()-1; idx>=mainCursor; idx--)
//            {
//                if ( m_itemList[idx]->sequence ==mainItem->sequence
//                && m_itemList[idx]->subSequence == mainItem->subSequence)
//                {
//                    // Found the location!
//                    m_itemList.insert( m_itemList.begin()+idx+1, xtra);
//                    customerDisplay( idx+1);
//                    return mainCursor; //idx+1;
//                }
//            }
//            break;
//
//            default:
//            for ( idx=m_itemList.size()-1; idx>=mainCursor; idx--)
//            {
//                if ( m_itemList[idx]->sequence ==mainItem->sequence)
//                {
//                    // Found the location!
//                    m_itemList.insert( m_itemList.begin()+idx+1, xtra);
//                    customerDisplay( idx+1);
//                    if ( idx+1 >=(int)m_itemList.size()-1)
//                    {
//                        return m_itemList.size();
//                    }
//                    return mainCursor; //idx+1;
//                }
//            }
//        }
//        // Totally not found, probably an error already.
//        m_itemList.push_back( xtra);
//        return m_itemList.size();
    }

    /*----------------------------------------------------------------------------*/
    /** @brief Remove one item.
     *  @param cursor Where on screen?
     *  @param timeFrameId What time?
     *  @param why Reason to delete.
     */
    suspend fun deleteItem(cursor: CCursor, timeFrameId: ETimeFrameIndex, why: EDeletedStatus): Boolean
    {
        var size = mItems.size
        if (cursor.position < 0 || cursor.position >= size)
        {
            Log.e("CTI", "DeleteItem  Cursor overflow!!")
            return false
        }
        var item: CItem = mItems.getItem(cursor) ?: return false
        var statiegeld = item.getStatiegeldPerPiece()
        var unitPrice = item.getUnitPrice()
        var quantity = item.getQuantity()
        deleteTwinItem(item, why)
        val itemsDb = GrpcServiceFactory.createDailyTransactionItemService()
        val whys = EDeletedStatus.toDeletedStatus(why)
        mChanged = true
        when (item.level)
        {
            // Remove item with sub-items and sub-sub-items
            EOrderLevel.LEVEL_INFO, EOrderLevel.LEVEL_SYSTEM, EOrderLevel.LEVEL_ITEMGROUP,
            EOrderLevel.LEVEL_NOTHING, EOrderLevel.LEVEL_SEPARATOR, EOrderLevel.LEVEL_ZERO,
            EOrderLevel.LEVEL_ITEM, EOrderLevel.LEVEL_FREE, EOrderLevel.LEVEL_TWIN_ITEM,
            EOrderLevel.LEVEL_MINUTES_PRICE, EOrderLevel.LEVEL_PERSON, EOrderLevel.LEVEL_CHARITY,
            EOrderLevel.LEVEL_COMBINE_ALL
                ->
            {
                // Remove in database.
                itemsDb.createItem(
                    item.menuItemId,
                    mItemOperations.transactionId,
                    item.sequence,
                    item.subSequence,
                    item.subSubSequence,
                    -quantity,
                    EOrderLevel.toOrderLevel(item.level),
                    item.group,
                    item.page,
                    item.parts,
                    unitPrice.cents(),
                    item.originalAmount.cents(),
                    item.originalHalfAmount.cents(),
                    item.tax,
                    item.locations,
                    timeFrameId.toInt(),
                    item.deviceId,
                    item.clusterId,
                    Payed.PAID_NO,
                    statiegeld,
                    whys,
                    item.timeFrameId.toInt()
                )
                mItems.eraseItem(cursor)
                size = size - 1
                mListeners.forEach { it.onItemRemoved(cursor.position, mItems.itemLines) }

                while (cursor.position < size)
                {
                    item = mItems.getItem(cursor) ?: break
                    unitPrice = item.getUnitPrice();
                    quantity = item.getQuantity();
                    statiegeld = item.getStatiegeldPerPiece();
                    if (item.level == EOrderLevel.LEVEL_SPICES
                        || item.level == EOrderLevel.LEVEL_EXTRA ||
                        item.level == EOrderLevel.LEVEL_SUB_EXTRA ||
                        item.level == EOrderLevel.LEVEL_SUB_ITEM ||
                        item.level == EOrderLevel.LEVEL_SUB_SPICES
                    )
                    {
                        val level = EOrderLevel.toOrderLevel(item.level)
                        deleteTwinItem(item, why)
                        itemsDb.createItem(
                            item.menuItemId, mItemOperations.transactionId,
                            item.sequence, item.subSequence, item.subSubSequence, -quantity,
                            level, item.group, item.page, item.parts,
                            unitPrice.cents(), item.originalAmount.cents(),
                            item.originalHalfAmount.cents(), item.tax,
                            item.locations, timeFrameId.toInt(),
                            item.deviceId, item.clusterId, Payed.PAID_NO,
                            statiegeld, whys, item.timeFrameId.toInt()
                        )
                        mItems.eraseItem(cursor)
                        size = size - 1
                        mListeners.forEach { it.onItemRemoved(cursor.position, mItems.itemLines) }
                    }
                }
            }
            // Remove item with sub-items
            EOrderLevel.LEVEL_EXTRA, EOrderLevel.LEVEL_SPICES ->
            {
                val level = EOrderLevel.toOrderLevel(item.level)
                itemsDb.createItem(
                    item.menuItemId, mItemOperations.transactionId,
                    item.sequence,
                    item.subSequence, item.subSubSequence, -quantity,
                    level, item.group, item.page, item.parts,
                    unitPrice.cents(), item.originalAmount.cents(),
                    item.originalHalfAmount.cents(),
                    item.tax, item.locations,
                    timeFrameId.toInt(), item.deviceId,
                    item.clusterId, Payed.PAID_NO, statiegeld, whys,
                    item.timeFrameId.toInt()
                )
                mItems.eraseItem(cursor)
                size = size - 1
                mListeners.forEach { it.onItemRemoved(cursor.position, mItems.itemLines) }
                while (cursor.position < size)
                {
                    item = mItems.getItem(cursor) ?: break
                    statiegeld = item.getStatiegeldPerPiece()
                    unitPrice = item.getUnitPrice()
                    quantity = item.getQuantity()

                    deleteTwinItem(item, why)
                    if (item.level == EOrderLevel.LEVEL_SUB_EXTRA
                        || item.level == EOrderLevel.LEVEL_SUB_ITEM
                        || item.level == EOrderLevel.LEVEL_SUB_SPICES
                    )
                    {
                        val level = EOrderLevel.toOrderLevel(item.level)
                        itemsDb.createItem(
                            item.menuItemId, mItemOperations.transactionId,
                            item.sequence, item.subSequence, item.subSubSequence, -quantity,
                            level, item.group, item.page, item.parts,
                            unitPrice.cents(), item.originalAmount.cents(),
                            item.originalHalfAmount.cents(), item.tax,
                            item.locations, timeFrameId.toInt(), item.deviceId,
                            item.clusterId, Payed.PAID_NO, statiegeld,
                            whys, item.timeFrameId.toInt()
                        )
                        mItems.eraseItem(cursor)
                        size = size - 1
                        mListeners.forEach { it.onItemRemoved(cursor.position, mItems.itemLines) }
                    } else break
                }
            }
            // Remove item
            EOrderLevel.LEVEL_SUB_EXTRA, EOrderLevel.LEVEL_SUB_ITEM,
            EOrderLevel.LEVEL_SUB_SPICES, EOrderLevel.LEVEL_OUTOFSTOCK
                ->
            {
                val level = EOrderLevel.toOrderLevel(item.level)
                itemsDb.createItem(
                    item.menuItemId, mItemOperations.transactionId,
                    item.sequence,
                    item.subSequence, item.subSubSequence, -quantity,
                    level, item.group, item.page, item.parts,
                    unitPrice.cents(), item.originalAmount.cents(),
                    item.originalHalfAmount.cents(), item.tax,
                    item.locations, timeFrameId.toInt(),
                    item.deviceId, item.clusterId,
                    Payed.PAID_NO, statiegeld,
                    whys, item.timeFrameId.toInt()
                )
                mItems.eraseItem(cursor)
                size = size - 1
                mListeners.forEach { it.onItemRemoved(cursor.position, mItems.itemLines) }
            }

            EOrderLevel.LEVEL_ASK_CLUSTER ->
            {
            }
        }
        // Adjust cursor if needed.
        return true
    }

    /*----------------------------------------------------------------------------*/
    /** @brief Delete the twin item if any.
     */
    private suspend fun deleteTwinItem(item: CItem, why: EDeletedStatus)
    {
        if (item.twinItemId > 0)
        {
            val itemsDb = GrpcServiceFactory.createDailyTransactionItemService()
            withContext(Dispatchers.IO)
            {
                val whys = EDeletedStatus.toDeletedStatus(why)
                itemsDb.deleteTwinItem(
                    item.twinItemId.toLong(),
                    mItemOperations.transactionId,
                    item.sequence, item.subSequence,
                    item.subSubSequence, whys
                )
            }
            mChanged = true
        }
    }

    fun getDrinksTotal(): CMoney
    {
        val drinks = location2Locations(EItemLocation.ITEM_BAR) +
           location2Locations(EItemLocation.ITEM_DRINKS)
        var m = CMoney(0)
        for (item in mItems)
        {
            if ((item.getLocations() and drinks) != 0)
            {
                m = m + item.getTotal()
            }
        }
        return m
    }

    fun getItemsTotal(): CMoney
    {
        var m = CMoney(0)
        for (item in mItems)
        {
            m = m + item.getTotal()
        }
        return m
    }

    fun getKitchenTotal(): CMoney
    {
        val drinks = location2Locations(EItemLocation.ITEM_BAR) +
            location2Locations(EItemLocation.ITEM_DRINKS)
        var m = CMoney(0)
        for (item in mItems)
        {
            if ((item.getLocations() and drinks) == 0)
            {
                m = m + item.getTotal()
            }
        }
        return m
    }

    private fun getNewSequence(): Int
    {
        val seq = (mItems.maxOfOrNull { it.getSequence() } ?: 0) + 1
        Log.i(tag, "getNewSequence =$seq")
        return seq
    }

    fun getNrItemsAndSubItems(): Int
    {
        var m = 0
        for (item in mItems)
        {
            m = m + item.size
        }
        return m
    }

    fun getValidCursorPosition(cursor: CCursor): CCursor
    {
        var y = cursor.position
        val sz = getNrItemsAndSubItems()
        if (y >= sz)
        {
            y = sz - 1
        }
        return CCursor(y)
    }

    suspend fun insertItem(
        menuItem: CMenuItem, cursr: CCursor,
        quantity: Int, level: EOrderLevel, group: Int, page: Int, parts: Int,
        unitPrice: CMoney, originalPrice: CMoney, originalHalfPrice: CMoney,
        taxPercentage: Double, locations: Int,
        timeFrameId: ETimeFrameIndex, deviceId: Short, clusterId: Short,
        isPaid: EPayed, statiegeld: CMoney
    ): Boolean
    {
        var cursor = cursr
        var sequence = FIRST_SEQUENCE_ID
        val subSequence = 1 // NORMAL_ITEM_SUB_SEQUENCE
        val subSubSequence = 0 // NORMAL_ITEM_SUB_SEQUENCE;
        mChanged = true

        val itemsDb = GrpcServiceFactory.createDailyTransactionItemService()
        if (cursor.position >= 0 && cursor.position < mItems.itemLines)
        {
            val sortedItem: CSortedItem? = mItems.mainItem(cursor)
            if (sortedItem != null)
            {
                val i: CItem = sortedItem.mainItem()
                if (i.menuItemId == menuItem.menuItemId
                    && i.level == level
                    && i.group == group
                    && i.page == page
                    && i.parts == parts
                    && i.getUnitPrice() == unitPrice
                    && i.originalAmount == originalPrice
                    && i.originalHalfAmount == originalHalfPrice
                    && i.tax == taxPercentage
                    && i.clusterId == clusterId
                    && i.deviceId == deviceId
                    && i.locations == locations
                )
                {
                    return addQuantity(cursor, timeFrameId, 1)
                }
                val curItem : CItem? = mItems.getItem(cursor)
                sequence = curItem?.sequence ?: 1
                withContext(Dispatchers.IO)
                {
                    // Not same as below cursor.
                    itemsDb.insertSequence(mItemOperations.transactionId, sequence)
                    Log.i(tag, "insertItem should not happen anymore!!")
                }
            } else
            {
                cursor.set(mItems.size)
                sequence = getNewSequence()
            }
        } else
        {
            cursor.set(mItems.size);
            sequence = getNewSequence()
        }

        val orderLevel = EOrderLevel.toOrderLevel(level)
        val timeFrame = timeFrameId.toInt()
        val paid = EPayed.toPayed(isPaid)
        withContext(Dispatchers.IO)
        {
            itemsDb.createItem(
                menuItem.menuItemId, mItemOperations.transactionId,
                sequence, subSequence, subSubSequence, quantity, orderLevel, group, page,
                parts, unitPrice.cents(), originalPrice.cents(),
                originalHalfPrice.cents(), taxPercentage,
                locations, timeFrame,
                deviceId, clusterId, paid, statiegeld.cents()
            )
        }
        val i = CItem(
            menuItem.menuItemId,
            0,
            menuItem.alias,
            sequence,
            subSequence,
            subSubSequence,
            menuItem.Name(
                ETaal.LANG_DUTCH, parts,
                ENameType.NAME_SCREEN, false
            ),
            menuItem.Name(
                ETaal.LANG_SIMPLIFIED, parts,
                ENameType.NAME_SCREEN, false
            ),
            originalPrice,
            originalHalfPrice,
            locations,
            EDeletedStatus.DELETE_NOT,
            parts,
            group,
            page,
            timeFrameId,
            level,
            taxPercentage,
            menuItem.menuItemId.toInt(),
            deviceId,
            "", "",
            clusterId,
            ETreeRow.TREE_ITEM,
            menuItem.paperCutPerItem,
            isPaid,
            "",
            unitPrice,
            quantity,
            statiegeld
        )
        i.setQuantityPrice(quantity, unitPrice, statiegeld);
        if (cursor.position >= mItems.itemLines)
        {
            val cs = CSortedItem(i)
            mItems.add(cs)
            mListeners.forEach { it.onItemAdded(cursor.position+1, i) }
        } else
        {
            // Increase sequence ID's.
            for (item in mItems)
            {
                if (item.getSequence() >= sequence)
                {
                    item.increaseSequence()
                }
            }
            mItems.add(cursor, i)
            mListeners.forEach { it.onItemAdded(cursor.position+1, i) }
        }
        return true
    }

    private fun invalidateDisplayAndSetOrderChanged()
    {
        mChanged = true;
    }

    suspend fun selectTransactionId(
        transactionId: Int, sort: EItemSort, timeFrame: ETimeFrameIndex)
    {
        mItems.selectTransactionId(transactionId, sort, timeFrame)
    }

    fun undoTimeFrame(timeFrameId: ETimeFrameIndex, deviceId: Short)
    {
        mItems.undoTimeFrame(mItemOperations.transactionId, timeFrameId, deviceId)
    }
}
