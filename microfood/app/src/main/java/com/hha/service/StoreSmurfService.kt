package com.hha.service

import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

import com.hha.store.smurf.DangerName
import com.hha.store.smurf.SearchName
import com.hha.store.smurf.SmurfQuantity
import com.hha.store.smurf.StoreSmurfId
import com.hha.store.smurf.TotalStore
import com.hha.store.smurf.TotalStoreSmurf
import com.hha.store.smurf.storeSmurfServiceGrpcKt

class StoreSmurfService(channel: ManagedChannel) : BaseGrpcService<storeSmurfServiceGrpcKt
   .storeSmurfServiceCoroutineStub>(channel)
{
   public final val SQL_ALL_ITEMS: Int = -1
   override val stub: storeSmurfServiceGrpcKt.storeSmurfServiceCoroutineStub by lazy {
      storeSmurfServiceGrpcKt.storeSmurfServiceCoroutineStub(channel)
   }

   fun addOrGetSmurf(dangerLevel: Int, name: String) = runBlocking {
      try
      {
         val request = DangerName.newBuilder()
            .setDangerLevel(dangerLevel)
            .setName(name)
            .build()
         stub.addOrGetSmurf(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun addQuantity(smurfId: Int, quantity: Int) = runBlocking {
      try
      {
         val request = SmurfQuantity.newBuilder()
            .setSmurfId(smurfId)
            .setQuantity(quantity)
            .build()
         stub.addQuantity(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun calculateFirstDay(storeSmurfId: Int) = runBlocking {
      try
      {
         val request = StoreSmurfId.newBuilder()
            .setStoreSmurfId(storeSmurfId)
            .build()
         stub.calculateFirstDay(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun calculateSoldOutDates(storeSmurfId: Int) = runBlocking {
      try
      {
         val request = StoreSmurfId.newBuilder()
            .setStoreSmurfId(storeSmurfId)
            .build()
         stub.calculateSoldOutDates(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun calculateTotalSold(storeSmurfId: Int) = runBlocking {
      try
      {
         val request = StoreSmurfId.newBuilder()
            .setStoreSmurfId(storeSmurfId)
            .build()
         stub.calculateTotalSold(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun calculateTotalStock(storeSmurfId: Int) = runBlocking {
      try
      {
         val request = StoreSmurfId.newBuilder()
            .setStoreSmurfId(storeSmurfId)
            .build()
         stub.calculateTotalStock(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun calculateUsagePerYear(storeSmurfId: Int) = runBlocking {
      try
      {
         val request = StoreSmurfId.newBuilder()
            .setStoreSmurfId(storeSmurfId)
            .build()
         stub.calculateUsagePerYear(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun removeSmurf(storeSmurfId: Int) = runBlocking {
      try
      {
         val request = StoreSmurfId.newBuilder()
            .setStoreSmurfId(storeSmurfId)
            .build()
         stub.removeSmurf(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun setSearchString(name: String) = runBlocking {
      try
      {
         val request = SearchName.newBuilder()
            .setName(name)
            .build()
         stub.setSearchString(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun updateQuantity(smurfId: Int, totals: TotalStore) = runBlocking {
      try
      {
         val request = TotalStoreSmurf.newBuilder()
            .setSmurfId(smurfId)
            .setTotals(totals)
            .build()
         stub.updateQuantity(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }
}
