package com.hha.service

import com.hha.common.Empty
import com.hha.common.MenuCardId
import com.hha.common.MenuItem
import com.hha.common.SkipInvisible
import com.hha.menu.item.DuplicationRequest
import com.hha.menu.item.ExchangeClusterRequest
import com.hha.menu.item.ExchangePagesRequest
import com.hha.menu.item.ExchangeSpecialPricesRequest
import com.hha.menu.item.FindAliasRequest
import com.hha.menu.item.FindItemRequest
import com.hha.menu.item.FindOptions1
import com.hha.menu.item.FirstItemReply
import com.hha.menu.item.GetMenuItemsRequest
import com.hha.menu.item.MenuCardClusterId
import com.hha.menu.item.MenuCardIdSpecialPriceIndex
import com.hha.menu.item.MenuCardPageId
import com.hha.menu.item.MenuCardSearchString
import com.hha.menu.item.MenuItemId
import com.hha.menu.item.MenuItemIdList
import com.hha.menu.item.MenuItemList
import com.hha.menu.item.MenuItemServiceGrpcKt
import com.hha.menu.item.NameRequest
import com.hha.menu.item.NextMenuItemIdRequest
import com.hha.menu.item.PageColourRequest
import com.hha.menu.item.PageLocationsRequest
import com.hha.menu.item.PageRequest
import com.hha.menu.item.ShortMenuItem
import com.hha.menu.item.ShortMenuItemList
import com.hha.menu.item.SizeReply
import com.hha.menu.item.TextDump
import com.hha.menu.item.UpdatePosRequest
import com.hha.menu.item.WidthHeigtRequest

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class MenuItemService(channel: ManagedChannel) : BaseGrpcService<MenuItemServiceGrpcKt.MenuItemServiceCoroutineStub>(channel) {
    override val stub: MenuItemServiceGrpcKt.MenuItemServiceCoroutineStub by lazy {
        MenuItemServiceGrpcKt.MenuItemServiceCoroutineStub(channel)
    }

    fun getMenuItemsFromPage(menuCardId: Int, menuPageId: Int, isVertical: Boolean, skipInvisible: SkipInvisible): MenuItemList = runBlocking {
        try {
            val request = GetMenuItemsRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId(menuPageId)
                .setIsVertical(isVertical)
                .setSkipInvisible(skipInvisible)
                .build()
            stub.getMenuItemsFromPage(request)
        } catch (e: Exception) {
            MenuItemList.getDefaultInstance()
        }
    }

    fun insertNewItemOnMenuCard(item: MenuItem): MenuItem? = runBlocking {
        try {
            stub.insertNewItemOnMenuCard(item)
        } catch (e: Exception) {
            null
        }
    }

    fun insertItem(item: MenuItem): MenuItem? = runBlocking {
        try {
            stub.insertItem(item)
        } catch (e: Exception) {
            null
        }
    }

    fun updateItem(item: MenuItem): Boolean = runBlocking {
        try {
            stub.updateItem(item)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun eraseItem(menuItemId: Int): Boolean = runBlocking {
        try {
            val request = MenuItemId.newBuilder()
                .setMenuItemId(menuItemId)
                .build()
            stub.eraseItem(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getProductFromProductId(menuItemId: Int): MenuItem? = runBlocking {
        try {
            val request = MenuItemId.newBuilder()
                .setMenuItemId(menuItemId)
                .build()
            stub.getProductFromProductId(request)
        } catch (e: Exception) {
            null
        }
    }

    fun findMenuItemsByPage(menuCardId: Int, menuPageId: Int, skipInvisible: SkipInvisible): MenuItemList = runBlocking {
        try {
            val request = PageRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId(menuPageId)
                .setSkipInvisibleItems(skipInvisible)
                .build()
            stub.findMenuItemsByPage(request)
        } catch (e: Exception) {
            MenuItemList.getDefaultInstance()
        }
    }

    fun getMenuItemsFromName(menuCardId: Int, localName: String, skipInvisible: SkipInvisible): MenuItemList = runBlocking {
        try {
            val request = NameRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setLocalName(localName)
                .setSkipInvisibleItems(skipInvisible)
                .build()
            stub.getMenuItemsFromName(request)
        } catch (e: Exception) {
            MenuItemList.getDefaultInstance()
        }
    }

    fun getCluster(menuCardId: Int, clusterId: Int): ShortMenuItemList = runBlocking {
        try {
            val request = MenuCardClusterId.newBuilder()
                .setMenuCardId(menuCardId)
                .setClusterId(clusterId)
                .build()
            stub.getCluster(request)
        } catch (e: Exception) {
            ShortMenuItemList.getDefaultInstance()
        }
    }

    fun deleteProduct(menuItemId: Int): Boolean = runBlocking {
        try {
            val request = MenuItemId.newBuilder()
                .setMenuItemId(menuItemId)
                .build()
            stub.deleteProduct(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun duplicateItem(menuItemId: Int): Int? = runBlocking {
        try {
            val request = MenuItemId.newBuilder()
                .setMenuItemId(menuItemId)
                .build()
            stub.duplicateItem(request).menuItemId
        } catch (e: Exception) {
            null
        }
    }

    fun searchString(menuCardId: Int, searchString: String): MenuItemList = runBlocking {
        try {
            val request = MenuCardSearchString.newBuilder()
                .setMenuCardId(menuCardId)
                .setSearchString(searchString)
                .build()
            stub.searchString(request)
        } catch (e: Exception) {
            MenuItemList.getDefaultInstance()
        }
    }

    fun findItemsWithOptions1(
        menuCardId: Int,
        menuPageId: Int,
        sortByLocalName: Boolean,
        includeNamelessItems: Boolean,
        orderByPosition: Boolean,
        isVerticalPage: Boolean,
        skipInvisible: SkipInvisible
    ): MenuItemList = runBlocking {
        try {
            val request = FindOptions1.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId(menuPageId)
                .setSortByLocalName(sortByLocalName)
                .setIncludeNamelessItems(includeNamelessItems)
                .setOrderByPosition(orderByPosition)
                .setIsVerticalPage(isVerticalPage)
                .setSkipInvisibleItems(skipInvisible)
                .build()
            stub.findItemsWithOptions1(request)
        } catch (e: Exception) {
            MenuItemList.getDefaultInstance()
        }
    }

    fun backup(): Boolean = runBlocking {
        try {
            stub.backup(Empty.getDefaultInstance())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun restore(): Boolean = runBlocking {
        try {
            stub.restore(Empty.getDefaultInstance())
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getMenuItem(menuItemId: Int): MenuItem? = runBlocking {
        try {
            val request = MenuItemId.newBuilder()
                .setMenuItemId(menuItemId)
                .build()
            stub.getMenuItem(request)
        } catch (e: Exception) {
            null
        }
    }

    fun setPageColour(
        menuCardId: Int,
        menuPageId: Int,
        fg: Int,
        bg: Int,
        selFg: Int,
        selBg: Int,
        bg2: Int,
        selBg2: Int
    ): Boolean = runBlocking {
        try {
            val request = PageColourRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId(menuPageId)
                .setFg(fg)
                .setBg(bg)
                .setSelFg(selFg)
                .setSelBg(selBg)
                .setBg2(bg2)
                .setSelBg2(selBg2)
                .build()
            stub.setPageColour(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setPageLocations(menuCardId: Int, menuPageId: Int, locations: Int): Boolean = runBlocking {
        try {
            val request = PageLocationsRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId(menuPageId)
                .setLocations(locations)
                .build()
            stub.setPageLocations(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun findAlias(menuCardId: Int, alias: String): MenuItemList = runBlocking {
        try {
            val request = FindAliasRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setAlias(alias)
                .build()
            stub.findAlias(request)
        } catch (e: Exception) {
            MenuItemList.getDefaultInstance()
        }
    }

    fun nextMenuItemId(menuItemId: Int): Int? = runBlocking {
        try {
            val request = NextMenuItemIdRequest.newBuilder()
                .setMenuItemId(menuItemId)
                .build()
            stub.nextMenuItemId(request).menuItemId
        } catch (e: Exception) {
            null
        }
    }

    fun updatePos(itemId: Int, positionX: Int, positionY: Int, width: Int): Boolean = runBlocking {
        try {
            val request = UpdatePosRequest.newBuilder()
                .setItemId(itemId)
                .setPositionX(positionX)
                .setPositionY(positionY)
                .setWidth(width)
                .build()
            stub.updatePos(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setWidthHeight(
        menuCardId: Int,
        menuPageId: Int,
        oldWidthHeight: Int,
        newWidthHeight: Int
    ): Boolean = runBlocking {
        try {
            val request = WidthHeigtRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId(menuPageId)
                .setOldWidthHeight(oldWidthHeight)
                .setNewWidthHeight(newWidthHeight)
                .build()
            stub.setWidthHeight(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removeMenuItemsFromMenuCard(menuCardId: Int): Boolean = runBlocking {
        try {
            val request = MenuCardId.newBuilder()
                .setMenuCardId(menuCardId)
                .build()
            stub.removeMenuItemsFromMenuCard(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getNewMenuCardId(): Int? = runBlocking {
        try {
            stub.getNewMenuCardId(Empty.getDefaultInstance()).menuCardId
        } catch (e: Exception) {
            null
        }
    }

    fun exchangePages(menuCardId: Int, menuPageId1: Int, menuPageId2: Int): Boolean = runBlocking {
        try {
            val request = ExchangePagesRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId1(menuPageId1)
                .setMenuPageId2(menuPageId2)
                .build()
            stub.exchangePages(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun duplicateMenuCard(sourceCardId: Int, targetCardId: Int): Boolean = runBlocking {
        try {
            val request = DuplicationRequest.newBuilder()
                .setMenuCardId1(sourceCardId)
                .setMenuCardId2(targetCardId)
                .build()
            stub.duplicateMenuCard(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getSize(menuCardId: Int): Int? = runBlocking {
        try {
            val request = MenuCardId.newBuilder()
                .setMenuCardId(menuCardId)
                .build()
            stub.getSize(request).size
        } catch (e: Exception) {
            null
        }
    }

    fun getFirstMinutesItem(menuCardId: Int): FirstItemReply? = runBlocking {
        try {
            val request = MenuCardId.newBuilder()
                .setMenuCardId(menuCardId)
                .build()
            stub.getFirstMinutesItem(request)
        } catch (e: Exception) {
            null
        }
    }

    fun findMenuItemId(
        localName: String,
        chineseName: String,
        menuPageId: String,
        menuCardId: Int
    ): Int? = runBlocking {
        try {
            val request = FindItemRequest.newBuilder()
                .setLocalName(localName)
                .setChineseName(chineseName)
                .setMenuPageId(menuPageId)
                .setMenuCardId(menuCardId)
                .build()
            stub.findMenuItemId(request).menuItemId
        } catch (e: Exception) {
            null
        }
    }

    fun exchangeCluster(menuCardId: Int, cluster1: Int, cluster2: Int): Boolean = runBlocking {
        try {
            val request = ExchangeClusterRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setCluster1(cluster1)
                .setCluster2(cluster2)
                .build()
            stub.exchangeCluster(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun exchangeSpecialPrices(
        menuCardId: Int,
        specialPricesIndex1: Int,
        specialPricesIndex2: Int
    ): Boolean = runBlocking {
        try {
            val request = ExchangeSpecialPricesRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setSpecialPricesIndex1(specialPricesIndex1)
                .setSpecialPricesIndex2(specialPricesIndex2)
                .build()
            stub.exchangeSpecialPrices(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getProductsFromAlias(menuCardId: Int, alias: String): MenuItemIdList = runBlocking {
        try {
            val request = FindAliasRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setAlias(alias)
                .build()
            stub.getProductsFromAlias(request)
        } catch (e: Exception) {
            MenuItemIdList.getDefaultInstance()
        }
    }

    fun getProductsFromName(menuCardId: Int, searchString: String): MenuItemIdList = runBlocking {
        try {
            val request = MenuCardSearchString.newBuilder()
                .setMenuCardId(menuCardId)
                .setSearchString(searchString)
                .build()
            stub.getProductsFromName(request)
        } catch (e: Exception) {
            MenuItemIdList.getDefaultInstance()
        }
    }

    fun eraseClusterIndex(menuCardId: Int, clusterId: Int): Boolean = runBlocking {
        try {
            val request = MenuCardClusterId.newBuilder()
                .setMenuCardId(menuCardId)
                .setClusterId(clusterId)
                .build()
            stub.eraseClusterIndex(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun eraseSpecialPriceIndex(menuCardId: Int, specialPriceIndex: Int): Boolean = runBlocking {
        try {
            val request = MenuCardIdSpecialPriceIndex.newBuilder()
                .setMenuCardId(menuCardId)
                .setSpecialPriceIndex(specialPriceIndex)
                .build()
            stub.eraseSpecialPriceIndex(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun saveAllItems(items: MenuItemList): Boolean = runBlocking {
        try {
            stub.saveAllItems(items)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun mysqlDump(): String? = runBlocking {
        try {
            stub.mysqlDump(Empty.getDefaultInstance()).dump
        } catch (e: Exception) {
            null
        }
    }

    fun getPageIdFromMenuItemId(menuItemId: Int): MenuCardPageId? = runBlocking {
        try {
            val request = MenuItemId.newBuilder()
                .setMenuItemId(menuItemId)
                .build()
            stub.getPageIdFromMenuItemId(request)
        } catch (e: Exception) {
            null
        }
    }
}