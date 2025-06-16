package com.hha.types

/**
 * Defines transaction types for restaurant operations
 */
//enum class TransTypeDontUse {
//    TRANS_TYPE_UNDEFINED,    // Not defined yet
//    TRANS_TYPE_NOCHANGE,     // No change to existing type
//    TRANS_TYPE_SITIN,        // Restaurant table
//    TRANS_TYPE_DELIVERY,     // Delivery order
//    TRANS_TYPE_SHOP,         // Just sales (no seating)
//    TRANS_TYPE_TAKEAWAY,     // Takeaway order
//    TRANS_TYPE_TAKEAWAY_PHONE, // Phone order to collect later
//    TRANS_TYPE_EAT_INSIDE,   // Eat inside (alternative to SITIN)
//    TRANS_TYPE_RECHAUD,      // Buffet/rechaud style
//    TRANS_TYPE_WOK,          // Wok station
//    TRANS_TYPE_ALL;          // Special case for queries
//
//    companion object {
//        /**
//         * Safe conversion from ordinal/Int with fallback to UNDEFINED
//         */
//        fun fromInt(value: Int): TransTypeDontUse {
//            return values().getOrElse(value) { TRANS_TYPE_UNDEFINED }
//        }
//    }
//}