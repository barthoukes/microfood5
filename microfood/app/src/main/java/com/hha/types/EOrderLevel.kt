package com.hha.types

import com.hha.common.OrderLevel

/**
 * Represents different levels/types of items in an ordering system.
 * Similar to the protobuf OrderLevel but in Kotlin enum form.
 */
enum class EOrderLevel(val value: Int) {
    LEVEL_ITEMGROUP(0),          ///< Item group level
    LEVEL_COMBINE_ALL(1),        ///< Combine all items
    LEVEL_ITEM(2),               ///< Normal item
    LEVEL_EXTRA(3),              ///< Extra food like bami, nasi
    LEVEL_CHARITY(4),            ///< Charity items
    LEVEL_ASK_CLUSTER(5),        ///< Ask cluster level
    LEVEL_SPICES(6),             ///< Extra spices like pepper, salt
    LEVEL_TWIN_ITEM(7),          ///< Combine with previous item
    LEVEL_FREE(8),               ///< Free items
    LEVEL_INFO(9),               ///< Informational items
    LEVEL_MINUTES_PRICE(10),     ///< Price per minute for a customer
    LEVEL_SYSTEM(11),            ///< System-level items
    LEVEL_SEPARATOR(12),         ///< Visual separator
    LEVEL_OUTOFSTOCK(13),        ///< Out of stock items
    LEVEL_SUB_EXTRA(14),         ///< Extra food for "Extra food", like extra sauce on the satay
    LEVEL_SUB_SPICES(15),        ///< Extra spices for "Extra food" and "Extra spices", like extra hot for the sauce
    LEVEL_SUB_ITEM(16),          ///< Extra item for "Extra food" or "Spices"
    LEVEL_PERSON(80),            ///< Person-level items
    LEVEL_NOTHING(100),          ///< Represents nothing/none
    LEVEL_ZERO(25);              ///< Special zero level

    companion object {
        /**
         * Get EOrderLevel from an integer value
         * @param value The integer value to look up
         * @return Corresponding EOrderLevel or null if not found
         */
        fun fromInt(value: Int?): EOrderLevel {
            return value?.let { nonNullValue ->
                values().firstOrNull { it.value == nonNullValue }
            } ?: LEVEL_ITEM
        }
        fun fromOrderLevel(level: OrderLevel): EOrderLevel {
            return when (level) {
                OrderLevel.LEVEL_ITEMGROUP -> LEVEL_ITEMGROUP
                OrderLevel.LEVEL_COMBINE_ALL -> LEVEL_COMBINE_ALL
                OrderLevel.LEVEL_ITEM -> LEVEL_ITEM
                OrderLevel.LEVEL_EXTRA -> LEVEL_EXTRA
                OrderLevel.LEVEL_CHARITY -> LEVEL_CHARITY
                OrderLevel.LEVEL_ASK_CLUSTER -> LEVEL_ASK_CLUSTER
                OrderLevel.LEVEL_SPICES -> LEVEL_SPICES
                OrderLevel.LEVEL_TWIN_ITEM -> LEVEL_TWIN_ITEM
                OrderLevel.LEVEL_FREE -> LEVEL_FREE
                OrderLevel.LEVEL_INFO -> LEVEL_INFO
                OrderLevel.LEVEL_MINUTES_PRICE -> LEVEL_MINUTES_PRICE
                OrderLevel.LEVEL_SYSTEM -> LEVEL_SYSTEM
                OrderLevel.LEVEL_SEPARATOR -> LEVEL_SEPARATOR
                OrderLevel.LEVEL_OUTOFSTOCK -> LEVEL_OUTOFSTOCK
                OrderLevel.LEVEL_SUB_EXTRA -> LEVEL_SUB_EXTRA
                OrderLevel.LEVEL_SUB_SPICES -> LEVEL_SUB_SPICES
                OrderLevel.LEVEL_SUB_ITEM -> LEVEL_SUB_ITEM
                OrderLevel.LEVEL_PERSON -> LEVEL_PERSON
                OrderLevel.LEVEL_NOTHING -> LEVEL_NOTHING
                OrderLevel.LEVEL_ZERO -> LEVEL_ZERO
                else -> LEVEL_ITEM // default fallback
            }
        }

        fun toOrderLevel(level: EOrderLevel) : OrderLevel {
            return when (level) {
                LEVEL_ITEMGROUP -> OrderLevel.LEVEL_ITEMGROUP
                LEVEL_COMBINE_ALL -> OrderLevel.LEVEL_COMBINE_ALL
                LEVEL_ITEM -> OrderLevel.LEVEL_ITEM
                LEVEL_EXTRA -> OrderLevel.LEVEL_EXTRA
                LEVEL_CHARITY -> OrderLevel.LEVEL_CHARITY
                LEVEL_ASK_CLUSTER -> OrderLevel.LEVEL_ASK_CLUSTER
                LEVEL_SPICES -> OrderLevel.LEVEL_SPICES
                LEVEL_TWIN_ITEM -> OrderLevel.LEVEL_TWIN_ITEM
                LEVEL_FREE -> OrderLevel.LEVEL_FREE
                LEVEL_INFO -> OrderLevel.LEVEL_INFO
                LEVEL_MINUTES_PRICE -> OrderLevel.LEVEL_MINUTES_PRICE
                LEVEL_SYSTEM -> OrderLevel.LEVEL_SYSTEM
                LEVEL_SEPARATOR -> OrderLevel.LEVEL_SEPARATOR
                LEVEL_OUTOFSTOCK -> OrderLevel.LEVEL_OUTOFSTOCK
                LEVEL_SUB_EXTRA -> OrderLevel.LEVEL_SUB_EXTRA
                LEVEL_SUB_SPICES -> OrderLevel.LEVEL_SUB_SPICES
                LEVEL_SUB_ITEM -> OrderLevel.LEVEL_SUB_ITEM
                LEVEL_PERSON -> OrderLevel.LEVEL_PERSON
                LEVEL_NOTHING -> OrderLevel.LEVEL_NOTHING
                LEVEL_ZERO -> OrderLevel.LEVEL_ZERO
                // else -> OrderLevel.LEVEL_ITEM
            }
        }
    }
}