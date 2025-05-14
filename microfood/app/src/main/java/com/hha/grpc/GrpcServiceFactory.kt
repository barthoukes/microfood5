package com.hha.grpc

import io.grpc.ManagedChannel
import com.hha.service.PingPongService
import com.hha.service.AddressService
import com.hha.service.AdvertisementService
import com.hha.service.ArchiveTransactionService
import com.hha.service.MenuItemService

class GrpcServiceFactory {
//    fun createCommonTypesService(channel: ManagedChannel): CommonTypesService {
//        return CommonTypesService(channel)
//    }

    fun createAddressService(channel: ManagedChannel): AddressService {
        return AddressService(channel)
    }

    fun createPingPongService(channel: ManagedChannel): PingPongService {
        return PingPongService(channel);
    }

    fun createAdvertisementService(channel: ManagedChannel): AdvertisementService {
        return AdvertisementService(channel)
    }

    fun createArchiveTransactionService(channel: ManagedChannel): ArchiveTransactionService {
        return ArchiveTransactionService(channel)
    }

    fun createMenuItemService(channel: ManagedChannel): MenuItemService {
        return MenuItemService(channel)
    }
}