package com.hha.modalDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ModalDialogYesNo : DialogFragment()
{
   // 1. Listener Interface: Activities will implement this to get the result.
   interface MessageBoxYesNoListener
   {
      fun onDialogPositiveClick(dialog: DialogFragment, requestCode: Int) // "Yes" was clicked
      fun onDialogNegativeClick(dialog: DialogFragment, requestCode: Int) // "No" was clicked
   }

   private lateinit var mListener: MessageBoxYesNoListener
   private var mRequestCode: Int = 0
   private var mTitle: String? = null
   private var mMessage: String? = null

   // This method is called by the system to attach the listener from the Activity.
   override fun onAttach(context: Context)
   {
      super.onAttach(context)
      // Verify that the host activity implements the callback interface.
      try
      {
         // Instantiate the listener so we can send events to the host.
         mListener = context as MessageBoxYesNoListener
      } catch (e: ClassCastException)
      {
         // The activity doesn't implement the interface, throw exception.
         throw ClassCastException(("$context must implement MessageBoxYesNoListener"))
      }
   }

   // 2. Main Dialog Creation Logic
   override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
   {
      // Retrieve arguments set by the newInstance method
      mTitle = arguments?.getString(ARG_TITLE)
      mMessage = arguments?.getString(ARG_MESSAGE)

      return activity?.let {
         val builder = AlertDialog.Builder(it)
         builder.setTitle(mTitle)
         builder.setMessage(mMessage)
            // Set the "Yes" button and its click listener
            .setPositiveButton("Yes") { _, _ ->
               mListener.onDialogPositiveClick(this, mRequestCode)
            }
            // Set the "No" button and its click listener
            .setNegativeButton("No") { _, _ ->
               mListener.onDialogNegativeClick(this, mRequestCode)
            }
         // Create the AlertDialog object and return it.
         builder.create()
      } ?: throw IllegalStateException("Activity cannot be null")
   }

   companion object
   {
      private const val ARG_TITLE = "title"
      private const val ARG_MESSAGE = "message"

      // 3. Factory Method: Use this to create and show the dialog
      fun newInstance(title: String, message: String, requestCode: Int): ModalDialogYesNo
      {
         val fragment = ModalDialogYesNo()
         val args = Bundle().apply {
            putString(ARG_TITLE, title)
            putString(ARG_MESSAGE, message)
         }
         fragment.arguments = args
         return fragment
      }
   }
}
