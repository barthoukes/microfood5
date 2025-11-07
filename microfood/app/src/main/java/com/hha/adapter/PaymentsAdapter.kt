////////////////////////////////////////////////////////////////////////////////
// @file PaymentsAdapter.kt
// @author Bart
// @brief Adapter for payments during billing. Uses ListAdapter for performance
//        and to be a stateless UI component, fitting a modern MVVM architecture.
////////////////////////////////////////////////////////////////////////////////

package com.hha.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hha.framework.CPayment
import com.hha.model.BillDisplayLine
import com.hha.types.CMoney
import tech.hha.microfood.databinding.AdapterPaymentBinding

/**
 * A modern, stateless adapter for displaying a list of CPayment objects.
 * It receives a list from the ViewModel and knows nothing about business logic.
 *
 * @param onPaymentSelected A lambda function to be called when a payment item is clicked.
 */
class PaymentsAdapter(
    private val onPaymentSelected: (BillDisplayLine) -> Unit
) : ListAdapter<BillDisplayLine, PaymentsAdapter.PaymentsViewHolder>(PaymentDiffCallback()) {

    /**
     * The ViewHolder that holds the views for a single payment item.
     */
    inner class PaymentsViewHolder(val binding: AdapterPaymentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentsViewHolder {
        val binding = AdapterPaymentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PaymentsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentsViewHolder, position: Int) {
        // getItem() is a built-in method from ListAdapter
        val payment = getItem(position)

        // Set the listener on the entire item view for a better user experience
        holder.binding.root.setOnClickListener {
            onPaymentSelected(payment)
        }

        // Bind the data from the CPayment object to the views
        // Note: The specific properties like .name and .total might need to be
        // adjusted based on the actual fields in your CPayment class.
        holder.binding.paymentName.text = payment.text.toString()
        holder.binding.paymentAmount.text = payment.amount.str()
        // holder.binding.paymentAmount.text = payment.total.toString() // Example if you have a total
        holder.binding.dividerView.visibility = View.VISIBLE
    }
}

/**
 * DiffUtil.ItemCallback implementation for CPayment.
 * This is a requirement for ListAdapter and allows it to perform efficient
 * updates by calculating the difference between old and new lists,
 * avoiding unnecessary redraws.
 */
private class PaymentDiffCallback : DiffUtil.ItemCallback<BillDisplayLine>()
{
    override fun areItemsTheSame(oldItem: BillDisplayLine, newItem: BillDisplayLine): Boolean
    {
        // This should return true if the items represent the same entity.
        // If CPayment has a unique ID, use that: oldItem.id == newItem.id
        // As a fallback, we check for object reference equality.
        return oldItem.amount === newItem.amount && oldItem.text === newItem.text
    }

    override fun areContentsTheSame(oldItem: BillDisplayLine, newItem: BillDisplayLine): Boolean
    {
        // This should return true if the contents of the items are identical.
        // For this to work correctly, CPayment should be a 'data class'.
        return oldItem.amount == newItem.amount && oldItem.text == newItem.text
    }
}
