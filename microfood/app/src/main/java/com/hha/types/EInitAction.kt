package com.hha.types

/**
 * Represents the required UI action after the TransactionViewModel's onInit logic is executed.
 */
enum class EInitAction {
   /** No specific UI action is required. */
   INIT_ACTION_NOTHING,

   /** The UI should prompt the user to select a payment/money type. */
   INIT_ACTION_ASK_MONEY,

   /** The UI should close or finish the current activity/screen. */
   INIT_ACTION_FINISH,

   INIT_ACTION_BILL_PAYMENTS,
}
