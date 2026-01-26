package com.hha.service

import com.hha.common.Empty
import com.hha.common.ItemList
import com.hha.common.MenuCardId
import com.hha.common.Money
import com.hha.common.DeletedStatus
import com.hha.common.Item
import com.hha.common.ItemLocation
import com.hha.common.ItemSort
import com.hha.common.OrderLevel
import com.hha.common.Payed
import com.hha.common.PaymentStatus
import com.hha.common.SubItems
import com.hha.daily.item.CreateItemRequest
import com.hha.daily.item.CreateItemWithTimestampRequest
import com.hha.daily.item.DailyTransactionItemServiceGrpcKt
import com.hha.daily.item.DeleteTwinItemRequest
import com.hha.daily.item.EmptyTransactionRequest
import com.hha.daily.item.ExchangeSequencesRequest
import com.hha.daily.item.FindItemsRequest
import com.hha.daily.item.FindSequenceItemsRequest
import com.hha.daily.item.FindSequencesReply
import com.hha.daily.item.FindSequencesRequest
import com.hha.daily.item.GetAllDrinksTotalRequest
import com.hha.daily.item.GetAllKitchenTotalRequest
import com.hha.daily.item.InsertSequenceRequest
import com.hha.daily.item.IsEmptyResponse
import com.hha.daily.item.ItemGroupQuantityList
import com.hha.daily.item.NumberReply
import com.hha.daily.item.ReplaceListItemRequest
import com.hha.daily.item.SelectTransactionSort
import com.hha.daily.item.SetTwinQuantityRequest
import com.hha.daily.item.TransactionId
import com.hha.daily.item.TransactionIds
import com.hha.daily.item.TransactionLocationIndex
import com.hha.daily.item.TransactionSubItems
import com.hha.daily.item.TransactionTimeFrame
import com.hha.daily.item.TransactionTimeFrameDeviceWhy
import com.hha.daily.item.TransactionToTransaction
import com.hha.daily.item.UndoTimeFrameRequest
import com.hha.daily.item.createItemRequest
import com.hha.types.ETimeFrameIndex

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking
import kotlin.collections.mutableListOf

class DailyTransactionItemService(channel: ManagedChannel) : BaseGrpcService<DailyTransactionItemServiceGrpcKt.DailyTransactionItemServiceCoroutineStub>(channel) {
    override val stub: DailyTransactionItemServiceGrpcKt.DailyTransactionItemServiceCoroutineStub by lazy {
        DailyTransactionItemServiceGrpcKt.DailyTransactionItemServiceCoroutineStub(channel)
    }

    fun numberKitchenItems(transactionId: Int, timeFrameIndex: Short): Int = runBlocking {
        try {
            val request = TransactionTimeFrame.newBuilder()
                .setTransactionId(transactionId.toLong())
                .setTimeFrameIndex(timeFrameIndex.toInt())
                .build()
            stub.numberKitchenItems(request).number
        } catch (e: Exception) {
            0
        }
    }

    fun selectTimeFrame(transactionId: Long, timeFrameIndex: Int): ItemList? = runBlocking {
        try {
            val request = TransactionTimeFrame.newBuilder()
                .setTransactionId(transactionId)
                .setTimeFrameIndex(timeFrameIndex)
                .build()
            stub.selectTimeFrame(request)
        } catch (e: Exception) {
            null
        }
    }

    fun selectTransaction(transactionId: Int): ItemList? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.selectTransaction(request)
        } catch (e: Exception) {
            null
        }
    }

    fun selectTransactionId(transactionId: Int, sorted: ItemSort,
                            timeFrameId: Short, deviceId: Short)
    : ItemList? = runBlocking {
        try {
            val request = SelectTransactionSort.newBuilder()
                .setTransactionId(transactionId.toLong())
                .setSorted(sorted)
                .setTimeFrameId(timeFrameId.toInt())
                .setDeviceId(deviceId.toInt())
                .build()
            stub.selectTransactionId(request)
        } catch (e: Exception) {
            null
        }
    }

    fun createItem(
        itemId: Int,
        transactionId: Int,
        sequence: Int,
        subSequence: Int,
        subSubSequence: Int,
        quantity: Int,
        level: OrderLevel,
        taxGroup: Int,
        page: Int,
        parts: Int,
        unitPrice: Int,
        originalPrice: Int,
        originalHalfPrice: Int,
        tax: Double,
        locations: Int,
        timeFrameIndex: Int,
        deviceId: Short,
        clusterId: Short,
        isPaid: Payed,
        statiegeld: Int,
        deletedStatus: DeletedStatus = DeletedStatus.DELETE_NOT,
        deletedTimeFrame: Int = 0
    ): Int? = runBlocking {
        try {
        val request = CreateItemRequest.newBuilder()
            .setItemId(itemId.toLong())
            .setTransactionId(transactionId.toLong())
            .setSequence(sequence)
            .setSubSequence(subSequence)
            .setSubSubSequence(subSubSequence)
            .setQuantity(quantity)
            .setLevel(level)
            .setTaxGroup(taxGroup)
            .setPage(page)
            .setParts(parts)
            .setUnitPrice(unitPrice)
            .setOriginalPrice(originalPrice)
            .setOriginalHalfPrice(originalHalfPrice)
            .setTax(tax)
            .setLocations(locations)
            .setTimeFrameIndex(timeFrameIndex)
            .setDeviceId(deviceId.toInt())
            .setClusterId(clusterId.toInt())
            .setIsPaid(isPaid)
            .setStatiegeld(statiegeld)
            .setDeletedStatus(deletedStatus)
            .setDeletedTimeFrame(deletedTimeFrame)
            .build()
            stub.createItem(request).number
        } catch (e: Exception) {
            null
        }
    }
    fun createItem(request: CreateItemRequest): Int? = runBlocking {
        try {
            stub.createItem(request).number
        } catch (e: Exception) {
            null
        }
    }

    fun createItemWithTimestamp(request: CreateItemWithTimestampRequest): Int? = runBlocking {
        try {
            stub.createItemWithTimestamp(request).number
        } catch (e: Exception) {
            null
        }
    }

    fun finishPayments(transactionId: Int): Boolean = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.finishPayments(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun duplicateTransaction(transactionId: Int, newTransactionId: Int): Boolean = runBlocking {
        try {
            val request = TransactionToTransaction.newBuilder()
                .setTransactionId(transactionId)
                .setNewTransactionId(newTransactionId)
                .build()
            stub.duplicateTransaction(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteAll(transactionId: Long, timeFrameId: Int, deviceId: Int, deletedWhy: DeletedStatus): Boolean = runBlocking {
        try {
            val request = TransactionTimeFrameDeviceWhy.newBuilder()
                .setTransactionId(transactionId)
                .setTimeFrameId(timeFrameId)
                .setDeviceId(deviceId)
                .setDeletedWhy(deletedWhy)
                .build()
            stub.deleteAll(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun itemCount(transactionId: Long, timeFrameIndex: Int): Int? = runBlocking {
        try {
            val request = TransactionTimeFrame.newBuilder()
                .setTransactionId(transactionId)
                .setTimeFrameIndex(timeFrameIndex)
                .build()
            stub.itemCount(request).number
        } catch (e: Exception) {
            null
        }
    }

    fun itemSum(transactionId: Long, timeFrameIndex: Int): com.hha.daily.item.Money? = runBlocking {
        try {
            val request = TransactionTimeFrame.newBuilder()
                .setTransactionId(transactionId)
                .setTimeFrameIndex(timeFrameIndex)
                .build()
            stub.itemSum(request)
        } catch (e: Exception) {
            null
        }
    }

    fun updateItemToDatabase(item: Item): Boolean = runBlocking {
        try {
            stub.updateItemToDatabase(item)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getAllDrinksTotal(transactionId: Long, payStatus: PaymentStatus, isWithStatiegeld: Boolean): com.hha.daily.item.Money? = runBlocking {
        try {
            val request = GetAllDrinksTotalRequest.newBuilder()
                .setTransactionId(transactionId)
                .setPayStatus(payStatus)
                .setIsWithStatiegeld(isWithStatiegeld)
                .build()
            stub.getAllDrinksTotal(request)
        } catch (e: Exception) {
            null
        }
    }

    fun getAllKitchenTotal(transactionId: Int, payStatus: PaymentStatus, isWithStatiegeld: Boolean): com.hha.daily.item
        .Money? = runBlocking {
        try {
            val request = GetAllKitchenTotalRequest.newBuilder()
                .setTransactionId(transactionId.toLong())
                .setPayStatus(payStatus)
                .setIsWithStatiegeld(isWithStatiegeld)
                .build()
            stub.getAllKitchenTotal(request)
        } catch (e: Exception) {
            null
        }
    }

    fun getTimeFrameLocationTotal(transactionId: Long, index: Int, location: ItemLocation): com.hha.daily.item.Money? = runBlocking {
        try {
            val request = TransactionLocationIndex.newBuilder()
                .setTransactionId(transactionId)
                .setIndex(index)
                .setLocation(location)
                .build()
            stub.getTimeFrameLocationTotal(request)
        } catch (e: Exception) {
            null
        }
    }

    fun setTimeFrameLocation(transactionId: Long, index: Int, location: ItemLocation): ItemList? = runBlocking {
        try {
            val request = TransactionLocationIndex.newBuilder()
                .setTransactionId(transactionId)
                .setIndex(index)
                .setLocation(location)
                .build()
            stub.setTimeFrameLocation(request)
        } catch (e: Exception) {
            null
        }
    }

    fun deleteTransactionItems(transactionId: Int): Boolean = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.deleteTransactionItems(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setNegativeQuantityToRemovedItems(transactionId: Int): Boolean = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.setNegativeQuantityToRemovedItems(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun undoTimeFrame(transactionId: Long, timeFrameIndex: Int, deviceId: Short): Boolean = runBlocking {
        try {
            val request = UndoTimeFrameRequest.newBuilder()
                .setTransactionId(transactionId)
                .setTimeFrameIndex(timeFrameIndex)
                .setDeviceId(deviceId.toInt())
                .build()
            stub.undoTimeFrame(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun insertSequence(transactionId: Int, sequence: Int): Boolean = runBlocking {
        try {
            val request = InsertSequenceRequest.newBuilder()
                .setTransactionId(transactionId.toLong())
                .setSequence(sequence)
                .build()
            stub.insertSequence(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun select(transactionId: Long, subItems: SubItems): ItemList? = runBlocking {
        try {
            val request = TransactionSubItems.newBuilder()
                .setTransactionId(transactionId)
                .setSubItems(subItems)
                .build()
            stub.select(request)
        } catch (e: Exception) {
            null
        }
    }

    fun replaceListItem(
        transactionId: Long,
        sequence: Int,
        oldMenuItem: Int,
        newItemId: Int,
        unitPrice: Int,
        newTaxPercentage: Double
    ): Boolean = runBlocking {
        try {
            val request = ReplaceListItemRequest.newBuilder()
                .setTransactionId(transactionId)
                .setSequence(sequence)
                .setOldMenuItem(oldMenuItem)
                .setNewItemId(newItemId)
                .setUnitPrice(unitPrice)
                .setNewTaxPercentage(newTaxPercentage)
                .build()
            stub.replaceListItem(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setTwinQuantity(
        transactionId: Int,
        sequence: Int,
        subQuantity: Int,
        subSubQuantity: Int,
        quantity: Int,
        parts: Int
    ): Boolean = runBlocking {
        try {
            val request = SetTwinQuantityRequest.newBuilder()
                .setTransactionId(transactionId.toLong())
                .setSequence(sequence)
                .setSubQuantity(subQuantity)
                .setSubSubQuantity(subSubQuantity)
                .setQuantity(quantity)
                .setParts(parts)
                .build()
            stub.setTwinQuantity(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun deleteTwinItem(
        twinItemId: Long,
        transactionId: Int,
        sequence: Int,
        subSequence: Int,
        subSubSequence: Int,
        deletedWhy: DeletedStatus
    ): Boolean = runBlocking {
        try {
            val request = DeleteTwinItemRequest.newBuilder()
                .setTwinItemId(twinItemId)
                .setTransactionId(transactionId.toLong())
                .setSequence(sequence)
                .setSubSequence(subSequence)
                .setSubSubSequence(subSubSequence)
                .setDeletedWhy(deletedWhy)
                .build()
            stub.deleteTwinItem(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun exchangeSequences(transactionId: Long, sequence1: Int, sequence2: Int): Boolean = runBlocking {
        try {
            val request = ExchangeSequencesRequest.newBuilder()
                .setTransactionId(transactionId)
                .setSequence1(sequence1)
                .setSequence2(sequence2)
                .build()
            stub.exchangeSequences(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun findSequences(transactionId: Long, timeFrameLow: Int, timeFrameHigh: Int, itemLocations: Int): List<Int> = runBlocking {
        try {
            val request = FindSequencesRequest.newBuilder()
                .setTransactionId(transactionId.toInt())
                .setTimeFrameLow(timeFrameLow)
                .setTimeFrameHigh(timeFrameHigh)
                .setItemLocations(itemLocations)
                .build()
            stub.findSequences(request).outputList ?:emptyList<Int>()
            // FindSequencesReply?
        } catch (e: Exception) {
            emptyList<Int>()
        }
    }

    fun getExtraHalfHourTotal(transactionId: Int): com.hha.daily.item.Money? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.getExtraHalfHourTotal(request)
        } catch (e: Exception) {
            null
        }
    }

    fun findItems(allRfidKeyIds: Boolean, rfidKeyIds: List<Int>, date: String, deleted: Int, taxClusterId: Int): ItemGroupQuantityList? = runBlocking {
        try {
            val request = FindItemsRequest.newBuilder()
                .setAllRfidKeyIds(allRfidKeyIds)
                .addAllRfidKeyId(rfidKeyIds)
                .setDate(date)
                .setDeleted(deleted)
                .setTaxClusterId(taxClusterId)
                .build()
            stub.findItems(request)
        } catch (e: Exception) {
            null
        }
    }

    fun isEmpty(menuCardId: Int): Boolean? = runBlocking {
        try {
            val request = MenuCardId.newBuilder()
                .setMenuCardId(menuCardId)
                .build()
            stub.isEmpty(request).isEmpty
        } catch (e: Exception) {
            null
        }
    }

    fun isEmptyTransaction(transactionId: Int, excludeFrameId: Boolean): Boolean? = runBlocking {
        try {
            val request = EmptyTransactionRequest.newBuilder()
                .setTransactionId(transactionId)
                .setExcludeFrameId(excludeFrameId)
                .build()
            stub.isEmptyTransaction(request).isEmpty
        } catch (e: Exception) {
            null
        }
    }

    fun mergeTransactionItems(sourceOrder: Int, destinationOrder: Int): Boolean = runBlocking {
        try {
            val request = TransactionIds.newBuilder()
                .setSourceOrder(sourceOrder)
                .setDestinationOrder(destinationOrder)
                .build()
            stub.mergeTransactionItems(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun findSequenceItems(
        transactionId: Int,
        sequenceList: List<Int>,
        maxTimeFrameIndex: ETimeFrameIndex,
        negative: Boolean
    ): ItemList? = runBlocking {
        try {
            val request = FindSequenceItemsRequest.newBuilder()
                .setTransactionId(transactionId)
                .addAllSequenceList(sequenceList)
                .setMaxTimeFrameIndex(maxTimeFrameIndex.index.toInt())
                .setNegative(negative)
                .build()
            stub.findSequenceItems(request)
        } catch (e: Exception) {
            null
        }
    }
}
