package com.hha.types

/**
 * Defines how the payment process is initiated or handled.
 * Corresponds to the C++ EpayingMode enum.
 */
enum class EPayingMode
{
   /** User has to manually select how to pay. */
   PAYING_MODE_MANUAL,

   /** The entire amount is paid with cash by default. */
   PAYING_MODE_ALL_CASH,

   /** The payment process starts by immediately showing the payments dialog. */
   PAYING_MODE_START_PAYMENTS_DIALOG,
}

