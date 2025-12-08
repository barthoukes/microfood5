package com.hha.callback

import com.hha.framework.CItem
import com.hha.framework.CShortTransaction

interface ShortTransactionListener
{
   fun onTransactionAdded(position: Int, item: CShortTransaction)
   fun onTransactionRemoved(position: Int, newSize: CShortTransaction)
   fun onTransactionUpdated(position: Int, item: CShortTransaction)
   fun onTransactionCleared()
}