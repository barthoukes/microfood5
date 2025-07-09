package com.hha.framework

import android.util.Log
import com.hha.framework.CItem
import com.hha.common.Payed
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import com.hha.common.DeletedStatus
import com.hha.service.DailyTransactionItemService
import com.hha.types.EOrderLevel
import com.hha.types.EItemSort
import com.hha.types.EItemLocation
import com.hha.resources.Configuration
import com.hha.types.CMoney
import com.hha.types.EDeletedStatus
import com.hha.types.ESubItems
import com.hha.types.ETimeFrameIndex
import kotlin.collections.ArrayList

class CclientItemsHandler {
    private val m_pItemsDb = GrpcServiceFactory.createDailyTransactionItemService()
    private var m_itemList = CItemList(emptyList<CItem>())
    private var m_subItems: ESubItems = ESubItems.ORDER_ITEMS
    private var global = Global.getInstance()
    private var CFG = global.CFG
    private var m_transactionId: Long = 0
    private var m_timeFrameIndex : ETimeFrameIndex =
        ETimeFrameIndex(ETimeFrameIndex.TIME_FRAME_LATEST)
    private val m_freeItems = CFG.getOption("bill_print_free_items")
    private val m_giftItems = CFG.getOption("bill_print_gift_items")
    private val m_customerDisplayAlignName = CFG.getOption("customer_display_align_name")
    private var m_pTransactionSequencer = CTransactionSequencer()
    private var m_sortedItems = CSortedItemList() ///< List of items


    companion object {
        private const val FIRST_SEQUENCE_ID = 1
        private const val FIRST_SUB_SEQUENCE_ID = 1
        private const val NORMAL_ITEM_SUB_SEQUENCE = 0
    }

    init {
        m_pTransactionSequencer = CTransactionSequencer()
    }

    fun selectTransaction(transactionId: Long, sort: EItemSort, timeFrameIndex: ETimeFrameIndex) {
        m_timeFrameIndex = timeFrameIndex
        m_transactionId = transactionId
        val itemsDb = m_pItemsDb.selectTransactionId(
            transactionId, EItemSort.toItemSort(sort), timeFrameIndex.index.toInt(), -1)
        m_subItems = if (CFG.getOption("entry_show_deleted"))
            ESubItems.ORDER_ITEMS_AND_DELETED_AND_EXTRA
        else ESubItems.ORDER_ITEMS_AND_EXTRA

        if (transactionId <= 0) {
            m_itemList.clear()
            return
        }

        // Convert itemsDb to our domain.
       // val itemList = CItemList(itemsDb)

        val cil = CSortedItemList()
        if (itemsDb != null) {
            m_itemList = cil.convert(sort, itemsDb)
        }
        m_pTransactionSequencer = cil.getTransactionSequencer()
    }

    fun selectTimeFrame(transactionId: Long, timeFrameIndex: ETimeFrameIndex) {
        m_timeFrameIndex = timeFrameIndex
        m_transactionId = transactionId
        // @TODO Implement selectTimeFrame
        m_subItems = if (CFG.getOption("entry_show_deleted"))
            ESubItems.ORDER_ITEMS_AND_DELETED_AND_EXTRA else ESubItems.ORDER_ITEMS_AND_EXTRA

        if (transactionId <= 0) {
            m_itemList.clear()
            return
        }

        val cil = CSortedItemList()
        // @TODO Implement cil.convert
        m_pTransactionSequencer = cil.getTransactionSequencer()
    }

    fun getNewSequence(): Int = m_pTransactionSequencer.getNewSequence()

    fun push_back(item: CItem) {
        m_itemList.add(item)
    }

    operator fun get(index: Int): CItem {
        val invalid = CItem()
        if (index < 0 || index >= m_itemList.size) {
            return invalid
        }
        return m_itemList[index]
    }

    fun nextPortion(cursor: Int, timeFrameId: ETimeFrameIndex): Boolean {
        if (cursor < 0 || cursor >= m_itemList.size) {
            Log.e("ERROR", "CclientItemsHandler::nextPortion  Cursor overflow!!")
            return false
        }

        val item = m_itemList[cursor]
        val nextPortion = if (item.parts == 2) 1 else 2
        var price = item.originalAmount
        val halfPrice = item.originalHalfAmount
        val sequence = item.sequence
        val subSequence = item.subSequence
        val subSubSequence = item.subSubSequence
        val statiegeld : CMoney = item.getStatiegeld()
        var quantity = item.getQuantity()
        val why = if (item.timeFrameId.value.toShort() != timeFrameId.index)
            EDeletedStatus.DELETE_PORTION_AFTER_PRINTING
        else EDeletedStatus.DELETE_PORTION_IMMEDIATE
        var unitPrice : CMoney = item.getUnitPrice()

        m_pItemsDb.createItem(
            item.menuItemId, m_transactionId, sequence,
            subSequence, subSubSequence, -quantity,
            EOrderLevel.toOrderLevel(item.level),
            item.group, item.page, item.parts,
            unitPrice.cents(), item.originalAmount.cents(),
            item.originalHalfAmount.cents(), item.tax,
            item.locations, timeFrameId.index.toInt(),
            item.deviceId, item.clusterId,
            Payed.PAID_NO, item.getStatiegeldPerPiece(),
            EDeletedStatus.toDeletedStatus(why),
            item.timeFrameId.value.toInt()
        )

        item.parts = nextPortion
        item.updateName()

        if (item.parts == 1) {
            price = CMoney((price.cents() * CFG.getValue("portion_half_price")) / 100
            + CFG.getValue("portion_round"))
            price.round(CFG.getValue("portion_round"))

            if (CFG.getOption("portion_define_price") && !halfPrice.empty()) {
                price = halfPrice
            }
            item.setUnitPrice(price)
        } else {
            item.setUnitPrice((item.parts * price.cents()) / 2)
        }

        unitPrice = item.getUnitPrice()
        quantity = item.getQuantity()

        m_pItemsDb.createItem(
            item.menuItemId, m_transactionId, sequence,
            subSequence, subSubSequence, quantity,
            EOrderLevel.toOrderLevel(item.level),
            item.group, item.page, item.parts,
            unitPrice.cents(), item.originalAmount.cents(),
            item.originalHalfAmount.cents(), item.tax,
            item.locations, timeFrameId.index.toInt(),
            item.deviceId, item.clusterId,
            Payed.PAID_NO, item.getStatiegeldPerPiece(),
            DeletedStatus.DELETE_NOT, 0)
        return true
    }

    fun filterUnchangedSequences(sequenceBefore: MutableList<Int>, sequence: List<Int>) {
        var n = 0
        while (n < sequenceBefore.size) {
            val seq = sequenceBefore[n]
            val found = sequence.any { it == seq }
            if (found) {
                n++
            } else {
                sequenceBefore.removeAt(n)
            }
        }
    }

    fun renumber(list: MutableList<CSortedItem>) {
        for (n in list.indices) {
            list[n].row = n + 1
        }
    }

    fun move2List(source: CSortedItemList,
                  destination: CSortedItemList, locations: Int) {
        var nextIndex = destination.size + 1
        while (!source.empty) {
            if (source[0].getLocations() and locations != 0) {
                source[0].row = nextIndex++
                destination.add(source[0])
            }
            source.erase(0)
        }
    }

}