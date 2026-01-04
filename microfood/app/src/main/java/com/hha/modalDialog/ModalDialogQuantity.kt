package com.hha.modalDialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import tech.hha.microfood.databinding.ModalDialogQuantityBinding

class ModalDialogQuantity : DialogFragment()
{
      // 1. LISTENER INTERFACE
      // This interface defines how the dialog sends the result back.
      interface ModalDialogQuantityListener
      {
         fun onQuantitySelected(quantity: Int, toBilling: Boolean, stop: Boolean)
      }

      private val tag = "ModalDialogQuantity"
      private var _binding: ModalDialogQuantityBinding? = null
      private val binding get() = _binding!!

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

      override fun onCreateView(
         inflater: LayoutInflater,
         container: ViewGroup?,
         savedInstanceState: Bundle?,
      ): View
      {
         Log.i(tag, "onCreateView")
         _binding = ModalDialogQuantityBinding.inflate(inflater, container, false)
         return binding.root
      }

   /**
    * This method is called after the dialog is created and is the best place
    * to set the size and position of the dialog's window.
    */
   override fun onStart() {
      super.onStart()

      // Get the dialog's window
      dialog?.window?.let { window ->
         // Get the screen's width
         val displayMetrics = resources.displayMetrics
         val width = displayMetrics.widthPixels

         // Set the dialog's width to 50% of the screen width
         // You can adjust the percentage (0.5) as needed
         val dialogWidth = (width * 0.50).toInt()

         // Apply the new layout parameters
         window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT)
      }
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?)
      {
         Log.i(tag, "onViewCreated")
         super.onViewCreated(view, savedInstanceState)

         // Set the title
         binding.quantityDialogTitle.text = dialogTitle ?: "Select Quantity"

         // Set up click listeners for all the buttons
         binding.buttonQuantity0.setOnClickListener {
            Log.i(tag, "buttonQuantity0")
            (activity as? ModalDialogQuantityListener)?.onQuantitySelected(0, false, false)
            dismiss()
         }

         binding.buttonQuantity1.setOnClickListener {
            Log.i(tag, "buttonQuantity1")
            (activity as? ModalDialogQuantityListener)?.onQuantitySelected(1, false, false)
            dismiss()
         }

         binding.buttonQuantity2.setOnClickListener {
            Log.i(tag, "buttonQuantity2")
            (activity as? ModalDialogQuantityListener)?.onQuantitySelected(2, false, false)
            dismiss()
         }

         binding.buttonBill.setOnClickListener {
            Log.i(tag, "buttonBill")
            (activity as? ModalDialogQuantityListener)?.onQuantitySelected(1, true, false)
            // Return"/Enter/OK
            dismiss()
         }

         binding.buttonEscape.setOnClickListener {
            Log.i(tag, "buttonEscape")
            (activity as? ModalDialogQuantityListener)?.onQuantitySelected(0, false, true)
            dismiss()
         }
      }

      override fun onDestroyView()
      {
         Log.i(tag, "onDestroyView")
         super.onDestroyView()
         _binding = null
      }
}