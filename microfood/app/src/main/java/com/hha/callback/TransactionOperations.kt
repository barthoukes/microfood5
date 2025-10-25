package com.hha.callback

import com.hha.types.CMoney
import com.hha.types.EPaymentMethod
import com.hha.types.EPaymentStatus

// Interface defining the payment functions we want to expose
interface TransactionOperations
{
   fun getTotalTransaction(): CMoney
   fun getCustomerId(): Int
}