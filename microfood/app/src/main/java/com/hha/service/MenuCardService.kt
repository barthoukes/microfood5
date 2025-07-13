package com.hha.service

import com.hha.common.Empty
import com.hha.common.MenuCardId
import com.hha.common.MenuItem
import com.hha.menu.card.ExchangeClusterRequest
import com.hha.menu.card.ExchangeRequest
import com.hha.menu.card.MenuCard
import com.hha.menu.card.MenuCardPageId
import com.hha.menu.card.MenuCardServiceGrpcKt
import com.hha.menu.card.NewMenuCardRequest
import com.hha.menu.card.ProductRequest
import com.hha.menu.card.SetMenuCardNameRequest
import com.hha.menu.card.WidthHeightRequest

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class MenuCardService(channel: ManagedChannel) : BaseGrpcService<MenuCardServiceGrpcKt.MenuCardServiceCoroutineStub>(channel) {
    override val stub: MenuCardServiceGrpcKt.MenuCardServiceCoroutineStub by lazy {
        MenuCardServiceGrpcKt.MenuCardServiceCoroutineStub(channel)
    }

    fun findAllMenuCards(): List<MenuCard> = runBlocking {
        try {
            stub.findAllMenuCards(Empty.getDefaultInstance()).menuCardsList
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun setMenuCardName(menuCardId: Int, name: String, isChinese: Boolean): Boolean = runBlocking {
        try {
            val request = SetMenuCardNameRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setName(name)
                .setIsChinese(isChinese)
                .build()
            stub.setMenuCardName(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun insertMenuCard(localName: String, chineseName: String): MenuCard? = runBlocking {
        try {
            val request = NewMenuCardRequest.newBuilder()
                .setLocalName(localName)
                .setChineseName(chineseName)
                .build()
            stub.insertMenuCard(request)
        } catch (e: Exception) {
            null
        }
    }

    fun removeMenuCard(menuCardId: Int): Boolean = runBlocking {
        try {
            val request = MenuCardId.newBuilder()
                .setMenuCardId(menuCardId)
                .build()
            stub.removeMenuCard(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun updateMenuCard(menuCard: MenuCard): Boolean = runBlocking {
        try {
            stub.updateMenuCard(menuCard)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun exchangeMenuCards(sequence1: Int, sequence2: Int): Boolean = runBlocking {
        try {
            val request = ExchangeRequest.newBuilder()
                .setSequence1(sequence1)
                .setSequence2(sequence2)
                .build()
            stub.exchangeMenuCards(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getMaxSequence(): Int? = runBlocking {
        try {
            stub.getMaxSequence(Empty.getDefaultInstance()).menuCardId
        } catch (e: Exception) {
            null
        }
    }

    fun getProductFromProductId(menuCardId: Int, menuPageId: Int, menuItemId: Int): MenuItem? = runBlocking {
        try {
            val request = ProductRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId(menuPageId)
                .setMenuItemId(menuItemId)
                .build()
            stub.getProductFromProductId(request)
        } catch (e: Exception) {
            null
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

    fun rotate(menuCardId: Int, menuPageId: Int): Boolean = runBlocking {
        try {
            val request = MenuCardPageId.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId(menuPageId)
                .build()
            stub.rotate(request)
            true
        } catch (e: Exception) {
            false
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

    fun setSize(menuCardId: Int, menuPageId: Int, newWidth: Int, newHeight: Int): Boolean = runBlocking {
        try {
            val request = WidthHeightRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId(menuPageId)
                .setNewWidth(newWidth)
                .setNewHeight(newHeight)
                .build()
            stub.setSize(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun clear(menuCardId: Int, menuPageId: Int): Boolean = runBlocking {
        try {
            val request = MenuCardPageId.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId(menuPageId)
                .build()
            stub.clear(request)
            true
        } catch (e: Exception) {
            false
        }
    }
}