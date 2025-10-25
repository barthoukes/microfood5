package com.hha.service

import com.hha.common.CookingState
import com.hha.common.Empty
import com.hha.prepare.transaction.*
import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking

class PrepareTransactionService(channel: ManagedChannel) : BaseGrpcService<PrepareTransactionServiceGrpcKt.PrepareTransactionServiceCoroutineStub>(channel)
{
   override val stub: PrepareTransactionServiceGrpcKt.PrepareTransactionServiceCoroutineStub by lazy {
      PrepareTransactionServiceGrpcKt.PrepareTransactionServiceCoroutineStub(channel)
   }

   fun updatePrepareTransaction(
      id: Int,
      transactionId: Int,
      cookingState: CookingState,
      displayPosition: Int,
      startTime: String,
      lowestTimeFrame: Int,
      highestTimeFrame: Int
   ) = runBlocking {
      try
      {
         val request = PrepareTransaction.newBuilder()
            .setId(id)
            .setTransactionId(transactionId)
            .setCookingState(cookingState)
            .setDisplayPosition(displayPosition)
            .setStartTime(startTime)
            .setLowestTimeFrame(lowestTimeFrame)
            .setHighestTimeFrame(highestTimeFrame)
            .build()
         stub.updatePrepareTransaction(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun removeFinishedTransactions() = runBlocking {
      try
      {
         val request = Empty.getDefaultInstance()
         stub.removeFinishedTransactions(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun getTransactionMap() = runBlocking {
      try
      {
         val request = Empty.getDefaultInstance()
         stub.getTransactionMap(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun getAllVisibleTransactions() = runBlocking {
      try
      {
         val request = Empty.getDefaultInstance()
         stub.getAllVisibleTransactions(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun getAllNewTransactions() = runBlocking {
      try
      {
         val request = Empty.getDefaultInstance()
         stub.getAllNewTransactions(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun setState(transactionId: Long, cookingState: CookingState, position: Int) = runBlocking {
      try
      {
         val request = SetStateRequest.newBuilder()
            .setTransactionId(transactionId)
            .setCookingState(cookingState)
            .setPosition(position)
            .build()
         stub.setState(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun clear(transactionId: Int) = runBlocking {
      try
      {
         val request = ClearRequest.newBuilder()
            .setTransactionId(transactionId)
            .build()
         stub.clear(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }

   fun addPrepare(transactionId: Int) = runBlocking {
      try
      {
         val request = AddPrepareRequest.newBuilder()
            .setTransactionId(transactionId)
            .build()
         stub.addPrepare(request)
      } catch (e: Exception)
      {
         // Handle exception
      }
   }
}
