package com.hha.framework

import com.hha.callback.PaymentsListener
import com.hha.callback.TransactionOperations
import com.hha.common.PaymentMethod
import com.hha.daily.payment.PaymentDetails
import com.hha.daily.payment.PaymentDetailsList
import com.hha.daily.payment.PaymentList
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import com.hha.service.DailyTransactionPaymentService
import com.hha.types.CMoney
import com.hha.types.EPayed
import com.hha.types.EPaymentMethod
import com.hha.types.EPaymentStatus

class CPaymentList(transactionOperations: TransactionOperations)
{
    var mTransaction: TransactionOperations = transactionOperations
    val mPayments: MutableList<CPayment> = mutableListOf()
    var global = Global.getInstance()
    var m_alreadyPaid: CMoney = CMoney(0)
    var m_valid: Boolean = false
    private val m_listeners = mutableListOf<PaymentsListener>()
    var mPaymentTotals = CPaymentTransaction() ///< Total payments per money type.

    // Get payment count
    val count: Int
        get() = mPayments.size

    // Check if list is empty
    val isEmpty: Boolean
        get() = mPayments.isEmpty()

    val size: Int
        get() = mPayments.size

    fun addListener(listener: PaymentsListener)
    {
        if (!m_listeners.contains(listener))
            m_listeners.add(listener)
    }

    // Add a payment to the list
    fun addPayment(payment: CPayment)
    {
        mPayments.add(payment)
        val position = mPayments.size - 1
        m_listeners.forEach { it.onPaymentAdded(position, mPayments[position]) }
        invalidate()
    }

    fun addPayment(paymentMethod: EPaymentMethod, amount: CMoney)
    {
        // Add in memory
        invalidate()
        val customerId = mTransaction.getCustomerId()
        val pay = CPayment(-1, customerId, paymentMethod, amount, "", EPayed.PAID_NO);
        mPayments.add(pay)

        // Notify dialog
        val position = mPayments.size - 1
        m_listeners.forEach { it.onPaymentAdded(position, mPayments[position]) }

        // Notify server GRPC
        var service = GrpcServiceFactory.createDailyTransactionPaymentService()
        val paymentIndex = service.getNewPaymentIndex(
            mTransaction.transactionId)
        service.addPayment(
            mTransaction.transactionId, pay.toPayment(),
            paymentIndex)
    }

    fun addReturnMoney()
    {
        invalidate()
        val total = mTransaction.getTotalTransaction()
        var service = GrpcServiceFactory.createDailyTransactionPaymentService()

        // Only close if all payments together are bigger or equal to the total.
        val paymentDetailsList: PaymentDetailsList? = service.getTransactionPaymentTotals(global.transactionId, false);
        val paymentTransaction = CPaymentTransaction(paymentDetailsList)

        val totalPayments: CMoney = paymentTransaction.getTotal(EPaymentStatus.PAY_STATUS_ANY);
        if (totalPayments > total)
        {
            addPayment(EPaymentMethod.PAYMENT_RETURN, total - totalPayments);
        }
    }

    fun cancelNewPayments()
    {
        cancelPayments(-1, EPaymentStatus.PAY_STATUS_UNPAID)
    }

    fun cancelPayments(index: Int, paymentStatus: EPaymentStatus)
    {
        val indicesRemoved = mutableListOf<Int>()
        // Remove in memory
        if (index >= 0)
        {
            mPayments.removeAt(index)
            // Notify dialog
            m_listeners.forEach { it.onPaymentRemoved(index) }
        } else
        {
            var idx = 0
            val iterator = mPayments.iterator()
            while (iterator.hasNext())
            {
                val payment = iterator.next()
                if (shouldRemovePayment(paymentStatus, payment.isPaid))
                {
                    iterator.remove()
                    indicesRemoved.add(idx)
                    m_listeners.forEach { it.onPaymentRemoved(idx) }
                } else
                {
                    indicesRemoved.add(idx)
                }
            }
            // Notify dialog
            m_listeners.forEach { it.onPaymentsCleared() }
        }
        // Notify server GRPC
        val service = GrpcServiceFactory.createDailyTransactionPaymentService()
        service.cancelPayment(
            mTransaction.transactionId, index,
            paymentStatus.toPaymentStatus())
        invalidate()
    }

    fun shouldRemovePayment(paymentStatus: EPaymentStatus, isPaid: EPayed): Boolean
    {
        when (paymentStatus)
        {
            EPaymentStatus.PAY_STATUS_ANY -> return true
            EPaymentStatus.PAY_STATUS_UNPAID -> return isPaid == EPayed.PAID_NO
            EPaymentStatus.PAY_STATUS_PAID_ORDER_BEFORE -> return isPaid == EPayed.PAID_BEFORE
            EPaymentStatus.PAY_STATUS_CANCEL -> return isPaid == EPayed.PAID_CANCEL
            EPaymentStatus.PAY_STATUS_PAID_BEFORE -> return isPaid == EPayed.PAID_BEFORE
            EPaymentStatus.PAY_STATUS_PAID_ORDER -> return isPaid == EPayed.PAID_ORDER
        }
    }

    fun cancelPayments(status: EPaymentStatus)
    {
        val transactionId = mTransaction.transactionId
        val service = GrpcServiceFactory.createDailyTransactionPaymentService()
        service.cancelPayments2(
            transactionId, status.toPaymentStatus())
        invalidate()
    }

    // Clear all payments
    fun clearPayments()
    {
        mPayments.clear()
    }

    fun fromPaymentList(payments: PaymentList?)
    {
        mPayments.clear()
        m_listeners.forEach { it.onPaymentsCleared() }

        if (payments == null) return
        val size = payments.paymentCount
        for (i in 0 until size)
        {
            val raw = payments.getPayment(i)
            var payment = CPayment.fromPayment(raw)
            mPayments.add(payment)
            m_listeners.forEach { it.onPaymentAdded(i, payment) }
        }
    }

    // Index operator
    operator fun get(index: Int): CPayment
    {
        return mPayments[index]
    }

    fun getCardTotal(): CMoney
    {
        var total = CMoney(0)
        for (payment in mPayments)
        {
            if (payment.paymentMethod != EPaymentMethod.PAYMENT_CASH &&
                payment.paymentMethod != EPaymentMethod.PAYMENT_RETURN
            )
            {
                total = total + payment.total
            }
        }
        return total
    }

    fun getCardTotal(paymentStatus: EPaymentStatus): CMoney
    {
        val payments = getTransactionPaymentTotals(false )
        return payments.getPayment(
            paymentStatus, EPaymentMethod.PAYMENT_TOTAL_CARDS)
    }

    fun getCashTotal(paymentStatus: EPaymentStatus): CMoney
    {
        val payments = getTransactionPaymentTotals(false )
        return payments.getPayment(
            paymentStatus, EPaymentMethod.PAYMENT_CASH)
    }

    fun getHighestPaymentIndex(): Int
    {
        val transactionId = mTransaction.transactionId
        val service = GrpcServiceFactory.createDailyTransactionPaymentService()
        return service.getHighestPaymentIndex(transactionId)
    }

    fun getPartialTotal(partial_index: Short): CMoney
    {
        return getPartialTotal(
            partial_index, EPaymentMethod.PAYMENT_ALL)
    }

    fun getPaidTotal(payStatus: EPaymentStatus): CMoney
    {
        val paymentTransaction = getTransactionPaymentTotals(false)
        return paymentTransaction.getTotal(payStatus); // PAY_STATUS_PAID_ORDER_BEFORE);
    }

    fun getReturnTotal(payStatus: EPaymentStatus): CMoney
    {
        val paymentDetails =
            getTransactionPaymentTotals(false)
        val money = paymentDetails.getPayment(
            payStatus, EPaymentMethod.PAYMENT_RETURN)
        return money
    }

    fun getPartialTotal(partialIndex: Short, paymentType: EPaymentMethod): CMoney
    {
        val transactionId = mTransaction.transactionId
        val service = GrpcServiceFactory.createDailyTransactionPaymentService()
        val method = EPaymentMethod.toPaymentMethod(paymentType)
        val cents = service.getPartialTotal(
            transactionId, partialIndex, method
        )
        return CMoney(cents)
    }

    fun getPayments(): List<CPayment>
    {
        return mPayments;
    }

    // Filter payments by type
    fun getPaymentsByType(type: EPaymentMethod): List<CPayment>
    {
        return mPayments.filter { it.paymentMethod == type }
    }

    fun getTotalAlreadyPaid(): CMoney
    {
        return m_alreadyPaid
    }

    fun getCashTotal(): CMoney
    {
        var total = CMoney(0)
        for (payment in mPayments)
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

    fun getTotal(): CMoney
    {
        return CMoney(mPayments.sumOf { it.total.cents() })
    }

    // Get total amount of all payments
    fun getTotalAmount(): CMoney
    {
        return mPayments.fold(CMoney(0)) { total, payment ->
            total.add(payment.total)
        }
    }

    fun getTotalAmountByType(paymentType: EPaymentMethod): CMoney
    {
        return mPayments.filter { it.paymentMethod == paymentType }
            .fold(CMoney(0)) { total, payment ->
                total.add(payment.total)
            }
    }

    fun getTransactionPaymentTotals(
        includeCancelledPayments: Boolean): CPaymentTransaction
    {
        val transactionId = mTransaction.transactionId
        if (transactionId <= 0)
        {
            return CPaymentTransaction()
        }
        if (!mPaymentTotals.valid)
        {
            val service: DailyTransactionPaymentService =
                GrpcServiceFactory.createDailyTransactionPaymentService()
            val response = service.selectTransactionId(transactionId, includeCancelledPayments)

            if (response != null)
            {
                val count = response.paymentDetailsCount
                // Single loop to process each payment once.
                for (n in 0..<count)
                {
                    val p: PaymentDetails = response.getPaymentDetails(n) ?: continue
                    val paid = EPayed.fromPayed(p.isPaid)
                    val method = EPaymentMethod.fromPaymentMethod(p.paymentMethod)
                    val m = CMoney(p.total)
                    val payment = CPayment(
                        n, -1, method,
                        m, "", paid
                    )
                    mPayments.add(payment)
                }
            }
            mPaymentTotals = CPaymentTransaction(response)
            mPaymentTotals.valid = true
        }
        return mPaymentTotals
    }

    fun removeListener(listener: PaymentsListener)
    {
        m_listeners.remove(listener)
    }

    fun invalidate()
    {
        mPaymentTotals.valid = false
    }

    public fun setTransactionId()
    {
        val transactionId = mTransaction.transactionId
        if (transactionId < 0)
        {
            mPayments.clear()
            mPaymentTotals = CPaymentTransaction()
            return
        }
        invalidate()
        getTransactionPaymentTotals(false)
        val service: DailyTransactionPaymentService =
            GrpcServiceFactory.createDailyTransactionPaymentService()
        m_alreadyPaid = CMoney(service.getPartialTotal(transactionId, -1,
            PaymentMethod.PAYMENT_ALL))
    }
}
