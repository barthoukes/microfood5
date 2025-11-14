package com.hha.framework

import com.hha.callback.PaymentsListener
import com.hha.callback.TransactionOperations
import com.hha.common.PaymentMethod
import com.hha.daily.payment.PaymentDetailsList
import com.hha.daily.payment.PaymentList
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import com.hha.service.DailyTransactionPaymentService
import com.hha.types.CMoney
import com.hha.types.EPayed
import com.hha.types.EPaymentMethod
import com.hha.types.EPaymentStatus

class CPaymentList(transactionOperations: TransactionOperations) {
    var m_transaction: TransactionOperations = transactionOperations
    val m_payments: MutableList<CPayment> = mutableListOf()
    var global = Global.getInstance()
    var m_transactionId: Int = 0
    var m_alreadyPaid: CMoney = CMoney(0)
    var m_valid: Boolean = false
    private val m_listeners = mutableListOf<PaymentsListener>()

    fun addListener(listener: PaymentsListener)
    {
        if (!m_listeners.contains(listener))
            m_listeners.add(listener)
    }

    fun removeListener(listener: PaymentsListener)
    {
        m_listeners.remove(listener)
    }

    fun getPayments(): List<CPayment>
    {
        return m_payments;
    }

    // Add a payment to the list
    fun addPayment(payment: CPayment)
    {
        m_payments.add(payment)
        val position = m_payments.size-1
        m_listeners.forEach { it.onPaymentAdded(position, m_payments[position]) }
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
        var payments = CPaymentTransaction(details)
        m_alreadyPaid = CMoney(service.getPartialTotal(transactionId, -1,
            PaymentMethod.PAYMENT_ALL))
        fromPaymentList(details)
    }

    // Get total amount of all payments
    fun getTotalAmount(): CMoney
    {
        return m_payments.fold(CMoney(0)) { total, payment ->
            total.add(payment.total)
        }
    }

    fun getTotalAlreadyPaid(): CMoney
    {
        return m_alreadyPaid
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

    fun addReturnMoney()
    {
        val total = m_transaction.getTotalTransaction()
        var service = GrpcServiceFactory.createDailyTransactionPaymentService()

        // Only close if all payments together are bigger or equal to the total.
        val paymentDetailsList : PaymentDetailsList? = service.getTransactionPaymentTotals(global.transactionId, false);
        val paymentTransaction = CPaymentTransaction(paymentDetailsList)

        val totalPayments: CMoney =paymentTransaction.getTotal(EPaymentStatus.PAY_STATUS_ANY);
        if ( totalPayments > total)
        {
            addPayment(EPaymentMethod.PAYMENT_RETURN, total-totalPayments);
        }
    }

    fun cancelPayment(index : Int, paymentStatus : EPaymentStatus)
    {
        // Remove in memory
        if (index >=0)
        {
            m_payments.removeAt(index)
            // Notify dialog
            m_listeners.forEach { it.onPaymentRemoved(index) }
        }
        else
        {
            clearPayments()
            // Notify dialog
            m_listeners.forEach { it.onPaymentsCleared() }
        }
        // Notify server GRPC
        val service = GrpcServiceFactory.createDailyTransactionPaymentService()
        service.cancelPayment(m_transactionId, index, EPaymentStatus.toPaymentStatus(paymentStatus))
    }

    fun addPayment(paymentMethod: EPaymentMethod, amount: CMoney)
    {
        // Add in memory
        val customerId = m_transaction.getCustomerId()
        val pay = CPayment( -1, customerId, paymentMethod, amount, "", EPayed.PAID_NO);
        m_payments.add(pay)

        // Notify dialog
        val position = m_payments.size-1
        m_listeners.forEach { it.onPaymentAdded(position, m_payments[position]) }

        // Notify server GRPC
        var service = GrpcServiceFactory.createDailyTransactionPaymentService()
        val paymentIndex = service.getNewPaymentIndex(m_transactionId);
        service.addPayment( m_transactionId, pay.toPayment(), paymentIndex);
    }

    fun fromPaymentList(payments: PaymentList?)
    {
        m_payments.clear()
        m_listeners.forEach { it.onPaymentsCleared() }

        if (payments == null) return
        val size = payments.paymentCount
        for (i in 0 until size)
        {
            val raw = payments.getPayment(i)
            var payment = CPayment.fromPayment(raw)
            m_payments.add(payment)
            m_listeners.forEach { it.onPaymentAdded(i, payment) }
        }
    }
}
