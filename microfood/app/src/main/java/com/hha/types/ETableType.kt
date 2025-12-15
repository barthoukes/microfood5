package com.hha.types

import com.hha.common.TableType

/**
 * Defines the different types of tables available.
 */
enum class ETableType {
   TABLE_TYPE_UNDEFINED,
   TABLE_TYPE_RESTAURANT,
   TABLE_TYPE_WOK,
   TABLE_TYPE_TAKEAWAY,
   TABLE_TYPE_EATINSIDE,
   TABLE_TYPE_NOFOOD;

   companion object {
      fun fromTableType(value: TableType): ETableType {
         return when (value) {
            TableType.TABLE_TYPE_UNDEFINED -> TABLE_TYPE_UNDEFINED
            TableType.TABLE_TYPE_RESTAURANT -> TABLE_TYPE_RESTAURANT
            TableType.TABLE_TYPE_WOK -> TABLE_TYPE_WOK
            TableType.TABLE_TYPE_TAKEAWAY -> TABLE_TYPE_TAKEAWAY
            TableType.TABLE_TYPE_EATINSIDE -> TABLE_TYPE_EATINSIDE
            TableType.TABLE_TYPE_NOFOOD -> TABLE_TYPE_NOFOOD
            else -> TABLE_TYPE_UNDEFINED
         }
      }
   }
}
