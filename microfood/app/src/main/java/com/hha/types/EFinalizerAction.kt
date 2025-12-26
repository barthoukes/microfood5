package com.hha.types

// You can add this enum inside PageOrderActivity or in a dedicated file for types.
enum class EFinalizerAction
{
   FINALIZE_NO_ACTION, ///< Do nothing.
   FINALIZE_TO_BE_IMPLEMENTED,
   FINALIZE_NOT_IDENTIFIED, ///< Not for ne.
   FINALIZE_MODE_ASK_TABLE, ///< Now change to ask table mode.
   FINALIZE_MODE_BILLING, ///< Change to billing mode.
   FINALIZE_RESET_CHANGES, ///< Reset changes only.
   FINALIZE_SET_ENTER_PRESSED, ///< Set enter pressed.
   FINALIZE_ASK_CANCEL_REASON, ///< Finalizer sees changes and empty transaction
   FINALIZE_TA_ASK_DELAY_MINUTES,
   FINALIZE_TA_ASK_DELAY_CONTINUE,
   FINALIZE_MODAL_DIALOG_QUANTITY,
   FINALIZE_MODAL_DIALOG_QUANTITIES,
}