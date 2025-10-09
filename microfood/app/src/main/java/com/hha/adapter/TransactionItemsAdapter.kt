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
    colourOrderBackgroundSelected : Int,
    colourOrderBackgroundOdd : Int,
    colourOrderBackgroundEven : Int
) : RecyclerView.Adapter<TransactionItemsAdapter.TransactionItemViewHolder>() {

    private val global = Global.getInstance()
    private val m_cursor = CCursor(global.cursor.position)
    private val m_colourOrderText = colourOrderText
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

    override fun onBindViewHolder(holder: TransactionItemViewHolder, position: Int) {
        // ... (Your existing setup code for clearing listeners, getting items, setting backgrounds)

        val item: CItem? = global.transaction?.get(position)    holder.binding.transactionOrder.setBackgroundColor(getBackgroundColour(position))
        // ... other setup ...
        if (item == null) {
            setupEmptyItem(holder, position)
        } else {
            setupItemContent(holder, item, m_colourOrderText)
        }

        // SMART TOUCH LISTENER
        val touchSlop = ViewConfiguration.get(holder.itemView.context).scaledTouchSlop
        var startX = 0f
        var startY = 0f
        var isClick = true

        holder.binding.root.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    isClick = true
                    // Request the touch event for this view.
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    // Return true to indicate we are handling subsequent events.
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = Math.abs(event.x - startX)
                    val dy = Math.abs(event.y - startY)

                    if (dx > touchSlop || dy > touchSlop) {
                        // This is a scroll.
                        isClick = false
                        // Release the lock so the parent RecyclerView can scroll.
                        v.parent.requestDisallowInterceptTouchEvent(false)
                        // *** THE FIX ***
                        // Return false to pass the event to the parent.
                        return@setOnTouchListener false
                    }
                    // If it's not a scroll yet, continue consuming the event.
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // Give back the touch control unconditionally on UP.
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    if (isClick) {
                        // Perform the click action.
                        Log.i("TIA", "Item clicked at position: $position")
                        if (item != null) {
                            onTransactionItemSelected(item)
                        } else {
                            Log.i("TIA", "Empty item clicked at position: $position")
                        }
                        v.performClick() // Important for accessibility.
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    // Action was canceled (e.g., by the parent scrolling).
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    isClick = false
                    true
                }
                else -> false
            }
        }

        // Set the OnClickListener. It will be called by v.performClick().
        holder.binding.root.setOnClickListener {
            // The logic is now in ACTION_UP, but this listener is still required
            // for accessibility and for `performClick()` to work.
            // You can leave the body empty or add a backup log.
            Log.v("TIA", "OnClickListener triggered for position $position")
        }

        // Ensure the view is marked as clickable.
        holder.binding.root.isClickable = true
    }


    fun onBindViewHolder2(holder: TransactionItemViewHolder, position: Int)
    {
        // Clear previous click listener to avoid duplicates
        holder.binding.root.setOnClickListener(null)

        // Remove this line - it's causing all items to appear "selected"
        // holder.binding.transactionOrder.isSelected = true

        // Safe transaction access
        val transaction = global.transaction
        if (transaction == null)
        {
            setupEmptyItem(holder, position)
            return
        }

        val item: CItem? = transaction[position]
        val background = getBackgroundColour(position)

        holder.binding.transactionOrder.setBackgroundColor(background)
        holder.binding.transactionItem.isContextClickable = false

        // Set selection state based on cursor position, not force all to true
        holder.binding.transactionOrder.isSelected = false

        if (item == null)
        {
            setupEmptyItem(holder, position)
        } else
        {
            setupItemContent(holder, item, m_colourOrderText)
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

    private fun setupItemContent(holder: TransactionItemViewHolder, item: CItem,
                                 colourOrderText : Int)
    {
        val tc = colourOrderText //  colourCFG.getTextColour("COLOUR_ORDER_TEXT")
        holder.binding.transactionItem.setTextColor(tc)

        val quantityStr = "\n${item.getQuantity()}x "
        holder.binding.transactionItem.text = if (global.isChinese())
        {
            quantityStr + item.chineseName
        } else
        {
            quantityStr + item.localName
        }

        holder.binding.transactionPrice.text = item.getTotal().str()
        holder.binding.transactionPrice.setTextColor(tc)
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
