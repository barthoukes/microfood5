package com.hha.framework

import android.util.Log
import com.hha.common.OrderLevel
import com.hha.resources.Global
import com.hha.types.EEnterState
import com.hha.types.EOrderLevel

class CTransactionItems : Iterable<CSortedItem> {
    var m_state: EEnterState = EEnterState.ENTER_ITEM_STATE
    val global = Global.getInstance()
    var m_items = CSortedItemList()

    constructor() {
        m_state = EEnterState.ENTER_ITEM_STATE
        global
    }

    // Add this iterator implementation
    //override fun iterator(): Iterator<CSortedItem> = m_items.iterator()
    operator fun get(index: Int): CSortedItem {
        return m_items[index]
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

    /*============================================================================*/
    /// @brief Touch or type any item.
    /// @param item_id What item is pressed
    /// @param cluster_id What cluster is pressed
    /// @param twin_item_id The twin item to use.
    /// @post Item handled. Screen to be updated.
    /// @return true when item added.
    /*============================================================================*/
    fun touchItem(cursor: Int, menuItem: CMenuItem): Boolean {
        var retVal = false
        var change2spice = false
        val prices = getPriceAndHalfPrice(menuItem)
        if (!prices.valid) {
            Log.i("framework", "CtransactionItemDialog::touchItem cannot find cluster $cluster_id")
            return false
        }
        return true
//        val menuCardId = global.menuCardId
//        var level : EOrderLevel = item_id.level
//        if (level == EOrderLevel.LEVEL_NOTHING) {
//            Log.i("framework", "Level nothing will not be added")
//            return false
//        }
//        if (m_state == EEnterState.ENTER_SUBITEM_STATE) {
//            change2spice = true
//        }
//        do {
//            if (change2spice) {
//                prices.clear()
//                if (level != EOrderLevel.LEVEL_SPICES) {
//                    level = EOrderLevel.LEVEL_EXTRA
//                }
//            }
//            // Check if we touch an extra/descriptive item.
//            when (level) {
//                EOrderLevel.LEVEL_EXTRA, EOrderLevel.LEVEL_SPICES,
//                EOrderLevel.LEVEL_SUB_EXTRA, EOrderLevel.LEVEL_SUB_SPICES ->
//                    return touchExtraOrSpice(item_id, prices, cluster_id, twin_item_id)
//                else -> {}
//                }
//
//            // Check if the item is at the end.
//            if (m_transactionItemControl.cursor >= m_clientOrdersHandler.size) {
//                // If so: Call another function.
//                retVal = touchEnd(item_id, 1, prices, cluster_id, twin_item_id)
//                m_transactionItemControl.customerDisplay(m_transactionItemControl.cursor)
//                return retVal
//            }
//            val cursorItem = m_clientOrdersHandler[m_transactionItemControl.cursor]
//            change2spice = cursorItem.level == LEVEL_COMBINE_ALL
//        } while (change2spice)
//
//        // If same as below cursor, then add one.
//        val cursor_item = m_clientOrdersHandler[m_transactionItemControl.cursor]
//        if (cursor_item.menuItemId == item_id && cursor_item.twinItemId == twin_item_id && CFG("entry_merge_similar_items")) {
//            addOneToCursorPosition()
//            return true
//        }
//        // Just insert this one at the cursor.
//        retVal = touchEnd(item_id, 1, prices, cluster_id, twin_item_id)
//        return retVal
//    }
    }

    fun getPriceAndHalfPrice(item_id: CMenuItem, cluster_id: Int): CPriceAndHalfPrice {
        val prices = CPriceAndHalfPrice()
//    val menuCardId = m_clientOrdersHandler.menuCardId
//
//    val option = CspecialPrices.Instance.find(menuCardId, product.special_prices)
//    val isTakeawayPrice = if (m_clientOrdersHandler.isTakeawaySitin) {
//        CFG("takeaway_sitin") < 2
//    } else {
//        m_clientOrdersHandler.isTakeaway || m_clientOrdersHandler.isTelephone
//    }
//
//    if (option == null) {
//        return getPriceAndHalfPrice(product, item_id, cluster_id, isTakeawayPrice)
//    } else {
//        prices.full_price = if (isTakeawayPrice) option.newTakeawayAmount else option.newRestaurantAmount
//        prices.half_price = if (isTakeawayPrice) option.newTakeawayHalfAmount else option.newRestaurantHalfAmount
//    }
        return prices
    }

    fun touchExtraOrSpice(
        item_id: CMenuItem, prices: CPriceAndHalfPrice,
        cluster_id: Short, twin_item_id: Int
    ): Boolean {
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
        return true;
    }
}