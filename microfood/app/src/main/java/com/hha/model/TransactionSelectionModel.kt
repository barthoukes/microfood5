package com.hha.model

import androidx.activity.result.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hha.framework.COpenClientsHandler
import com.hha.framework.CShortTransaction
import com.hha.framework.CShortTransactionList
import com.hha.framework.CTransaction
import com.hha.grpc.GrpcServiceFactory
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







}
