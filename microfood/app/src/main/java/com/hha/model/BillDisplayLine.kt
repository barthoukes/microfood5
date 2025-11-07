package com.hha.model

import com.hha.types.CMoney
import com.hha.types.EBillBackgroundType
import com.hha.types.EBillLineType

// Using your C++ struct as a reference
data class BillDisplayLine(
   val id: Int,
   val iconResId: Int?,              // Drawable resource for the icon (e.g., R.drawable.cash_icon)
   val text: String,                 // The main text to display (e.g., "Cash", "Total")
   val amount: CMoney,               // The monetary value for the line
   val sign: String = "",            // e.g., "+", "-"
   val isCrossedOut: Boolean = false, // If the line should have a strike-through
   val showAmount: Boolean = true,   // Whether to display the amount
   val lineType: EBillLineType,      // Enum to differentiate line types (TOTAL, PAYMENT, etc.)
   val backgroundType: EBillBackgroundType // Enum for styling the background
)
