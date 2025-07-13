package com.hha.client

import com.hha.common.Empty
import com.hha.client.ClientServiceGrpcKt
import com.hha.client.AddBuyOnAccountPaymentsRequest
import com.hha.client.AddCustomerPayedRequest
import com.hha.client.AddCustomerRequest
import com.hha.client.AddCustomerResponse
import com.hha.client.Customer
import com.hha.client.CustomerList
import com.hha.client.DeleteCustomerRequest
import com.hha.client.DuplicateCustomerRequest
import com.hha.client.DuplicateCustomerResponse
import com.hha.client.FindAllResponse
import com.hha.client.FindClientsWithOpenBillsResponse
import com.hha.client.FindCustomerByNameRequest
import com.hha.client.FindCustomerByNameResponse
import com.hha.client.FindCustomerOrAddRequest
import com.hha.client.FindCustomerOrAddResponse
import com.hha.client.FindIdByAddressRequest
import com.hha.client.FindIdByAddressResponse
import com.hha.client.FindIdByPhoneRequest
import com.hha.client.FindIdByPhoneResponse
import com.hha.client.FindSortedClientsRequest
import com.hha.client.FindSortedClientsResponse
import com.hha.client.FindSortedValidClientsRequest
import com.hha.client.FindSortedValidClientsResponse
import com.hha.client.GetClientIdFromPhoneNumberRequest
import com.hha.client.GetClientIdFromPhoneNumberResponse
import com.hha.client.GetCustomerFromIdRequest
import com.hha.client.GetCustomerFromIdResponse
import com.hha.client.GetCustomerTextRequest
import com.hha.client.GetCustomerTextResponse
import com.hha.client.InsertNewCustomerRequest
import com.hha.client.InsertNewCustomerResponse
import com.hha.client.InvisibleCustomerRequest
import com.hha.client.UpdateCustomerRequest
import com.hha.client.UpdateOrAddCustomerRequest
import com.hha.client.UpdateOrAddCustomerResponse
import com.hha.client.UpdateTotalsOpenBillsRequest
import com.hha.client.VerifyResponse
import com.hha.service.BaseGrpcService

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class ClientService(channel: ManagedChannel) : BaseGrpcService<ClientServiceGrpcKt.ClientServiceCoroutineStub>(channel) {
    override val stub: ClientServiceGrpcKt.ClientServiceCoroutineStub by lazy {
        ClientServiceGrpcKt.ClientServiceCoroutineStub(channel)
    }

    fun updateCustomer(customer: Customer): Boolean = runBlocking {
        try {
            val request = UpdateCustomerRequest.newBuilder()
                .setCustomer(customer)
                .build()
            stub.updateCustomer(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun insertNewCustomer(customer: Customer): Long? = runBlocking {
        try {
            val request = InsertNewCustomerRequest.newBuilder()
                .setCustomer(customer)
                .build()
            stub.insertNewCustomer(request).id
        } catch (e: Exception) {
            null
        }
    }

    fun verify(): Boolean? = runBlocking {
        try {
            stub.verify(Empty.getDefaultInstance()).result
        } catch (e: Exception) {
            null
        }
    }

    fun findAll(): Int? = runBlocking {
        try {
            stub.findAll(Empty.getDefaultInstance()).count
        } catch (e: Exception) {
            null
        }
    }

    fun findSortedClients(customerId: Long): CustomerList? = runBlocking {
        try {
            val request = FindSortedClientsRequest.newBuilder()
                .setCustomerId(customerId)
                .build()
            stub.findSortedClients(request).customers
        } catch (e: Exception) {
            null
        }
    }

    fun invisibleCustomer(customerId: Long): Boolean = runBlocking {
        try {
            val request = InvisibleCustomerRequest.newBuilder()
                .setCustomerId(customerId)
                .build()
            stub.invisibleCustomer(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun findSortedValidClients(onlyValidCustomers: Boolean): CustomerList? = runBlocking {
        try {
            val request = FindSortedValidClientsRequest.newBuilder()
                .setOnlyValidCustomers(onlyValidCustomers)
                .build()
            stub.findSortedValidClients(request).customers
        } catch (e: Exception) {
            null
        }
    }

    fun findClientsWithOpenBills(): CustomerList? = runBlocking {
        try {
            stub.findClientsWithOpenBills(Empty.getDefaultInstance()).customers
        } catch (e: Exception) {
            null
        }
    }

    fun getCustomerFromId(customerId: Long): Customer? = runBlocking {
        try {
            val request = GetCustomerFromIdRequest.newBuilder()
                .setCustomerId(customerId)
                .build()
            stub.getCustomerFromId(request).customer
        } catch (e: Exception) {
            null
        }
    }

    fun getClientIdFromPhoneNumber(phone: String): Long? = runBlocking {
        try {
            val request = GetClientIdFromPhoneNumberRequest.newBuilder()
                .setPhone(phone)
                .build()
            stub.getClientIdFromPhoneNumber(request).clientId
        } catch (e: Exception) {
            null
        }
    }

    fun duplicateCustomer(id: Long): Long? = runBlocking {
        try {
            val request = DuplicateCustomerRequest.newBuilder()
                .setId(id)
                .build()
            stub.duplicateCustomer(request).newId
        } catch (e: Exception) {
            null
        }
    }

    fun addBuyOnAccountPayments(transactionId: Int): Boolean = runBlocking {
        try {
            val request = AddBuyOnAccountPaymentsRequest.newBuilder()
                .setTransactionId(transactionId)
                .build()
            stub.addBuyOnAccountPayments(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun addCustomerPayed(customerId: Long, total: Int): Boolean = runBlocking {
        try {
            val request = AddCustomerPayedRequest.newBuilder()
                .setCustomerId(customerId)
                .setTotal(total)
                .build()
            stub.addCustomerPayed(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun findCustomerOrAdd(
        phoneNumber: String,
        zipCode: String,
        houseNumber: String,
        name: String,
        company: String,
        streetName: String,
        city: String
    ): Long? = runBlocking {
        try {
            val request = FindCustomerOrAddRequest.newBuilder()
                .setPhoneNumber(phoneNumber)
                .setZipCode(zipCode)
                .setHouseNumber(houseNumber)
                .setName(name)
                .setCompany(company)
                .setStreetName(streetName)
                .setCity(city)
                .build()
            stub.findCustomerOrAdd(request).id
        } catch (e: Exception) {
            null
        }
    }

    fun addCustomer(
        phoneNumber: String,
        zipCode: String,
        houseNumber: String,
        name: String,
        company: String,
        streetName: String,
        city: String
    ): Long? = runBlocking {
        try {
            val request = AddCustomerRequest.newBuilder()
                .setPhoneNumber(phoneNumber)
                .setZipCode(zipCode)
                .setHouseNumber(houseNumber)
                .setName(name)
                .setCompany(company)
                .setStreetName(streetName)
                .setCity(city)
                .build()
            stub.addCustomer(request).id
        } catch (e: Exception) {
            null
        }
    }

    fun updateOrAddCustomer(customer: Customer): Int? = runBlocking {
        try {
            val request = UpdateOrAddCustomerRequest.newBuilder()
                .setCustomer(customer)
                .build()
            stub.updateOrAddCustomer(request).customerId
        } catch (e: Exception) {
            null
        }
    }

    fun getCustomerText(customerId: Long): String? = runBlocking {
        try {
            val request = GetCustomerTextRequest.newBuilder()
                .setCustomerId(customerId)
                .build()
            stub.getCustomerText(request).text
        } catch (e: Exception) {
            null
        }
    }

    fun findCustomerByName(name: String): CustomerList? = runBlocking {
        try {
            val request = FindCustomerByNameRequest.newBuilder()
                .setName(name)
                .build()
            stub.findCustomerByName(request).customers
        } catch (e: Exception) {
            null
        }
    }

    fun updateFoodOrdered(): Boolean = runBlocking {
        try {
            stub.updateFoodOrdered(Empty.getDefaultInstance())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateTotalsOpenBills(customerId: Long): Boolean = runBlocking {
        try {
            val request = UpdateTotalsOpenBillsRequest.newBuilder()
                .setCustomerId(customerId)
                .build()
            stub.updateTotalsOpenBills(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun findIdByAddress(street: String, houseNumber: String, city: String): Long? = runBlocking {
        try {
            val request = FindIdByAddressRequest.newBuilder()
                .setStreet(street)
                .setHouseNumber(houseNumber)
                .setCity(city)
                .build()
            stub.findIdByAddress(request).customerId
        } catch (e: Exception) {
            null
        }
    }

    fun findIdByPhone(phoneNumber: String): Long? = runBlocking {
        try {
            val request = FindIdByPhoneRequest.newBuilder()
                .setPhoneNumber(phoneNumber)
                .build()
            stub.findIdByPhone(request).customerId
        } catch (e: Exception) {
            null
        }
    }

    fun destroyClient(customerId: Int): Boolean = runBlocking {
        try {
            val request = DeleteCustomerRequest.newBuilder()
                .setCustomerId(customerId)
                .build()
            stub.destroyClient(request)
            true
        } catch (e: Exception) {
            false
        }
    }
}