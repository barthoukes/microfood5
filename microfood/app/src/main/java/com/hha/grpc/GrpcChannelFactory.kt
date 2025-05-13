package com.hha.grpc

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

object GrpcChannelFactory {
    fun createChannel(): ManagedChannel {
        return ManagedChannelBuilder.forAddress(GrpcConfig.SERVER_IP, GrpcConfig.SERVER_PORT)
            .apply {
                if (GrpcConfig.USE_PLAINTEXT) {
                    usePlaintext()
                }
            }
            .build()
    }
}