package com.hha.service

import com.hha.common.MenuItem
import com.hha.store.part.*
import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class StorePartService(channel: ManagedChannel) : BaseGrpcService<StorePartServiceGrpcKt.StorePartServiceCoroutineStub>(channel)
{
   override val stub: StorePartServiceGrpcKt.StorePartServiceCoroutineStub by lazy {
      StorePartServiceGrpcKt.StorePartServiceCoroutineStub(channel)
   }

   fun reduceStorage(transactionId: Int) = runBlocking {
      try
      {
         val request = TransactionId.newBuilder()
            .setTransactionId(transactionId)
            .build()
         stub.reduceStorage(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun selectMenuItem(menuCardId: Int, menuItemId: Int) = runBlocking {
      try
      {
         val request = MenuCardItemId.newBuilder()
            .setMenuCardId(menuCardId)
            .setMenuItemId(menuItemId)
            .build()
         stub.selectMenuItem(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun getMenuItems(menuCardId: Int) = runBlocking {
      try
      {
         val request = MenuCardId.newBuilder()
            .setMenuCardId(menuCardId)
            .build()
         stub.getMenuItems(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun insertSmurf(menuItemId: Int, smurfId: Int, percentage: Float) = runBlocking {
      try
      {
         val request = NewSmurfRequest.newBuilder()
            .setMenuItemId(menuItemId)
            .setSmurfId(smurfId)
            .setPercentage(percentage)
            .build()
         stub.insertSmurf(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun removeStorePart(storePartId: Int) = runBlocking {
      try
      {
         val request = StorePartId.newBuilder()
            .setStorePartId(storePartId)
            .build()
         stub.removeStorePart(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun calculateUsagePerYear(itemId: Int) = runBlocking {
      try
      {
         val request = ItemId.newBuilder()
            .setItemId(itemId)
            .build()
         stub.calculateUsagePerYear(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun calculateSoldOutDates(itemId: Int) = runBlocking {
      try
      {
         val request = ItemId.newBuilder()
            .setItemId(itemId)
            .build()
         stub.calculateSoldOutDates(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun calculateMenuTranslations(itemId: Int) = runBlocking {
      try
      {
         val request = ItemId.newBuilder()
            .setItemId(itemId)
            .build()
         stub.calculateMenuTranslations(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun calculateFirstDay(itemId: Int) = runBlocking {
      try
      {
         val request = ItemId.newBuilder()
            .setItemId(itemId)
            .build()
         stub.calculateFirstDay(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun calculateTotalSold(itemId: Int) = runBlocking {
      try
      {
         val request = ItemId.newBuilder()
            .setItemId(itemId)
            .build()
         stub.calculateTotalSold(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun updateSold(itemId: Int) = runBlocking {
      try
      {
         val request = ItemId.newBuilder()
            .setItemId(itemId)
            .build()
         stub.updateSold(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun getStorePartFromId(storePartId: Int) = runBlocking {
      try
      {
         val request = StorePartId.newBuilder()
            .setStorePartId(storePartId)
            .build()
         stub.getStorePartFromId(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }
}
