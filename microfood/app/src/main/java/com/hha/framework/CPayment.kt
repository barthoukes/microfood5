package com.hha.framework

import com.hha.daily.payment.Payment

import com.hha.types.CMoney
import com.hha.types.EPayed
import com.hha.types.EPaymentMethod

data class CPayment (
    var partialIndex : Int, // = 0
    var customerId: Int,
    var paymentMethod : EPaymentMethod, // = EPaymentMethod.PAYMENT_CASH
    var total : CMoney, // = CMoney(0)
    var msg : String,
    var isPaid : EPayed
)
{
    fun toPayment(): Payment {
        return Payment.newBuilder()
            .setPartialIndex(partialIndex)
            .setCustomerId(customerId)
            .setPaymentMethod(EPaymentMethod.toPaymentMethod(paymentMethod))
            .setTotal(total.cents())
            .setMsg(msg)
            .setIsPaid(EPayed.toPayed(isPaid))
            .build()
    }

    companion object
    {
        fun fromPayment(payment: Payment): CPayment
        {
            return CPayment(
                partialIndex = payment.partialIndex,
                customerId = payment.customerId,
                paymentMethod = EPaymentMethod.fromPaymentMethod(payment.paymentMethod),
                total = CMoney(payment.total),
                msg = payment.msg,
                isPaid = EPayed.fromPayed(payment.isPaid)
            )
        }
    }
}