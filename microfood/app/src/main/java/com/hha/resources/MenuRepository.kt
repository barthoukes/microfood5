package com.hha.resources

import com.hha.framework.CMenuCard
import com.hha.framework.CMenuItems
import com.hha.grpc.GrpcServiceFactory
import kotlin.collections.set

object MenuRepository {

   // Store all pages and their items in memory
   private val allPages = mutableMapOf<Int, CMenuCard>()
   private val allItemsByPage = mutableMapOf<Int, CMenuItems>()
   private val CFG = Global.getInstance().CFG
   private val userCFG = Global.getInstance().userCFG

   var isLoaded = false
      private set

   /**
    * Call this from your Application's onCreate() or a loading screen.
    * It pre-loads all menu cards, pages, and items.
    */
   fun preloadAllMenus()
   {
      if (isLoaded) return

      val service = GrpcServiceFactory.createMenuCardService()
      val cards = service.findAllMenuCards()
      allPages.clear()
      for (card in cards)
      {
         val newCard = CMenuCard( card.menuCardId,
            card.sequence, card.chineseName, card.localName)
         allPages[card.menuCardId] = newCard
      }
      isLoaded = true
   }

   fun getPage(pageId: Int): CMenuCard? {
      return allPages[pageId]
   }

   fun getItemsForPage(pageId: Int): CMenuItems? {
      return allItemsByPage[pageId]
   }

   fun getOrderedPagesForCard(menuCardId: Int): List<CMenuCard> {
      // This is a simplified example; you might need to filter by menuCardId if you have many
      return allPages.values.sortedBy { it.sequence }
   }
}
