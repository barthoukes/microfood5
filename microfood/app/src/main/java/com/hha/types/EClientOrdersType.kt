package com.hha.types

import com.hha.common.ClientOrdersType

enum class EClientOrdersType(val value: Int)
{
    INIT(0),               // No order yet
    OPEN(1),               // Busy to order
    PAYING(16),            // Busy paying
    CLOSED(2),             // Closed order
    OPEN_PAID(3),          // Order paid but not closed yet
    EMPTY(4),              // Empty order, all removed
    ALL(5),                // All things
    PERSONNEL(6),          // Eaten by myself or my personnel
    CREDIT(7),             // Negative amount for another transaction
    CLOSED_CREDIT(8);      // Transaction closed and credited

    companion object
    {
        fun fromValue(value: Int): EClientOrdersType?
        {
            return EClientOrdersType.entries.firstOrNull { it.value == value }
        }

        fun fromClientOrdersType(value: ClientOrdersType): EClientOrdersType
        {
            return when (value)
            {
                ClientOrdersType.CLIENT_ORDER_INIT -> INIT
                ClientOrdersType.CLIENT_ORDER_OPEN -> OPEN
                ClientOrdersType.CLIENT_ORDER_CLOSED -> CLOSED
                ClientOrdersType.CLIENT_ORDER_OPEN_PAID -> OPEN_PAID
                ClientOrdersType.CLIENT_ORDER_EMPTY -> EMPTY
                ClientOrdersType.CLIENT_ORDER_ALL -> ALL
                ClientOrdersType.CLIENT_ORDER_PERSONNEL -> PERSONNEL
                ClientOrdersType.CLIENT_ORDER_CREDIT -> CREDIT
                ClientOrdersType.CLIENT_ORDER_CLOSED_CREDIT -> CLOSED_CREDIT
                ClientOrdersType.CLIENT_ORDER_PAYING -> PAYING
                else -> EMPTY
            }
        }

        fun toClientOrdersType(value: EClientOrdersType): ClientOrdersType
        {
            return when (value)
            {
                INIT -> ClientOrdersType.CLIENT_ORDER_INIT
                OPEN -> ClientOrdersType.CLIENT_ORDER_OPEN
                PAYING -> ClientOrdersType.CLIENT_ORDER_PAYING
                CLOSED -> ClientOrdersType.CLIENT_ORDER_CLOSED
                OPEN_PAID -> ClientOrdersType.CLIENT_ORDER_OPEN_PAID
                EMPTY -> ClientOrdersType.CLIENT_ORDER_EMPTY
                ALL -> ClientOrdersType.CLIENT_ORDER_ALL
                PERSONNEL -> ClientOrdersType.CLIENT_ORDER_PERSONNEL
                CREDIT -> ClientOrdersType.CLIENT_ORDER_CREDIT
                CLOSED_CREDIT -> ClientOrdersType.CLIENT_ORDER_CLOSED_CREDIT
            }
        }
    }
}
