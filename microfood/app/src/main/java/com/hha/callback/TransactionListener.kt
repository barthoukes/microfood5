package com.hha.callback

import com.hha.framework.CItem

interface TransactionListener
{
   fun onItemAdded(position: Int, item: CItem)
   fun onItemRemoved(position: Int)
   fun onItemUpdated(position: Int, item: CItem)
   fun onTransactionCleared()
}
