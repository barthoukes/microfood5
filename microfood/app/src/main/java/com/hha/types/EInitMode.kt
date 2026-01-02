package com.hha.types

enum class EInitMode
{
   VIEW_NONE,
   VIEW_PAGE_ORDER,   // For PageOrderActivity: creates transaction and starts a timeframe
   VIEW_BILLING  // For BillOrderActivity: only loads the existing transaction
}