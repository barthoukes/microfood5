package com.hha.types

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
    }
}
