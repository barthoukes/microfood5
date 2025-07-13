package com.hha.types

import android.R

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
        fun getDisplayName(paymentMethod: EPaymentMethod): String {
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
//                PAYMENT_CASH -> R.drawable.ic_cash
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
    }
}