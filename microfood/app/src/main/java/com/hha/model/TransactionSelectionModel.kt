package com.hha.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hha.framework.CShortTransaction
import com.hha.framework.CShortTransactionList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel responsible for fetching and managing a list of short transactions.
 * This is typically used for overview screens like a floor plan or a list of open orders.
 */
class TransactionSelectionModel : ViewModel()
{

   // 1. LIVE DATA
   // The private MutableLiveData that can be modified within the ViewModel.
   private val _shortTransactionList = MutableLiveData<List<CShortTransaction>>()
   private var mBill = false
   private var mShowAllTransactions = false

   // The public LiveData that is exposed to the UI for observation. It's read-only from the outside.
   val shortTransactionList: LiveData<List<CShortTransaction>> = _shortTransactionList

   // LiveData to signal when data is being loaded, allowing the UI to show a progress indicator.
   private val _isLoading = MutableLiveData<Boolean>()
   val isLoading: LiveData<Boolean> = _isLoading

   fun listOpen()
   {
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
      }
   }

   fun listAll(sortOnTime: Boolean = true)
   {
      viewModelScope.launch {
         _isLoading.value = true
         val resultList = withContext(Dispatchers.IO) {
            val list = CShortTransactionList()
            list.loadAllTransactions(sortOnTime)
            list.getList()
         }
         _shortTransactionList.value = resultList
         _isLoading.value = false
      }
   }

   fun refreshAllData()
   {
      if (mBill || mShowAllTransactions)
      {
         listAll(true);
         //m_tableSelection.scrollToPixel( m_offsetBill, 0);
      }
      else
      {
         listOpen();
         //m_tableSelection.scrollToPixel( m_offsetOrder, 0);
      }


      listOpen()
//      val currentShortTransactionList = _shortTransactionList.value
//      if (currentShortTransactionList != null)
//      {
//         val list = CShortTransactionList()
//         list.loadOpenTransactions()
//
//         _shortTransactionList.postValue(list.getList())
//      }
   }
}
