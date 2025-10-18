package com.hha.framework

import com.hha.framework.CItem
import android.util.Log
import com.hha.common.ItemList
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import com.hha.types.EItemLocation
import com.hha.types.EItemSort
import com.hha.types.EOrderLevel
import com.hha.types.ETimeFrameIndex
import java.util.logging.Logger
import kotlin.collections.mutableListOf

const val FIRST_SEQUENCE_ID = 1
private val log = Logger.getLogger("CsortedItemList")

class CSortedItemList : Iterable<CSortedItem> {
    val global = Global.getInstance()
    val CFG = global.CFG
    var freeItems = CFG.getOption("bill_print_free_items")
    var giftItems = CFG.getOption("bill_print_gift_items")
    var highestSequence: Int = 0
    var transactionId: Long = -1
    var lowestTimeFrame: ETimeFrameIndex = ETimeFrameIndex(ETimeFrameIndex.TIME_FRAME_UNDEFINED)
    var highestTimeFrame: ETimeFrameIndex = ETimeFrameIndex(ETimeFrameIndex.TIME_FRAME_UNDEFINED)
    var isKitchenPrintout: Boolean = false
    var splitPoint: Int = -1

    private var m_sortedItems = mutableListOf<CSortedItem>()
    private val sequencer = CTransactionSequencer()

    //fun clean() {
    //    m_sortedItems.clear()
    // }
    // Add this iterator implementation
    override fun iterator(): Iterator<CSortedItem> = m_sortedItems.iterator()

    fun add(item: CSortedItem) {
        m_sortedItems.add(item)
    }

    fun cursor2index(cursor: CCursor): Int {
        var index = 0
        var sortedIndex = 0
        for (item in m_sortedItems) {
            if (index + item.size > cursor.position) {
                break
            }
            index += item.size
            sortedIndex +=1
        }
        return sortedIndex
    }

    /** Erase one CItem from a certain index */
    fun eraseItem(cursor: CCursor)
    {
        var index = 0
        var sortedIndex = 0
        for (item in m_sortedItems) {
            if (index < cursor.position)
            {
                index += item.size
                sortedIndex++
            }
            if (index == cursor.position)
            {
                // Delete item + subitems
                m_sortedItems.removeAt(sortedIndex)
                return
            }
            else
            {
                // Delete subitem
                m_sortedItems[sortedIndex].items.removeAt(cursor.position - index)
                return
            }
        }
    }

    fun add(cursor: CCursor, item: CItem) {
        val c2i = cursor2index(cursor)
        val cItem = CSortedItem(item)
        m_sortedItems.add(c2i, cItem)
    }

    // Same as removeIt
    fun eraseSortedItem(index: Int) {
        m_sortedItems.removeAt(index)
    }

    // Override the [] operator for read access
    fun getSortedItem(index: Int): CSortedItem = m_sortedItems[index]

    fun getItem(index: CCursor): CItem?
    {
        var mutableIndex = index.position
        for (item in m_sortedItems)
        {
            if (mutableIndex < item.size)
            {
                return item[mutableIndex]
            }
            mutableIndex -= item.size
        }
        return null
    }

    // Override size property
    val size: Int
        get() {
            var sz = 0
            for (item in m_sortedItems) {
                sz += item.size
            }
            return sz
        }

    val itemSize : Int
        get() = m_sortedItems.size

    val empty: Boolean
        get() = m_sortedItems.isEmpty()

    fun mainItem(cursor: CCursor): CSortedItem? {
        var offset = 0
        for (item in m_sortedItems) {
            if (offset + item.size > cursor.position) {
                return item
            }
            offset += item.size
        }
        return null
    }

    fun convert(sort: EItemSort, itemsDb: ItemList): CItemList {
        val lst = mutableListOf<CItem>()
        for (item in itemsDb.entryList) {
            lst.add(CItem(item))
        }
        val list_items = CItemList(lst)
        val sortedItems = createList(list_items, true)

        return when (sort) {
            EItemSort.SORT_BILL -> convertSortBill(sortedItems)
            EItemSort.SORT_SPLIT -> convertSortSplit(sortedItems)
            EItemSort.SORT_PRINTER_SORTED, EItemSort.SORT_PRINTER_SORTED_ALL ->
                convertSortedPrinter(sortedItems)

            else -> convertUnsortedPrinter(sortedItems)
        }
    }

    private fun createList(itemsDb: CItemList, cleanList: Boolean) : MutableList<CSortedItem> {
        val items: MutableList<CSortedItem> = mutableListOf()
        if (cleanList) this.clean()
        var highestSequence = FIRST_SEQUENCE_ID - 1
        var row = 1
        val twinItems: MutableList<CItem> = mutableListOf()
        val listItems: MutableList<CItem> = mutableListOf()

        for (item in itemsDb) {
            if (item.level == EOrderLevel.LEVEL_TWIN_ITEM)
                twinItems.add(item) else listItems.add(item)
        }
        sequencer.setSortedItemList(listItems)
        mergeTwinItemsToListItems(twinItems, listItems)

        for (item in listItems) {
            if (item.sequence > highestSequence) {
                highestSequence = item.sequence
            }

            when (item.level) {
                EOrderLevel.LEVEL_ITEM, EOrderLevel.LEVEL_FREE,
                EOrderLevel.LEVEL_MINUTES_PRICE, EOrderLevel.LEVEL_PERSON
                    -> {
                    val newItem = CSortedItem()
                    newItem.row = row++
                    newItem.add(item)
                    items.add(newItem)
                }

                else -> {
                    // Handle sub-items (spices, extras, etc.)
                    var p = items.size
                    while (p > 0) {
                        p--
                        if (items[p][0].sequence == item.sequence) {
                            var found = false
                            var q = 0
                            while (q < items[p].items.size) {
                                val ip = items[p].items[q]
                                if (ip.menuItemId == item.menuItemId
                                    && ip.twinItemId == item.twinItemId
                                    && ip.sequence == item.sequence
                                    && ip.subSequence == item.subSequence
                                    && ip.subSubSequence == item.subSubSequence
                                    && ip.level == item.level
                                    && ip.originalAmount == item.originalAmount
                                    && ip.originalHalfAmount == item.originalHalfAmount
                                ) {
                                    ip.addQuantity(item.getQuantity())
                                    ip.chineseName = item.chineseName
                                    ip.localName = item.localName
                                    ip.chinesePrinterName = item.chinesePrinterName
                                    ip.localPrinterName = item.localPrinterName
                                    ip.parts = item.parts
                                    ip.setUnitPrice(item.getUnitPrice())
                                    ip.timeFrameId = item.timeFrameId
                                    found = true
                                    break
                                }
                                q = q + 1
                            }
                            if (!found) {
                                items[p].add(item)
                            }
                            break
                        }
                    }
                }
            }
        }
        this.highestSequence = highestSequence
        return items
    }

    private fun mergeTwinItemsToListItems(twinItems: CItemList, listItems: CItemList) {
        for (twinItem in twinItems) {
            for (listItem in listItems) {
                if (listItem.sequence == twinItem.sequence &&
                    listItem.subSequence == twinItem.subSequence &&
                    listItem.subSubSequence == twinItem.subSubSequence) {
                    listItem.twinItemId = twinItem.menuItemId
                    listItem.updateName()
                }
            }
        }
    }

    fun setTransaction(transactionId: Int): Boolean {
        val dti = GrpcServiceFactory.createDailyTransactionItemService()
        val itemsDb : ItemList? = dti.selectTransaction(transactionId)
        val list_items = CItemList(false, itemsDb)
        m_sortedItems = createList(list_items, true)
        sortAndTry2Merge(0, m_sortedItems.size, false)
        return true
    }

    fun getTransactionSequencer(): CTransactionSequencer {
        return sequencer
    }

    fun setTransaction(
        transactionId: Long,
        timeFrameLow: ETimeFrameIndex,
        timeFrameHigh: ETimeFrameIndex,
        location: EItemLocation,
        is_kitchen_printout: Boolean
    ): Boolean {
        highestSequence = 0
        this.transactionId = transactionId
        lowestTimeFrame = timeFrameLow
        highestTimeFrame = timeFrameHigh
        isKitchenPrintout = is_kitchen_printout

        val transactionItems = GrpcServiceFactory.createDailyTransactionItemService()
        val locations = EItemLocation.location2Locations(location)

        val sequence = transactionItems.findSequences(transactionId, timeFrameLow.index.toInt(),
            timeFrameHigh.index.toInt(), locations)

        var sequenceChanges = mutableListOf<Int>()
        if (timeFrameLow.index > 1) {
            val sc = transactionItems.findSequences(transactionId, 1,
                timeFrameLow.index - 1, locations)
            sequenceChanges = sc.toMutableList()
            filterUnchangedSequences(sequenceChanges, sequence)
        }
        val idx : ETimeFrameIndex = timeFrameLow

        val itemsDb = transactionItems.findSequenceItems(
            transactionId.toInt(), sequenceChanges,
            idx.previous(), true
        )
        val list_items = CItemList(false,itemsDb)
        m_sortedItems = createList(list_items, true)
        renumber(m_sortedItems)
        if (m_sortedItems.isNotEmpty()) {
            splitPoint = m_sortedItems.size
        }
        val listDb = transactionItems.findSequenceItems(transactionId.toInt(),
            sequence, timeFrameHigh, false)
        val list_items2 = CItemList(false, listDb)

        val list2 = createList(list_items2, false)
        move2List(list2, m_sortedItems, locations)
        return m_sortedItems.isNotEmpty()
    }

    public fun move2List( source: MutableList<CSortedItem>, destination: MutableList<CSortedItem>,
                          locations : Int) {
        var nextIndex = destination.size+1;

        for (item in source) {
            if (item.getLocations() and locations !=0) {
                item.row =nextIndex++
                destination.add(item)
            }
        }
        source.clear()
    }

    private fun renumber( list: MutableList<CSortedItem>) {
        var n =1
        for ( item in list)
        {
            item.row = n++
        }
    }

    fun filterUnchangedSequences(sequenceBefore :MutableList<Int> , sequence: List<Int> ) {
        var n=0
        while ( n<sequenceBefore.size)
        {
            val seq =sequenceBefore[n]
            var found =false;
            for ( p in sequence)
            {
                if ( p ==seq)
                {
                    found =true;
                    break;
                }
            }
            if ( found)
            {
                n++;
            }
            else
            {
                sequenceBefore.removeAt(n);
            }
        }
    }

    fun convertSortedPrinter(items: MutableList<CSortedItem>) : CItemList {
        // Merge similar items.
        var list_items = CItemList()

        var p = 0
        var q: Int
        while (p < items.size) {
            if (items[p].done) {
                p++
                continue
            }
            q = p + 1
            while (q < items.size) {
                if (items[q].done) {
                    q++
                    continue
                }
                if (items[p].mergeSame(items[q], true,
                        isKitchenPrintout, lowestTimeFrame,
                        highestTimeFrame)) {
                    // Continue, maybe the next one can also merge.
                }
                q++
            }
            p++
        }
        // Erase spices and extras with a zero quantity.
        p = 0
        while (p < items.size) {
            if (items[p].items[0].getQuantity() == 0 || items[p].done) {
                items.removeAt(p)
            } else {
                // Erase extra with quantity 0
                q = 1
                while (q < items[p].items.size) {
                    if (items[p].items[q].getQuantity() == 0) {
                        items[p].items.removeAt(q)
                    } else {
                        q++
                    }
                }
                p++
            }
        }
        // Decide who is first...
        while (items.isNotEmpty()) {
            p = 0
            var page = -1
            for (q in items.indices) {
                if (items[q].items[0].page < page || page == -1) {
                    page = items[q].items[0].page
                    p = q
                }
            }
            for (q in items[p].items.indices) {
                list_items.add(items[p].items[q])
            }
            items.removeAt(p)
        }
        return list_items
    }

    fun convertUnsortedPrinter(items: MutableList<CSortedItem>) : CItemList {
        // Merge similar items.
        var list_items = CItemList()

        var p = 0
        var q: Int
        while (p < items.size) {
            if (items[p].done) {
                p++
                continue
            }
            q = p + 1
            while (q < items.size) {
                if (items[q].done) {
                    q++
                    continue
                }
                if (items[p].mergeSame(items[q], false,
                        isKitchenPrintout, lowestTimeFrame,
                        highestTimeFrame)) {
                    // Continue, maybe the next one can also merge.
                }
                q++
            }
            p++
        }
        // Erase spices and extras with a zero quantity.
        p = 0
        while (p < items.size) {
            if (items[p].items[0].getQuantity() == 0 || items[p].done) {
                items.removeAt(p)
            } else {
                // Erase extra with quantity 0
                q = 1
                while (q < items[p].items.size) {
                    if (items[p].items[q].getQuantity() == 0) {
                        items[p].items.removeAt(q)
                    } else {
                        q++
                    }
                }
                p++
            }
        }
        for (item in items) {
            for (subItem in item.items) {
                list_items.add(subItem)
            }
        }
        return list_items
    }

    fun convertOrder(items: MutableList<CSortedItem>) : CItemList {
        // Merge similar items.
        var list_items = CItemList()

        var p = 0
        var q: Int
        while (p < items.size) {
            if (items[p].done) {
                p++
                continue
            }
            q = p + 1
            while (q < items.size) {
                if (items[q].done) {
                    q++
                    continue
                }
                if (items[p].mergeSame(items[q], false,
                        isKitchenPrintout, lowestTimeFrame,
                        highestTimeFrame)) {
                    // Continue, maybe the next one can also merge.
                }
                q++
            }
            p++
        }
        // Erase spices and extras with a zero quantity.
        p = 0
        while (p < items.size) {
            if (items[p].items[0].getQuantity() == 0 || items[p].done) {
                items.removeAt(p)
            } else {
                // Erase extra with quantity 0
                q = 1
                while (q < items[p].items.size) {
                    if (items[p].items[q].getQuantity() == 0) {
                        items[p].items.removeAt(q)
                    } else {
                        q++
                    }
                }
                p++
            }
        }
        serializeItems(items, list_items)
        return list_items
    }


    fun serializeItems(source: List<CSortedItem>, destination: CItemList) {
        for (a in source) {
            for (b in a.items.indices) {
                destination.add(a[b])
            }
        }
    }

    fun eraseDoneAndFreeItems(items: MutableList<CSortedItem>): Int {
        // Erase spices and extras with a zero quantity or zero.
        var p = 0
        var erased = 0
        while (p < items.size) {
            if (items[p].items[0].getQuantity() == 0 || items[p].done) {
                items.removeAt(p)
                erased++
            } else {
                // Erase extra without price or quantity 0 if free item and not gift
                var q = 1
                while (q < items[p].items.size) {
                    var free = items[p].items[q].getQuantity() == 0
                    if (!freeItems && items[p].items[q].getTotal().empty()) {
                        if (!giftItems || items[p].items[q].originalAmount.empty()) {
                            free = true
                        }
                    }
                    if (free) {
                        items[p].items.removeAt(q)
                        erased++
                    } else {
                        q++
                    }
                }
                p++
            }
        }
        return erased
    }


    fun eraseDoneItems(items: MutableList<CSortedItem>): Int {
        // Erase spices and extras with a zero quantity or zero.
        var p = 0
        var erased = 0
        while (p < items.size) {
            if (items[p].items[0].getQuantity() == 0 || items[p].done) {
                items.removeAt(p)
                erased++
            } else {
                // Erase extra without price or quantity 0 if free item and not gift
                var q = 1
                while (q < items[p].items.size) {
                    if (items[p].items[q].getQuantity() == 0) {
                        items[p].items.removeAt(q)
                        erased++
                    } else {
                        q++
                    }
                }
                p++
            }
        }
        return erased
    }


    fun convertSortBill(items: MutableList<CSortedItem>) : CItemList {
        // Merge similar items.
        eraseDoneAndFreeItems(items)
        val mergeOnlySameSequence = false
        var p = 0
        while (p < items.size) {
            var q = p + 1
            while (q < items.size) {
                if (!items[p].done && !items[q].done &&
                    items[q].isSimilarItemStructureAndOrder(
                        items[p], isKitchenPrintout,
                        mergeOnlySameSequence,
                        lowestTimeFrame, highestTimeFrame
                    )
                ) {
                    items[p].mergeSame(items[q], true,
                        isKitchenPrintout, lowestTimeFrame,
                        highestTimeFrame)
                    items[q].done = true
                }
                q++
            }
            p++
        }
        // Erase free and gift items if they're not printed
        eraseDoneAndFreeItems(items)
        // Decide who is first...
        val list_items = sortItems(items)
        return list_items
    }

    fun convertSortSplit(items: MutableList<CSortedItem>) : CItemList {
        // Merge similar items.
        eraseDoneItems(items)
        val mergeOnlySameSequence = false
        var p = 0
        while (p < items.size) {
            var q = p + 1
            while (q < items.size) {
                if (!items[p].done && !items[q].done &&
                    items[q].isSimilarItemStructureAndOrder(
                        items[p], isKitchenPrintout,
                        mergeOnlySameSequence,
                        lowestTimeFrame, highestTimeFrame
                    )
                ) {
                    items[p].mergeSame(items[q], true,
                        isKitchenPrintout, lowestTimeFrame,
                        highestTimeFrame)
                    items[q].done = true
                }
                q++
            }
            p++
        }
        // Erase free and gift items if they're not printed
        eraseDoneItems(items)
        // Decide who is first...
        val list_items = sortItems(items)
        return list_items
    }

    fun sortItems(items: MutableList<CSortedItem>) : CItemList {
        var list_items = CItemList()
        while (!items.isEmpty()) {
            var p = 0
            var page = -1
            for (q in items.indices) {
                if (items[q].items[0].page < page || page == -1) {
                    page = items[q].items[0].page
                    p = q
                }
            }
            for (q in items[p].items.indices) {
                list_items.add(items[p].items[q])
            }
            items.removeAt(p)
        }
        return list_items
    }

    fun clean() {
        m_sortedItems.clear()
    }


    fun mergeTwinItemsToListItems(twinItems: List<CItem>, listItems: MutableList<CItem>) {
        // Merge twinItems and listItems
        for (twinItem in twinItems) {
            for (listItem in listItems) {
                if (listItem.sequence == twinItem.sequence &&
                    listItem.subSequence == twinItem.subSequence &&
                    listItem.subSubSequence == twinItem.subSubSequence
                ) {
                    listItem.twinItemId = twinItem.menuItemId
                    listItem.updateName()
                }
            }
        }
    }


    fun convert(sort: EItemSort, itemsDb: CItemList) : CItemList {
        /// Select all items and add to vector. Sorting is not needed anymore.
        val sortedItems = createList(itemsDb, true)

        val listItems = when (sort) {
            EItemSort.SORT_BILL -> convertSortBill(sortedItems)
            EItemSort.SORT_SPLIT -> convertSortSplit(sortedItems)
            EItemSort.SORT_PRINTER_SORTED, EItemSort.SORT_PRINTER_SORTED_ALL,
                 EItemSort.SORT_COOKING ->
                convertSortedPrinter(sortedItems)
            EItemSort.SORT_PRINTER_UNSORTED_ALL, EItemSort.SORT_PRINTER_UNSORTED,
            EItemSort.SORT_NONE, EItemSort.SORT_DEFAULT -> convertUnsortedPrinter(sortedItems)
            EItemSort.SORT_ORDER -> convertOrder(sortedItems)
        }
        return listItems
    }

    fun size(): Int {
        return m_sortedItems.size
    }

    fun sortAndTryToMerge(sortEnabled: Boolean, splitPoint: Int): Int {
        var lowest = 0
        var index = 0
        var splitPoint = splitPoint
        while (index < size()) {
            val c = m_sortedItems[index][0]
            if (c.level == EOrderLevel.LEVEL_SEPARATOR && splitPoint <= 0) {
                lowest = sortAndTry2Merge(lowest, index, sortEnabled)
                index = lowest
                lowest++
            } else if (splitPoint == index && splitPoint > 0) {
                lowest = sortAndTry2Merge(lowest, index, sortEnabled)
                index = lowest
                splitPoint = -1
            }
            index++
        }
        sortAndTry2Merge(lowest, index, sortEnabled)
        return m_sortedItems.size
    }

    fun eraseEmpty(first: Int, last: Int): Boolean {
        var change = false
        var x = first
        var last = last
        while (x < last) {
            val p = m_sortedItems[x]
            if (p.getQuantity() == 0) {
                m_sortedItems.removeAt(x)
                last--
                change = true
                continue
            } else {
                change = change or p.eraseEmpty()
            }
            x++
        }
        return change
    }



    fun sortByItemPage(first: Int, last: Int): Boolean {
        var change = false
        var x = first
        while (x <= last - 2) {
            val p = m_sortedItems[x]
            val q = m_sortedItems[x + 1]

            val sign_p = p.getSign()
            val sign_q = q.getSign()

            if (sign_q < sign_p || (sign_q == sign_p && p.getPositionFactor() > q.getPositionFactor())) {
                // Exchange these
                val i = p
                m_sortedItems[x] = q
                m_sortedItems[x + 1] = i
                change = true
                continue
            }
            x++
        }
        return change
    }

    fun sortByItemSequence(first: Int, last: Int): Boolean {
        var change = false
        var x = first
        while (x <= last - 2) {
            val p = m_sortedItems[x]
            val q = m_sortedItems[x + 1]

            val sign_p = p.getSign()
            val sign_q = q.getSign()

            if (sign_q < sign_p || (sign_q == sign_p && p.getSequence() > q.getSequence())) {
                // Exchange these
                val i = q
                m_sortedItems[x + 1] = p
                m_sortedItems[x] = i
                change = true
                continue
            }
            x++
        }
        return change
    }

    fun mergeItems(first: Int, last: Int, sortEnabled: Boolean): Boolean {
        var change = false
        var index = first
        var last = last
        while (index <= last - 2) {
            var y = index + 1
            while (y < last) {
                val sortOnlySameSequence = (y - index > 1) && (!sortEnabled)
                val p : CSortedItem = m_sortedItems[index]
                val q = m_sortedItems[y]
                if (p.isSimilarItemStructureAndOrder(
                        q, isKitchenPrintout,
                        sortOnlySameSequence,
                        lowestTimeFrame, highestTimeFrame
                    )
                ) {
                    if (p.mergeSame(q, true,
                            isKitchenPrintout, lowestTimeFrame,
                            highestTimeFrame)) {
                        change = true
                        m_sortedItems.removeAt(y)
                        last--
                        continue
                    }
                }
                y++
            }
            index++
        }
        return change
    }

    fun mergePortionChanges(first: Int, last: Int): Boolean {
        var change = false
        var x = first
        var last = last
        while (x <= last - 2) {
            var y = x + 1
            while (y < last) {
                val p = m_sortedItems[x]
                val q = m_sortedItems[y]
                if (p.isPossibleToMerge(q, isKitchenPrintout)) {
                    if (p.merge(q)) {
                        change = true
                        m_sortedItems.removeAt(y)
                        last--
                        continue
                    }
                }
                y++
            }
            x++
        }
        return change
    }

    fun mergeSameSequence(first: Int, last: Int): Boolean {
        var change = false
        val mergeOnlySameSequence = false
        var x = first
        var last = last
        while (x <= last - 2) {
            var y = x + 1
            while (y < last) {
                val p = m_sortedItems[x]
                val q = m_sortedItems[y]
                if (q.getSequence() == p.getSequence()) {
                    if (p.isSimilarItemStructureAndOrder(
                            q, isKitchenPrintout,
                            mergeOnlySameSequence,
                            lowestTimeFrame, highestTimeFrame
                        )
                    ) {
                        if (p.mergeSame(q, true,
                                isKitchenPrintout,
                                lowestTimeFrame,
                                highestTimeFrame)) {
                            change = true
                            m_sortedItems.removeAt(y)
                            last--
                            continue
                        }
                    }
                }
                y++
            }
            x++
        }
        return change
    }

    fun mergeItemsSameTimeFrame(first: Int, last: Int): Boolean {
        var change = false
        val mergeOnlySameSequence = false
        var x = first
        var last = last
        while (x <= last - 2) {
            var y = x + 1
            while (y < last) {
                val p = m_sortedItems[x]
                val q = m_sortedItems[y]
                if (p.isSameTimeFrame(q)) {
                    if (p.isSimilarItemStructureAndOrder(
                            q, isKitchenPrintout,
                            mergeOnlySameSequence,
                            lowestTimeFrame, highestTimeFrame
                        )
                    ) {
                        if (p.mergeSame(q, true,
                                isKitchenPrintout, lowestTimeFrame,highestTimeFrame)) {
                            change = true
                            m_sortedItems.removeAt(y)
                            last--
                            continue
                        }
                    }
                }
                y++
            }
            x++
        }
        return change
    }

    fun mergeNegativeAndPositive(first: Int, last: Int): Boolean {
        var change = false
        var x = first
        var last = last
        val mergeOnlySameSequence = false
        while (x <= last - 2) {
            var y = x + 1
            while (y < last) {
                val p = m_sortedItems[x]
                val q = m_sortedItems[y]
                if (p.getSign() == -1 && q.getSign() == 1) {
                    if (p.isSimilarItemStructureAndOrder(
                            q, isKitchenPrintout,
                            mergeOnlySameSequence,
                            lowestTimeFrame, highestTimeFrame
                        )
                    ) {
                        if (p.mergeSame(q, true,
                                isKitchenPrintout,
                                lowestTimeFrame,
                                highestTimeFrame)) {
                            change = true
                            m_sortedItems.removeAt(y)
                            last--
                            continue
                        }
                    }
                }
                y++
            }
            x++
        }
        return change
    }

    fun sortAndTry2Merge(first: Int, last: Int, sortEnabled: Boolean): Int {
        var change = true
        var retry = 200
        var first = first
        var last = last

        while (change) {
            while (change && last - first > 1) {
                change = mergeItems(first, last, sortEnabled)
                if (sortEnabled) {
                    change = change or sortByItemPage(first, last)
                } else {
                    change = change or sortByItemSequence(first, last)
                }
                change = change or mergeNegativeAndPositive(first, last)
                change = change or mergeSameSequence(first, last)
                if (--retry == 0) {
                    Log.e("CsortedItemList", "sortAndTry2Merge retries too many times")
                }
            }
            change = eraseEmpty(first, last)
        }
        return last
    }

    fun getSplitPointValue(): Int {
        return splitPoint
    }

    fun tryToMerge(first: Int, last: Int) {
        if (last <= first) {
            return
        }
        eraseDoneAndFreeItems(m_sortedItems)
    }

}
