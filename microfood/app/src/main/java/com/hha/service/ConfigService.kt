package com.hha.service

import com.hha.common.Empty
import com.hha.config.CheckConfigRequest
import com.hha.config.ConfigurationServiceGrpcKt
import com.hha.config.DecrementConfigRequest
import com.hha.config.DecrementConfigResponse
import com.hha.config.GetConfigStringRequest
import com.hha.config.GetConfigStringResponse
import com.hha.config.GetConfigValueRequest
import com.hha.config.GetConfigValueResponse
import com.hha.config.IncrementConfigRequest
import com.hha.config.IncrementConfigResponse
import com.hha.config.SetConfigIntRequest
import com.hha.config.SetConfigStringRequest

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class ConfigurationService(channel: ManagedChannel) : BaseGrpcService<ConfigurationServiceGrpcKt.ConfigurationServiceCoroutineStub>(channel) {
    override val stub: ConfigurationServiceGrpcKt.ConfigurationServiceCoroutineStub by lazy {
        ConfigurationServiceGrpcKt.ConfigurationServiceCoroutineStub(channel)
    }

    fun getConfigString(name: String): String? = runBlocking {
        try {
            val request = GetConfigStringRequest.newBuilder()
                .setName(name)
                .build()
            stub.getConfigString(request).value
        } catch (e: Exception) {
            null
        }
    }

    fun getConfigValue(name: String): Int? = runBlocking {
        try {
            val request = GetConfigValueRequest.newBuilder()
                .setName(name)
                .build()
            stub.getConfigValue(request).value
        } catch (e: Exception) {
            null
        }
    }

    fun setConfigString(name: String, value: String): Boolean = runBlocking {
        try {
            val request = SetConfigStringRequest.newBuilder()
                .setName(name)
                .setValue(value)
                .build()
            stub.setConfigString(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setConfigInt(name: String, value: Int): Boolean = runBlocking {
        try {
            val request = SetConfigIntRequest.newBuilder()
                .setName(name)
                .setValue(value)
                .build()
            stub.setConfigInt(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun incrementConfig(name: String): Int? = runBlocking {
        try {
            val request = IncrementConfigRequest.newBuilder()
                .setName(name)
                .build()
            stub.incrementConfig(request).value
        } catch (e: Exception) {
            null
        }
    }

    fun decrementConfig(name: String): Int? = runBlocking {
        try {
            val request = DecrementConfigRequest.newBuilder()
                .setName(name)
                .build()
            stub.decrementConfig(request).value
        } catch (e: Exception) {
            null
        }
    }

    fun checkConfig(name: String, value: String): Boolean = runBlocking {
        try {
            val request = CheckConfigRequest.newBuilder()
                .setName(name)
                .setValue(value)
                .build()
            stub.checkConfig(request)
            true
        } catch (e: Exception) {
            false
        }
    }
}