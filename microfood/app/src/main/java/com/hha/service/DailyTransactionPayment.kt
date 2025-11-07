package com.hha.service

import com.hha.common.Empty
import com.hha.common.Payed
import com.hha.common.PaymentMethod
import com.hha.common.PaymentStatus
import com.hha.daily.payment.AddPaymentRequest
import com.hha.daily.payment.AddPaymentRequest2
import com.hha.daily.payment.CancelPaymentRequest
import com.hha.daily.payment.CancelPaymentsRequest2
import com.hha.daily.payment.CancelPaymentsRequest3
import com.hha.daily.payment.DailyTransactionPaymentServiceGrpcKt
import com.hha.daily.payment.DuplicateTransactionRequest
import com.hha.daily.payment.PartialIndex
import com.hha.daily.payment.PartialTotal
import com.hha.daily.payment.PartialTotalRequest
import com.hha.daily.payment.Payment
import com.hha.daily.payment.PaymentDetails
import com.hha.daily.payment.PaymentDetailsList
import com.hha.daily.payment.PaymentList
import com.hha.daily.payment.PaymentMap
import com.hha.daily.payment.SetCashTotalRequest
import com.hha.daily.payment.SetTimesRequest
import com.hha.daily.payment.TransactionId
import com.hha.daily.payment.TransactionPaymentRequest
import com.hha.daily.payment.UpdatePaymentMethodRequest
import com.hha.daily.payment.UpdatePaymentRequest

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class DailyTransactionPaymentService(channel: ManagedChannel) : BaseGrpcService<DailyTransactionPaymentServiceGrpcKt.DailyTransactionPaymentServiceCoroutineStub>(channel) {
    override val stub: DailyTransactionPaymentServiceGrpcKt.DailyTransactionPaymentServiceCoroutineStub by lazy {
        DailyTransactionPaymentServiceGrpcKt.DailyTransactionPaymentServiceCoroutineStub(channel)
    }

    fun addPayment(transactionId: Int, payment: Payment, paymentIndex: Int): Boolean = runBlocking {
        try {
            val request = AddPaymentRequest.newBuilder()
                .setTransactionId(transactionId)
                .setPayment(payment)
                .setPaymentIndex(paymentIndex)
                .build()
            stub.addPayment(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun addPayment2(transactionId: Int, paymentMethod: PaymentMethod, total: Int, endTime: String, isPaid: Payed): Boolean = runBlocking {
        try {
            val request = AddPaymentRequest2.newBuilder()
                .setTransactionId(transactionId)
                .setPaymentMethod(paymentMethod)
                .setTotal(total)
                .setEndTime(endTime)
                .setIsPaid(isPaid)
                .build()
            stub.addPayment2(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun createPartialIndex(transactionId: Int): Int? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.createPartialIndex(request).partialIndex
        } catch (e: Exception) {
            null
        }
    }

    fun cancelMoneyPayments(transactionId: Int): Boolean = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.cancelMoneyPayments(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun cancelPayment(transactionId: Int, index: Int, status: PaymentStatus): Boolean = runBlocking {
        try {
            val request = CancelPaymentRequest.newBuilder()
                .setTransactionId(transactionId)
                .setIndex(index)
                .setStatus(status)
                .build()
            stub.cancelPayment(request)
            true
        } catch (e: Exception) {
            false
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

    fun cancelPayments(transactionId: Int): Boolean = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.cancelPayments(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun cancelPayments2(transactionId: Int, payStatus: PaymentStatus): Boolean = runBlocking {
        try {
            val request = CancelPaymentsRequest2.newBuilder()
                .setTransactionId(transactionId)
                .setPayStatus(payStatus)
                .build()
            stub.cancelPayments2(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun cancelPayments3(transactionId: Int, partialIndex: Int, onlyUnfinishedPayments: Boolean): Boolean = runBlocking {
        try {
            val request = CancelPaymentsRequest3.newBuilder()
                .setTransactionId(transactionId)
                .setPartialIndex(partialIndex)
                .setOnlyUnfinishedPayments(onlyUnfinishedPayments)
                .build()
            stub.cancelPayments3(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun cancelTransaction(transactionId: Int): Boolean = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.cancelTransaction(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun duplicateTransaction(transactionId: Int, newTransactionId: Int): Boolean = runBlocking {
        try {
            val request = DuplicateTransactionRequest.newBuilder()
                .setTransactionId(transactionId)
                .setNewTransactionId(newTransactionId)
                .build()
            stub.duplicateTransaction(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun findAllTransactionsSorted(): PaymentMap? = runBlocking {
        try {
            stub.findAllTransactionsSorted(Empty.getDefaultInstance())
        } catch (e: Exception) {
            null
        }
    }

    fun findAllTransactionsUnsorted(): PaymentMap? = runBlocking {
        try {
            stub.findAllTransactionsUnsorted(Empty.getDefaultInstance())
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

    fun getNewPaymentIndex(transactionId: Int): Int = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.getNewPaymentIndex(request).partialIndex
        } catch (e: Exception) {
            1
        }
    }

    fun getHighestPaymentIndex(transactionId: Int): Int? = runBlocking {
        try {
            val request = TransactionId.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.getHighestPaymentIndex(request).partialIndex
        } catch (e: Exception) {
            null
        }
    }

    fun getPartialTotal(transactionId: Int, partialIndex: Int, paymentMethod: PaymentMethod): Int = runBlocking {
        try {
            val request = PartialTotalRequest.newBuilder()
                .setTransactionId(transactionId)
                .setPartialIndex(partialIndex)
                .setPaymentMethod(paymentMethod)
                .build()
            stub.getPartialTotal(request).total
        } catch (e: Exception) {
            0
        }
    }

    fun getTransactionPaymentTotals(transactionId: Int, includeCancelledPayments: Boolean)
    : PaymentDetailsList? = runBlocking {
        try {
            val request = TransactionPaymentRequest.newBuilder()
                .setTransactionId(transactionId)
                .setIncludeCancelledPayments(includeCancelledPayments)
                .build()
            stub.getTransactionPaymentTotals(request)
        } catch (e: Exception) {
            null
        }
    }

    fun getTransactionPaymentsList(transactionId: Int, includeCancelledPayments: Boolean): PaymentList? = runBlocking {
        try {
            val request = TransactionPaymentRequest.newBuilder()
                .setTransactionId(transactionId)
                .setIncludeCancelledPayments(includeCancelledPayments)
                .build()
            stub.getTransactionPaymentsList(request)
        } catch (e: Exception) {
            null
        }
    }

    fun selectTransactionId(transactionId: Int, includeCancelledPayments: Boolean): PaymentDetailsList? = runBlocking {
        try {
            val request = TransactionPaymentRequest.newBuilder()
                .setTransactionId(transactionId)
                .setIncludeCancelledPayments(includeCancelledPayments)
                .build()
            stub.selectTransactionId(request)
        } catch (e: Exception) {
            null
        }
    }

    fun setCashTotal(transactionId: Int, clientId: Int, total: Int): Boolean = runBlocking {
        try {
            val request = SetCashTotalRequest.newBuilder()
                .setTransactionId(transactionId)
                .setClientId(clientId)
                .setTotal(total)
                .build()
            stub.setCashTotal(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setTimes(transactionId: Int, timestamp: String): Boolean = runBlocking {
        try {
            val request = SetTimesRequest.newBuilder()
                .setTransactionId(transactionId)
                .setTimestamp(timestamp)
                .build()
            stub.setTimes(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updatePaymentIndexMethod(transactionId: Int, paymentIndex: Int, newPaymentMethod: PaymentMethod): Boolean = runBlocking {
        try {
            val request = UpdatePaymentMethodRequest.newBuilder()
                .setTransactionId(transactionId)
                .setPaymentIndex(paymentIndex)
                .setNewPaymentMethod(newPaymentMethod)
                .build()
            stub.updatePaymentIndexMethod(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateTransactionPaymentTotal(transactionId: Int, total: Int): Boolean = runBlocking {
        try {
            val request = UpdatePaymentRequest.newBuilder()
                .setTransactionId(transactionId)
                .setTotal(total)
                .build()
            stub.updateTransactionPaymentTotal(request)
            true
        } catch (e: Exception) {
            false
        }
    }
}