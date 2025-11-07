package com.hha.grpc

import android.util.Log
import com.hha.resources.Global // Import Global
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.TimeUnit

/**
 * A factory that creates and manages a SINGLE gRPC channel.
 * It gets the server IP address from a global configuration, which should be
 * set at application startup.
 */
object GrpcChannelFactory {
    private var channel: ManagedChannel? = null
    private const val DEFAULT_PORT = 50051
    private const val TAG = "GrpcChannelFactory"

    /**
     * Creates or reuses a gRPC channel.
     * This is a synchronous, blocking function that assumes the server IP is already known.
     */
    fun getChannel(): ManagedChannel {
        // Reuse existing channel if it's still active.
        if (channel?.isShutdown == false && channel?.isTerminated == false) {
            return channel!!
        }

        // Get the server IP from the Global singleton.
        val serverIp = Global.getInstance().serverIp
        if (serverIp.isNullOrEmpty()) {
            Log.e(TAG, "Server IP is not set! Network scan must be run at startup.")
            throw IllegalStateException("Server IP has not been configured.")
        }

        Log.d(TAG, "Building new channel for $serverIp:$DEFAULT_PORT")
        channel = ManagedChannelBuilder.forAddress(serverIp, DEFAULT_PORT)
            .usePlaintext()
            // Add your other configurations like keepAlive, retryPolicy etc.
            .build()

        return channel!!
    }

    /**
     * Shuts down the current channel to force a full reconnection on the next call.
     */
    fun reconnect() {
        Log.d(TAG, "Forcing reconnection. Shutting down channel.")
        channel?.shutdownNow()
        channel = null
    }
}
