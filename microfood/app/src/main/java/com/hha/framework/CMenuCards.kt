package com.hha.framework
import com.google.protobuf.kotlin.toByteStringUtf8
import kotlinx.coroutines.runBlocking

import com.hha.common.MenuItem
import com.hha.common.SkipInvisible.SKIP_INVISIBLE_TRUE
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global

class CMenuCards private constructor() {

    val menuCard: MutableMap<Int,
            CMenuCard> = mutableMapOf()
    var isTakeawayPreloaded = false
    val CFG = Global.getInstance().CFG
    val userCFG = Global.getInstance().userCFG

    // Add a menu card to the structure
    fun addMenuCard(newCard: CMenuCard) {
        menuCard[newCard.menuCardId] = newCard
    }

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

    fun loadTakeaway()
    {
        if (isTakeawayPreloaded)
        {
            return
        }
        val global = Global.getInstance()
        global.menuPageId = 1
        global.menuCardId = userCFG.getValue("menu_card_takeaway")
        //val groups = CFG.getValue("display_groups")
        //val horizontal = CFG.getValue("display_groups_horizontal")
        if (menuCard.isEmpty()) {
            loadMenuCards()
        }
        val currentCard = getMenuCard(global.menuCardId)
        currentCard.loadPages()
        currentCard.getMenuPage(global.menuPageId).loadItems(SKIP_INVISIBLE_TRUE)
        isTakeawayPreloaded = true
    }

    fun getMenuCard(menuCardId: Int) : CMenuCard {
        if (menuCard.isEmpty())
        {
            loadMenuCards();
        }
        if (menuCard.containsKey(menuCardId))
        {
            return menuCard[menuCardId]!!
        }
        else
        {
            val maxId = getMaxSequence()+1
            // Create empty menu card
            val mc = CMenuCard(menuCardId = menuCardId,
                sequence = maxId + 1,
                chineseName = "",
                localName = "nieuw")
            menuCard[menuCardId] = mc
            return mc
        }
    }

    fun loadMenuCards() {
        val service = GrpcServiceFactory.createMenuCardService()
        val cards = service.findAllMenuCards()
        menuCard.clear()
        for (card in cards) {
            val newCard = CMenuCard( card.menuCardId,
                card.sequence, card.chineseName, card.localName)
            menuCard[card.menuCardId] = newCard
        }
    }

    fun getMaxMenuCardId(): Int {
        var maxId = 0
        for (mc in menuCard) {
            if (mc.value.menuCardId > maxId) {
                maxId = mc.key
            }
        }
        return maxId
    }

    fun getMaxSequence(): Int {
        var maxSeq = 1
        for (mc in menuCard) {
            if (mc.value.sequence > maxSeq) {
                maxSeq = mc.key
            }
        }
        return maxSeq
    }

    fun getMenuPage(menuCardId: Int, pageId: Int) : CMenuPage {
        val card: CMenuCard = getMenuCard(menuCardId)
        return card.getMenuPage(pageId)
    }
}
