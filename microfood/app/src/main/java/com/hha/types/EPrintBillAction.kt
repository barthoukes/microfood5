package com.hha.types

/**
 * Represents the required action to be taken after checking if a bill can be printed.
 */
enum class EPrintBillAction {
   /** The process was cancelled or is invalid, navigate back to the main transaction screen. */
   NAVIGATE_TO_ASK_TRANSACTION,

   /** The bill is settled, but the table needs to be cleaned. Ask the user for confirmation. */
   ASK_WOK_CONFIRM_CLEAN_TABLE,

   /** A general confirmation is required before printing the bill or offer. */
   ASK_CONFIRM_PRINT,

   /** The process was explicitly cancelled by the user or a rule. Clean up unpaid payments. */
   CANCEL_AND_CLEANUP,

   /** All checks passed. It is safe to proceed with finalizing and printing the bill. */
   PROCEED_TO_PRINT,

   CANCEL_BILL_PRINT,

   PRINT_BILL_YES
}