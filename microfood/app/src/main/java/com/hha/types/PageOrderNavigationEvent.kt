package com.hha.types

data class PageOrderNavigationEvent(
   val transactionId: Int,
   val fromBilling: Boolean,
   val newTimeFrame: Boolean
)
