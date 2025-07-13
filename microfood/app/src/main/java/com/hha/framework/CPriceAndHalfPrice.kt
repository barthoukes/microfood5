package com.hha.framework

import com.hha.types.CMoney

data class CPriceAndHalfPrice(
    var fullPrice: CMoney = CMoney(0),
    var halfPrice: CMoney = CMoney(0),
) {
    val isValid: Boolean
        get() = true
}


fun CPriceAndHalfPrice.clear() {
    fullPrice = CMoney(0)
    halfPrice = CMoney(0)
}
