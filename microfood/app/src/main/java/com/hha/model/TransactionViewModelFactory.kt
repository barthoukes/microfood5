package com.hha.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * A Singleton Factory for providing the same instance of TransactionViewModel to all Activities.
 */
object TransactionViewModelFactory : ViewModelProvider.Factory
{

   // The single, shared instance of the ViewModel
   private val viewModel = TransactionViewModel()

   override fun <T : ViewModel> create(modelClass: Class<T>): T
   {
      if (modelClass.isAssignableFrom(TransactionViewModel::class.java))
      {
         @Suppress("UNCHECKED_CAST")
         return viewModel as T
      }
      throw IllegalArgumentException("Unknown ViewModel class")
   }
}
