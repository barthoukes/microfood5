package com.hha.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hha.callback.ShortTransactionListener
import com.hha.framework.CShortTransaction

import tech.hha.microfood.databinding.AdapterShortTransactionBinding
import com.hha.resources.Global

class ShortTransactionListAdapter(
    private val onShortTransactionSelected: (CShortTransaction) -> Unit
) : RecyclerView.Adapter<ShortTransactionListAdapter.TransactionViewHolder>(),
    ShortTransactionListener
{
    val global = Global.getInstance()
    private var clientList: List<CShortTransaction> = mutableListOf()

    inner class TransactionViewHolder(val binding: AdapterShortTransactionBinding) :
        RecyclerView.ViewHolder(binding.root)

    /**
    * Updates the list of transactions in the adapter and notifies the
    * RecyclerView that the data has changed.
    */
    fun submitList(newTransactions: List<CShortTransaction>)
    {
        clientList = newTransactions
        // This tells the RecyclerView to completely redraw itself with the new data.
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder
    {
        val binding = AdapterShortTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int)
    {
        val transaction = getShortTransaction(position)
        // Set click listener on the BUTTON (not just root view)
        holder.binding.tableId.setOnClickListener {
            if (transaction != null)
            {
                Log.d("CLICK_TEST", "Name clicked at position $position") // Add this for testing
                onShortTransactionSelected(transaction)
            }
        }

        holder.binding.priceId.setOnClickListener {
            if (transaction != null)
            {
                Log.d("CLICK_TEST", "Button clicked at position $position") // Add this for testing
                onShortTransactionSelected(transaction)
            }
        }
        if (transaction != null)
        {
            holder.binding.tableId.text = transaction.name
        }
    }

    fun getShortTransaction(position: Int): CShortTransaction?
    {
        return clientList[position]
    }

    override fun getItemCount(): Int
    {
        return clientList.size
    }

    override fun onTransactionAdded(position: Int, item: CShortTransaction)
    {
        Log.d("ShortTransaction", "Listener: Item added at $position")

        // 1. Add the new item to our list at the specified position
        // Use an if-check to handle potential index-out-of-bounds scenarios safely.
        if (position >= 0 && position <= clientList.size) {
            //clientList.add(position, item)
        } else {
            // Fallback: add to the end if the position is invalid
            //clientList.add(item)
        }

        // 2. Notify the adapter that an item has been inserted
        notifyItemInserted(position)
    }

    override fun onTransactionRemoved(position: Int, newSize: CShortTransaction)
    {
        Log.d("ShortTransaction", "Listener: Item removed at $position")
        notifyItemRemoved(position)
    }

    override fun onTransactionUpdated(position: Int, item: CShortTransaction)
    {
        if (position >0 && position < clientList.size)
        {
            //clientList[position] = item
        }
        notifyItemChanged(position)
    }

    override fun onTransactionCleared()
    {
        val oldSize = clientList.size
        //clientList.clear()
        notifyItemRangeRemoved(0, oldSize)
    }
}
