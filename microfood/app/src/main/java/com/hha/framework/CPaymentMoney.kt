/**
 * Money conversion for any transaction totals.
 * Handling and counting money for any transaction
 *
 * @author mensfort
 * @property EPayed Payment status enum
 * @property EPaymentMethod Payment method enum
 * @property EpaymentStatus Payment status enum
 */

package com.hha.framework

import com.hha.types.CMoney
import com.hha.types.EPayed
import com.hha.types.EPaymentMethod
import com.hha.types.EPaymentStatus

class CPaymentMoney {
    var m_cash = CMoney(0.0)
    var m_pin = CMoney(0.0)
    var m_credit_card = CMoney(0.0)
    var m_visa = CMoney(0.0)
    var m_pay_pal = CMoney(0.0)
    var m_master_card = CMoney(0.0)
    var m_american_express = CMoney(0.0)
    var m_debit = CMoney(0.0)
    var m_zettle = CMoney(0.0)
    var m_send_bill = CMoney(0.0)
    var m_renminbi = CMoney(0.0)
    var m_return_amount = CMoney(0.0)
    var m_discount_total = CMoney(0.0)
    var m_tips_total = CMoney(0.0)

    // Default constructor
    constructor()

    // Copy constructor
    constructor(other: CPaymentMoney) {
        this.m_cash = other.m_cash
        this.m_pin = other.m_pin
        this.m_credit_card = other.m_credit_card
        this.m_visa = other.m_visa
        this.m_pay_pal = other.m_pay_pal
        this.m_master_card = other.m_master_card
        this.m_american_express = other.m_american_express
        this.m_debit = other.m_debit
        this.m_zettle = other.m_zettle
        this.m_send_bill = other.m_send_bill
        this.m_renminbi = other.m_renminbi
        this.m_return_amount = other.m_return_amount
        this.m_discount_total = other.m_discount_total
        this.m_tips_total = other.m_tips_total
    }

    // Alternative: You can also create a copy() function for convenience
    fun copy(): CPaymentMoney {
        return CPaymentMoney(this)
    }

    fun all(): CMoney
    {
        return m_cash + m_pin + m_credit_card + m_visa + m_pay_pal +
              m_master_card + m_american_express + m_debit + m_zettle +
              m_send_bill + m_renminbi + m_return_amount + m_tips_total - m_discount_total
    }

    operator fun plusAssign(other: CPaymentMoney)
    {
        m_cash = m_cash + other.m_cash
        m_pin = m_pin + other.m_pin
        m_credit_card = m_credit_card + other.m_credit_card
        m_visa = m_visa + other.m_visa
        m_pay_pal = m_pay_pal + other.m_pay_pal
        m_master_card = m_master_card + other.m_master_card
        m_american_express = m_american_express + other.m_american_express
        m_send_bill = m_send_bill + other.m_send_bill
        m_renminbi = m_renminbi + other.m_renminbi
        m_return_amount = m_return_amount + other.m_return_amount
        m_debit = m_debit + other.m_debit
        m_zettle = m_zettle + other.m_zettle
        m_discount_total = m_discount_total + other.m_discount_total
        m_tips_total = m_tips_total + other.m_tips_total
    }

    // + operator
    operator fun plus(other: CPaymentMoney): CPaymentMoney
    {
        val result = CPaymentMoney()
        result.m_cash = this.m_cash + other.m_cash
        result.m_pin = this.m_pin + other.m_pin
        result.m_credit_card = this.m_credit_card + other.m_credit_card
        result.m_visa = this.m_visa + other.m_visa
        result.m_pay_pal = this.m_pay_pal + other.m_pay_pal
        result.m_master_card = this.m_master_card + other.m_master_card
        result.m_american_express = this.m_american_express + other.m_american_express
        result.m_send_bill = this.m_send_bill + other.m_send_bill
        result.m_renminbi = this.m_renminbi + other.m_renminbi
        result.m_return_amount = this.m_return_amount + other.m_return_amount
        result.m_debit = this.m_debit + other.m_debit
        result.m_zettle = this.m_zettle + other.m_zettle
        result.m_discount_total = this.m_discount_total + other.m_discount_total
        result.m_tips_total = this.m_tips_total + other.m_tips_total
        return result
    }

    fun countPaymentTypes(): Int
    {
        var count = 0
        if (!m_cash.empty()) count++
        if (!m_pin.empty()) count++
        if (!m_credit_card.empty()) count++
        if (!m_visa.empty()) count++
        if (!m_pay_pal.empty()) count++
        if (!m_master_card.empty()) count++
        if (!m_debit.empty()) count++
        if (!m_zettle.empty()) count++
        if (!m_american_express.empty()) count++
        if (!m_send_bill.empty()) count++
        if (!m_renminbi.empty()) count++
        if (!m_return_amount.empty()) count++
        if (!m_discount_total.empty()) count++
        if (!m_tips_total.empty()) count++
        return count
    }

    fun addPayment(method: EPaymentMethod, total: CMoney)
    {
        when (method)
        {
            // Change `+=` to `= ... + ...` to resolve the ambiguity
            EPaymentMethod.PAYMENT_CASH -> m_cash = m_cash + total
            EPaymentMethod.PAYMENT_PIN -> m_pin = m_pin + total
            EPaymentMethod.PAYMENT_CREDIT_CARD -> m_credit_card = m_credit_card + total
            EPaymentMethod.PAYMENT_VISA_CARD -> m_visa = m_visa + total
            EPaymentMethod.PAYMENT_PAY_PAL -> m_pay_pal = m_pay_pal + total
            EPaymentMethod.PAYMENT_MASTERCARD -> m_master_card = m_master_card + total
            EPaymentMethod.PAYMENT_DEBIT -> m_debit = m_debit + total
            EPaymentMethod.PAYMENT_ZETTLE -> m_zettle = m_zettle + total
            EPaymentMethod.PAYMENT_AMERICAN_EXPRESS -> m_american_express = m_american_express + total
            EPaymentMethod.PAYMENT_SEND_BILL -> m_send_bill = m_send_bill + total
            EPaymentMethod.PAYMENT_RENMINBI -> m_renminbi = m_renminbi + total
            EPaymentMethod.PAYMENT_RETURN -> m_return_amount = m_return_amount + total
            EPaymentMethod.PAYMENT_DISCOUNT -> m_discount_total = m_discount_total + total
            EPaymentMethod.PAYMENT_TIPS -> m_tips_total = m_tips_total + total
            else ->
            {
                // PAYMENT_INVALID, PAYMENT_ALL, PAYMENT_SEND_BILL_WAIT, PAYMENT_SEND_BILL_DONE, PAYMENT_TOTAL_CARDS
                // Do nothing
            }
        }
    }
}
