package com.hha.service

import io.grpc.ManagedChannel

abstract class BaseGrpcService<T>(protected val channel: ManagedChannel) {
    abstract val stub: T

    fun shutdown() {
        channel.shutdown()
    }
}