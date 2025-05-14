package com.hha.service

import android.os.Build
import androidx.annotation.RequiresApi
import com.hha.archive.transaction.*
import com.hha.archive.transaction.ArchiveTransactionServiceGrpcKt
import com.hha.common.ClientOrdersType
import com.hha.common.Empty
import com.hha.common.Money
import com.hha.common.TransType
import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.ZoneId

class ArchiveTransactionService(channel: ManagedChannel) :
    BaseGrpcService<ArchiveTransactionServiceGrpcKt.ArchiveTransactionServiceCoroutineStub>(channel) {

    override val stub: ArchiveTransactionServiceGrpcKt.ArchiveTransactionServiceCoroutineStub by lazy {
        ArchiveTransactionServiceGrpcKt.ArchiveTransactionServiceCoroutineStub(channel)
    }

    fun getTransactionRange(date: Date): Range? = runBlocking {
        try {
            stub.getTransactionRange(date)
        } catch (e: Exception) {
            null
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

    fun findCustomerOpenTransactions(customerId: Int): List<CustomerTransaction> = runBlocking {
        try {
            val request = FindCustomerRequest.newBuilder()
                .setCustomerId(customerId)
                .build()
            stub.findCustomerOpenTransactions(request).customerTransactionList
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getStatiegeldItems(lowest: Int, highest: Int): Int = runBlocking {
        try {
            val request = TransactionRange.newBuilder()
                .setLowest(lowest)
                .setHighest(highest)
                .build()
            stub.getStatiegeldItems(request).nrItems
        } catch (e: Exception) {
            -1
        }
    }

    fun insertTransaction(
        tableName: String,
        rfidKeyId: Int,
        status: ClientOrdersType,
        startTime: String,
        endTime: String,
        customerId: Int,
        archived: Int,
        message: String,
        high: Money,
        low: Money,
        taxFree: Money,
        taxPercentageHigh: Float,
        taxPercentageLow: Float,
        transType: TransType
    ): Int? = runBlocking {
        try {
            val request = InsertTransactionRequest.newBuilder()
                .setTableName(tableName)
                .setRfidKeyId(rfidKeyId)
                .setStatus(status)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setCustomerId(customerId)
                .setArchived(archived)
                .setMessage(message)
                .setHigh(high)
                .setLow(low)
                .setTaxFree(taxFree)
                .setTaxPercentageHigh(taxPercentageHigh)
                .setTaxPercentageLow(taxPercentageLow)
                .setTransType(transType)
                .build()
            stub.insertTransaction(request).transactionId
        } catch (e: Exception) {
            null
        }
    }

    fun selectTransaction(transactionId: Int): TransactionData? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.selectTransactionId(request)
        } catch (e: Exception) {
            null
        }
    }

    fun getYearsTotals(): List<TotalPerTime> = runBlocking {
        try {
            // Note: getYearsTotals returns TotalTimeLowHighList, not TotalPerTimeList
            // You'll need to convert between these types
            val response = stub.getYearsTotals(Empty.getDefaultInstance())
            convertToTotalPerTimeList(response) // Implement this conversion
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun convertToTotalPerTimeList(response: TotalTimeLowHighList): List<TotalPerTime> {
        // Implement conversion from TotalTimeLowHigh to TotalPerTime
        return response.totalList.map { timeLowHigh ->
            TotalPerTime.newBuilder()
                .setTimestamp(timeLowHigh.timestamp)
                // Set other fields with appropriate conversions/default values
                .build()
        }
    }

    fun getMonthTotals(year: Int, month: Int): List<TotalPerTime> = runBlocking {
        try {
            val request = Date.newBuilder()
                .setYear(year)
                .setMonth(month)
                .build()
            stub.getMonthTotals(request).totalList
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun copyTransaction(table: String, transactionId: Int): Boolean = runBlocking {
        try {
            val request = CopyTransactionRequest.newBuilder()
                .setTable(table)
                .setTransactionId(transactionId)
                .build()
            stub.copyTransaction(request)
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

    fun getUnusedDays(): List<YearMonthDay> = runBlocking {
        try {
            stub.findUnusedDays(Empty.getDefaultInstance()).rawDateList
        } catch (e: Exception) {
            emptyList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getLowestTimestamp(): Long? = runBlocking {
        try {
            val ts = stub.getLowestTimeStamp(Empty.getDefaultInstance())
            LocalDateTime.of(
                ts.year,
                ts.month,
                ts.day,
                ts.hour,
                ts.minute,
                ts.second
            ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            null
        }
    }

    // Additional methods for other RPC calls would follow the same pattern
    // ...
}
