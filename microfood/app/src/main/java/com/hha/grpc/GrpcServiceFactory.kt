package com.hha.grpc

import com.hha.grpc.GrpcChannelFactory
import io.grpc.ManagedChannel
import com.hha.service.AddressService
import com.hha.service.AdvertisementService
import com.hha.service.ArchiveTransactionService
import com.hha.service.DailyTransactionService
import com.hha.service.MenuItemService

object GrpcServiceFactory {
    private val channel: ManagedChannel by lazy {
        GrpcChannelFactory.createChannel()
    }

    fun createAddressService(): AddressService {
        return AddressService(channel)
    }

    fun createAdvertisementService(): AdvertisementService {
        return AdvertisementService(channel)
    }

    fun createArchiveTransactionService(): ArchiveTransactionService {
        return ArchiveTransactionService(channel)
    }

    fun createDailyTransactionService(): DailyTransactionService {
        return DailyTransactionService(channel)
    }

    fun createMenuItemService(): MenuItemService {
        return MenuItemService(channel)
    }
}
