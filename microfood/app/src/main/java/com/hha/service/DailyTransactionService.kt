package com.hha.service

import com.hha.common.ClientOrdersType
import com.hha.common.Empty
import com.hha.common.Money
import com.hha.common.SortOrders
import com.hha.common.TransType
import com.hha.common.TransactionData
import com.hha.daily.transaction.CloseTransactionRequest
import com.hha.daily.transaction.CloseWokRequest
import com.hha.daily.transaction.CustomerId
import com.hha.daily.transaction.DailyTransactionServiceGrpcKt
import com.hha.daily.transaction.DaysList
import com.hha.daily.transaction.DuplicationRequest
import com.hha.daily.transaction.FindArchivedTransactionsRequest
import com.hha.daily.transaction.FindArchivedTransactionsResponse
import com.hha.daily.transaction.FindClosedOrderRequest
import com.hha.daily.transaction.FindDaysRequest
import com.hha.daily.transaction.FindOpenTableRequest
import com.hha.daily.transaction.FindSortedTransactionsRequest
import com.hha.daily.transaction.FindTransactionsRequest
import com.hha.daily.transaction.GetStatusResponse
import com.hha.daily.transaction.InsertTransactionRequest
import com.hha.daily.transaction.IsRechaudResponse
import com.hha.daily.transaction.IsShopResponse
import com.hha.daily.transaction.IsTakeawayBagResponse
import com.hha.daily.transaction.IsTakeawayPhoneResponse
import com.hha.daily.transaction.IsTakeawayPriceResponse
import com.hha.daily.transaction.IsTakeawayResponse
import com.hha.daily.transaction.IsTakeawaySitinResponse
import com.hha.daily.transaction.IsTelephoneResponse
import com.hha.daily.transaction.LatestTransactionNumber
import com.hha.daily.transaction.LatestTransactionRequest
import com.hha.daily.transaction.MinutesResponse
import com.hha.daily.transaction.PersonId
import com.hha.daily.transaction.RenameTableRequest
import com.hha.daily.transaction.SetCustomerRequest
import com.hha.daily.transaction.SetDiscountRequest
import com.hha.daily.transaction.SetMessageRequest
import com.hha.daily.transaction.SetNameRequest
import com.hha.daily.transaction.SetRfidKeyIdRequest
import com.hha.daily.transaction.SetStatusRequest
import com.hha.daily.transaction.SetTipsRequest
import com.hha.daily.transaction.SortOnTime
import com.hha.daily.transaction.TimeFrameIndex
import com.hha.daily.transaction.TimeFramesPerTransactionList
import com.hha.daily.transaction.TransactionId
import com.hha.daily.transaction.TransactionIdList
import com.hha.daily.transaction.TransactionList
import com.hha.daily.transaction.TransactionMinutes
import com.hha.daily.transaction.TransactionTime
import com.hha.daily.transaction.TransTypeRequest

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class DailyTransactionService(channel: ManagedChannel) : BaseGrpcService<DailyTransactionServiceGrpcKt.DailyTransactionServiceCoroutineStub>(channel) {
    override val stub: DailyTransactionServiceGrpcKt.DailyTransactionServiceCoroutineStub by lazy {
        DailyTransactionServiceGrpcKt.DailyTransactionServiceCoroutineStub(channel)
    }

    fun cleanTransaction(transactionId: Int): Boolean = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.cleanTransaction(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun closeTransaction(transactionId: Int, why: ClientOrdersType): Boolean = runBlocking {
        try {
            val request = CloseTransactionRequest.newBuilder()
                .setTransactionId(transactionId)
                .setWhy(why)
                .build()
            stub.closeTransaction(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun closeWokTablesAfterEndTime(removeTime: Int): Boolean = runBlocking {
        try {
            val request = CloseWokRequest.newBuilder()
                .setRemoveTime(removeTime)
                .build()
            stub.closeWokTablesAfterEndTime(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun copyEndTimeFromFloorplan(transactionId: Int, minutes: Int): Boolean = runBlocking {
        try {
            val request = TransactionMinutes.newBuilder()
                .setTransactionId(transactionId)
                .setMinutes(minutes)
                .build()
            stub.copyEndTimeFromFloorplan(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun copyNegativeTransaction(transactionId: Int): Int? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.copyNegativeTransaction(request).transactionId
        } catch (e: Exception) {
            null
        }
    }

    fun deleteTransaction(transactionId: Int): Boolean = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.deleteTransaction(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun duplicateTransaction(transactionId: Int, personId: Int, orderType: ClientOrdersType): Int? = runBlocking {
        try {
            val request = DuplicationRequest.newBuilder()
                .setTransactionId(transactionId)
                .setPersonId(personId)
                .setOrderType(orderType)
                .build()
            stub.duplicateTransaction(request).transactionId
        } catch (e: Exception) {
            null
        }
    }

    fun findArchivedTransactions(rfidKeyId: Int, onlyOld: Boolean): FindArchivedTransactionsResponse? = runBlocking {
        try {
            val request = FindArchivedTransactionsRequest.newBuilder()
                .setRfidKeyId(rfidKeyId)
                .setOnlyOld(onlyOld)
                .build()
            stub.findArchivedTransactions(request)
        } catch (e: Exception) {
            null
        }
    }

    fun findCashOrderTotal(): Money? = runBlocking {
        try {
            stub.findCashOrderTotal(Empty.getDefaultInstance())
        } catch (e: Exception) {
            null
        }
    }

    fun findClosedOrderFromKey(rfidKeyId: Int, onlyOld: Int): Int? = runBlocking {
        try {
            val request = FindClosedOrderRequest.newBuilder()
                .setRfidKeyId(rfidKeyId)
                .setOnlyOld(onlyOld)
                .build()
            stub.findClosedOrderFromKey(request).transactionId
        } catch (e: Exception) {
            null
        }
    }

    fun findDays(allRfidKeyIds: Boolean, rfidKeyIds: List<Int>): DaysList? = runBlocking {
        try {
            val request = FindDaysRequest.newBuilder()
                .setAllRfidKeyIds(allRfidKeyIds)
                .addAllRfidKeyId(rfidKeyIds)
                .build()
            stub.findDays(request)
        } catch (e: Exception) {
            null
        }
    }

    fun findLatestTransaction(transType: TransType, separateNumbers: Boolean, maxRange: Int): Int = runBlocking {
        try {
            val request = LatestTransactionRequest.newBuilder()
                .setTransType(transType)
                .setSeparateNumbers(separateNumbers)
                .setMaxRange(maxRange)
                .build()
            stub.findLatestTransaction(request).number
        } catch (e: Exception) {
            0
        }
    }

    fun findMinutes(transactionId: Int): Int? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.findMinutes(request).minutes
        } catch (e: Exception) {
            null
        }
    }

    fun findNewPrepareTransactions(): TimeFramesPerTransactionList? = runBlocking {
        try {
            stub.findNewPrepareTransactions(Empty.getDefaultInstance())
        } catch (e: Exception) {
            null
        }
    }

    fun findOpenTable(name: String): Int? = runBlocking {
        try {
            val request = FindOpenTableRequest.newBuilder()
                .setName(name)
                .build()
            stub.findOpenTable(request).transactionId
        } catch (e: Exception) {
            null
        }
    }

    fun findOpenTransactionWithCustomer(customerId: Int): Int? = runBlocking {
        try {
            val request = CustomerId.newBuilder()
                .setCustomerId(customerId)
                .build()
            stub.findOpenTransactionWithCustomer(request).transactionId
        } catch (e: Exception) {
            null
        }
    }

    fun findSortedTransactions(sortOrders: SortOrders): List<Int> = runBlocking {
        try {
            val request = FindSortedTransactionsRequest.newBuilder()
                .setSortOrders(sortOrders)
                .build()
            stub.findSortedTransactions(request).transactionIdList
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun findTransactions(allRfidKeyIds: Boolean, rfidKeyIds: List<Int>, day: String, sortOrders: SortOrders): List<Int> = runBlocking {
        try {
            val request = FindTransactionsRequest.newBuilder()
                .setAllRfidKeyIds(allRfidKeyIds)
                .addAllRfidKeyId(rfidKeyIds)
                .setDay(day)
                .setSortOrders(sortOrders)
                .build()
            stub.findTransactions(request).transactionIdList
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getElapsedMinutes(transactionId: Int, timeFrameIndex: Int): Int? = runBlocking {
        try {
            val request = TimeFrameIndex.newBuilder()
                .setTransactionId(transactionId)
                .setTimeFrameIndex(timeFrameIndex)
                .build()
            stub.getElapsedMinutes(request).minutes
        } catch (e: Exception) {
            null
        }
    }

    fun getStatus(transactionId: Int): ClientOrdersType? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.getStatus(request).type
        } catch (e: Exception) {
            null
        }
    }

    fun getTotal(transactionId: Int): Money? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.getTotal(request)
        } catch (e: Exception) {
            null
        }
    }

    fun getWaiterTotal(personId: Int): Money? = runBlocking {
        try {
            val request = PersonId.newBuilder()
                .setPersonId(personId)
                .build()
            stub.getWaiterTotal(request)
        } catch (e: Exception) {
            null
        }
    }

    fun isTakeaway(transactionId: Int): Boolean? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.isTakeaway(request).isTakeaway
        } catch (e: Exception) {
            null
        }
    }

    fun isRechaud(transactionId: Int): Boolean? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.isRechaud(request).isRechaud
        } catch (e: Exception) {
            null
        }
    }

    fun isTakeawayBag(transactionId: Int): Boolean? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.isTakeawayBag(request).isTakeawayBag
        } catch (e: Exception) {
            null
        }
    }

    fun isTakeawaySitin(transactionId: Int): Boolean? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.isTakeawaySitin(request).isTakeawaySitin
        } catch (e: Exception) {
            null
        }
    }

    fun isTakeawayPrice(transactionId: Int): Boolean? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.isTakeawayPrice(request).isTakeawayPrice
        } catch (e: Exception) {
            null
        }
    }

    fun isTelephone(transactionId: Int): Boolean? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.isTelephone(request).isTelephone
        } catch (e: Exception) {
            null
        }
    }

    fun isTakeawayPhone(transactionId: Int): Boolean? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.isTakeawayPhone(request).isTakeawayPhone
        } catch (e: Exception) {
            null
        }
    }

    fun isShop(transactionId: Int): Boolean? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.isShop(request).isShop
        } catch (e: Exception) {
            null
        }
    }

    fun insertTransaction(tableName: String, customerId: Int, rfidKeyId: Short, transType: TransType): Int? =
        runBlocking {
        try {
            val request = InsertTransactionRequest.newBuilder()
                .setTableName(tableName)
                .setRfidKeyId(rfidKeyId.toInt())
                .setTransType(transType)
                .setCustomerId(customerId)
                .build()
            stub.insertTransaction(request).transactionId
        } catch (e: Exception) {
            null
        }
    }

    fun listOpen(): TransactionList? = runBlocking {
        try {
            stub.listOpen(Empty.getDefaultInstance())
        } catch (e: Exception) {
            null
        }
    }

    fun listAll(sortOnTime: Boolean): TransactionList? = runBlocking {
        try {
            val request = SortOnTime.newBuilder()
                .setSortOnTime(sortOnTime)
                .build()
            stub.listAll(request)
        } catch (e: Exception) {
            null
        }
    }

    fun renameTableName(oldName: String, newName: String): Boolean = runBlocking {
        try {
            val request = RenameTableRequest.newBuilder()
                .setOldName(oldName)
                .setNewName(newName)
                .build()
            stub.renameTableName(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun selectTransactionId(transactionId: Int): TransactionData? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.selectTransactionId(request)
        } catch (e: Exception) {
            null
        }
    }

    fun setArchived(transactionId: Int): Boolean = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.setArchived(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setCustomer(transactionId: Int, customerId: Int): Boolean = runBlocking {
        try {
            val request = SetCustomerRequest.newBuilder()
                .setTransactionId(transactionId)
                .setCustomerId(customerId)
                .build()
            stub.setCustomer(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setCustomerTime(transactionId: Int, timestamp: String): Boolean = runBlocking {
        try {
            val request = TransactionTime.newBuilder()
                .setTransactionId(transactionId)
                .setTimestamp(timestamp)
                .build()
            stub.setCustomerTime(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setDiscount(
        transactionId: Int,
        discountHigh: Int,
        discountLow: Int,
        discountTaxFree: Int,
        factorHigh: Double,
        factorLow: Double
    ): Boolean = runBlocking {
        try {
            val request = SetDiscountRequest.newBuilder()
                .setTransactionId(transactionId)
                .setDiscountHigh(discountHigh)
                .setDiscountLow(discountLow)
                .setDiscountTaxFree(discountTaxFree)
                .setFactorHigh(factorHigh)
                .setFactorLow(factorLow)
                .build()
            stub.setDiscount(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setMessage(transactionId: Int, message: String): Boolean = runBlocking {
        try {
            val request = SetMessageRequest.newBuilder()
                .setTransactionId(transactionId)
                .setMessage(message)
                .build()
            stub.setMessage(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setName(transactionId: Int, name: String): Boolean = runBlocking {
        try {
            val request = SetNameRequest.newBuilder()
                .setTransactionId(transactionId)
                .setName(name)
                .build()
            stub.setName(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setRfidKeyId(transactionId: Int, newRfidKeyId: Short): Boolean = runBlocking {
        try {
            val request = SetRfidKeyIdRequest.newBuilder()
                .setTransactionId(transactionId)
                .setNewRfidKeyId(newRfidKeyId.toInt())
                .build()
            stub.setRfidKeyId(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setStatus(transactionId: Int, status: ClientOrdersType): Boolean = runBlocking {
        try {
            val request = SetStatusRequest.newBuilder()
                .setTransactionId(transactionId)
                .setStatus(status)
                .build()
            stub.setStatus(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setTime(transactionId: Int, timestamp: String): Boolean = runBlocking {
        try {
            val request = TransactionTime.newBuilder()
                .setTransactionId(transactionId)
                .setTimestamp(timestamp)
                .build()
            stub.setTime(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setTips(
        transactionId: Int,
        tipsHigh: Money,
        tipsLow: Money,
        tipsTaxFree: Money
    ): Boolean = runBlocking {
        try {
            val request = SetTipsRequest.newBuilder()
                .setTransactionId(transactionId)
                .setTipsHigh(tipsHigh)
                .setTipsLow(tipsLow)
                .setTipsTaxFree(tipsTaxFree)
                .build()
            stub.setTips(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setTransType(transactionId: Int, transType: TransType): Boolean = runBlocking {
        try {
            val request = TransTypeRequest.newBuilder()
                .setTransactionId(transactionId)
                .setTransType(transType)
                .build()
            stub.setTransType(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateTotal(transactionId: Int): Boolean = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.updateTotal(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateWokOpenOrOpenPaid(): Boolean = runBlocking {
        try {
            stub.updateWokOpenOrOpenPaid(Empty.getDefaultInstance())
            true
        } catch (e: Exception) {
            false
        }
    }
}