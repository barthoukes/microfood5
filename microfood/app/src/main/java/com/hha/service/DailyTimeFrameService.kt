package com.hha.service

import com.hha.common.Empty
import com.hha.common.TimeFrameList
import com.hha.common.Timestamp
import com.hha.common.CookingState
import com.hha.daily.timeframe.DailyTimeFrameServiceGrpcKt
import com.hha.daily.timeframe.DelayRequest
import com.hha.daily.timeframe.DeliverTimeRequest
import com.hha.daily.timeframe.EndTimeFrameRequest
import com.hha.daily.timeframe.InsertTimeFrameRequest
import com.hha.daily.timeframe.SelectTransactionRequest
import com.hha.daily.timeframe.StartTimeFrameRequest
import com.hha.daily.timeframe.TimeFrameIndex
import com.hha.daily.timeframe.TimeFramesSorted
import com.hha.daily.timeframe.TimestampRequest
import com.hha.daily.timeframe.TransactionId
import com.hha.daily.timeframe.TransactionIdTimeFrameIndexParam
import com.hha.daily.timeframe.TransactionToTransaction

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class DailyTimeFrameService(channel: ManagedChannel) : BaseGrpcService<DailyTimeFrameServiceGrpcKt.DailyTimeFrameServiceCoroutineStub>(channel) {
    override val stub: DailyTimeFrameServiceGrpcKt.DailyTimeFrameServiceCoroutineStub by lazy {
        DailyTimeFrameServiceGrpcKt.DailyTimeFrameServiceCoroutineStub(channel)
    }

    fun nextKitchenIndex(transactionId: Int, timeFrameIndex: Int): Int? = runBlocking {
        try {
            val request = TransactionIdTimeFrameIndexParam.newBuilder()
                .setTransactionId(transactionId)
                .setTimeFrameIndex(timeFrameIndex)
                .build()
            stub.nextKitchenIndex(request).timeFrameIndex
        } catch (e: Exception) {
            null
        }
    }

    fun findCookingTimeFrames(): TimeFrameList? = runBlocking {
        try {
            stub.findCookingTimeFrames(Empty.getDefaultInstance())
        } catch (e: Exception) {
            null
        }
    }

    fun findOpenTimeFrames(timeFrames: List<Int>, isSortOnTime: Boolean): TimeFrameList? = runBlocking {
        try {
            val request = TimeFramesSorted.newBuilder()
                .addAllTimeFrame(timeFrames.map {
                    TimeFrameIndex.newBuilder().setTimeFrameIndex(it).build()
                })
                .setIsSortOnTime(isSortOnTime)
                .build()
            stub.findOpenTimeFrames(request)
        } catch (e: Exception) {
            null
        }
    }

    fun setCooked(transactionId: Int, timeFrameIndex: Int): Boolean = runBlocking {
        try {
            val request = TransactionIdTimeFrameIndexParam.newBuilder()
                .setTransactionId(transactionId)
                .setTimeFrameIndex(timeFrameIndex)
                .build()
            stub.setCooked(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun copyTimeFrames(transactionId: Int, newTransactionId: Int): Boolean = runBlocking {
        try {
            val request = TransactionToTransaction.newBuilder()
                .setTransactionId(transactionId)
                .setNewTransactionId(newTransactionId)
                .build()
            stub.copyTimeFrames(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun insertTimeFrame(request: InsertTimeFrameRequest): Int? = runBlocking {
        try {
            stub.insertTimeFrame(request).timeFrameIndex
        } catch (e: Exception) {
            null
        }
    }

    fun getLatestTimeFrameIndex(transactionId: Int): Int? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.getLatestTimeFrameIndex(request).timeFrameIndex
        } catch (e: Exception) {
            null
        }
    }

    fun changeDeliverTime(request: DeliverTimeRequest): Boolean = runBlocking {
        try {
            stub.changeDeliverTime(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun endTimeFrame(request: EndTimeFrameRequest): Boolean = runBlocking {
        try {
            stub.endTimeFrame(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removeTimeFrame(transactionId: Int, timeFrameIndex: Int): Boolean = runBlocking {
        try {
            val request = TransactionIdTimeFrameIndexParam.newBuilder()
                .setTransactionId(transactionId)
                .setTimeFrameIndex(timeFrameIndex)
                .build()
            stub.removeTimeFrame(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun startTimeFrame(request: StartTimeFrameRequest): Boolean = runBlocking {
        try {
            stub.startTimeFrame(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun selectTransactionId(transactionId: Int, sortDescending: Boolean): TimeFrameList? = runBlocking {
        try {
            val request = SelectTransactionRequest.newBuilder()
                .setTransactionId(transactionId)
                .setSortDescending(sortDescending)
                .build()
            stub.selectTransactionId(request)
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

    fun findAllOpenTransactions(): TimeFrameList? = runBlocking {
        try {
            stub.findAllOpenTransactions(Empty.getDefaultInstance())
        } catch (e: Exception) {
            null
        }
    }

    fun insertNewTimeFrame(): Int? = runBlocking {
        try {
            stub.insertNewTimeFrame(Empty.getDefaultInstance()).timeFrameIndex
        } catch (e: Exception) {
            null
        }
    }

    fun setTimes(transactionId: Int, timestamp: String): Boolean = runBlocking {
        try {
            val request = TimestampRequest.newBuilder()
                .setTransactionId(transactionId)
                .setTimestamp(timestamp)
                .build()
            stub.setTimes(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun mergeTransactions(transactionId: Int, newTransactionId: Int): Boolean = runBlocking {
        try {
            val request = TransactionToTransaction.newBuilder()
                .setTransactionId(transactionId)
                .setNewTransactionId(newTransactionId)
                .build()
            stub.mergeTransactions(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setDelay(transactionId: Int, timeFrame: Int, delay: Int): Boolean = runBlocking {
        try {
            val request = DelayRequest.newBuilder()
                .setTransactionId(transactionId)
                .setTimeFrame(timeFrame)
                .setDelay(delay)
                .build()
            stub.setDelay(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getDeliverTime(transactionId: Int, timeFrameIndex: Int): Timestamp? = runBlocking {
        try {
            val request = TransactionIdTimeFrameIndexParam.newBuilder()
                .setTransactionId(transactionId)
                .setTimeFrameIndex(timeFrameIndex)
                .build()
            stub.getDeliverTime(request)
        } catch (e: Exception) {
            null
        }
    }
}
