package com.hha.framework

import com.hha.common.TaxType
import com.hha.grpc.GrpcServiceFactory
import com.hha.types.ETaxType

object CTaxProvider {
    private var isLoaded: Boolean = false
    private var lowTax: Double = 0.0
    private var highTax: Double = 0.0
    private val noTax: Double  = 0.0

    private suspend fun loadTaxRates() {
        if (!isLoaded) {
            val factory = GrpcServiceFactory.createTaxService()
            lowTax = factory.getRate(TaxType.BTW_LOW)
            highTax = factory.getRate(TaxType.BTW_HIGH)
            isLoaded = true
        }
    }

    fun getTax(tax: TaxType): Double =
        when (tax) {
            TaxType.BTW_NONE -> noTax
            TaxType.BTW_HIGH -> highTax
            else -> lowTax
        }

    fun isInitialized(): Boolean = isLoaded
}