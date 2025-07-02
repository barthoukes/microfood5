package com.hha.types

import com.hha.common.ItemSort

/**
 * Enum representing different item sorting methods.
 */
enum class EItemSort(val value: Int) {
    SORT_NONE(0),              ///< Sort only by sequence or default
    SORT_BILL(1),              ///< Special sorting to create the bill (no extra items)
    SORT_SPLIT(2),             ///< Sorting with extra items
    SORT_ORDER(3),             ///< Normal order during ordering
    SORT_PRINTER_SORTED(4),     ///< Sorted for printer
    SORT_PRINTER_UNSORTED(5),   ///< Unsorted for printer
    SORT_PRINTER_SORTED_ALL(6), ///< Sorted all from timeframe 1 until now
    SORT_PRINTER_UNSORTED_ALL(7), ///< Unsorted all from timeframe 1 until now
    SORT_COOKING(8),           ///< Sort for kitchen cooking
    SORT_DEFAULT(9);           ///< Default sorting method

    companion object {
        /**
         * Converts an integer value to the corresponding [EItemSort].
         * @param value The integer value to convert
         * @return The matching [EItemSort] or null if invalid
         */
        fun fromInt(value: Int): EItemSort? {
            return values().firstOrNull { it.value == value }
        }

        fun toItemSort(itemSort: EItemSort): ItemSort {
            return when (itemSort) {
                SORT_NONE -> ItemSort.SORT_NONE
                SORT_BILL -> ItemSort.SORT_BILL
                SORT_SPLIT -> ItemSort.SORT_SPLIT
                SORT_ORDER -> ItemSort.SORT_ORDER
                SORT_PRINTER_SORTED -> ItemSort.SORT_PRINTER_SORTED
                SORT_PRINTER_UNSORTED -> ItemSort.SORT_PRINTER_UNSORTED
                SORT_PRINTER_SORTED_ALL -> ItemSort.SORT_PRINTER_SORTED_ALL
                SORT_PRINTER_UNSORTED_ALL -> ItemSort.SORT_PRINTER_UNSORTED_ALL
                SORT_COOKING -> ItemSort.SORT_COOKING
                SORT_DEFAULT -> ItemSort.SORT_DEFAULT
                else -> ItemSort.SORT_NONE
            }
        }

    } // Companion
}
