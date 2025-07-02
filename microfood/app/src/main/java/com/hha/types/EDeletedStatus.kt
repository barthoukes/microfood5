package com.hha.types

import com.hha.common.DeletedStatus
import com.hha.common.ItemSort
import com.hha.types.EItemSort.SORT_BILL
import com.hha.types.EItemSort.SORT_COOKING
import com.hha.types.EItemSort.SORT_DEFAULT
import com.hha.types.EItemSort.SORT_NONE
import com.hha.types.EItemSort.SORT_ORDER
import com.hha.types.EItemSort.SORT_PRINTER_SORTED
import com.hha.types.EItemSort.SORT_PRINTER_SORTED_ALL
import com.hha.types.EItemSort.SORT_PRINTER_UNSORTED
import com.hha.types.EItemSort.SORT_PRINTER_UNSORTED_ALL
import com.hha.types.EItemSort.SORT_SPLIT

/**
 * Status for deleted items.
 */
enum class EDeletedStatus(val value: Int) {
    DELETE_MOVE_OTHER_TABLE(-9),          ///< Move to other table.
    DELETE_PRICE_IMMEDIATE(-8),           ///< Immediately change price.
    DELETE_PORTION_IMMEDIATE(-7),         ///< Quick change portion after item select.
    DELETE_PORTION_AFTER_PRINTING(-6),    ///< Change portion after printing.
    DELETE_CANCEL_TIMEFRAME(-5),          ///< Time frame removed.
    DELETE_REMOVE_AFTER_PRINTING(-4),     ///< Remove after printing.
    DELETE_QUANTITY_AFTER_PRINTING(-3),   ///< First print, then delete.
    DELETE_PRICE_AFTER_PRINTING(-2),      ///< Price change after printing.
    DELETE_REMOVE_IMMEDIATE(-1),          ///< Delete immediately in same time frame.
    DELETE_NOT(0),                        ///< Normal order.
    DELETE_CAUSE_CHANGE_QUANTITY(1),      ///< Old way of quantity remove.
    DELETE_CAUSE_CHANGE_ITEM(2),          ///< Replace item with new item.
    DELETE_CAUSE_DELETED(3),             ///< Old way of delete items.
    DELETE_CAUSE_MOVE_TABLE(4),          ///< Move to other table.
    DELETE_CAUSE_CHANGE_PRICE(5),        ///< Change price old way.
    DELETE_CAUSE_CHANGE_PORTION_OLD(6),  ///< Old way of change portion. Neglect items.
    DELETE_CAUSE_ESCAPE_ORDER(7);         ///< Cancel everything we ordered now.

    companion object {
        /**
         * Converts from hha.common.DeletedStatus to EDeletedStatus
         * @param deletedStatus The common DeletedStatus to convert
         * @return Corresponding EDeletedStatus value
         * @throws IllegalArgumentException if the value is not recognized
         */
        fun fromDeletedStatus(deletedStatus: com.hha.common.DeletedStatus): EDeletedStatus {
            return when (deletedStatus) {
                DeletedStatus.DELETE_NOT -> DELETE_NOT
                DeletedStatus.DELETE_MOVE_OTHER_TABLE -> DELETE_MOVE_OTHER_TABLE
                DeletedStatus.DELETE_PRICE_IMMEDIATE -> DELETE_PRICE_IMMEDIATE
                DeletedStatus.DELETE_PORTION_IMMEDIATE -> DELETE_PORTION_IMMEDIATE
                DeletedStatus.DELETE_PORTION_AFTER_PRINTING -> DELETE_PORTION_AFTER_PRINTING
                DeletedStatus.DELETE_CANCEL_TIMEFRAME -> DELETE_CANCEL_TIMEFRAME
                DeletedStatus.DELETE_REMOVE_AFTER_PRINTING -> DELETE_REMOVE_AFTER_PRINTING
                DeletedStatus.DELETE_QUANTITY_AFTER_PRINTING -> DELETE_QUANTITY_AFTER_PRINTING
                DeletedStatus.DELETE_PRICE_AFTER_PRINTING -> DELETE_PRICE_AFTER_PRINTING
                DeletedStatus.DELETE_REMOVE_IMMEDIATE -> DELETE_REMOVE_IMMEDIATE
                DeletedStatus.DELETE_CAUSE_CHANGE_QUANTITY -> DELETE_CAUSE_CHANGE_QUANTITY
                DeletedStatus.DELETE_CAUSE_CHANGE_ITEM -> DELETE_CAUSE_CHANGE_ITEM
                DeletedStatus.DELETE_CAUSE_DELETED -> DELETE_CAUSE_DELETED
                DeletedStatus.DELETE_CAUSE_MOVE_TABLE -> DELETE_CAUSE_MOVE_TABLE
                DeletedStatus.DELETE_CAUSE_CHANGE_PRICE -> DELETE_CAUSE_CHANGE_PRICE
                DeletedStatus.DELETE_CAUSE_CHANGE_PORTION_OLD -> DELETE_CAUSE_CHANGE_PORTION_OLD
                DeletedStatus.DELETE_CAUSE_ESCAPE_ORDER -> DELETE_CAUSE_ESCAPE_ORDER
                else -> DELETE_NOT
            }
        }

        fun toDeletedStatus(deletedStatus: EDeletedStatus): DeletedStatus {
            return when (deletedStatus) {
                DELETE_NOT -> DeletedStatus.DELETE_NOT
                DELETE_MOVE_OTHER_TABLE -> DeletedStatus.DELETE_MOVE_OTHER_TABLE
                DELETE_PRICE_IMMEDIATE -> DeletedStatus.DELETE_PRICE_IMMEDIATE
                DELETE_PORTION_IMMEDIATE -> DeletedStatus.DELETE_PORTION_IMMEDIATE
                DELETE_PORTION_AFTER_PRINTING -> DeletedStatus.DELETE_PORTION_AFTER_PRINTING
                DELETE_CANCEL_TIMEFRAME -> DeletedStatus.DELETE_CANCEL_TIMEFRAME
                DELETE_REMOVE_AFTER_PRINTING -> DeletedStatus.DELETE_REMOVE_AFTER_PRINTING
                DELETE_QUANTITY_AFTER_PRINTING -> DeletedStatus.DELETE_QUANTITY_AFTER_PRINTING
                DELETE_PRICE_AFTER_PRINTING -> DeletedStatus.DELETE_PRICE_AFTER_PRINTING
                DELETE_REMOVE_IMMEDIATE -> DeletedStatus.DELETE_REMOVE_IMMEDIATE
                DELETE_CAUSE_CHANGE_QUANTITY -> DeletedStatus.DELETE_CAUSE_CHANGE_QUANTITY
                DELETE_CAUSE_CHANGE_ITEM -> DeletedStatus.DELETE_CAUSE_CHANGE_ITEM
                DELETE_CAUSE_DELETED -> DeletedStatus.DELETE_CAUSE_DELETED
                DELETE_CAUSE_MOVE_TABLE -> DeletedStatus.DELETE_CAUSE_MOVE_TABLE
                DELETE_CAUSE_CHANGE_PRICE -> DeletedStatus.DELETE_CAUSE_CHANGE_PRICE
                DELETE_CAUSE_CHANGE_PORTION_OLD -> DeletedStatus.DELETE_CAUSE_CHANGE_PORTION_OLD
                DELETE_CAUSE_ESCAPE_ORDER -> DeletedStatus.DELETE_CAUSE_ESCAPE_ORDER
                else -> DeletedStatus.DELETE_NOT
            }
        }

        /**
         * Safely converts from hha.common.DeletedStatus to EDeletedStatus with a default fallback
         * @param deletedStatus The common DeletedStatus to convert
         * @param default The default value to return if conversion fails (defaults to DELETE_NOT)
         * @return Corresponding EDeletedStatus value or default if not found
         */
        fun fromDeletedStatusSafe(
            deletedStatus: com.hha.common.DeletedStatus,
            default: EDeletedStatus = DELETE_NOT
        ): EDeletedStatus {
            return try {
                fromDeletedStatus(deletedStatus)
            } catch (e: IllegalArgumentException) {
                default
            }
        }
    }
}