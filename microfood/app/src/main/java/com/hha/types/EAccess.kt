package com.hha.types

import com.hha.personnel.Access

/**
 * Enum representing different tax types
 */
enum class EAccess(val value: Int) {
   ACCESS_INVALID_KEY(0),          ///< 21%
   ACCESS_NO_KEY(78),           ///< 9%
   ACCESS_EMPLOYEE_KEY(87),               ///< No tax
   ACCESS_MANAGER_KEY(77),
   ACCESS_SERVICE_KEY(83);
   ///< Unused

   companion object {
      /**
       * Converts an integer value to the corresponding [EAccess].
       * @param value The integer value to convert
       * @return The matching [EAccess] or LOW if invalid
       */
      fun fromInt(value: Int): EAccess
      {
         return when(value)
         {
            78 -> ACCESS_NO_KEY
            77 -> ACCESS_EMPLOYEE_KEY
            67 -> ACCESS_MANAGER_KEY
            83 -> ACCESS_SERVICE_KEY
            else -> ACCESS_INVALID_KEY
         }
      }

      fun fromAccess(value: Access) : EAccess
      {
         return when (value)
         {
            Access.EMPLOYEE_KEY -> ACCESS_EMPLOYEE_KEY;
            Access.MANAGER_KEY -> ACCESS_MANAGER_KEY
            Access.SERVICE_KEY -> ACCESS_SERVICE_KEY
            else -> ACCESS_INVALID_KEY
         }
      }

   } // Companion

   fun toAccess(): Access
   {
      return when (this)
      {
         ACCESS_EMPLOYEE_KEY -> Access.EMPLOYEE_KEY
         ACCESS_MANAGER_KEY -> Access.MANAGER_KEY
         ACCESS_NO_KEY -> Access.NO_KEY
         ACCESS_SERVICE_KEY -> Access.SERVICE_KEY
         else -> Access.INVALID_KEY
      }
   }
}
