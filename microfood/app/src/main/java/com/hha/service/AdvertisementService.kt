package com.hha.service

import com.hha.advertisement.AdvertisementServiceGrpcKt
import com.hha.advertisement.Advertisement
import com.hha.common.Empty
import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class AdvertisementService(channel: ManagedChannel) :
    BaseGrpcService<AdvertisementServiceGrpcKt.AdvertisementServiceCoroutineStub>(channel) {

    // MySQL timestamp format: "yyyy-MM-dd HH:mm:ss"
    private val mysqlDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override val stub: AdvertisementServiceGrpcKt.AdvertisementServiceCoroutineStub =
        AdvertisementServiceGrpcKt.AdvertisementServiceCoroutineStub(channel)

    /**
     * Converts MySQL timestamp string to Date object
     */
    fun stringToDate(mysqlTimestamp: String): Date? {
        return try {
            mysqlDateFormat.parse(mysqlTimestamp)
        } catch (e: Exception) {
            logError("Failed to parse MySQL timestamp: $mysqlTimestamp", e)
            null
        }
    }

    /**
     * Converts Date object to MySQL timestamp string
     */
    fun dateToString(date: Date): String {
        return mysqlDateFormat.format(date)
    }

    /**
     * Creates a new advertisement with default values including current timestamp
     */
    fun createNewAdvertisement(): Advertisement = runBlocking {
        try {
            val currentTime = dateToString(Date())
            stub.newAdvertisement(Empty.getDefaultInstance()).toBuilder()
                .setStart(currentTime)
                .setEnd(currentTime)
                .build()
        } catch (e: Exception) {
            logError("Failed to create new advertisement", e)
            // Fallback to empty advertisement with current time
            Advertisement.newBuilder()
                .setStart(dateToString(Date()))
                .setEnd(dateToString(Date()))
                .build()
        }
    }

    /**
     * Updates advertisement with proper timestamp handling
     */
    fun updateAdvertisement(
        ad: Advertisement,
        newStartDate: Date? = null,
        newEndDate: Date? = null
    ): Boolean = runBlocking {
        try {
            val updatedAd = ad.toBuilder().apply {
                newStartDate?.let { setStart(dateToString(it)) }
                newEndDate?.let { setEnd(dateToString(it)) }
            }.build()

            stub.updateAdvertisement(updatedAd)
            true
        } catch (e: Exception) {
            logError("Failed to update advertisement", e)
            false
        }
    }

    /**
     * Gets advertisement list with parsed dates
     */
    fun getAdvertisementListWithDates(): List<Pair<Advertisement, Pair<Date?, Date?>>> = runBlocking {
        try {
            stub.getAdvertisementList(Empty.getDefaultInstance()).advertisementList.map { ad ->
                ad to (stringToDate(ad.start) to stringToDate(ad.end))
            }
        } catch (e: Exception) {
            logError("Failed to get advertisement list", e)
            emptyList()
        }
    }

    /**
     * Helper to validate advertisement timestamps
     */
    fun validateAdvertisementTimings(ad: Advertisement): Boolean {
        return try {
            val startDate = stringToDate(ad.start)
            val endDate = stringToDate(ad.end)

            if (startDate == null || endDate == null) {
                return false
            }

            // End date should be after start date
            endDate.after(startDate)
        } catch (e: Exception) {
            false
        }
    }

    // ... rest of the existing methods ...

    private fun logError(message: String, e: Exception) {
        // Implement your logging here
        println("AdvertisementService Error: $message - ${e.message}")
    }
}