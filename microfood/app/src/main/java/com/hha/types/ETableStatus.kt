package com.hha.types

import com.hha.common.TableStatus

/**
 * Represents the various states a table can be in.
 * This corresponds to the 'table_status' enum from the C++ code
 * and the 'common.TableStatus' from the .proto file.
 */
enum class ETableStatus(val value: Int)
{
   TABLE_EMPTY(0),
   TABLE_EXIST(1),
   TABLE_NEW(2),
   TABLE_ERROR(3),         // No tables left
   TABLE_BUSY(4),          // For network
   TABLE_OK(5),            // Already orders
   TABLE_OPEN_PAID(6),     // Paid and busy eating
   TABLE_OPEN_NOT_PAID(7), // Not paid and busy eating
   TABLE_CLOSED(8),        // Paid closed
   TABLE_CLEAN(9),
   TABLE_FOUND(10),
   TABLE_RESERVED(11),
   UNKNOWN(-1); // A default fallback for unknown values

   companion object {
      /**
       * Converts an integer value received from gRPC or another source
       * into a [TableStatus] enum constant.
       *
       * @param value The integer status to convert.
       * @return The corresponding [TableStatus], or [UNKNOWN] if no match is found.
       */
      fun fromTableStatus(value: TableStatus): ETableStatus
      {
         return when (value)
         {
            TableStatus.TABLE_EMPTY -> TABLE_EMPTY
            TableStatus.TABLE_EXIST -> TABLE_EXIST
            TableStatus.TABLE_NEW -> TABLE_NEW
            TableStatus.TABLE_ERROR -> TABLE_ERROR
            TableStatus.TABLE_BUSY -> TABLE_BUSY
            TableStatus.TABLE_OK -> TABLE_OK
            TableStatus.TABLE_OPEN_PAID -> TABLE_OPEN_PAID
            TableStatus.TABLE_OPEN_NOT_PAID -> TABLE_OPEN_NOT_PAID
            TableStatus.TABLE_CLOSED -> TABLE_CLOSED
            TableStatus.TABLE_CLEAN -> TABLE_CLEAN
            TableStatus.TABLE_FOUND -> TABLE_FOUND
            TableStatus.TABLE_RESERVED -> TABLE_RESERVED
            else -> UNKNOWN
         }
      }
   }
}
