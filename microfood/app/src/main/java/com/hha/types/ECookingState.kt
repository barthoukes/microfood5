package com.hha.types

/// @brief State where the dialog is in.
enum class ECookingState(val value: Short) {
   COOKING_STATE_UNDEFINED(0),
   COOKING_ORDER_ENTRY(1),
   ENTER_CLUSTER_STATE(100),
   COOKING_IN_KITCHEN_DISPLAY(101),
   COOKING_READY_KITCHEN(102),
   COOKING_DONE(108),
   UNRECOGNIZED(-1);

   companion object {
      /**
       * Get EnterState from a short value
       * @param value The integer value to look up
       * @return Corresponding EnterState or null if not found
       */
      fun fromInt(value: Short): ECookingState? {
         return ECookingState.entries.firstOrNull { it.value == value }
      }
   }
}