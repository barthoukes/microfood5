package com.hha.framework

import com.hha.types.CMoney
import com.hha.types.EPaymentMethod

class CPayment {
    var id : Int = 0
    var time : String = "2012-01-01 00:00:00"
    var paymentType : EPaymentMethod = EPaymentMethod.PAYMENT_CASH
    var amount = CMoney(0)
}