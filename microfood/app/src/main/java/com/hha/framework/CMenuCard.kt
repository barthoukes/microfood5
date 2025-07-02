package com.hha.framework

import com.hha.framework.CMenuPage
import com.hha.grpc.GrpcServiceFactory
import kotlin.collections.List

data class CMenuCard(
    var menuCardId: Int,
    var sequence: Int,
    var chineseName: String,
    var localName: String,
    var menuPage: MutableMap<Int, CMenuPage> = mutableMapOf()
) {
    // Secondary constructor should properly delegate to primary constructor
    constructor(
        cardId: Int,
        seq: Int,
        chinese: String,
        local: String
    ) : this(
        menuCardId = cardId,
        sequence = seq,
        chineseName = chinese,
        localName = local
    )

    fun getMenuPage(pageId: Int): CMenuPage
    {
        if (menuPage.isEmpty())
        {
            loadPages();
        }
        if (menuPage.containsKey(pageId))
        {
            return menuPage[pageId]!!
        }
        else
        {
            // Create empty menu page
            val mp = CMenuPage(menuCardId, pageId, 24, "CH", "ETEN")
            menuPage[pageId] = mp
            return mp
        }
    }

    fun loadPages() {
        val menuPageService = GrpcServiceFactory.createMenuPageService()
        val pages = menuPageService.findAllPages(menuCardId)
        menuPage.clear()
        for (page in pages) {
            val newPage = CMenuPage(page.menuCardId,
                page.menuPageId, page.pageButtonSize / 1000,
                page.chineseName, page.localName)
            addMenuPage(newPage)
        }
    }

    // Improved add method
    fun addMenuPage(newPage: CMenuPage) {
        menuPage[newPage.menuPageId] = newPage
    }

    override fun toString(): String {
        return "CMenuCard(id=$menuCardId, " +
                "name='$localName', pages=${menuPage.size})"
    }

    // Helper method to get ordered pages
    fun getOrderedPages(): MutableMap<Int, CMenuPage> {
        return menuPage // This line remains the same
    }
}
