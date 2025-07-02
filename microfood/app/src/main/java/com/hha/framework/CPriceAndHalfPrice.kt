package com.hha.framework

import com.hha.types.CMoney

data class CPriceAndHalfPrice(
    var full_price: CMoney = CMoney(0),
    var half_price: CMoney = CMoney(0),
    var valid: Boolean = true
)

fun CPriceAndHalfPrice.clear() {
    full_price = CMoney(0)
    half_price = CMoney(0)
    valid = false
}
