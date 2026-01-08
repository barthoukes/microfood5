package com.hha.model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hha.framework.CFloorTables
import com.hha.framework.CShortTransaction
import com.hha.framework.CShortTransactionList
import com.hha.resources.Global
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for managing the list of short transactions (overview).
 * This is used by AskTransactionActivity to show available tables/transactions.
 * Handles loading, filtering, and sorting of transaction lists.
 */
class ShortTransactionsModel : ViewModel()
{
   // LiveData for the list of short transactions
   private val _shortTransactionList = MutableLiveData<List<CShortTransaction>>()
   val shortTransactionList: LiveData<List<CShortTransaction>> = _shortTransactionList

   // StateFlow for loading state (more modern approach)
   private val _isLoading = MutableStateFlow(false)
   val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

   // Error handling
   private val _errorMessage = MutableLiveData<String?>()
   val errorMessage: LiveData<String?> = _errorMessage

   // For auto-refresh functionality
   private var autoRefreshJob: Job? = null
   private var isAutoRefreshEnabled = false
   private val autoRefreshInterval = 30000L // 30 seconds

   // Configuration
   private val global = Global.getInstance()
   private val CFG = global.CFG
   private val tag = "ShortTransactionsModel"

   // Cache management
   private var lastLoadedTime: Long = 0
   private val cacheValidityDuration = 30000L // 30 seconds
   private var mBill = false
   private val mShowAllTransactions = CFG.getBoolean("show_all_transactions")

   init
   {
      Log.i(tag, "ShortTransactionsModel initialized")
      setupAutoRefresh()
   }

   /**
    * Enable/disable auto-refresh
    */
   fun setAutoRefresh(enabled: Boolean)
   {
      isAutoRefreshEnabled = enabled
      if (enabled)
      {
         setupAutoRefresh()
      } else
      {
         stopAutoRefresh()
      }
   }

   /**
    * Clear error message
    */
   fun clearError()
   {
      _errorMessage.value = null
   }

   /**
    * Clear transaction list (e.g., on logout)
    */
   fun clearTransactions()
   {
      _shortTransactionList.value = emptyList()
      lastLoadedTime = 0
   }

   override fun onCleared()
   {
      super.onCleared()
      stopAutoRefresh()
      Log.i(tag, "ShortTransactionsModel cleared")
   }

   fun refreshAllShortTransactions()
   {
      Log.i(tag, "refreshAllShortTransactions")
      if (mBill || mShowAllTransactions)
      {
         listAll(true)
      } else
      {
         listOpen()
      }
   }

   private fun setupAutoRefresh()
   {
      if (isAutoRefreshEnabled && autoRefreshJob?.isActive != true)
      {
         autoRefreshJob = viewModelScope.launch {
            while (isActive && isAutoRefreshEnabled)
            {
               delay(autoRefreshInterval)
               if (isAutoRefreshEnabled)
               {
                  Log.d(tag, "Auto-refreshing transactions")
                  listOpen()
               }
            }
         }
      }
   }

   private fun stopAutoRefresh()
   {
      autoRefreshJob?.cancel()
      autoRefreshJob = null
   }

   private fun shouldUseCache(): Boolean
   {
      val now = System.currentTimeMillis()
      return lastLoadedTime > 0 && (now - lastLoadedTime) < cacheValidityDuration
   }

   fun listOpen()
   {
      Log.i(tag, "listOpen")
      if (_isLoading.value) {
         Log.i(tag, "listOpen called but a load is already in progress. Aborting.")
         return
      }
      // Launch a coroutine in the ViewModel's lifecycle scope.
      // This ensures the task is cancelled if the ViewModel is destroyed.
      viewModelScope.launch {
         // 1. Set loading state to TRUE on the main thread before starting the background task.
         _isLoading.value = true
         val list = CShortTransactionList()

         // 2. Switch to a background thread (Dispatchers.IO) for the blocking call.
         val resultList = withContext(Dispatchers.IO) {
            list.loadOpenTransactions() // This now runs safely in the background
            list.getList() // Return the result from the background task
         }

         // 3. Back on the main thread, post the result and set loading state to FALSE.
         _shortTransactionList.value = resultList
         _isLoading.value = false
         Log.i(tag, "listOpen loaded")
      }
   }


   fun listAll(sortOnTime: Boolean = true)
   {
      Log.i(tag, "listAll")

      if (_isLoading.value) {
         Log.i(tag, "listAll called but a load is already in progress. Aborting.")
         return
      }
      viewModelScope.launch {
         _isLoading.value = true
         val resultList = withContext(Dispatchers.IO) {
            val list = CShortTransactionList()
            list.loadAllTransactions(sortOnTime)
            list.getList()
         }
         _shortTransactionList.value = resultList
         _isLoading.value = false
         Log.i(tag, "listAll loaded")
      }
   }

}
