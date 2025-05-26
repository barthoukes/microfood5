package com.hha.types

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
    DELETE_CAUSE_ESCAPE_ORDER(7)         ///< Cancel everything we ordered now.
}