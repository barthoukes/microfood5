package com.hha.service

import com.hha.pingpong.PingPongGrpcKt
import com.hha.pingpong.Pingpong
import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class PingPongService(channel: ManagedChannel) : BaseGrpcService<PingPongGrpcKt.PingPongCoroutineStub>(channel) {
    override val stub: PingPongGrpcKt.PingPongCoroutineStub = PingPongGrpcKt.PingPongCoroutineStub(channel)

    fun ping(name: String): String = runBlocking {
        try {
            val request = Pingpong.PingPongMsg.newBuilder()
                .setPayload(name)
                .build()
            val response = stub.ping(request)
            response.toString()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}