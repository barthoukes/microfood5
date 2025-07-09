package com.hha.service

import com.hha.common.Empty
import com.hha.common.TaxType
import com.hha.tax.SetRateRequest
import com.hha.tax.TaxServiceGrpcKt
import com.hha.tax.GetRateRequest
import com.hha.tax.GetNameRequest
import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class TaxService(channel: ManagedChannel) :
    BaseGrpcService<TaxServiceGrpcKt.TaxServiceCoroutineStub>(channel) {
    override val stub: TaxServiceGrpcKt.TaxServiceCoroutineStub =
        TaxServiceGrpcKt.TaxServiceCoroutineStub(channel)

    fun setRate(taxType: TaxType, percentage: Double): Boolean = runBlocking {
        try {
            val request = SetRateRequest.newBuilder()
                .setTaxType(taxType)
                .setPercentage(percentage)
                .build()
            stub.setRate(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getName(taxType: TaxType): String = runBlocking {
        try {
            val request = GetNameRequest.newBuilder()
                .setTaxType(taxType)
                .build()
            stub.getName(request).name
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    fun verify(): Boolean = runBlocking {
        try {
            stub.verify(Empty.getDefaultInstance()).result
        } catch (e: Exception) {
            false
        }
    }

    fun getRate(taxType: TaxType): Double = runBlocking {
        try {
            val request = GetRateRequest.newBuilder()
                .setTaxType(taxType)
                .build()
            stub.getRate(request).percentage
        } catch (e: Exception) {
            -1.0 // or handle error appropriately
        }
    }

    fun dump(): String = runBlocking {
        try {
            stub.dump(Empty.getDefaultInstance()).fileName
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}