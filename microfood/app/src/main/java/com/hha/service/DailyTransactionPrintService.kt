package com.hha.service

import com.hha.common.Empty
import com.hha.common.PrinterStatus
import com.hha.daily.print.DailyTransactionPrintServiceGrpcKt
import com.hha.daily.print.PrintTimeFrameRequest
import com.hha.daily.print.SetPrintedRequest
import com.hha.daily.print.TimeFrame
import com.hha.daily.print.VerifyResponse

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class DailyTransactionPrintService(channel: ManagedChannel) : BaseGrpcService<DailyTransactionPrintServiceGrpcKt.DailyTransactionPrintServiceCoroutineStub>(channel) {
    override val stub: DailyTransactionPrintServiceGrpcKt.DailyTransactionPrintServiceCoroutineStub by lazy {
        DailyTransactionPrintServiceGrpcKt.DailyTransactionPrintServiceCoroutineStub(channel)
    }

    fun findPrintTimeFrame(): TimeFrame? = runBlocking {
        try {
            stub.findPrintTimeFrame(Empty.getDefaultInstance())
        } catch (e: Exception) {
            null
        }
    }

    fun printTimeFrame(
        transactionId: Int,
        timeFrameIndex: Int,
        deviceId: Int,
        rfidKeyId: Int,
        prints: Int,
        tickets: Int,
        collectPrinter: Boolean,
        billPrinter: Boolean
    ): Boolean = runBlocking {
        try {
            val request = PrintTimeFrameRequest.newBuilder()
                .setTransactionId(transactionId)
                .setTimeFrameIndex(timeFrameIndex)
                .setDeviceId(deviceId)
                .setRfidKeyId(rfidKeyId)
                .setPrints(prints)
                .setTickets(tickets)
                .setCollectPrinter(collectPrinter)
                .setBillPrinter(billPrinter)
                .build()
            stub.printTimeFrame(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun verify(): Boolean? = runBlocking {
        try {
            stub.verify(Empty.getDefaultInstance()).verified
        } catch (e: Exception) {
            null
        }
    }

    fun setPrinted(jobId: Int, status: PrinterStatus): Boolean = runBlocking {
        try {
            val request = SetPrintedRequest.newBuilder()
                .setJobId(jobId)
                .setStatus(status)
                .build()
            stub.setPrinted(request)
            true
        } catch (e: Exception) {
            false
        }
    }
}