package com.hha.service

import com.hha.common.Empty
import com.hha.personnel.AccessReply
import com.hha.personnel.DongleId
import com.hha.personnel.DongleRestaurantRequest
import com.hha.personnel.IsValidKey
import com.hha.personnel.Person
import com.hha.personnel.PersonComplete
import com.hha.personnel.PersonId
import com.hha.personnel.PersonList
import com.hha.personnel.PersonnelServiceGrpcKt

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class PersonnelService(channel: ManagedChannel) : BaseGrpcService<PersonnelServiceGrpcKt.PersonnelServiceCoroutineStub>(channel) {
    override val stub: PersonnelServiceGrpcKt.PersonnelServiceCoroutineStub by lazy {
        PersonnelServiceGrpcKt.PersonnelServiceCoroutineStub(channel)
    }

    fun getPersonById(personId: Int): Person? = runBlocking {
        try {
            val request = PersonId.newBuilder()
                .setPersonId(personId)
                .build()
            stub.getPersonById(request)
        } catch (e: Exception) {
            null
        }
    }

    fun getPersonByDongle(dongleId: Int): Person? = runBlocking {
        try {
            val request = DongleId.newBuilder()
                .setDongleId(dongleId)
                .build()
            stub.getPersonByDongle(request)
        } catch (e: Exception) {
            null
        }
    }

    fun getAllPersons(): PersonList? = runBlocking {
        try {
            stub.getAllPersons(Empty.getDefaultInstance())
        } catch (e: Exception) {
            null
        }
    }

    fun updatePerson(person: Person, complete: Boolean): Boolean = runBlocking {
        try {
            val request = PersonComplete.newBuilder()
                .setPerson(person)
                .setComplete(complete)
                .build()
            stub.updatePerson(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getNewKey(dongle: Int, restaurantId: Int): Person? = runBlocking {
        try {
            val request = DongleRestaurantRequest.newBuilder()
                .setDongle(dongle)
                .setRestaurantId(restaurantId)
                .build()
            stub.getNewKey(request)
        } catch (e: Exception) {
            null
        }
    }

    fun clearDatabaseKey(personId: Int): Boolean = runBlocking {
        try {
            val request = PersonId.newBuilder()
                .setPersonId(personId)
                .build()
            stub.clearDatabaseKey(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isValid(personId: Int): Boolean? = runBlocking {
        try {
            val request = PersonId.newBuilder()
                .setPersonId(personId)
                .build()
            stub.isValid(request).isValidKey
        } catch (e: Exception) {
            null
        }
    }

    fun getAccess(personId: Int): AccessReply? = runBlocking {
        try {
            val request = PersonId.newBuilder()
                .setPersonId(personId)
                .build()
            stub.getAccess(request)
        } catch (e: Exception) {
            null
        }
    }
}