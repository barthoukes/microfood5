package com.hha.types

import com.hha.common.PaymentStatus

/**
 * Represents different levels/types of payed items.
 */
enum class EPaymentStatus(val value: Int)
{
    PAY_STATUS_UNPAID(0),
    PAY_STATUS_PAID_ORDER(1),
    PAY_STATUS_PAID_BEFORE(2),
    PAY_STATUS_PAID_ORDER_BEFORE(3),
    PAY_STATUS_CANCEL(4),
    PAY_STATUS_ANY(5);

    companion object {
        /**
         * Get EPaymentStatus from an integer value
         * @param value The integer value to look up
         * @return Corresponding EPaymentStatus or null if not found
         */
        fun fromInt(value: Int): EPaymentStatus?
        {
            return values().firstOrNull { it.value == value }
        }

        /**
         * Convert from protobuf PaymentStatus to EPaymentStatus
         * @param status The protobuf PaymentStatus to convert
         * @return Corresponding EPaymentStatus or PAY_STATUS_UNPAID as default fallback
         */
        fun fromPaymentStatus(status: PaymentStatus): EPaymentStatus
        {
            return when (status)
            {
                PaymentStatus.PAY_STATUS_UNPAID -> PAY_STATUS_UNPAID
                PaymentStatus.PAY_STATUS_PAID_ORDER -> PAY_STATUS_PAID_ORDER
                PaymentStatus.PAY_STATUS_PAID_BEFORE -> PAY_STATUS_PAID_BEFORE
                PaymentStatus.PAY_STATUS_PAID_ORDER_BEFORE -> PAY_STATUS_PAID_ORDER_BEFORE
                PaymentStatus.PAY_STATUS_CANCEL -> PAY_STATUS_CANCEL
                PaymentStatus.PAY_STATUS_ANY -> PAY_STATUS_ANY
                else -> PAY_STATUS_UNPAID // default fallback for UNRECOGNIZED or unknown values
            }
        }
    }

    /**
     * Convert from EPaymentStatus to protobuf PaymentStatus
     * @param status The EPaymentStatus to convert
     * @return Corresponding protobuf PaymentStatus
     */
    fun toPaymentStatus(): PaymentStatus
    {
        return when (this)
        {
            PAY_STATUS_UNPAID -> PaymentStatus.PAY_STATUS_UNPAID
            PAY_STATUS_PAID_ORDER -> PaymentStatus.PAY_STATUS_PAID_ORDER
            PAY_STATUS_PAID_BEFORE -> PaymentStatus.PAY_STATUS_PAID_BEFORE
            PAY_STATUS_PAID_ORDER_BEFORE -> PaymentStatus.PAY_STATUS_PAID_ORDER_BEFORE
            PAY_STATUS_CANCEL -> PaymentStatus.PAY_STATUS_CANCEL
            PAY_STATUS_ANY -> PaymentStatus.PAY_STATUS_ANY
        }
    }
}
