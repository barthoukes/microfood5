package com.hha.modalDialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import tech.hha.microfood.databinding.ModalDialogQuantitiesBinding

/**
 * A dialog for selecting print quantities for the kitchen and the bill.
 * The quantity buttons (0-2) only update the state and do not close the dialog.
 * The action buttons at the bottom (Bill, Time, Print) close the dialog and
 * return the selected quantities and the action that was pressed.
 */
class ModalDialogQuantities : DialogFragment()
{
   // 1. Define the Listener Interface
   // This defines how the dialog sends its result back. It includes all the state.
   interface ModalDialogQuantitiesListener
   {
      fun handleFinishQuantiesAfterAskingQuantities(
         kitchenQuantity: Int, billQuantity: Int, action: Action
      )
   }

   // 2. Enum for the final action button pressed
   enum class Action
   {
      BILL, TIME, PRINT
   }

   // --- Class Properties ---

   private var _binding: ModalDialogQuantitiesBinding? = null
   private val binding get() = _binding!!

   // Internal state to hold the selected quantities
   private var mSelectedKitchenQuantity: Int = 0
   private var mSelectedBillQuantity: Int = 0

   // List of buttons for easier management
   private lateinit var mKitchenButtons: List<MaterialButton>
   private lateinit var mBillButtons: List<MaterialButton>

   private var mListener: ModalDialogQuantitiesListener? = null

   // --- Dialog Lifecycle Methods ---

   override fun onAttach(context: android.content.Context)
   {
      super.onAttach(context)
      // Set the listener from the hosting Activity or Fragment
      mListener = parentFragment as? ModalDialogQuantitiesListener ?: activity as? ModalDialogQuantitiesListener
      if (mListener == null)
      {
         throw ClassCastException("$context must implement ModalDialogQuantitiesListener")
      }
   }

   override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?,
   ): View
   {
      _binding = ModalDialogQuantitiesBinding.inflate(inflater, container, false)
      return binding.root
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?)
   {
      super.onViewCreated(view, savedInstanceState)

      val title = arguments?.getString("dialog_title")
      binding.quantityDialogTitle.text = title

      // Initialize button lists for easy state management
      mKitchenButtons = listOf(binding.buttonKitchen0, binding.buttonKitchen1, binding.buttonKitchen2)
      mBillButtons = listOf(binding.buttonBillQ0, binding.buttonBillQ1, binding.buttonBillQ2)

      // Set up click listeners for quantity buttons
      setupQuantityButtons()

      // Set up click listeners for the bottom action buttons
      setupActionButtons()

      // Set the initial visual state of the buttons
      updateButtonVisuals()
   }

   private fun setupQuantityButtons()
   {
      mKitchenButtons.forEachIndexed { index, button ->
         button.setOnClickListener {
            mSelectedKitchenQuantity = index
            updateButtonVisuals()
         }
      }

      mBillButtons.forEachIndexed { index, button ->
         button.setOnClickListener {
            mSelectedBillQuantity = index
            updateButtonVisuals()
         }
      }
   }

   private fun setupActionButtons()
   {
      binding.buttonBill.setOnClickListener {
         mListener?.handleFinishQuantiesAfterAskingQuantities(
            mSelectedKitchenQuantity, mSelectedBillQuantity, Action.BILL)
         dismiss()
      }
      binding.buttonTime.setOnClickListener {
         mListener?.handleFinishQuantiesAfterAskingQuantities(
            mSelectedKitchenQuantity, mSelectedBillQuantity, Action.TIME)
         dismiss()
      }
      binding.buttonPrint.setOnClickListener {
         mListener?.handleFinishQuantiesAfterAskingQuantities(
            mSelectedKitchenQuantity, mSelectedBillQuantity, Action.PRINT)
         dismiss()
      }
   }

   /**
    * Updates the visual state (e.g., color) of the buttons based on the current selection.
    */
   private fun updateButtonVisuals()
   {
      // Toggle the visual state for kitchen buttons
      mKitchenButtons.forEachIndexed { index, button ->
         button.isActivated = (index == mSelectedKitchenQuantity)
      }

      // Toggle the visual state for bill buttons
      mBillButtons.forEachIndexed { index, button ->
         button.isActivated = (index == mSelectedBillQuantity)
      }
   }

   // --- Boilerplate for DialogFragment ---

   override fun onDestroyView()
   {
      super.onDestroyView()
      _binding = null
   }

   // This makes the dialog have rounded corners from your drawable
   override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
   {
      val dialog = super.onCreateDialog(savedInstanceState)
      dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
      return dialog
   }

   companion object
   {
      const val TAG = "ModalDialogQuantities"

      // Optional: A newInstance() method if you need to pass initial values
      /**
       * Creates a new instance of ModalDialogQuantities with a specified title.
       * @param title The text to be displayed as the dialog's title.
       * @return A new instance of ModalDialogQuantities.
       */
      fun newInstance(title: String): ModalDialogQuantities
      {
         val args = Bundle()
         args.putString("dialog_title", title)
         val fragment = ModalDialogQuantities()
         fragment.arguments = args
         return fragment
      }
   }
}
