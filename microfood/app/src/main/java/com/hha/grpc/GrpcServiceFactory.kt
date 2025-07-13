package com.hha.grpc

import com.hha.client.ClientService
import com.hha.database.SqlDatabase
import com.hha.grpc.GrpcChannelFactory
import io.grpc.ManagedChannel
import com.hha.service.AddressService
import com.hha.service.AdvertisementService
import com.hha.service.ArchiveTransactionService
import com.hha.service.ConfigurationService
import com.hha.service.DailyTimeFrameService
import com.hha.service.DailyTransactionItemService
import com.hha.service.DailyTransactionPaymentService
import com.hha.service.DailyTransactionPrintService
import com.hha.service.DailyTransactionService
import com.hha.service.DatabaseService
import com.hha.service.FloorPlanService
import com.hha.service.FloorTableService
import com.hha.service.MenuCardService
import com.hha.service.MenuClusterItemService
import com.hha.service.MenuClusterService
import com.hha.service.MenuItemService
import com.hha.service.MenuPageService
import com.hha.service.PersonnelService
import com.hha.service.TaxService
import com.hha.service.ZipCodeService

object GrpcServiceFactory {
    private var my_channel : ManagedChannel? = null

    fun getChannel() : ManagedChannel
    {
        while (my_channel == null)
        {
            my_channel = GrpcChannelFactory.createChannel()
        }
        return my_channel!!
    }

    fun reconnect() {
    }

    fun createAddressService(): AddressService {
        val channel = getChannel()
        return AddressService(channel)
    }

    fun createAdvertisementService(): AdvertisementService {
        val channel = getChannel()
        return AdvertisementService(channel)
    }

    fun createArchiveTransactionItemService(): DailyTransactionItemService {
        val channel = getChannel()
        return DailyTransactionItemService(channel)
    }

    fun createClientService(): ClientService {
        val channel = getChannel()
        return ClientService(channel)
    }

    fun createConfigService(): ConfigurationService {
        val channel = getChannel()
        return ConfigurationService(channel)
    }

    fun createDailyTimeFrameService(): DailyTimeFrameService {
        val channel = getChannel()
        return DailyTimeFrameService(channel)
    }

    fun createDailyTransactionItemService(): DailyTransactionItemService {
        val channel = getChannel()
        return DailyTransactionItemService(channel)
    }

    fun createDailyTransactionPaymentService(): DailyTransactionPaymentService {
        val channel = getChannel()
        return DailyTransactionPaymentService(channel)
    }

    fun createDailyTransactionPrintService(): DailyTransactionPrintService {
        val channel = getChannel()
        return DailyTransactionPrintService(channel)
    }

    fun createDatabaseService(): DatabaseService {
        val channel = getChannel()
        return DatabaseService(channel)
    }

    fun createDailyTransactionService(): DailyTransactionService {
        val channel = getChannel()
        return DailyTransactionService(channel)
    }

    fun createFloorPlanService(): FloorPlanService {
        val channel = getChannel()
        return FloorPlanService(channel)
    }

    fun createFloorTableService(): FloorTableService {
        val channel = getChannel()
        return FloorTableService(channel)
    }

    fun createMenuCardService(): MenuCardService {
        val channel = getChannel()
        return MenuCardService(channel)
    }

    fun createMenuClusterItemService(): MenuClusterItemService {
        val channel = getChannel()
        return MenuClusterItemService(channel)
    }

    fun createMenuClusterService(): MenuClusterService {
        val channel = getChannel()
        return MenuClusterService(channel)
    }

    fun createMenuItemService(): MenuItemService {
        val channel = getChannel()
        return MenuItemService(channel)
    }

    fun createMenuPageService(): MenuPageService {
        val channel = getChannel()
        return MenuPageService(channel)
    }

    fun createPersonnelService(): PersonnelService {
        val channel = getChannel()
        return PersonnelService(channel)
    }

    fun createTaxService(): TaxService {
        val channel = getChannel()
        return TaxService(channel)
    }

    fun createZipCodeService(): ZipCodeService {
        val channel = getChannel()
        return ZipCodeService(channel)
    }

}
