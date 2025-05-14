package com.hha.service

import com.hha.daily.reservation.AddressRequest
import com.hha.daily.reservation.ClientIdReply
import com.hha.daily.reservation.ClientIdRequest
import com.hha.daily.reservation.ClientIndexReply
import com.hha.daily.reservation.ClientListReply
import com.hha.daily.reservation.ClientNamePartRequest
import com.hha.daily.reservation.ClientReply
import com.hha.daily.reservation.ClientRequest
import com.hha.daily.reservation.ClientTextReply
import com.hha.daily.reservation.CustomerDetails
import com.hha.daily.reservation.CustomerPayRequest
import com.hha.daily.reservation.CustomerTotal
import com.hha.daily.reservation.DaysRequest
import com.hha.daily.reservation.EreservationStatus
import com.hha.daily.reservation.empty
import com.hha.daily.reservation.IndexRequest
import com.hha.daily.reservation.OnlyValidRequest
import com.hha.daily.reservation.PhoneRequest
import com.hha.daily.reservation.Reservation
import com.hha.daily.reservation.ReservationDaysReply
import com.hha.daily.reservation.ReservationIdRequest
import com.hha.daily.reservation.ReservationListReply
import com.hha.daily.reservation.ReservationLocationNames
import com.hha.daily.reservation.ReservationLocationsReply
import com.hha.daily.reservation.ReservationServiceGrpcKt
import com.hha.daily.reservation.ReservationTimeReply
import com.hha.daily.reservation.Street
import com.hha.daily.reservation.StreetListReply
import com.hha.daily.reservation.TransactionIdRequest

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class ReservationService(channel: ManagedChannel) : BaseGrpcService<ReservationServiceGrpcKt.ReservationServiceCoroutineStub>(channel) {
    override val stub: ReservationServiceGrpcKt.ReservationServiceCoroutineStub by lazy {
        ReservationServiceGrpcKt.ReservationServiceCoroutineStub(channel)
    }

    fun addReservation(reservation: Reservation): Boolean = runBlocking {
        try {
            stub.addReservation(reservation)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateReservation(reservation: Reservation): Boolean = runBlocking {
        try {
            stub.updateReservation(reservation)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun dropReservation(reservationId: Int): Boolean = runBlocking {
        try {
            val request = ReservationIdRequest.newBuilder()
                .setReservationId(reservationId)
                .build()
            stub.dropReservation(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getLocations(): List<String> = runBlocking {
        try {
            stub.getLocations(empty.getDefaultInstance()).locationsList
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getDays(): List<String> = runBlocking {
        try {
            stub.getDays(empty.getDefaultInstance()).dateList
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getTotalDays(): List<String> = runBlocking {
        try {
            stub.getTotalDays(empty.getDefaultInstance()).dateList
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getTotalLocations(): List<Int> = runBlocking {
        try {
            stub.getTotalLocations(empty.getDefaultInstance()).locationList
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun findReservations(numberOfDays: Int): List<Reservation> = runBlocking {
        try {
            val request = DaysRequest.newBuilder()
                .setNumberOfDays(numberOfDays)
                .build()
            stub.findReservations(request).reservationsList
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getReservationTime(index: Int): ReservationTimeReply? = runBlocking {
        try {
            val request = IndexRequest.newBuilder()
                .setIndex(index)
                .build()
            stub.getReservationTime(request)
        } catch (e: Exception) {
            null
        }
    }

    fun populateReservations(): Boolean = runBlocking {
        try {
            stub.populateReservations(empty.getDefaultInstance())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removeReservation(reservationId: Int): Boolean = runBlocking {
        try {
            val request = ReservationIdRequest.newBuilder()
                .setReservationId(reservationId)
                .build()
            stub.removeReservation(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun findAllPostcodes(): List<Street> = runBlocking {
        try {
            stub.findAllPostcodes(empty.getDefaultInstance()).streetsList
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun findAllStreets(): List<Street> = runBlocking {
        try {
            stub.findAllStreets(empty.getDefaultInstance()).streetsList
        } catch (e: Exception) {
            emptyList()
        }
    }
}