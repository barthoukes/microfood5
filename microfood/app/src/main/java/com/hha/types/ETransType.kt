package com.hha.types

import com.hha.common.TransType
import com.hha.dialog.Translation
import java.util.EnumSet
import tech.hha.microfood.R

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

    /** Opposite of fromTransType */
    fun toTransType(): TransType
    {
        return when(this)
        {
            TRANS_TYPE_UNDEFINED -> TransType.TRANS_TYPE_UNDEFINED
            TRANS_TYPE_RECHAUD -> TransType.TRANS_TYPE_RECHAUD
            TRANS_TYPE_WOK -> TransType.TRANS_TYPE_WOK
            TRANS_TYPE_EAT_INSIDE -> TransType.TRANS_TYPE_EAT_INSIDE
            TRANS_TYPE_TAKEAWAY_PHONE -> TransType.TRANS_TYPE_TAKEAWAY_PHONE
            TRANS_TYPE_TAKEAWAY -> TransType.TRANS_TYPE_TAKEAWAY
            TRANS_TYPE_SHOP -> TransType.TRANS_TYPE_SHOP
            TRANS_TYPE_DELIVERY -> TransType.TRANS_TYPE_DELIVERY
            TRANS_TYPE_SITIN -> TransType.TRANS_TYPE_SITIN
            TRANS_TYPE_NOCHANGE -> TransType.TRANS_TYPE_NOCHANGE
            TRANS_TYPE_ALL -> TransType.TRANS_TYPE_ALL
        }
    }

    /**
     * Converts the enum value to its user-facing, translated text representation.
     */
    override fun toString(): String {
        return when (this) {
            TRANS_TYPE_UNDEFINED -> "UNDEFINED" // Or Translation.get(TextId.TEXT_UNDEFINED)
            TRANS_TYPE_NOCHANGE -> "NO CHANGE"   // Or a translated equivalent
            TRANS_TYPE_SITIN -> Translation.get(Translation.TextId.TEXT_TABLE)
            TRANS_TYPE_DELIVERY -> Translation.get(Translation.TextId.TEXT_ORDER_TELEPHONE)
            TRANS_TYPE_SHOP -> Translation.get(Translation.TextId.TEXT_SHOP)
            TRANS_TYPE_TAKEAWAY -> Translation.get(Translation.TextId.TEXT_TAKEAWAY)
            TRANS_TYPE_TAKEAWAY_PHONE -> Translation.get(Translation.TextId.TEXT_ORDER_TELEPHONE)
            TRANS_TYPE_EAT_INSIDE -> Translation.get(Translation.TextId.TEXT_EAT_INSIDE)
            TRANS_TYPE_RECHAUD -> Translation.get(Translation.TextId.TEXT_RECHAUD)
            TRANS_TYPE_WOK -> Translation.get(Translation.TextId.TEXT_WOK)
            else -> ""
        }
    }

    fun toImageResource(): Int {
        return when (this) {
            TRANS_TYPE_UNDEFINED, TRANS_TYPE_NOCHANGE -> 0
            TRANS_TYPE_SITIN -> R.drawable.sitin_yellow
            TRANS_TYPE_DELIVERY -> R.drawable.icon_takephone_yellow
            TRANS_TYPE_SHOP -> R.drawable.bag_yellow
            TRANS_TYPE_TAKEAWAY -> R.drawable.bag_yellow
            TRANS_TYPE_TAKEAWAY_PHONE -> R.drawable.icon_takephone_yellow
            TRANS_TYPE_RECHAUD -> R.drawable.icon_wok_yellow
            TRANS_TYPE_WOK -> R.drawable.icon_wok_yellow
            TRANS_TYPE_EAT_INSIDE -> R.drawable.icon_inside_yellow
            else -> R.drawable.bag_yellow
        }
    }

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
            return ETransType.entries.toTypedArray()
                .getOrElse(value) { TRANS_TYPE_UNDEFINED }
        }

       fun useTakeawayPrices(value: ETransType): Boolean = TAKEAWAY_PRICE_TYPES.contains(value)
    }
}