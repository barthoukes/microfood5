package com.hha.framework

import com.hha.daily.payment.Payment
import com.hha.daily.payment.PaymentDetails
import com.hha.daily.payment.PaymentDetailsList
import com.hha.daily.payment.PaymentList
import com.hha.types.CMoney
import com.hha.types.EPayed
import com.hha.types.EPaymentMethod
import com.hha.types.EPaymentStatus

data class CPaymentTransaction(
   var openAmount: CPaymentMoney,
   var paidOrderAmount: CPaymentMoney,
   var paidBeforeAmount: CPaymentMoney,
   var paidCancel: CPaymentMoney,
   var valid: Boolean,
   var transactionId: Long
) {
   constructor() : this(
      CPaymentMoney(), CPaymentMoney(),
      CPaymentMoney(), CPaymentMoney(),
      false, -1
   )

   constructor(paymentList: PaymentList?) : this(
      CPaymentMoney(), CPaymentMoney(),
      CPaymentMoney(), CPaymentMoney(),
      false, -1
   )
   {
      val count = paymentList?.paymentCount ?: 0
      if (paymentList == null || count == 0)
      {
         return
      }
      // Set valid to true once.
      valid = true

      // Single loop to process each payment once.
      for (n in 0..<count)
      {
         val p: Payment = paymentList.getPayment(n) ?: continue // Safety check
         val paid = EPayed.fromPayed(p.isPaid)
         val method = EPaymentMethod.fromPaymentMethod(p.paymentMethod)
         val m = CMoney(p.total)
         addPayment(paid, method, m)
      }
   }

   constructor(paymentDetailsList: PaymentDetailsList?) : this(
      CPaymentMoney(), CPaymentMoney(),
      CPaymentMoney(), CPaymentMoney(),
      false, -1
   )
   {
      val count = paymentDetailsList?.paymentDetailsCount ?: 0
      if (paymentDetailsList == null || count == 0)
      {
         return
      }
      // Set valid to true once.
      valid = true

      // Single loop to process each payment once.
      for (n in 0..<count)
      {
         val p: PaymentDetails = paymentDetailsList.getPaymentDetails(n) ?: continue
         val paid = EPayed.fromPayed(p.isPaid)
         val method = EPaymentMethod.fromPaymentMethod(p.paymentMethod)
         val m = CMoney(p.total)
         addPayment(paid, method, m)
      }
   }

   private fun getPaymentMoney(isPaid: EPayed): CPaymentMoney =
      when (isPaid)
      {
         EPayed.PAID_CANCEL -> paidCancel
         EPayed.PAID_ORDER -> paidOrderAmount
         EPayed.PAID_BEFORE -> paidBeforeAmount
         EPayed.PAID_NO -> openAmount
         else -> CPaymentMoney()
      }

   fun addPayment(isPaid: EPayed, method: EPaymentMethod, total: CMoney): CPaymentTransaction
   {
      val p: CPaymentMoney = getPaymentMoney(isPaid)
      p.addPayment(method, total)
      return this
   }

   fun setPayment(isPaid: EPayed, method: EPaymentMethod, total: CMoney): CPaymentTransaction
   {
      val p = getPaymentMoney(isPaid)
      when (method)
      {
         EPaymentMethod.PAYMENT_CASH -> p.m_cash = total
         EPaymentMethod.PAYMENT_PIN -> p.m_pin = total
         EPaymentMethod.PAYMENT_CREDIT_CARD -> p.m_credit_card = total
         EPaymentMethod.PAYMENT_VISA_CARD -> p.m_visa = total
         EPaymentMethod.PAYMENT_PAY_PAL -> p.m_pay_pal = total
         EPaymentMethod.PAYMENT_MASTERCARD -> p.m_master_card = total
         EPaymentMethod.PAYMENT_DEBIT -> p.m_debit = total
         EPaymentMethod.PAYMENT_ZETTLE -> p.m_zettle = total
         EPaymentMethod.PAYMENT_AMERICAN_EXPRESS -> p.m_american_express = total
         EPaymentMethod.PAYMENT_SEND_BILL -> p.m_send_bill = total
         EPaymentMethod.PAYMENT_RENMINBI -> p.m_renminbi = total
         EPaymentMethod.PAYMENT_RETURN -> p.m_return_amount = total
         EPaymentMethod.PAYMENT_DISCOUNT -> p.m_discount_total = total
         EPaymentMethod.PAYMENT_TIPS -> p.m_tips_total = total
         else ->
         {
            // PAYMENT_INVALID, PAYMENT_ALL - do nothing
         }
      }
      return this
   }

   fun getPayment(paidStatus: EPaymentStatus, method: EPaymentMethod): CMoney
   {
      val isPaid = when (paidStatus)
      {
         EPaymentStatus.PAY_STATUS_UNPAID -> EPayed.PAID_NO
         EPaymentStatus.PAY_STATUS_CANCEL -> EPayed.PAID_CANCEL
         EPaymentStatus.PAY_STATUS_PAID_ORDER -> EPayed.PAID_ORDER
         EPaymentStatus.PAY_STATUS_PAID_BEFORE -> EPayed.PAID_BEFORE
         EPaymentStatus.PAY_STATUS_PAID_ORDER_BEFORE ->
            return getPayment(EPaymentStatus.PAY_STATUS_PAID_ORDER, method) +
               getPayment(EPaymentStatus.PAY_STATUS_PAID_BEFORE, method)

         EPaymentStatus.PAY_STATUS_ANY ->
            return getPayment(EPaymentStatus.PAY_STATUS_PAID_ORDER, method) +
               getPayment(EPaymentStatus.PAY_STATUS_UNPAID, method) +
               getPayment(EPaymentStatus.PAY_STATUS_PAID_BEFORE, method)
      }

      val p = getPaymentMoney(isPaid)
      return when (method)
      {
         EPaymentMethod.PAYMENT_CASH -> p.m_cash
         EPaymentMethod.PAYMENT_PIN -> p.m_pin
         EPaymentMethod.PAYMENT_CREDIT_CARD -> p.m_credit_card
         EPaymentMethod.PAYMENT_VISA_CARD -> p.m_visa
         EPaymentMethod.PAYMENT_PAY_PAL -> p.m_pay_pal
         EPaymentMethod.PAYMENT_MASTERCARD -> p.m_master_card
         EPaymentMethod.PAYMENT_DEBIT -> p.m_debit
         EPaymentMethod.PAYMENT_ZETTLE -> p.m_zettle
         EPaymentMethod.PAYMENT_AMERICAN_EXPRESS -> p.m_american_express
         EPaymentMethod.PAYMENT_SEND_BILL -> p.m_send_bill
         EPaymentMethod.PAYMENT_RENMINBI -> p.m_renminbi
         EPaymentMethod.PAYMENT_RETURN -> p.m_return_amount
         EPaymentMethod.PAYMENT_TOTAL_CARDS ->
            p.m_american_express + p.m_credit_card + p.m_master_card + p.m_pay_pal +
               p.m_pin + p.m_renminbi + p.m_send_bill + p.m_visa + p.m_debit + p.m_zettle

         EPaymentMethod.PAYMENT_ALL -> p.all()
         else -> CMoney(0.0)
      }
   }

   fun getTotal(paidStatus: EPaymentStatus): CMoney
   {
      return when (paidStatus)
      {
         EPaymentStatus.PAY_STATUS_ANY ->
            openAmount.all() + paidBeforeAmount.all() + paidOrderAmount.all()

         EPaymentStatus.PAY_STATUS_PAID_ORDER_BEFORE ->
            paidBeforeAmount.all() + paidOrderAmount.all()

         EPaymentStatus.PAY_STATUS_PAID_ORDER -> paidOrderAmount.all()
         EPaymentStatus.PAY_STATUS_PAID_BEFORE -> paidBeforeAmount.all()
         EPaymentStatus.PAY_STATUS_CANCEL -> paidCancel.all()
         EPaymentStatus.PAY_STATUS_UNPAID -> openAmount.all()
      }
   }

   fun getAmounts(paidStatus: EPaymentStatus): CPaymentMoney
   {
      return when (paidStatus)
      {
         EPaymentStatus.PAY_STATUS_UNPAID -> CPaymentMoney(openAmount) // Using copy constructor
         EPaymentStatus.PAY_STATUS_PAID_ORDER -> CPaymentMoney(paidOrderAmount)
         EPaymentStatus.PAY_STATUS_PAID_BEFORE -> CPaymentMoney(paidBeforeAmount)
         EPaymentStatus.PAY_STATUS_ANY ->
         {
            val p = CPaymentMoney()
            p += paidOrderAmount
            p += openAmount
            p += paidBeforeAmount
            p
         }

         else -> CPaymentMoney(openAmount)
      }
   }
}

class CpaymentUnit(
   val isPaid: EPayed,
   val method: EPaymentMethod,
   val total: CMoney
)

