package com.hha.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hha.framework.CCursor
import com.hha.framework.CItem
import com.hha.framework.CTransaction
import com.hha.resources.Global
import tech.hha.microfood.databinding.AdapterTransactionItem2Binding
import tech.hha.microfood.databinding.AdapterTransactionItemBinding

class TransactionItemsAdapter(
    private val onTransactionItemSelected: (CItem) -> Unit,
    colourOrderText : Int,
    colourOrderSelectedText : Int,
    colourOrderBackgroundSelected : Int,
    colourOrderBackgroundOdd : Int,
    colourOrderBackgroundEven : Int
) : RecyclerView.Adapter<TransactionItemsAdapter.TransactionItemViewHolder>() {

    private val global = Global.getInstance()
    private val m_cursor = CCursor(global.cursor.position)
    private val m_colourOrderText = colourOrderText
    private val m_colourOrderSelectedText = colourOrderSelectedText
    private val m_colourOrderBackgroundSelected = colourOrderBackgroundSelected
    private val m_colourOrderBackgroundOdd = colourOrderBackgroundOdd
    private val m_colourOrderBackgroundEven = colourOrderBackgroundEven

    inner class TransactionItemViewHolder(val binding: AdapterTransactionItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionItemViewHolder
    {
        val binding = AdapterTransactionItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionItemViewHolder(binding)
    }

    // Add this new function to your adapter
    fun updateData(newTransaction: CTransaction) {
        // The adapter now knows about the new data.
        // We don't need to store it, as we can rely on the global,
        // but we MUST tell the RecyclerView to redraw itself completely.
        notifyDataSetChanged()
    }

    fun setCursor(cursor: CCursor)
    {
        Log.i("TIA", "setCursor: $cursor")
        val oldPosition = m_cursor.position
        m_cursor.set(cursor.position)

        if (oldPosition != m_cursor.position)
        {
            if (oldPosition in 0 until itemCount)
            {
                notifyItemChanged(oldPosition)
            }
            if (m_cursor.position in 0 until itemCount)
            {
                notifyItemChanged(m_cursor.position)
            }
        }
    }

    override fun getItemCount(): Int {
        return global.transaction?.size?.plus(1) ?: 1
    }

    override fun onBindViewHolder(holder: TransactionItemViewHolder, position: Int)
    {
        // ... (Your existing setup code for clearing listeners, getting items, setting backgrounds)

        val item: CItem? = global.transaction?.get(position)
        holder.binding.transactionOrder.setBackgroundColor(getBackgroundColour(position))
        val colour = getTextColour(position)
        holder.binding.transactionItem.setTextColor(colour)
        holder.binding.transactionPrice.setTextColor(colour)
        // ... other setup ...
        if (item == null)
        {
            setupEmptyItem(holder, position)
        } else
        {
            setupItemContent(holder, item)
        }
        // Set click listener
        holder.binding.root.setOnClickListener {
            Log.i("TIA", "Item clicked at position: $position")
            if (item != null)
            {
                onTransactionItemSelected(item)
            } else
            {
                Log.i("TIA", "Empty item clicked at position: $position")
                // You might want to handle empty item clicks differently
            }
        }
    }

    private fun setupEmptyItem(holder: TransactionItemViewHolder, position: Int) {
        holder.binding.transactionItem.text = ""
        holder.binding.transactionPrice.text = ""
    }

    private fun setupItemContent(holder: TransactionItemViewHolder, item: CItem)
    {
        val quantityStr = "\n${item.getQuantity()}x "
        holder.binding.transactionItem.text = if (global.isChinese())
        {
            quantityStr + item.chineseName
        } else
        {
            quantityStr + item.localName
        }

        holder.binding.transactionPrice.text = item.getTotal().str()
    }

    private fun getTextColour(position: Int): Int
    {
        return when
        {
            m_cursor.position == position -> m_colourOrderSelectedText
            else -> m_colourOrderText
        }
    }

    private fun getBackgroundColour(position: Int): Int
    {
        return when
        {
            m_cursor.position == position -> m_colourOrderBackgroundSelected
            (position and 1) == 0 -> m_colourOrderBackgroundOdd
            else -> m_colourOrderBackgroundEven
        }
    }
}
