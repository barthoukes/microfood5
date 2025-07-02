package com.hha.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.TimeUnit

object GrpcChannelFactory {
    private var channel: ManagedChannel? = null

    fun createChannel(): ManagedChannel {
        if (channel != null)
            return channel!!
        channel = ManagedChannelBuilder.forAddress("192.168.0.124", 50051)
            .usePlaintext()
            .defaultServiceConfig(getRetryPolicy()) // Custom retry policy
            .keepAliveTime(30, TimeUnit.SECONDS) // Send pings every 30s
            .keepAliveTimeout(10, TimeUnit.SECONDS) // Wait 10s for ping
            .keepAliveWithoutCalls(true) // Keep alive even with no active calls
            .build().also { channel = it }
        return channel!!
    }

    fun getRetryPolicy(): Map<String, Any> {
        val methodConfig = mapOf(
            "name" to listOf(mapOf("service" to "")),
            "retryPolicy" to mapOf(
                "maxAttempts" to 5.0,
                "initialBackoff" to "1s",
                "maxBackoff" to "30s",
                "backoffMultiplier" to 1.6,
                "retryableStatusCodes" to listOf(
                    "UNAVAILABLE",
                    "RESOURCE_EXHAUSTED",
                    "INTERNAL"
                )
            )
        )
        return mapOf(
            "methodConfig" to listOf(methodConfig),
            "retryThrottling" to mapOf(
                "maxTokens" to 10.0,
                "tokenRatio" to 0.1
            )
        )
    }

    fun reconnect() {
        createChannel() // Creates new channel with fresh connection
    }
}