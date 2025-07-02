package com.hha.types

/**
 * Enum representing different kinds of subitems filtering options.
 *
 * @property ORDER_ITEMS Only regular items
 * @property ORDER_DELETED Only deleted items
 * @property ORDER_ITEMS_AND_DELETED Both regular and deleted items
 * @property ORDER_ITEMS_EXTRA Only extra items
 * @property ORDER_ITEMS_AND_EXTRA Both regular and extra items
 * @property ORDER_MAX_CURSOR Maximum cursor value (special case)
 * @property ORDER_ITEMS_AND_DELETED_AND_EXTRA Regular, deleted and extra items
 * @property ORDERS_INVALID Invalid/unknown filter
 */
enum class ESubItems {
    ORDER_ITEMS,
    ORDER_DELETED,
    ORDER_ITEMS_AND_DELETED,
    ORDER_ITEMS_EXTRA,
    ORDER_ITEMS_AND_EXTRA,
    ORDER_MAX_CURSOR,
    ORDER_ITEMS_AND_DELETED_AND_EXTRA,
    ORDERS_INVALID
}