package com.hha.service

import com.hha.common.Empty
import com.hha.zipcode.FindLowHighStreetRequest
import com.hha.zipcode.FindLowHighZipCodeRequest
import com.hha.zipcode.FindZipCodeRequest
import com.hha.zipcode.GetZipCodesResponse
import com.hha.zipcode.SetZipCodesRequest
import com.hha.zipcode.ZipCode
import com.hha.zipcode.ZipCodeServiceGrpcKt

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class ZipCodeService(channel: ManagedChannel) : BaseGrpcService<ZipCodeServiceGrpcKt.ZipCodeServiceCoroutineStub>(channel) {
    override val stub: ZipCodeServiceGrpcKt.ZipCodeServiceCoroutineStub by lazy {
        ZipCodeServiceGrpcKt.ZipCodeServiceCoroutineStub(channel)
    }

    fun verify(): Boolean? = runBlocking {
        try {
            stub.verify(Empty.getDefaultInstance()).result
        } catch (e: Exception) {
            null
        }
    }

    fun findAllZipCodes(): GetZipCodesResponse? = runBlocking {
        try {
            stub.findAllZipCodes(Empty.getDefaultInstance())
        } catch (e: Exception) {
            null
        }
    }

    fun findAllStreets(): GetZipCodesResponse? = runBlocking {
        try {
            stub.findAllStreets(Empty.getDefaultInstance())
        } catch (e: Exception) {
            null
        }
    }

    fun findLowHighZipCode(zipCodePart: String): GetZipCodesResponse? = runBlocking {
        try {
            val request = FindLowHighZipCodeRequest.newBuilder()
                .setZipCodePart(zipCodePart)
                .build()
            stub.findLowHighZipCode(request)
        } catch (e: Exception) {
            null
        }
    }

    fun findLowHighStreet(streetNamePart: String): GetZipCodesResponse? = runBlocking {
        try {
            val request = FindLowHighStreetRequest.newBuilder()
                .setStreetNamePart(streetNamePart)
                .build()
            stub.findLowHighStreet(request)
        } catch (e: Exception) {
            null
        }
    }

    fun findZipCode(streetName: String, city: String): GetZipCodesResponse? = runBlocking {
        try {
            val request = FindZipCodeRequest.newBuilder()
                .setStreetName(streetName)
                .setCity(city)
                .build()
            stub.findZipCode(request)
        } catch (e: Exception) {
            null
        }
    }

    fun setZipCodes(zipCodes: List<ZipCode>, clear: Boolean): Boolean = runBlocking {
        try {
            val request = SetZipCodesRequest.newBuilder()
                .addAllZipCode(zipCodes)
                .setClear(clear)
                .build()
            stub.setZipCodes(request)
            true
        } catch (e: Exception) {
            false
        }
    }
}
