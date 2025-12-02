package com.hha.framework

import android.util.Log
import com.hha.framework.CItem
import com.hha.resources.Global
import com.hha.types.ETimeFrameIndex
import com.hha.resources.Configuration
import com.hha.types.C3Moneys
import com.hha.types.CMoney
import com.hha.types.EOrderLevel
import com.hha.types.EPaymentStatus
import com.hha.types.ETaal
import com.hha.types.ETreeRow

import java.text.DecimalFormat

class CSortedItem : Iterable<CItem>
{
    var done: Boolean = false
    val global = Global.getInstance()
    val CFG = global.CFG
    var row: Int = 0
    var items = mutableListOf<CItem>()

    // Required iterator implementation
    override fun iterator(): Iterator<CItem> = items.iterator()

    constructor(item: CItem)
    {
        items.clear()
        items.add(item)
    }

    constructor()
    {
        items.clear()
    }

    companion object
    {
        lateinit var startText: String
        lateinit var nextText: String
        lateinit var endText: String

        init
        {
            val CFG = Global.getInstance().CFG
            // Initialize text values once
            startText = CFG.getString("kitchen_brackets_start")
            nextText = CFG.getString("kitchen_brackets_next")
            endText = CFG.getString("kitchen_brackets_end")

            listOf(startText, nextText, endText).forEach { text ->
                text.replace("\\n", "\n").replace("\\t", "\t")

            }
        }
    }

    val size: Int
        get() = items.size

    fun add(item: CItem)
    {
        items.add(item)
    }

    fun calculateTotalItems(): C3Moneys
    {
        var total = C3Moneys()
        for (item in items)
        {
            when
            {
                item.tax > 13.5 -> total.taxHigh = total.taxHigh + item.getTotal()
                item.tax > 0.1 -> total.taxLow = total.taxLow + item.getTotal()
                else -> total.taxFree = total.taxFree + item.getTotal()
            }
        }
        return total
    }

    fun itemSum(): Int = items.sumOf { it.getQuantity() }

    fun isValidItem(payStatus: EPaymentStatus)
     = items[0].isValidItem(payStatus)

    fun addSorted(newItem: CItem)
    {
        val insertIndex = items.indexOfFirst { item ->
            when
            {
                item.sequence < newItem.sequence -> true
                item.sequence > newItem.sequence -> false
                item.subSequence > newItem.subSequence -> true
                item.subSequence < newItem.subSequence -> false
                else -> item.subSubSequence > newItem.subSubSequence
            }
        }.takeIf { it != -1 } ?: items.size

        items.add(insertIndex, newItem)
    }

    fun getLocations(): Int
    {
        return items[0].locations
    }

    fun merge(new_item: CSortedItem): Boolean
    {
        // Merge items for same sequence!
        items[0].merge(new_item.items[0]);
        Log.i("CsortedItem", "merge New quantity =$items[0].getQuantity()");
        // Find location of subitem and merge this.
        mergeSubItems(new_item);
        new_item.done = true;
        return true;
    }

    fun findSubSequence(subSequence: Int): Int
    {
        return items.indexOfFirst { it.subSequence == subSequence }
    }

    fun mergeSubItems(newItem: CSortedItem)
    {
        newItem.items.drop(1).forEach { newSubItem ->
            val existingIndex = findSubSequence(newSubItem.subSequence)
            if (existingIndex != -1)
            {
                items[existingIndex].merge(newSubItem)
            } else
            {
                items.add(newSubItem)
            }
        }
    }

    /** Find if the item is similar */
    fun isPossibleToMerge(new_item: CSortedItem, is_kitchen_printout: Boolean): Boolean
    {
        if (done != new_item.done)
        {
            return false;
        }
        val item0 = items[0]
        val new_item0 = new_item.items[0]

        if (item0.sequence != new_item0.sequence)
        {
            return false;
        }
        if (item0.group != new_item0.group)
        {
            return false;
        }
        if (item0.level != new_item0.level)
        {
            return false;
        }
        if (item0.menuItemId != new_item0.menuItemId)
        {
            return false;
        }
        if (item0.twinItemId != new_item0.twinItemId)
        {
            return false;
        }
        if (item0.getUnitPrice() != new_item0.getUnitPrice() && !is_kitchen_printout)
        {
            return false;
        }
        if (item0.originalAmount != new_item0.originalAmount && is_kitchen_printout)
        {
            return false;
        }
        return true;
    }

    fun mergeSame(
        newItem: CSortedItem, compareStructure: Boolean, isKitchenPrintout: Boolean,
        lowestTimeFrame: ETimeFrameIndex, highestTimeFrame: ETimeFrameIndex
    ): Boolean
    {
        val mergeOnlyPortionChanges = false
        if (items[0].sequence == newItem.items[0].sequence)
        {
            return performMerge(newItem)
        }
        if (compareStructure && isSimilarItemStructureAndOrder(
                newItem,
                isKitchenPrintout, mergeOnlyPortionChanges,
                lowestTimeFrame, highestTimeFrame
            )
        )
        {
            return performMerge(newItem)
        }
        return false
    }

    private fun performMerge(newItem: CSortedItem): Boolean
    {
        items[0].merge(newItem.items[0])
        mergeSubItems(newItem)
        newItem.done = true
        return true
    }

    fun getLevel(): EOrderLevel = items[0].level
    fun size(): Int = items.size
    operator fun get(index: Int): CItem = items[index]

    fun eraseEmpty(): Boolean
    {
        var changed = false
        val iterator = items.listIterator(1) // Skip main item
        while (iterator.hasNext())
        {
            val item = iterator.next()
            if (item.getQuantity() == 0)
            {
                if (items[0].timeFrameId.index < item.timeFrameId.index)
                {
                    items[0].timeFrameId = item.timeFrameId
                }
                iterator.remove()
                changed = true
            }
        }
        return changed
    }

    fun getTotal(): CMoney
    {
        var total = CMoney(0)
        for (item in items)
        {
            total = total + item.getTotal()
        }
        return total
    }

    fun getTotalWithoutStatiegeld(): CMoney
    {
        var total = CMoney(0)
        for (item in items)
        {
            total = total + item.getTotalWithoutStatiegeld()
        }
        return total
    }

    fun getItemLevel(): EOrderLevel = items[0].level
    fun getQuantity(): Int = items[0].getQuantity()
    fun getSign(): Int = when
    {
        items[0].getQuantity() < 0 -> -1
        items[0].getQuantity() > 0 -> 1
        else -> 0
    }

    fun getMenuItemId(): Int = items[0].menuItemId
    fun getPositionFactor(): Int = items[0].page * 1000000
    fun getSequence(): Int = items[0].sequence
    fun setSequence(seq: Int)
    {
        for (item in items) item.sequence = seq
    }

    fun increaseSequence()
    {
        for (item in items) item.sequence += 1
    }

    fun getOrder(): Int = items[0].subSequence
    fun getTimeFrameIndex(): ETimeFrameIndex = items[0].timeFrameId
    fun isItemGroup(): Boolean = items[0].level == EOrderLevel.LEVEL_ITEMGROUP

    fun isSameTimeFrame(other: CSortedItem): Boolean
    {
        if (size() != other.size()) return false
        return items.zip(other.items).all { (a, b) -> a.timeFrameId == b.timeFrameId }
    }

    fun undoTimeFrame(timeFrameId: ETimeFrameIndex)
    {
        val iterator = items.listIterator(1)
        while (iterator.hasNext())
        {
            val item = iterator.next()
            if (item.timeFrameId == timeFrameId)
            {
                iterator.remove()
            }
        }
    }

    fun isSimilarItemStructureAndOrder(
        new_item: CSortedItem, is_kitchen_printout: Boolean,
        merge_only_same_sequence: Boolean,
        lowestTimeFrame: ETimeFrameIndex,
        highestTimeFrame: ETimeFrameIndex
    ): Boolean
    {
        if (done != new_item.done)
        {
            return false;
        }
        val item0 = items[0];
        val new_item0 = new_item.items[0]

        if (merge_only_same_sequence)
        {
            if (item0.sequence != new_item0.sequence)
            {
                return false;
            }
        }
        if (item0.group != new_item0.group)
        {
            return false;
        }
        if (item0.level != new_item0.level)
        {
            return false;
        }
        if (item0.menuItemId != new_item0.menuItemId)
        {
            return false;
        }
        if (item0.twinItemId != new_item0.twinItemId)
        {
            return false;
        }
        if (item0.getUnitPrice() != new_item0.getUnitPrice() && !is_kitchen_printout)
        {
            return false;
        }
        if (item0.originalAmount != new_item0.originalAmount && is_kitchen_printout)
        {
            return false;
        }
        if (item0.getQuantity() < 0 && new_item0.getQuantity() >= 0
            && item0.parts != new_item0.parts
        )
        {
            return false;
        }
        if (item0.sequence == new_item0.sequence)
        {
            if (!is_kitchen_printout)
            {
                return true;
            }
            var first = true;
            for (item in items)
            {
                if (item.timeFrameId.isBetween(lowestTimeFrame, highestTimeFrame))
                {
                    first = false;
                    break;
                }
            }
            var second = true;

            for (item in new_item)
            {
                if (item.timeFrameId.isBetween(lowestTimeFrame, highestTimeFrame))
                {
                    second = false
                    break
                }
            }
            if (first == second)
            {
                return true;
            }
        }
        if (items.size == 1 && new_item.items.size == 1)
        {
            return true;
        }
        // Size only matters when it's not a bill
        if (items.size != new_item.items.size)
        {
            return false;
        }
        if (getQuantity() == 0 || new_item.getQuantity() == 0)
        {
            return false;
        }
        val multiple = getQuantity() / new_item.getQuantity()

        for (n in 1..items.size)
        {
            if (items[n].menuItemId != new_item.items[n].menuItemId)
            {
                return false;
            }
            if (items[n].twinItemId != new_item.items[n].twinItemId)
            {
                return false;
            }
            if (items[n].parts != new_item.items[n].parts)
            {
                return false;
            }
            if (items[n].getUnitPrice() != new_item.items[n].getUnitPrice())
            {
                return false;
            }
            if (items[n].originalAmount != new_item.items[n].originalAmount)
            {
                return false;
            }
            if (items[n].getQuantity() != new_item.items[n].getQuantity() * multiple)
            {
                return false;
            }
        }
        return true;
    }

    fun getPrinterSpices(language: ETaal, perItem: Boolean): String
    {
        val builder = StringBuilder()
        var count = 0
        val mainQuantity = items[0].getQuantity()

        items.drop(1).forEach { item ->
            if (item.getQuantity() != 0 && (item.level == EOrderLevel.LEVEL_SPICES ||
                   item.level == EOrderLevel.LEVEL_SUB_SPICES)
            )
            {
                if (count++ == 0) builder.append(startText) else builder.append(nextText)

                var q = item.getQuantity()
                if (perItem && mainQuantity != 0)
                {
                    q = if (q > 0) q / mainQuantity else -(-q / mainQuantity)
                }

                if (q != 1 || mainQuantity != 1)
                {
                    builder.append(q)
                }

                builder.append(
                    if (language.isChinese())
                        item.chinesePrinterName else item.localPrinterName
                )
            }
        }
        if (count > 0) builder.append(endText)
        return builder.toString()
    }

    /*----------------------------------------------------------------------------*/
    /** @brief Get string with all the extras. */
    fun getPrinterExtras(language: ETaal, perItem: Boolean): String
    {
        val builder = StringBuilder()
        var count = 0
        val mainQuantity = items[0].getQuantity()

        items.drop(1).forEach { item ->
            if (item.getQuantity() != 0 && (item.level == EOrderLevel.LEVEL_EXTRA ||
                   item.level == EOrderLevel.LEVEL_SUB_EXTRA)
            )
            {
                if (count++ == 0) builder.append(startText) else builder.append(nextText)

                var q = item.getQuantity()
                if (perItem && mainQuantity != 0)
                {
                    q = if (q > 0) q / mainQuantity else -(-q / mainQuantity)
                }

                if (q != 1 || mainQuantity != 1)
                {
                    builder.append(q)
                }

                builder.append(
                    if (language.isChinese())
                        item.chinesePrinterName else item.localPrinterName
                )
            }
        }

        if (count > 0) builder.append(endText)
        return builder.toString()
    }

    fun calculateRow(unit: Int): ETreeRow
    {
        if (unit !in items.indices) return ETreeRow.TREE_ONLY_ITEM

        val hasMoreSubItems = items.drop(unit + 1).any {
            it.level == EOrderLevel.LEVEL_EXTRA || it.level == EOrderLevel.LEVEL_SPICES
        }

        return when (items[unit].level)
        {
            EOrderLevel.LEVEL_EXTRA, EOrderLevel.LEVEL_SPICES ->
            {
                when
                {
                    unit == items.lastIndex -> ETreeRow.TREE_LAST_ONLY_SUBITEM
                    hasMoreSubItems -> ETreeRow.TREE_NEXT_SUBITEM
                    else -> ETreeRow.TREE_LAST_SUBITEM
                }
            }
            // Other cases...
            else -> ETreeRow.TREE_ONLY_ITEM
        }
    }

    fun mainItem(): CItem = items[0]
}