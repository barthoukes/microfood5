package com.hha.modalDialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.hha.types.EFinalizerAction
import com.hha.types.EModalDialogQuantity
import tech.hha.microfood.databinding.ModalDialogQuantityBinding

class ModalDialogQuantity : DialogFragment()
{
      // 1. LISTENER INTERFACE
      // This interface defines how the dialog sends the result back.
      interface ModalDialogQuantityListener
      {
         fun onQuantitySelected(action: EModalDialogQuantity)
      }

      private var _binding: ModalDialogQuantityBinding? = null
      private val binding get() = _binding!!

      // The listener instance. The calling activity will set this.
      private var listener: ModalDialogQuantityListener? = null

      // A simple way to pass the title to the dialog
      private var dialogTitle: String? = null

      // Static method to create a new instance of the dialog
      companion object
      {
         fun newInstance(title: String): ModalDialogQuantity
         {
            val args = Bundle()
            args.putString("dialog_title", title)
            val fragment = ModalDialogQuantity()
            fragment.arguments = args
            return fragment
         }
      }

      override fun onCreate(savedInstanceState: Bundle?)
      {
         super.onCreate(savedInstanceState)
         dialogTitle = arguments?.getString("dialog_title")
         // Try to get the listener from the parent fragment or activity
         listener = parentFragment as? ModalDialogQuantityListener ?: activity as? ModalDialogQuantityListener
      }

      override fun onCreateView(
         inflater: LayoutInflater,
         container: ViewGroup?,
         savedInstanceState: Bundle?,
      ): View
      {
         _binding = ModalDialogQuantityBinding.inflate(inflater, container, false)
         return binding.root
      }

      override fun onViewCreated(view: View, savedInstanceState: Bundle?)
      {
         super.onViewCreated(view, savedInstanceState)

         // Set the title
         binding.quantityDialogTitle.text = dialogTitle ?: "Select Quantity"

         // Set up click listeners for all the buttons
         binding.buttonQuantity0.setOnClickListener {
            listener?.onQuantitySelected(EModalDialogQuantity.FINALIZE_KEY_0)
            dismiss()
         }

         binding.buttonQuantity1.setOnClickListener {
            listener?.onQuantitySelected(EModalDialogQuantity.FINALIZE_KEY_1)
            dismiss()
         }

         binding.buttonQuantity2.setOnClickListener {
            listener?.onQuantitySelected(EModalDialogQuantity.FINALIZE_KEY_2)
            dismiss()
         }

         binding.buttonBill.setOnClickListener {
            listener?.onQuantitySelected(EModalDialogQuantity.FINALIZE_KEY_BILL) // Using CR for "Carriage Return"/Enter/OK
            dismiss()
         }

         binding.buttonEscape.setOnClickListener {
            listener?.onQuantitySelected(EModalDialogQuantity.FINALIZE_KEY_ESCAPE)
            dismiss()
         }
      }

      // This is called to make the dialog have rounded corners and not be full screen
      override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
      {
         val dialog = super.onCreateDialog(savedInstanceState)
         dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
         return dialog
      }

      override fun onDestroyView()
      {
         super.onDestroyView()
         _binding = null
      }
}