package com.hha.modalDialog

import com.hha.types.EPaymentMethod

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.hha.callback.PaymentEnteredListener
import com.hha.types.CMoney
import tech.hha.microfood.R
import tech.hha.microfood.databinding.MessageboxPaymentBinding

class ModalDialogPayment : DialogFragment()
{
   var listener: PaymentEnteredListener? = null

   private var _binding: MessageboxPaymentBinding? = null
   private val binding get() = _binding!!

   // Use a companion object to pass arguments safely
   companion object
   {
      private const val ARG_AMOUNT = "amount"
      fun newInstance(amount: CMoney): ModalDialogPayment
      {
         return ModalDialogPayment().apply {
            arguments = bundleOf(ARG_AMOUNT to amount.toLong())
         }
      }
   }

   override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
   {
      _binding = MessageboxPaymentBinding.inflate(inflater, container, false)
      return binding.root
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?)
   {
      super.onViewCreated(view, savedInstanceState)

      setupInitialState()
      setupClickListeners()
   }

   private fun setupInitialState()
   {
      // Set the initial amount from the arguments
      val initialAmountLong = arguments?.getInt(ARG_AMOUNT) ?: 0
      val initialAmount = CMoney(initialAmountLong)
      binding.amountEditText.setText(initialAmount.toString().replace(",", "."))

      // Pre-select the "Cash" chip
      binding.chipCash.isChecked = true
   }

   private fun setupClickListeners()
   {
      binding.buttonCancel.setOnClickListener {
         dismiss() // Close the dialog
      }

      binding.buttonOk.setOnClickListener {
         onConfirm()
      }
   }

   private fun onConfirm()
   {
      // 1. Get Amount
      val amountStr = binding.amountEditText.text.toString()
      val amount = try
      {
         // Convert to cents
         val amountValue = (amountStr.toDouble() * 100).toInt()
         CMoney(amountValue)
      } catch (e: NumberFormatException)
      {
         Toast.makeText(context, "Invalid amount", Toast.LENGTH_SHORT).show()
         return
      }

      // 2. Get Payment Type
      val selectedChipId = binding.paymentTypeChipGroup.checkedChipId
      val paymentType = when (selectedChipId)
      {
         R.id.chip_cash -> EPaymentMethod.PAYMENT_CASH
         R.id.chip_pin -> EPaymentMethod.PAYMENT_PIN
         else ->
         {
            Toast.makeText(context, "Please select a payment type", Toast.LENGTH_SHORT).show()
            return
         }
      }

      // 3. Create CPayment object and notify listener
      listener?.onPaymentEntered(paymentType, amount)

      // 4. Close the dialog
      dismiss()
   }

   override fun onDestroyView()
   {
      super.onDestroyView()
      _binding = null // Avoid memory leaks
   }
}
