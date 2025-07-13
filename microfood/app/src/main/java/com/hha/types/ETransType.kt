package com.hha.types

import com.hha.common.TransType
import java.util.EnumSet

/**
 * Defines transaction types for restaurant operations
 */
enum class ETransType {
    TRANS_TYPE_UNDEFINED,    // Not defined yet
    TRANS_TYPE_NOCHANGE,     // No change to existing type
    TRANS_TYPE_SITIN,        // Restaurant table
    TRANS_TYPE_DELIVERY,     // Delivery order
    TRANS_TYPE_SHOP,         // Just sales (no seating)
    TRANS_TYPE_TAKEAWAY,     // Takeaway order
    TRANS_TYPE_TAKEAWAY_PHONE, // Phone order to collect later
    TRANS_TYPE_EAT_INSIDE,   // Eat inside (alternative to SITIN)
    TRANS_TYPE_RECHAUD,      // Buffet/rechaud style
    TRANS_TYPE_WOK,          // Wok station
    TRANS_TYPE_ALL;          // Special case for queries

    companion object {
        private val TAKEAWAY_PRICE_TYPES = EnumSet.of(
            TRANS_TYPE_TAKEAWAY,
            TRANS_TYPE_DELIVERY,
            TRANS_TYPE_EAT_INSIDE,
            TRANS_TYPE_TAKEAWAY_PHONE
        )


        /**
         * Converts from TransType to ETransType
         */
        fun fromTransType(type: TransType): ETransType {
            return when (type) {
                TransType.TRANS_TYPE_UNDEFINED -> TRANS_TYPE_UNDEFINED
                TransType.TRANS_TYPE_NOCHANGE -> TRANS_TYPE_NOCHANGE
                TransType.TRANS_TYPE_SITIN -> TRANS_TYPE_SITIN
                TransType.TRANS_TYPE_DELIVERY -> TRANS_TYPE_DELIVERY
                TransType.TRANS_TYPE_SHOP -> TRANS_TYPE_SHOP
                TransType.TRANS_TYPE_TAKEAWAY -> TRANS_TYPE_TAKEAWAY
                TransType.TRANS_TYPE_TAKEAWAY_PHONE -> TRANS_TYPE_TAKEAWAY_PHONE
                TransType.TRANS_TYPE_EAT_INSIDE -> TRANS_TYPE_EAT_INSIDE
                TransType.TRANS_TYPE_RECHAUD -> TRANS_TYPE_RECHAUD
                TransType.TRANS_TYPE_WOK -> TRANS_TYPE_WOK
                TransType.TRANS_TYPE_ALL -> TRANS_TYPE_ALL
                else -> TRANS_TYPE_UNDEFINED
            }
        }

        /**
         * Safe conversion from ordinal/Int with fallback to UNDEFINED
         */
        fun fromInt(value: Int): ETransType {
            return values().getOrElse(value) { TRANS_TYPE_UNDEFINED }
        }

       fun useTakeawayPrices(value: ETransType): Boolean = TAKEAWAY_PRICE_TYPES.contains(value)
    }
}