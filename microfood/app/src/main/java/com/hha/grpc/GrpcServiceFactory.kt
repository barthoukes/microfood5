package com.hha.grpc

import com.hha.client.ClientService
import com.hha.database.SqlDatabase
import com.hha.grpc.GrpcChannelFactory
import io.grpc.ManagedChannel
import com.hha.service.AddressService
import com.hha.service.AdvertisementService
import com.hha.service.ArchiveTransactionService
import com.hha.service.ConfigurationService
import com.hha.service.DailyTransactionItemService
import com.hha.service.DailyTransactionPaymentService
import com.hha.service.DailyTransactionPrintService
import com.hha.service.DailyTransactionService
import com.hha.service.DatabaseService
import com.hha.service.MenuCardService
import com.hha.service.MenuClusterItemService
import com.hha.service.MenuClusterService
import com.hha.service.MenuItemService
import com.hha.service.MenuPageService
import com.hha.service.PersonnelService
import com.hha.service.ZipCodeService

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

    fun createArchiveTransactionItemService(): DailyTransactionItemService {
        return DailyTransactionItemService(channel)
    }

    fun createClientService(): ClientService {
        return ClientService(channel)
    }

    fun createConfigService(): ConfigurationService {
        return ConfigurationService(channel)
    }

    fun createDailyTransactionPaymentService(): DailyTransactionPaymentService {
        return DailyTransactionPaymentService(channel)
    }

    fun createDailyTransactionPrintService(): DailyTransactionPrintService {
        return DailyTransactionPrintService(channel)
    }

    fun createDailyTransactionService(): DailyTransactionService {
        return DailyTransactionService(channel)
    }

    fun createMenuCardService(): MenuCardService {
        return MenuCardService(channel)
    }

    fun createMenuClusterService(): MenuClusterService {
        return MenuClusterService(channel)
    }

    fun createMenuClusterItemService(): MenuClusterItemService {
        return MenuClusterItemService(channel)
    }

    fun createMenuItemService(): MenuItemService {
        return MenuItemService(channel)
    }

    fun createMenuPageService(): MenuPageService {
        return MenuPageService(channel)
    }

    fun createPersonnelService(): PersonnelService {
        return PersonnelService(channel)
    }

    fun createZipCodeService(): ZipCodeService {
        return ZipCodeService(channel)
    }

    fun createDatabaseService(): DatabaseService {
        return DatabaseService(channel)
    }
}
