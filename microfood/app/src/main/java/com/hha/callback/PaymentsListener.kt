package com.hha.callback

import com.hha.framework.CItem
import com.hha.framework.CPayment

interface PaymentsListener
{
   fun onPaymentAdded(position: Int, item: CPayment)
   fun onPaymentRemoved(position: Int)
   fun onPaymentUpdated(position: Int, item: CPayment)
   fun onPaymentsCleared()
}
