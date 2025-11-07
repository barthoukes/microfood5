package com.hha.types

import android.R
import com.hha.common.PaymentMethod

/**
 * Enum representing different payment methods.
 */
enum class EPaymentMethod(val value: Int) {
    PAYMENT_NONE(0),
    PAYMENT_ALL(1),                  ///< Also no meaning
    PAYMENT_CASH(10),                ///< Cash payment
    PAYMENT_PIN(11),                 ///< PIN payment
    PAYMENT_CREDIT_CARD(12),         ///< Pay with credit card
    PAYMENT_VISA_CARD(13),           ///< Pay with Visa card
    PAYMENT_PAY_PAL(14),             ///< Pay with Paypal
    PAYMENT_MASTERCARD(15),          ///< Pay with Mastercard
    PAYMENT_AMERICAN_EXPRESS(16),    ///< Pay with American Express
    PAYMENT_SEND_BILL(17),           ///< Don't pay, send a bill
    PAYMENT_RENMINBI(18),            ///< Joke, pay in Chinese money
    PAYMENT_RETURN(19),              ///< Money returned after paying cash
    PAYMENT_SEND_BILL_WAIT(20),      ///< Send but not paid yet
    PAYMENT_SEND_BILL_DONE(21),      ///< Payment done
    PAYMENT_TOTAL_CARDS(22),         ///< Payment all cards
    PAYMENT_DEBIT(24),               ///< Pay with debit
    PAYMENT_ZETTLE(25),              ///< Pay with pin2
    PAYMENT_DISCOUNT(30),            ///< Discount already given
    PAYMENT_TIPS(31),                ///< Tips already given
    PAYMENT_INVALID(-2);             ///< No payment

    companion object {
        /**
         * Converts an integer value to the corresponding [EPaymentMethod].
         * @param value The integer value to convert
         * @return The matching [EPaymentMethod] or null if invalid
         */
        fun fromInt(value: Int): EPaymentMethod? {
            return values().firstOrNull { it.value == value }
        }

        /**
         * Gets the display name for the payment method
         */
        fun toString(paymentMethod: EPaymentMethod): String {
            return when (paymentMethod) {
                PAYMENT_CASH -> "Cash"
                PAYMENT_PIN -> "PIN"
                PAYMENT_CREDIT_CARD -> "Credit Card"
                PAYMENT_VISA_CARD -> "Visa"
                PAYMENT_PAY_PAL -> "PayPal"
                PAYMENT_MASTERCARD -> "Mastercard"
                PAYMENT_AMERICAN_EXPRESS -> "American Express"
                PAYMENT_DEBIT -> "Debit Card"
                PAYMENT_ZETTLE -> "Zettle"
                PAYMENT_SEND_BILL -> "Send Bill"
                PAYMENT_RETURN -> "Return"
                PAYMENT_SEND_BILL_WAIT -> "Bill Pending"
                PAYMENT_SEND_BILL_DONE -> "Bill Paid"
                PAYMENT_DISCOUNT -> "Discount"
                PAYMENT_TIPS -> "Tips"
                else -> "Unknown"
            }
        }

        /**
         * Gets the icon resource ID for the payment method
         */
        fun getIconResource(paymentMethod: EPaymentMethod): Int {
            return when (paymentMethod) {
                PAYMENT_CASH -> R.drawable.ic_menu_add
//                PAYMENT_PIN -> R.drawable.ic_pin
//                PAYMENT_CREDIT_CARD,
//                PAYMENT_VISA_CARD,
//                PAYMENT_MASTERCARD,
//                PAYMENT_AMERICAN_EXPRESS,
//                PAYMENT_DEBIT -> R.drawable.ic_credit_card
//                PAYMENT_PAY_PAL -> R.drawable.ic_paypal
//                PAYMENT_ZETTLE -> R.drawable.ic_zettle
//                PAYMENT_SEND_BILL,
//                PAYMENT_SEND_BILL_WAIT,
//                PAYMENT_SEND_BILL_DONE -> R.drawable.ic_bill
//                PAYMENT_DISCOUNT -> R.drawable.ic_discount
//                PAYMENT_TIPS -> R.drawable.ic_tips
                else -> R.drawable.star_on
            }
        }

        fun toPaymentMethod(paymentMethod: EPaymentMethod): PaymentMethod  {
            return when(paymentMethod) {
                PAYMENT_CASH -> PaymentMethod.PAYMENT_CASH
                PAYMENT_PIN -> PaymentMethod.PAYMENT_PIN
                PAYMENT_CREDIT_CARD -> PaymentMethod.PAYMENT_CREDIT_CARD
                PAYMENT_VISA_CARD -> PaymentMethod.PAYMENT_VISA_CARD
                PAYMENT_PAY_PAL -> PaymentMethod.PAYMENT_PAY_PAL
                PAYMENT_MASTERCARD -> PaymentMethod.PAYMENT_MASTERCARD
                PAYMENT_AMERICAN_EXPRESS -> PaymentMethod.PAYMENT_AMERICAN_EXPRESS
                PAYMENT_DEBIT -> PaymentMethod.PAYMENT_DEBIT
                PAYMENT_ZETTLE -> PaymentMethod.PAYMENT_ZETTLE
                PAYMENT_SEND_BILL -> PaymentMethod.PAYMENT_SEND_BILL
                PAYMENT_RETURN -> PaymentMethod.PAYMENT_RETURN
                PAYMENT_SEND_BILL_WAIT -> PaymentMethod.PAYMENT_SEND_BILL_WAIT
                PAYMENT_SEND_BILL_DONE -> PaymentMethod.PAYMENT_SEND_BILL_DONE
                PAYMENT_DISCOUNT -> PaymentMethod.PAYMENT_DISCOUNT
                PAYMENT_TIPS -> PaymentMethod.PAYMENT_TIPS
                else -> PaymentMethod.PAYMENT_NONE
            }
        }

        fun fromPaymentMethod(paymentMethod: PaymentMethod): EPaymentMethod
        {
            return when (paymentMethod)
            {
                PaymentMethod.PAYMENT_CASH -> PAYMENT_CASH
                PaymentMethod.PAYMENT_PIN -> PAYMENT_PIN
                PaymentMethod.PAYMENT_CREDIT_CARD -> PAYMENT_CREDIT_CARD
                PaymentMethod.PAYMENT_VISA_CARD -> PAYMENT_VISA_CARD
                PaymentMethod.PAYMENT_PAY_PAL -> PAYMENT_PAY_PAL
                PaymentMethod.PAYMENT_MASTERCARD -> PAYMENT_MASTERCARD
                PaymentMethod.PAYMENT_AMERICAN_EXPRESS -> PAYMENT_AMERICAN_EXPRESS
                PaymentMethod.PAYMENT_DEBIT -> PAYMENT_DEBIT
                PaymentMethod.PAYMENT_ZETTLE -> PAYMENT_ZETTLE
                PaymentMethod.PAYMENT_SEND_BILL -> PAYMENT_SEND_BILL
                PaymentMethod.PAYMENT_RETURN -> PAYMENT_RETURN
                PaymentMethod.PAYMENT_SEND_BILL_WAIT -> PAYMENT_SEND_BILL_WAIT
                PaymentMethod.PAYMENT_SEND_BILL_DONE -> PAYMENT_SEND_BILL_DONE
                PaymentMethod.PAYMENT_DISCOUNT -> PAYMENT_DISCOUNT
                PaymentMethod.PAYMENT_TIPS -> PAYMENT_TIPS
                else -> PAYMENT_NONE
            }
        }

        /**
         * Gets the icon resource ID for this payment method instance.
         */
        fun getIcon(paymentMethod: EPaymentMethod): Int {
            return when (paymentMethod) {
                PAYMENT_CASH -> R.drawable.ic_menu_add
                PAYMENT_PIN -> R.drawable.ic_menu_add
                PAYMENT_CREDIT_CARD -> R.drawable.ic_menu_add
                PAYMENT_VISA_CARD -> R.drawable.ic_menu_add
                PAYMENT_MASTERCARD -> R.drawable.ic_menu_add
                PAYMENT_AMERICAN_EXPRESS -> R.drawable.ic_menu_add
                PAYMENT_DEBIT -> R.drawable.ic_menu_add
                PAYMENT_PAY_PAL -> R.drawable.ic_menu_add
                PAYMENT_ZETTLE -> R.drawable.ic_menu_add
                PAYMENT_SEND_BILL -> R.drawable.ic_menu_add
                PAYMENT_SEND_BILL_WAIT -> R.drawable.ic_menu_add
                PAYMENT_SEND_BILL_DONE -> R.drawable.ic_menu_add
                PAYMENT_DISCOUNT -> R.drawable.ic_menu_add
                PAYMENT_TIPS -> R.drawable.ic_menu_add
                else -> R.drawable.ic_menu_add // A generic payment icon
            }
        }

        fun getName(paymentMethod: EPaymentMethod): String {
            return when (paymentMethod) {
                PAYMENT_CASH -> "CONTANT"
                PAYMENT_PIN -> "PIN"
                PAYMENT_CREDIT_CARD -> "CREDIT CARD"
                PAYMENT_VISA_CARD -> "VISA"
                PAYMENT_MASTERCARD -> "MASTER"
                PAYMENT_AMERICAN_EXPRESS -> "AE"
                PAYMENT_DEBIT -> "DEBIT"
                PAYMENT_PAY_PAL -> "PAYPAL"
                PAYMENT_ZETTLE -> "ZETTLE"
                PAYMENT_SEND_BILL -> "KREDIET"
                PAYMENT_SEND_BILL_WAIT -> "WAIT"
                PAYMENT_SEND_BILL_DONE -> "--"
                PAYMENT_DISCOUNT -> "KORTING"
                PAYMENT_TIPS -> "TIPS"
                else -> "" // A generic payment
            }
        }
    }
}