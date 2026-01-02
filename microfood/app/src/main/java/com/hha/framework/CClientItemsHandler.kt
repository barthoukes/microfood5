package com.hha.framework

import android.util.Log
import com.hha.common.Payed
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import com.hha.common.DeletedStatus
import com.hha.types.EOrderLevel
import com.hha.types.EItemSort
import com.hha.types.CMoney
import com.hha.types.EDeletedStatus
import com.hha.types.ESubItems
import com.hha.types.ETimeFrameIndex

class CclientItemsHandler {
    private val mItemsDb = GrpcServiceFactory.createDailyTransactionItemService()
    private var mItemList = CItemList(emptyList<CItem>())
    private var mSubItems: ESubItems = ESubItems.ORDER_ITEMS
    private val global = Global.getInstance()
    private var mTransactionId = -1
    private val CFG = global.CFG
    private var mTimeFrameIndex : ETimeFrameIndex =
        ETimeFrameIndex(ETimeFrameIndex.TIME_FRAME_LATEST)
    private var mTransactionSequencer = CTransactionSequencer()

    companion object {
        private const val FIRST_SEQUENCE_ID = 1
        private const val FIRST_SUB_SEQUENCE_ID = 1
        private const val NORMAL_ITEM_SUB_SEQUENCE = 0
    }

    init {
        mTransactionSequencer = CTransactionSequencer()
    }

    fun selectTransaction(transactionId: Int, sort: EItemSort, timeFrameIndex: ETimeFrameIndex) {
        mTimeFrameIndex = timeFrameIndex
        mTransactionId = transactionId
        val itemsDb = mItemsDb.selectTransactionId(
            transactionId, EItemSort.toItemSort(sort), timeFrameIndex.index.toInt(), -1)
        mSubItems = if (CFG.getOption("entry_show_deleted"))
            ESubItems.ORDER_ITEMS_AND_DELETED_AND_EXTRA
        else ESubItems.ORDER_ITEMS_AND_EXTRA

        if (transactionId <= 0) {
            mItemList.clear()
            return
        }

        // Convert itemsDb to our domain.
       // val itemList = CItemList(itemsDb)

        val cil = CSortedItemList()
        if (itemsDb != null) {
            mItemList = cil.convert(sort, itemsDb)
        }
        mTransactionSequencer = cil.getTransactionSequencer()
    }

    fun selectTimeFrame(transactionId: Int, timeFrameIndex: ETimeFrameIndex) {
        mTimeFrameIndex = timeFrameIndex
        mTransactionId = transactionId
        // @TODO Implement selectTimeFrame
        mSubItems = if (CFG.getOption("entry_show_deleted"))
            ESubItems.ORDER_ITEMS_AND_DELETED_AND_EXTRA else ESubItems.ORDER_ITEMS_AND_EXTRA

        if (transactionId <= 0) {
            mItemList.clear()
            return
        }

        val cil = CSortedItemList()
        // @TODO Implement cil.convert
        mTransactionSequencer = cil.getTransactionSequencer()
    }

    fun getNewSequence(): Int = mTransactionSequencer.getNewSequence()

    fun push_back(item: CItem) {
        mItemList.add(item)
    }

    operator fun get(index: Int): CItem {
        val invalid = CItem()
        if (index < 0 || index >= mItemList.size) {
            return invalid
        }
        return mItemList[index]
    }

    fun nextPortion(cursor: Int, timeFrameId: ETimeFrameIndex): Boolean {
        if (cursor < 0 || cursor >= mItemList.size) {
            Log.e("ERROR", "CclientItemsHandler::nextPortion  Cursor overflow!!")
            return false
        }

        val item = mItemList[cursor]
        val nextPortion = if (item.parts == 2) 1 else 2
        var price = item.originalAmount
        val halfPrice = item.originalHalfAmount
        val sequence = item.sequence
        val subSequence = item.subSequence
        val subSubSequence = item.subSubSequence
        val statiegeld : CMoney = item.getStatiegeld()
        var quantity = item.getQuantity()
        val why = if (item.timeFrameId != timeFrameId)
            EDeletedStatus.DELETE_PORTION_AFTER_PRINTING
        else EDeletedStatus.DELETE_PORTION_IMMEDIATE
        var unitPrice : CMoney = item.getUnitPrice()

        mItemsDb.createItem(
            item.menuItemId, mTransactionId, sequence,
            subSequence, subSubSequence, -quantity,
            EOrderLevel.toOrderLevel(item.level),
            item.group, item.page, item.parts,
            unitPrice.cents(), item.originalAmount.cents(),
            item.originalHalfAmount.cents(), item.tax,
            item.locations, timeFrameId.index.toInt(),
            item.deviceId, item.clusterId,
            Payed.PAID_NO, item.getStatiegeldPerPiece(),
            EDeletedStatus.toDeletedStatus(why),
            item.timeFrameId.toInt()
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

        mItemsDb.createItem(
            item.menuItemId, mTransactionId, sequence,
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

//    fun move2List(source: CSortedItemList,
//                  destination: CSortedItemList, locations: Int)
//    {
//        var nextIndex = destination.size + 1
//        while (!source.empty)
//        {
//            if (source.getSortedItem(0).getLocations() and locations != 0)
//            {
//                source[0].row = nextIndex++
//                destination.add(source[0])
//            }
//            source.erase(0)
//        }
//    }
}