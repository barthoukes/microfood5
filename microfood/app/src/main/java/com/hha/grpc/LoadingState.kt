package com.hha.grpc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.atomic.AtomicInteger

/**
 * A thread-safe, lifecycle-aware class to manage the loading state for one or more concurrent tasks.
 */
class LoadingState
{

   private val loadingTaskCount = AtomicInteger(0)

   private val _isLoading = MutableLiveData<Boolean>(false)
   val isLoading: LiveData<Boolean> = _isLoading

   // --- The private helper methods remain the same ---
   private fun beginTask()
   {
      if (loadingTaskCount.incrementAndGet() == 1)
      {
         _isLoading.postValue(true)
      }
   }

   private fun endTask()
   {
      if (loadingTaskCount.decrementAndGet() == 0)
      {
         _isLoading.postValue(false)
      }
   }

   // --- SOLUTION: The new "macro" function ---
   /**
    * Executes a given suspend block, automatically managing the loading state.
    * It ensures that beginTask() is called before the block runs and endTask() is
    * called after, even if an exception occurs.
    *
    * @param block The suspend function to execute (e.g., a gRPC call).
    */
   suspend fun <T> runTask(block: suspend () -> T): T
   {
      beginTask()
      try
      {
         // Execute the user's block of code (the gRPC call, etc.)
         return block()
      } finally
      {
         // CRITICAL: This guarantees the counter is decremented, even if the
         // 'block' throws an exception.
         endTask()
      }
   }
}
