package com.hha.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hha.common.textDump
import com.hha.framework.CMenuPage
import com.hha.framework.CPayment
import com.hha.resources.Global
import tech.hha.microfood.databinding.AdapterPaymentBinding

class PaymentsAdapter(
    private val onPaymentSelected: (CPayment) -> Unit
) : RecyclerView.Adapter<PaymentsAdapter.PaymentsViewHolder>() {
    val global = Global.getInstance()

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

        // Set click listener on the BUTTON (not just root view)
//        holder.binding.menuPageButton.setOnClickListener {
//            onPageSelected(position)
//        }
        holder.binding.paymentName.setOnClickListener {
            Log.d("CLICK_TEST", "Payment clicked at position $position") // Add this for testing
            val payment = getPayment(position)
            if (payment != null) {
                onPaymentSelected(payment)
            }
        }
        holder.binding.paymentName.setOnClickListener {
            Log.d("CLICK_TEST", "PAyment clicked at position $position") // Add this for testing
            val payment = getPayment(position)
            if (payment !=null) {
                onPaymentSelected(payment)
            }
        }

//        var value = holder.itemView.context.resources.displayMetrics.widthPixels/7
//        holder.itemView.layoutParams.width = value
//        val page = getPage(position)
//        var text = "-"
//        if (page != null) {
//            if (global.isChinese()) {
//                text = page.chineseName
//            }
//            else {
//                text = page.localName
//            }
//            if (position == global.menuPageId)
//            {
//                text += "**"
//            }
//        }
//        // Update selected state
//        holder.binding.itemName.text = text
        holder.binding.paymentName.text = "payment"
        holder.binding.paymentAmount.text = "€ 10.00"
        holder.binding.dividerView.visibility = View.VISIBLE
    }

    fun getPayment(position: Int): CPayment? {
        return CPayment()
    }

    override fun getItemCount() = 10
}
