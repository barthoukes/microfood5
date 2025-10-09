package com.hha.framework

import com.hha.daily.payment.PaymentDetailsList
import com.hha.daily.payment.PaymentList
import com.hha.grpc.GrpcServiceFactory
import com.hha.service.DailyTransactionPaymentService
import com.hha.types.CMoney
import com.hha.types.EPayed
import com.hha.types.EPaymentMethod
import com.hha.types.EPaymentStatus

data class CPaymentList(
    var m_transactionId: Int = 0,
    var m_valid: Boolean = false
) {
    val m_payments: MutableList<CPayment> = mutableListOf()

    // Add a payment to the list
    fun addPayment(payment: CPayment)
    {
        m_payments.add(payment)
    }

    public fun setTransactionId(transactionId: Int)
    {
        if (transactionId < 0)
        {
            m_payments.clear()
            m_transactionId = -1
            return
        }
        m_transactionId = transactionId
        val service: DailyTransactionPaymentService =
            GrpcServiceFactory.createDailyTransactionPaymentService()
        val details = service.getTransactionPaymentsList(transactionId, false)
        fromPaymentList(details)
    }


//    // Remove a payment by ID
//    fun removePaymentById(id: Int): Boolean
//    {
//        return m_payments.removeIf { it.id == id }
//    }

    // Get total amount of all payments
    fun getTotalAmount(): CMoney
    {
        return m_payments.fold(CMoney(0)) { total, payment ->
            total.add(payment.total)
        }
    }

    fun getTotalAmountByType(paymentType: EPaymentMethod): CMoney
    {
        return m_payments.filter { it.paymentMethod == paymentType }
            .fold(CMoney(0)) { total, payment ->
                total.add(payment.total)
            }
    }

    // Filter payments by type
    fun getPaymentsByType(type: EPaymentMethod): List<CPayment>
    {
        return m_payments.filter { it.paymentMethod == type }
    }

    // Find payment by ID
//    fun findPaymentById(id: Int): CPayment?
//    {
//        return m_payments.find { it.id == id }
//    }

    // Clear all payments
    fun clearPayments()
    {
        m_payments.clear()
    }

    // Get payment count
    val count: Int
        get() = m_payments.size

    // Check if list is empty
    val isEmpty: Boolean
        get() = m_payments.isEmpty()

    val size : Int
        get() = m_payments.size

    // Index operator
    operator fun get(index: Int): CPayment
    {
        return m_payments[index]
    }

    fun getCashTotal(): CMoney
    {
        var total = CMoney(0)
        for (payment in m_payments)
        {
            if (payment.paymentMethod == EPaymentMethod.PAYMENT_CASH ||
                payment.paymentMethod == EPaymentMethod.PAYMENT_RETURN
            )
            {
                total = total + payment.total
            }
        }
        return total
    }

    fun getCardTotal(): CMoney
    {
        var total = CMoney(0)
        for (payment in m_payments) {
            if (payment.paymentMethod != EPaymentMethod.PAYMENT_CASH &&
                payment.paymentMethod != EPaymentMethod.PAYMENT_RETURN)
            {
                total = total + payment.total
            }
        }
        return total
    }

    fun getTotal(): CMoney
    {
        return CMoney(m_payments.sumOf { it.total.cents() })
    }

    fun cancelPayment(index : Int, paymentStatus : EPaymentStatus)
    {
        val service = GrpcServiceFactory.createDailyTransactionPaymentService()
        if (index >=0)
        {
            m_payments.removeAt(index)
        }
        else
        {
            clearPayments()
        }
        service.cancelPayment(m_transactionId, index, EPaymentStatus.toPaymentStatus(paymentStatus))
    }

    fun addPayment(paymentMethod: EPaymentMethod, customerId: Int, amount: CMoney)
    {
        var service = GrpcServiceFactory.createDailyTransactionPaymentService()
        val pay = CPayment( -1, customerId, paymentMethod, amount, "", EPayed.PAID_NO);
        val paymentIndex = service.getNewPaymentIndex(m_transactionId);
        service.addPayment( m_transactionId, pay.toPayment(), paymentIndex);
    }

    fun fromPaymentList(payments: PaymentList?)
    {
        m_payments.clear()
        if (payments == null) return
        val size = payments.paymentCount
        for (i in 0 until size)
        {
            val raw = payments.getPayment(i)
            var payment = CPayment.fromPayment(raw)
            m_payments.add(payment)
        }
    }
}