package com.hha.framework

import com.hha.service.MenuCardService
import com.hha.common.MenuItem
import com.hha.grpc.GrpcServiceFactory
import com.hha.menu.card.ProductRequest
import com.hha.resources.Global
import com.hha.service.MenuItemService
import kotlinx.coroutines.runBlocking

class CMenuCards private constructor() {

    companion object {

        @Volatile
        private var instance: CMenuCards? = null

        fun getInstance(): CMenuCards {
            return instance ?: synchronized(this) {
                instance ?: CMenuCards().also { instance = it }
            }
        }
    }

    fun getProductFromProductId(menuItemId: Int): MenuItem? = runBlocking {
        try {
            val mis = GrpcServiceFactory.createMenuItemService()
            return@runBlocking mis.getProductFromProductId(menuItemId)
        } catch (e: Exception) {
            null
        }
    }

}
