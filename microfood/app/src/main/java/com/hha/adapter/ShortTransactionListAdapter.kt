package com.hha.adapter

import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hha.callback.ShortTransactionListener
import com.hha.framework.CShortTransaction

import tech.hha.microfood.databinding.AdapterShortTransactionBinding
import com.hha.resources.Global
import com.hha.types.EClientOrdersType
import com.hha.types.ETransType
import com.hha.util.ColourUtils
import tech.hha.microfood.R

class ShortTransactionListAdapter(
    private val onShortTransactionSelected: (CShortTransaction) -> Unit
) : RecyclerView.Adapter<ShortTransactionListAdapter.TransactionViewHolder>(),
    ShortTransactionListener
{
    val global = Global.getInstance()
    val colourCFG = global.colourCFG
    private var clientList: List<CShortTransaction> = mutableListOf()
    val mCursor = 1

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
            val colour = calculateBackgroundColour(position, transaction)
            holder.binding.lineItem.background = createBackgroundGradient(colour, 0.35f)
            holder.binding.tableId.text = getTransactionName(transaction)
            holder.binding.priceId.text = transaction.total.toString()
            val resource = getIcon(transaction)
            holder.binding.iconKindof.setImageResource(resource)
            holder.binding.tableDuration.progress = transaction.minutes
        }
        else {
            holder.binding.tableId.text = "--"
            holder.binding.lineItem.background = createBackgroundGradient(0x404040, 0.35f)
            holder.binding.priceId.text = ""
            holder.binding.iconKindof.visibility = View.GONE
            holder.binding.tableDuration.visibility = View.GONE
        }
    }

    private fun getIcon(transaction: CShortTransaction): Int
    {
        val icon = transaction.transType.toImageResource()
        return icon
    }

    private fun getTransactionName(transaction: CShortTransaction): String
    {
        return transaction.transType.toString() + " " + transaction.name
    }

    private fun createBackgroundGradient(color1: Int, factor: Float = 0.35f): GradientDrawable =
        ColourUtils.createPyramidGradient(color1, factor)

    fun calculateBackgroundColour(position: Int, transaction: CShortTransaction): Int
    {
        val highlight = position == mCursor
        val odd = when (position and 1) { 0 -> "1" else -> "2" }
        return when (transaction.status)
        {
            EClientOrdersType.INIT, EClientOrdersType.EMPTY -> 0
            EClientOrdersType.ALL, EClientOrdersType.OPEN ->
                when (highlight)
                {
                    true -> colourCFG.getColour("COLOUR_SELECTED_TABLE")
                    false -> colourCFG.getColour("COLOUR_OPEN_TABLE$odd")
                }
            EClientOrdersType.OPEN_PAID, EClientOrdersType.PAYING ->
                when (highlight)
                {
                    true -> colourCFG.getColour("COLOUR_SELECTED_TABLE")
                    false -> colourCFG.getColour("COLOUR_PAYING_TABLE$odd")
                }
            EClientOrdersType.CLOSED ->
                when (highlight)
                {
                    true -> colourCFG.getColour("COLOUR_SELECTED_TABLE")
                    false -> colourCFG.getColour("COLOUR_CLOSED_TABLE$odd")
                }
            EClientOrdersType.PERSONNEL ->
                when (highlight)
                {
                    true -> colourCFG.getColour("COLOUR_SELECTED_TABLE")
                    false -> colourCFG.getColour("COLOUR_PERSONNEL_TABLE$odd")
                }
            EClientOrdersType.CREDIT, EClientOrdersType.CLOSED_CREDIT ->
                when (highlight)
                {
                    true -> colourCFG.getColour("COLOUR_CREDIT_TABLE")
                    false -> colourCFG.getColour("COLOUR_CREDIT_TABLE$odd")
                }
            else -> 0
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
