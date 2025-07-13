package com.hha.service

import com.hha.common.Empty
import com.hha.common.MenuCardId
import com.hha.menu.page.DuplicationRequest
import com.hha.menu.page.MenuPage
import com.hha.menu.page.MenuPageServiceGrpcKt
import com.hha.menu.page.NameChange
import com.hha.menu.page.SavePagesRequest
import com.hha.menu.page.FindLikeRequest
import com.hha.menu.page.VerifyRequest
import com.hha.menu.page.LocalNameRequest
import com.hha.menu.page.MenuTranslation
import com.hha.menu.page.MenuTranslationServiceGrpcKt
import com.hha.menu.page.ExchangePagesRequest
import com.hha.menu.page.PageButtonSizeRequest
import com.hha.menu.page.PageOrientation

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class MenuPageService(channel: ManagedChannel) : BaseGrpcService<MenuPageServiceGrpcKt.MenuPageServiceCoroutineStub>(channel) {
    override val stub: MenuPageServiceGrpcKt.MenuPageServiceCoroutineStub by lazy {
        MenuPageServiceGrpcKt.MenuPageServiceCoroutineStub(channel)
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

    fun findAllPages(menuCardId: Int): List<MenuPage> = runBlocking {
        try {
            val request = MenuCardId.newBuilder()
                .setMenuCardId(menuCardId)
                .build()
            stub.findAllPages(request).menuPagesList
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun setPageButtonSize(menuCardId: Int, menuPageId: Int, newSize: Int): Boolean = runBlocking {
        try {
            val request = PageButtonSizeRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId(menuPageId)
                .setNewPageButtonSize(newSize)
                .build()
            stub.setPageButtonSize(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setPageOrientation(menuCardId: Int, menuPageId: Int, isVertical: Boolean): Boolean = runBlocking {
        try {
            val request = PageOrientation.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId(menuPageId)
                .setIsVerticalOrientation(isVertical)
                .build()
            stub.setPageOrientation(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setChineseName(menuCardId: Int, menuPageId: Int, name: String): Boolean = runBlocking {
        try {
            val request = NameChange.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId(menuPageId)
                .setName(name)
                .build()
            stub.setChineseName(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setLocalName(menuCardId: Int, menuPageId: Int, name: String): Boolean = runBlocking {
        try {
            val request = NameChange.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId(menuPageId)
                .setName(name)
                .build()
            stub.setLocalName(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setImage(menuCardId: Int, menuPageId: Int, imagePath: String): Boolean = runBlocking {
        try {
            val request = NameChange.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId(menuPageId)
                .setName(imagePath)
                .build()
            stub.setImage(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun exchangePages(menuCardId: Int, pageId1: Int, pageId2: Int): Boolean = runBlocking {
        try {
            val request = ExchangePagesRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setMenuPageId1(pageId1)
                .setMenuPageId2(pageId2)
                .build()
            stub.exchangePages(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun removePagesFromMenuCard(menuCardId: Int): Boolean = runBlocking {
        try {
            val request = MenuCardId.newBuilder()
                .setMenuCardId(menuCardId)
                .build()
            stub.removePagesFromMenuCard(request)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun saveAllPages(menuCardId: Int, pages: List<MenuPage>): Boolean = runBlocking {
        try {
            val request = SavePagesRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .addAllPages(pages)
                .build()
            stub.saveAllPages(request)
            true
        } catch (e: Exception) {
            false
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

    fun mysqlDump(): String? = runBlocking {
        try {
            stub.mysqlDump(Empty.getDefaultInstance()).dump
        } catch (e: Exception) {
            null
        }
    }

    fun verify(menuCardId: Int, maxGroups: Int): Boolean = runBlocking {
        try {
            val request = VerifyRequest.newBuilder()
                .setMenuCardId(menuCardId)
                .setMaxGroups(maxGroups)
                .build()
            stub.verify(request)
            true
        } catch (e: Exception) {
            false
        }
    }
}

class MenuTranslationService(channel: ManagedChannel) : BaseGrpcService<MenuTranslationServiceGrpcKt.MenuTranslationServiceCoroutineStub>(channel) {
    override val stub: MenuTranslationServiceGrpcKt.MenuTranslationServiceCoroutineStub by lazy {
        MenuTranslationServiceGrpcKt.MenuTranslationServiceCoroutineStub(channel)
    }

    fun findOrCreateTranslation(localName: String, chineseName: String): List<MenuTranslation> = runBlocking {
        try {
            val request = MenuTranslation.newBuilder()
                .setLocalName(localName)
                .setChineseName(chineseName)
                .build()
            stub.findOrCreateTranslation(request).translationsList
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun findTranslation(localName: String): String? = runBlocking {
        try {
            val request = LocalNameRequest.newBuilder()
                .setLocalName(localName)
                .build()
            stub.findTranslation(request).name
        } catch (e: Exception) {
            null
        }
    }

    fun findLike(searchString: String, inChinese: Boolean): List<MenuTranslation> = runBlocking {
        try {
            val request = FindLikeRequest.newBuilder()
                .setSearchString(searchString)
                .setInChinese(inChinese)
                .build()
            stub.findLike(request).translationsList
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun dump(): Boolean = runBlocking {
        try {
            stub.dump(Empty.getDefaultInstance())
            true
        } catch (e: Exception) {
            false
        }
    }
}