package com.hha.grpc

import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

object NetworkScanner {

    // Timeout for connection attempt (milliseconds)
    private const val CONNECTION_TIMEOUT = 500

    /**
     * Scan the local subnet for devices with port 50051 open.
     * @param localIp Your local IP (e.g., "192.168.1.100").
     * @return List of IPs with port 50051 open.
     */
    suspend fun scanForGrpcServers(localIp: String, port: Int): List<String> {
        val subnet = localIp.substringBeforeLast('.') // e.g., "192.168.1"
        val ipRange = 1..255

        return withContext(Dispatchers.IO) {
            ipRange.map { ipEnd ->
                async {
                    val host = "$subnet.$ipEnd"
                    if (isPortOpen(host, port)) host else null
                }
            }.awaitAll().filterNotNull()
        }
    }

    /** Check if a port is open on a host. */
    private fun isPortOpen(host: String, port: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), CONNECTION_TIMEOUT)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}

// Usage:
//fun main() = runBlocking {
//    val localIp = "192.168.1.100" // Replace with your local IP
//    val grpcServers = NetworkScanner.scanForGrpcServers(localIp)
//    println("Discovered gRPC servers: $grpcServers")
//}