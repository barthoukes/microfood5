package com.hha.callback

import com.hha.types.CMoney
import com.hha.types.EPaymentMethod

interface PaymentEnteredListener
{
   fun onPaymentEntered(paymentMethod: EPaymentMethod,
                        amount: CMoney)
}
