package com.hha.grpc

import android.util.Log
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import java.util.concurrent.TimeUnit
import java.net.NetworkInterface
import com.hha.grpc.NetworkScanner
import kotlinx.coroutines.runBlocking

// sudo resolvectl mdns wlp0s20f3 yes
// resolvectl status | grep mDNS
// sudo resolvectl mdns wlp0s20f3 yes
// sudo nano /etc/systemd/resolved.conf
//   switch on : MulticastDNS=yes
// save (ctrl + o) and quit (ctrl + x)
// sudo systemctl restart systemd-resolved


object GrpcChannelFactory {
    private var channel: ManagedChannel? = null
    private var cachedIp: String? = null // Stores the last working IP
    private const val DEFAULT_PORT = 50051

    /**
     * Creates or reuses a channel, first checking if the cached IP is still valid.
     * If not, it searches for a new IP.
     */
    fun createChannel(): ManagedChannel {
        // Reuse existing channel if still active
        if (channel != null && isChannelActive(channel!!)) {
            return channel!!
        }

        // Try cached IP first (if available)
        if (cachedIp != null) {
            channel = buildChannel(cachedIp!!, DEFAULT_PORT)
            if (isChannelActive(channel!!)) {
                return channel!!
            }
        }

        // If cached IP fails, search for a new IP
        val newIps = findAvailableIps() // Implement your IP discovery logic
        for (newIp in newIps) {
            cachedIp = newIp // Cache the new IP
            channel = buildChannel(cachedIp!!, DEFAULT_PORT)
            //if (isChannelActive(channel!!)) {
                break
            //}
        }
        return channel!!
    }

    /**
     * Checks if a channel is still active.
     */
    private fun isChannelActive(channel: ManagedChannel): Boolean {
        return try {
            // Try a lightweight RPC call (e.g., health check)
            // If no exception, channel is active
            true
        } catch (e: StatusRuntimeException) {
            false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Builds a new gRPC channel with retry/keepalive settings.
     */
    private fun buildChannel(ip: String, port: Int): ManagedChannel {
        return ManagedChannelBuilder.forAddress(ip, port)
            .usePlaintext()
            .defaultServiceConfig(getRetryPolicy())
            .keepAliveTime(30, TimeUnit.SECONDS)
            .keepAliveTimeout(10, TimeUnit.SECONDS)
            .keepAliveWithoutCalls(true)
            .build()
    }

    /**
     * Searches for an available IP (replace with your logic).
     */
    private fun findAvailableIps(): List<String> = runBlocking {
        val localIp = getLocalIpAddress()
        Log.i("GrpcChannelFactory", "Local IP: $localIp")
        val list: List<String> = listOf("192.168.0.124", "mensfort-elite2")
        return@runBlocking list

        //val grpcServers = NetworkScanner.scanForGrpcServers(localIp, 50051)
        //return@runBlocking grpcServers // Replace with dynamic discovery
    }

    /**
     * Retry policy configuration (unchanged from your original).
     */
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

    fun getLocalIpAddress(): String {
        return NetworkInterface.getNetworkInterfaces()
            .toList()
            .flatMap { it.inetAddresses.toList() }
            .firstOrNull { !it.isLoopbackAddress && it.hostAddress.contains(".") }
            ?.hostAddress
            ?: "localhost"  // Fallback to localhost if nothing found
    }

    /**
     * Force a reconnection (clears cache and rebuilds channel).
     */
    fun reconnect() {
        channel?.shutdownNow()
        channel = null
        cachedIp = null
        createChannel() // Rebuild with fresh IP discovery
    }
}