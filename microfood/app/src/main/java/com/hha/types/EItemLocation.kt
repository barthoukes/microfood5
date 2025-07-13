package com.hha.types

/**
 * Item location for printing. Array should be able to use for indexing, start from 0 !!
 */
enum class EItemLocation(val value: Int) {
    ITEM_KITCHEN(0),       ///< Item belongs to kitchen.
    ITEM_DRINKS(1),        ///< Drinks printer location.
    ITEM_BAR(2),           ///< Bar printer location.
    ITEM_OTHERS(3),        ///< Other place in kitchen.
    ITEM_KITCHEN2(4),      ///< 2nd Kitchen.
    ITEM_SUSHI(5),         ///< Sushi.
    ITEM_NONFOOD(6),       ///< Not food.
    ITEM_KITCHEN3(7),      ///< 3rd Kitchen
    ITEM_KITCHEN4(8),      ///< 4th Kitchen
    ITEM_KITCHEN5(9),      ///< 5th Kitchen == Kitchen collect
    ITEM_MAX_LOCATION(10), ///< Maximum item location.
    ITEM_ALL_LOCATIONS(11), ///< Total all locations.
    ITEM_ALL_KITCHEN(12),  ///< All Kitchen items.
    ITEM_NO_LOCATION(13),
    ITEM_ARRAY_SIZE(14);

    companion object {
        /**
         * Convert an Int to EItemLocation
         * @param value The integer value to convert
         * @return Corresponding EItemLocation or ITEM_NONFOOD if not found
         */
        fun fromInt(value: Int): EItemLocation {
            return values().firstOrNull { it.value == value } ?: ITEM_NONFOOD
        }

        /**
         * Convert location to printers
         * @param loc What printer?
         * @return Printer bits set in integer
         */
        fun location2Locations(loc: EItemLocation): Int {
            return if (loc == ITEM_ALL_KITCHEN) {
                (16 shl ITEM_KITCHEN.value) or
                        (16 shl ITEM_KITCHEN2.value) or
                        (16 shl ITEM_KITCHEN3.value) or
                        (16 shl ITEM_KITCHEN4.value) or
                        (16 shl ITEM_KITCHEN5.value)
            } else {
                16 shl loc.value
            }
        }

        /**
         * Check if location is present in the bitmask
         */
        fun hasLocation(n: Int, loc: EItemLocation): Boolean {
            return (n and location2Locations(loc)) != 0
        }

        /**
         * Get the first available location from the bitmask
         */
        fun getFirstLocation(locations: Int): EItemLocation {
            var locs = locations
            if (locs < 16) {
                locs = location2Locations(EItemLocation.values()[locs])
            }

            for (n in 0 until ITEM_MAX_LOCATION.value) {
                val location = EItemLocation.values()[n]
                if (hasLocation(locs, location)) {
                    return location
                }
            }
            return ITEM_NONFOOD
        }
    }
}
