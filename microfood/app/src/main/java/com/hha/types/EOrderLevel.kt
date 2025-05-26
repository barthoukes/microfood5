package com.hha.types

/**
 * Represents different levels/types of items in an ordering system.
 */
enum class EOrderLevel(val value: Int) {
    LEVEL_ITEMGROUP(0),          ///< Item group level
    LEVEL_COMBINE_ALL(1),        ///< Combine all items
    LEVEL_ITEM(2),               ///< Normal item
    LEVEL_EXTRA(3),             ///< Extra food like bami, nasi
    LEVEL_CHARITY(4),           ///< Charity items
    LEVEL_ASK_CLUSTER(5),       ///< Ask cluster level
    LEVEL_SPICES(6),            ///< Extra spices like pepper, salt
    LEVEL_TWIN_ITEM(7),         ///< Combine with previous item
    LEVEL_FREE(8),              ///< Free items
    LEVEL_INFO(9),              ///< Informational items
    LEVEL_MINUTES_PRICE(10),    ///< Price per minute for a customer
    LEVEL_SYSTEM(11),           ///< System-level items
    LEVEL_SEPARATOR(12),        ///< Visual separator
    LEVEL_OUTOFSTOCK(13),       ///< Out of stock items
    LEVEL_SUB_EXTRA(14),        ///< Extra food for "Extra food", like extra sauce on the satay
    LEVEL_SUB_SPICES(15),       ///< Extra spices for "Extra food" and "Extra spices", like extra hot for the sauce
    LEVEL_SUB_ITEM(16),         ///< Extra item for "Extra food" or "Spices"
    LEVEL_PERSON(80),           ///< Person-level items
    LEVEL_NOTHING(100),         ///< Represents nothing/none
    LEVEL_ZERO(25);             ///< Special zero level

    companion object {
        /**
         * Get EOrderLevel from an integer value
         * @param value The integer value to look up
         * @return Corresponding EOrderLevel or null if not found
         */
        fun fromInt(value: Int): EOrderLevel? {
            return values().firstOrNull { it.value == value }
        }
    }
}