package com.hha.service

import com.hha.daily.checksum.DeleteTransactionRequest
import com.hha.daily.checksum.TransactionId
import com.hha.daily.checksum.DailyTransactionChecksumServiceGrpcKt
import com.hha.daily.item.TransactionTimeFrame
import io.grpc.ManagedChannel
import kotlinx.coroutines.runBlocking


class DailyTransactionChecksumService(channel: ManagedChannel) : BaseGrpcService<DailyTransactionChecksumServiceGrpcKt
   .DailyTransactionChecksumServiceCoroutineStub>(channel)
{
   override val stub: DailyTransactionChecksumServiceGrpcKt.DailyTransactionChecksumServiceCoroutineStub by lazy {
      DailyTransactionChecksumServiceGrpcKt.DailyTransactionChecksumServiceCoroutineStub(channel)
   }

   fun DeleteTransaction(transactionId: Int) = runBlocking {
      try {
         val request = DeleteTransactionRequest.newBuilder()
            .setTransactionId(transactionId)
            .build()
         stub.deleteTransaction(request)
      }
      catch (e: Exception) {}
   }

   fun Checksum(transactionId: Int) = runBlocking {
      try {
         val request = TransactionId.newBuilder()
            .setTransactionId(transactionId)
            .build()
         stub.checksum(request)
      }
      catch (e: Exception) {}
   }

   fun MoveChecksumToArchive(transactionId: Int) = runBlocking {
      try {
         val request = TransactionId.newBuilder()
            .setTransactionId(transactionId)
            .build()
         stub.moveChecksumToArchive(request)
      }
      catch (e: Exception) {}
   }
}