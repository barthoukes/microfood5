package com.hha.service

import com.hha.address.AddressServiceGrpcKt
import com.hha.address.AddressLine
import com.hha.common.Empty
import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class AddressService(channel: ManagedChannel) :
    BaseGrpcService<AddressServiceGrpcKt.AddressServiceCoroutineStub>(channel) {
    override val stub: AddressServiceGrpcKt.AddressServiceCoroutineStub =
        AddressServiceGrpcKt.AddressServiceCoroutineStub(channel)

    fun setAddressLine(lineId: Int, value: String, features: Int): Int = runBlocking {
        try {
            val request = AddressLine.newBuilder()
                .setLineId(lineId)
                .setValue(value)
                .setFeatures(features)
                .build()
            val response = stub.setAddressLine(request)
            response.result
        } catch (e: Exception) {
            -1 // or handle error appropriately
        }
    }

    fun getAllAddressLines(): List<AddressLine> = runBlocking {
        try {
            stub.getAllLines(Empty.getDefaultInstance()).lineList
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getMysqlDump(): String = runBlocking {
        try {
            stub.mysqlDump(Empty.getDefaultInstance()).dump
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    fun backupAddress(): Boolean = runBlocking {
        try {
            stub.backupAddress(Empty.getDefaultInstance())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun restoreAddress(): Boolean = runBlocking {
        try {
            stub.restoreAddress(Empty.getDefaultInstance())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removeAddressLine(lineId: Int): Boolean = runBlocking {
        try {
            val request = AddressLine.newBuilder()
                .setLineId(lineId)
                .build()
            stub.removeAddressLine(request)
            true
        } catch (e: Exception) {
            false
        }
    }
}
