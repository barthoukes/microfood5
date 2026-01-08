package com.hha.callback

import com.hha.framework.CItem

interface TransactionItemListener
{
   fun onItemAdded(position: Int, item: CItem)
   fun onItemRemoved(position: Int, newSize: Int)
   fun onItemUpdated(position: Int, item: CItem)
   fun onTransactionCleared()

   /* For the loading state in the Model add 2 functions. */
   /** When the background task starts */
   fun onBeginLoading()
   /** When the background task stops */
   fun onEndLoading()
}
