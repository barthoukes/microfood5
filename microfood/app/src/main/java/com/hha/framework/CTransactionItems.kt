package com.hha.framework

import android.util.Log
import com.hha.common.ItemVisible
import com.hha.common.OrderLevel
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import com.hha.types.CMoney
import com.hha.types.EDeletedStatus
import com.hha.types.EEnterState
import com.hha.types.EItemLocation
import com.hha.types.ENameType
import com.hha.types.EOrderLevel
import com.hha.types.EPayed
import com.hha.types.ETaal
import com.hha.types.ETimeFrameIndex
import com.hha.types.ETreeRow
import kotlin.math.max

@Suppress("VariableNaming")
class CTransactionItems : Iterable<CSortedItem> {
    var m_state: EEnterState = EEnterState.ENTER_ITEM_STATE
    val global = Global.getInstance()
    var m_items = CSortedItemList()
    var change2spice = false
    var m_clusterId: Short = -1
    var m_twinItemId = -1
    var m_changed = false
    var m_newChanged = false
    val m_clusterIsRunning = false

    constructor() {
        m_state = EEnterState.ENTER_ITEM_STATE
    }

    // Add this iterator implementation
    //override fun iterator(): Iterator<CSortedItem> = m_items.iterator()
    operator fun get(index: Int): CSortedItem {
        return m_items[index]
    }

    fun getCItem(position: Int): CItem? {
        var pos = position
        for (item in m_items) {
            if (pos<item.size) {
                return item[pos]
            }
            pos -= item.size
        }
        return null
    }

    val empty: Boolean
        get() = m_items.empty

    val size: Int
        get() {
            var sz = 0
            for (item in m_items) {
                sz += item.size
            }
            return sz
        }

    // Add this iterator implementation
    override fun iterator(): Iterator<CSortedItem> = m_items.iterator()

    //bool CtransactionItemDialog::touchItem(int item_id, short cluster_id)
    fun touchItem(menuItem: CMenuItem, clusterId: Short): Boolean {
        Log.i("CTransactionItems", "touchItem")
//        IclientOrdersHandlerPtr clientOrdersHandler(m_transactionItemModel.getClientOrdersHandler());
//
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
//        LOG_FUNC_EXIT << Endl;
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
    fun touchItem(menuItem: CMenuItem, clusterId: Short, twinItem: CMenuItem?): Boolean {
        var retVal = false
        var change2spice = false
        val prices = menuItem.getPriceAndHalfPrice(0, false)
        if (!prices.isValid) {
            Log.i(
                "framework", "CtransactionItemDialog::touchItem cannot find cluster" +
                        " $global.cluster_id"
            )
            return false
        }
        val menuCardId = global.menuCardId
        var level = menuItem.level
        if (level == EOrderLevel.LEVEL_NOTHING) {
            Log.i("framework", "LEVEL_NOTHING will not be added")
            return false
        }
        if (m_state == EEnterState.ENTER_SUBITEM_STATE) {
            change2spice = true
        }
        do {
            if (change2spice) {
                prices.clear()
                if (level != EOrderLevel.LEVEL_SPICES) {
                    level = EOrderLevel.LEVEL_EXTRA
                }
            }
            // Check if we touch an extra/descriptive item.
            when (level) {
                EOrderLevel.LEVEL_EXTRA, EOrderLevel.LEVEL_SPICES,
                EOrderLevel.LEVEL_SUB_EXTRA, EOrderLevel.LEVEL_SUB_SPICES
                    ->
                    return touchExtraOrSpice(menuItem, prices, clusterId, twinItem)

                else -> {}
            }

            // Check if the item is at the end.
            if (global.cursor >= m_items.size) {
                // If so: Call another function.
                retVal = touchEnd(menuItem, 1, prices, clusterId, twinItem)
                return retVal
            }
            assert(false)
//            val cursorItem = m_clientOrdersHandler[m_transactionItemControl.cursor]
//            change2spice = cursorItem.level == LEVEL_COMBINE_ALL
        } while (change2spice)
        assert(false)
        //
//        // If same as below cursor, then add one.
//        val cursor_item = m_clientOrdersHandler[m_transactionItemControl.cursor]
//        if (cursor_item.menuItemId == item_id && cursor_item.twinItemId == twin_item_id && CFG("entry_merge_similar_items")) {
//            addOneToCursorPosition()
//            return true
//        // Just insert this one at the cursor.
//        retVal = touchEnd(item_id, 1, prices, cluster_id, twin_item_id)
        return retVal
    }

    fun touchExtraOrSpice(
        menuItem: CMenuItem, prices: CPriceAndHalfPrice,
        clusterId: Short, twinItem: CMenuItem?
    ): Boolean {
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


    private fun insertItem(
        cursor: Int, menuItem: CMenuItem, quantity: Int, prices: CPriceAndHalfPrice,
        clusterId: Short, twinItem: CMenuItem?
    ): Boolean {
        Log.i("CTransactionItems", "insertItem item_id=${menuItem.menuItemId}")

        var taxClusterId = 0
        var page = 0
        var clusters = 0
        //CsqlTaxRateIterator itax

        val menuCardId = global.menuCardId
        if (menuItem.isVisible == ItemVisible.ITEM_INVISIBLE && !m_clusterIsRunning) {
            // only when visible
            Log.i(
                "CTransactionItems",
                "insertItem item not visible ${menuItem.menuItemId}"
            )
            return false;
        }
        if (twinItem != null) {
            if (twinItem.isVisible == ItemVisible.ITEM_INVISIBLE) {
                // only when visible
                Log.i(
                    "CTransactionItems",
                    "insertItem twinItem ${twinItem.menuItemId} not visible"
                )
                return false;
            }
        }
        var level = menuItem.level;
        if (m_state == EEnterState.ENTER_SUBITEM_STATE) {
            prices.clear();
            if (level != EOrderLevel.LEVEL_SPICES) {
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
        ) {
            level = EOrderLevel.LEVEL_EXTRA
        }
        if (level == EOrderLevel.LEVEL_EXTRA || level == EOrderLevel.LEVEL_SPICES) {
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
        } else {
            level = EOrderLevel.LEVEL_ITEM
        }

        taxClusterId = menuItem.taxClusterId
        page = menuItem.page
        clusters = menuItem.clusters
        if (level == EOrderLevel.LEVEL_COMBINE_ALL || prices.fullPrice.cents() == 1) {
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
        if (level == EOrderLevel.LEVEL_ASK_CLUSTER) {
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
        } else {
            Log.i("CtransactionItems", "insertItem insertItem")
            val tfi = global.timeFrame!!.time_frame_index
            insertItem(
                menuItem, cursor, quantity, level, taxClusterId, page, 2,
                prices.fullPrice, original, original_half,
                taxPercentage, loc, tfi,global.pcNumber,
                clusterId, EPayed.PAID_NO, menuItem.statiegeld
            );

            if (twinItem != null) {
                assert(false)
                // m_clientOrdersHandler ->
                //    insertTwinItem(twin_item_id, cursor, quantity, 2, taxPercentage);
            }
            global.cursor = m_items.size
        }
        if (m_state == EEnterState.ENTER_ITEM_STATE) {
            //assert(false)
            //m_transactionItemControl.askOrDisplayClusters(clusters, item_id);
        }
        return true;
    }

    /*----------------------------------------------------------------------------*/
    // @brief Touch item at the end of the list.
    // @post Screen to be updated.
    fun touchEnd(
        menuItem: CMenuItem, quantity: Int,
        prices: CPriceAndHalfPrice, clusterId: Short, twinItem: CMenuItem?
    ): Boolean {
        Log.i("framework", "CtransactionItemModel::touchEnd")
        var retVal = false
        if (menuItem.isOutOfStock() || (twinItem != null && twinItem.isOutOfStock())) {
            return false
        }
        val cursor = m_items.size;
        retVal = true
        global.cursor = cursor

        // No items yet, then add the food?
        if (cursor == 0) {
            retVal = insertItem(0, menuItem, quantity, prices, clusterId, twinItem);
            invalidateDisplayAndSetOrderChanged();
            Log.i("CTransactionItems", "First item added");
            return retVal
        }
//        CitemPtr previousOrder =(*m_clientOrdersHandler)[cursor - 1];
//        int unitPrice = previousOrder->getUnitPrice().Long();
//
//        if ( previousOrder->menuItemId == item_id
//        && previousOrder->twinItemId == twin_item_id
//        && unitPrice == prices.full_price.Long()
//            && previousOrder->deletedStatus == DELETE_NOT
//        && previousOrder->parts == 2
//        && CFG("entry_merge_similar_items"))
//        {
//            // Same as previous item.
//                m_clientOrdersHandler->addQuantity(cursor - 1, 1);
//            m_transactionItemControl.invalidateDisplayAndSetOrderChanged();
//            LOG_FUNC_EXIT << " Same item added" << Endl;
//            return true;
//        }
//        // Just add the item at the end.
        retVal = addItem(cursor, menuItem, prices, clusterId, twinItem)
        Log.i("CTransactionItems", "Item added")
        //assert(false)
        return retVal
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
    fun addItem(cursor: Int, menuItem: CMenuItem, prices: CPriceAndHalfPrice,
                clusterId: Short, twinItem: CMenuItem?): Boolean {
        var retVal = false
        Log.i("CTransactionItems", "addItem ${menuItem.localName}")
        if (insertItem( cursor+1, menuItem, 1, prices,
                clusterId, twinItem)) {
            global.cursor = m_items.size
            retVal = true
        }
        return retVal
    }

    /*----------------------------------------------------------------------------*/
    /** Insert extra or spice */
    fun insertExtra(
        menuItem: CMenuItem, twinItem: CMenuItem?, mainCursor: Int, quantity: Int,
        parts: Int, price: CMoney, halfPrice: CMoney, tax: Double, locations: Int,
        timeFrameId: ETimeFrameIndex, deviceId: Short, clusterId: Short,
        isPaid: EPayed, statiegeld: CMoney
    ) {
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

// bool CclientItemsHandler::insertItem( long itemId,...)
    fun insertItem(
        menuItem: CMenuItem, cursr: Int,
        quantity: Int, level: EOrderLevel, group: Int, page: Int, parts: Int,
        unitPrice: CMoney, originalPrice: CMoney, originalHalfPrice: CMoney,
        taxPercentage: Double, locations: Int,
        timeFrameId: CTimeFrameIndex, deviceId: Short, clusterId: Short,
        isPaid: EPayed, statiegeld: CMoney
    ): Boolean {
        var cursor = cursr
        var sequence = FIRST_SEQUENCE_ID
        val subSequence = 0 // NORMAL_ITEM_SUB_SEQUENCE
        val subSubSequence = 0 // NORMAL_ITEM_SUB_SEQUENCE;

        val itemsDb = GrpcServiceFactory.createDailyTransactionItemService()
        if (cursor >= 0 && cursor < m_items.size) {
            // Same as below cursor?
            val mi = m_items.mainItem(cursor)
            if (mi != null) {
                val i: CItem = mi.mainItem()
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
                ) {
                    return addQuantity(cursor, timeFrameId, 1)
                }
            }
            // Not same as below cursor.
            val mainItem = m_items.mainItem(cursor)
            if (mainItem != null) {
                sequence = mainItem.getSequence()
                itemsDb.insertSequence(global.transactionId, sequence)
                Log.i("CTransactionItems", "insertItem should not happen anymore!!")
            }
            else {
                cursor = m_items.size
                sequence = getNewSequence()
            }
        } else {
            cursor = m_items.size;
            sequence = getNewSequence()
        }

        val orderLevel = EOrderLevel.toOrderLevel(level)
        val timeFrame = timeFrameId.value.toInt()
        val paid = EPayed.toPayed(isPaid)
        itemsDb.createItem(menuItem.menuItemId, global.transactionId.toLong(),
            sequence, subSequence, subSubSequence, quantity, orderLevel, group, page,
            parts, unitPrice.cents(), originalPrice.cents(),
            originalHalfPrice.cents(), taxPercentage,
            locations, timeFrame,
            deviceId, clusterId, paid, statiegeld.cents())

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
        if (cursor >= m_items.size) {
            val cs = CSortedItem(i)
            m_items.add(cs);
        } else {
            // Increase sequence ID's.
            for (item in m_items) {
                if (item.getSequence() >= sequence) {
                    item.increaseSequence()
                }
            }
            m_items.add(cursor, i);
        }
        return true;
    }

    private fun getNewSequence(): Int =
        (m_items.maxOfOrNull { it.getSequence() } ?: 0) + 1

    private fun invalidateDisplayAndSetOrderChanged() {
        m_changed = true;
        m_newChanged = true;
    }

    fun addQuantity( cursor: Int, timeFrameId : CTimeFrameIndex, quantity: Int): Boolean {
        if (cursor < 0 || cursor >= m_items.size) {
            Log.i("CTransactionItems", "addQuantity  Cursor overflow!!")
            return false;
        }

        // Increase in items.
        assert(false)
        return false
    }
        //        CitemPtr item=m_itemList[cursor];
//        EdeletedStatus why =DELETE_NOT;
//        EtimeFrameIndex oldTimeFrame =TIME_FRAME_UNDEFINED;
//
//        if ( quantity<0)
//        {
//            why =(timeFrameId==item->timeFrameId) ?
//            DELETE_REMOVE_IMMEDIATE:DELETE_QUANTITY_AFTER_PRINTING;
//            oldTimeFrame =item->timeFrameId;
//        }
//        else if (item->level ==LEVEL_SEPARATOR)
//        {
//            return false;
//        }
//        int maxq =9999;
//        if ( item->getQuantity()+quantity >maxq)
//        {
//            quantity =maxq-item->getQuantity();
//        }
//        if ( item->getQuantity()+quantity <-maxq)
//        {
//            quantity =-maxq-item->getQuantity();
//        }
//        if ( item->getQuantity()+quantity ==0)
//        {
//            int level =CFG("entry_negative_quantity");
//            Eaccess access =personnel.getAccess(-1);
//            if ( level==0 || access ==NO_KEY || (level==1 && access==EMPLOYEE_KEY)
//                || (item->level !=LEVEL_ITEM && item->level !=LEVEL_COMBINE_ALL
//            && item->level !=LEVEL_FREE
//            && item->level !=LEVEL_MINUTES_PRICE && item->level !=LEVEL_PERSON))
//            {
//                return deleteItem( cursor, timeFrameId, why);
//            }
//        }
//
//        item->addQuantity(quantity);
//        customerDisplay( cursor);
//
//        // Increase in database.
//        int unitPrice = item->getUnitPrice().Long();
//        int statiegeld = item->getStatiegeldPerPiece();
//
//        m_pItemsDb->createItem( item->menuItemId, m_transactionId, item->sequence,
//        item->subSequence, item->subSubSequence, quantity, item->level,
//        item->group, item->page, item->parts,
//        unitPrice, item->originalAmount, item->originalHalfAmount, item->tax,
//        item->locations, timeFrameId, item->deviceId,
//        item->clusterId, PAID_NO, statiegeld, why, oldTimeFrame);
//        if (item->twinItemId >0)
//        {
//            int quantity = item->getQuantity();
//            m_pItemsDb->setTwinQuantity(m_transactionId, item->sequence,
//            item->subSequence, item->subSubSequence, quantity, item->parts);
//        }
//
//        // Remove extras and spices for normal items, except when we remove a spice.
//        if ( (item->level ==LEVEL_ITEM || item->level ==LEVEL_FREE)
//        && CFG("transaction_item_tree")==0 && CFG("entry_extra_main_quantity"))
//        {
//            // Add all the Spices and Extras also the same amount, remove them if 0!
//            int i;
//            for ( i=0; i<(int)m_itemList.size();)
//            {
//                CitemPtr subItem =m_itemList[i];
//                if ( subItem->sequence ==item->sequence &&
//                (subItem->level ==LEVEL_EXTRA || subItem->level ==LEVEL_SPICES))
//                {
//                    if ( why !=DELETE_REMOVE_IMMEDIATE)
//                    {
//                        oldTimeFrame =subItem->timeFrameId;
//                    }
//                    subItem->addQuantity(quantity);
//                    if ( subItem->getQuantity() ==0)
//                    {
//                        deleteItem( i, timeFrameId, why);
//                        continue;
//                    }
//                    statiegeld = subItem->getStatiegeldPerPiece();
//                    unitPrice = subItem->getUnitPrice().Long();
//                    m_pItemsDb->createItem( subItem->menuItemId, m_transactionId, subItem->sequence,
//                    subItem->subSequence, subItem->subSubSequence, quantity, subItem->level,
//                    subItem->group, subItem->page, subItem->parts,
//                    unitPrice, subItem->originalAmount, subItem->originalHalfAmount, subItem->tax,
//                    subItem->locations, timeFrameId, subItem->deviceId, subItem->clusterId, PAID_NO,
//                    statiegeld, why, oldTimeFrame);
//                }
//                i++;
//            }
//        }
//        return true;
//    }
//
//
}