package com.hha.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket

object NetworkScanner {
    private const val TAG = "NetworkScanner"

    /**
     * Scans the local network for the first device with the specified port open.
     * Must be called from a coroutine.
     *
     * @param port The port to scan for (e.g., 50051).
     * @return The IP address of the first server found, or null if none are found.
     */
    suspend fun findFirstOpenPort(port: Int): String? = withContext(Dispatchers.IO) {
        val localIp = getLocalIpAddress()
        if (localIp == null) {
            Log.e(TAG, "Could not determine local IP. Scan aborted.")
            return@withContext null
        }

        val subnet = localIp.substringBeforeLast('.')
        Log.d(TAG, "Scanning subnet: $subnet.* on port $port")

        val jobs = (1..254).map { i ->
            async {
                val ipAddress = "$subnet.$i"
                if (isPortOpen(ipAddress, port, 100)) {
                    ipAddress
                } else {
                    null
                }
            }
        }

        // Return the first non-null result without waiting for all scans to finish
        for (job in jobs) {
            val result = job.await()
            if (result != null) {
                Log.i(TAG, "SUCCESS: Found server at $result:$port")
                // Cancel remaining jobs to save resources
                jobs.forEach { it.cancel() }
                return@withContext result
            }
        }

        Log.w(TAG, "Scan complete. No open ports found for $port")
        return@withContext null
    }

    private fun isPortOpen(ip: String, port: Int, timeoutMs: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getLocalIpAddress(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces().asSequence()
                .flatMap { it.inetAddresses.asSequence() }
                .firstOrNull { !it.isLoopbackAddress && it.hostAddress.matches(Regex("\\d{1,3}(\\.\\d{1,3}){3}")) }
                ?.hostAddress
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get local IP address", e)
            null
        }
    }
}
