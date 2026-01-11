// In /.../framework/CTaxProvider.kt

package com.hha.framework

import com.hha.common.TaxType
import com.hha.grpc.GrpcServiceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CTaxProvider {
    private var isLoaded: Boolean = false
    private var lowTax: Double = 0.0
    private var highTax: Double = 0.0
    private val noTax: Double  = 0.0

    // Make this function public and suspend
    suspend fun initialize() {
        if (isLoaded) return // Don't load again if already loaded

        // Ensure the gRPC call runs on an I/O-optimized thread
        withContext(Dispatchers.IO) {
            val factory = GrpcServiceFactory.createTaxService()
            lowTax = factory.getRate(TaxType.BTW_LOW)
            highTax = factory.getRate(TaxType.BTW_HIGH)
            isLoaded = true
        }
    }

    fun getTax(tax: TaxType): Double {
        // It's good practice to ensure it's loaded before trying to get a value.
        // Though in this setup, it always will be.
        if (!isLoaded) {
            // Log an error or handle this case, although it shouldn't happen
            // with the Application class setup.
        }
        return when (tax) {
            TaxType.BTW_NONE -> noTax
            TaxType.BTW_HIGH -> highTax
            else -> lowTax
        }
    }

    fun isInitialized(): Boolean = isLoaded
}
