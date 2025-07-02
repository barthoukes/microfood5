package com.hha.types

import com.hha.common.Payed

/**
 * Represents different levels/types of payed items.
 */
enum class EPayed(val value: Int) {
    PAID_CANCEL(0),
    PAID_NO(1),		    // During billing we add payments until confirmation.
    PAID_ORDER(7),		// After billing, payments become final PAYED_ORDER. These bills can be printed.
    PAID_BEFORE(8), 	// Paid before in another transaction. When add food after bill, payed before is used to set the amount already-payed. Don't add this to the accounting.
    PAID_ALL(9);		// Not for database!!

    companion object {
        /**
         * Get EPayed from an integer value
         * @param value The integer value to look up
         * @return Corresponding EPayed or null if not found
         */
        fun fromInt(value: Int): EPayed? {
            return values().firstOrNull { it.value == value }
        }

        fun fromPayed(payed: Payed): EPayed {
            return when (payed) {
                Payed.PAID_CANCEL -> PAID_CANCEL
                Payed.PAID_NO -> PAID_NO
                Payed.PAID_ORDER -> PAID_ORDER
                Payed.PAID_BEFORE -> PAID_BEFORE
                Payed.PAID_ALL -> PAID_ALL
                else -> PAID_NO
            }
        }

        fun toPayed(payed: EPayed): Payed {
            return when (payed) {
                PAID_CANCEL -> Payed.PAID_CANCEL
                PAID_NO -> Payed.PAID_NO
                PAID_ORDER -> Payed.PAID_ORDER
                PAID_BEFORE -> Payed.PAID_BEFORE
                PAID_ALL -> Payed.PAID_ALL
            }
        }
    }
}
