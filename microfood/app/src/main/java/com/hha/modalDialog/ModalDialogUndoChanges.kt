package com.hha.modalDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ModalDialogUndoChanges : DialogFragment()
{

   // 1. Listener Interface: Activities will implement this to get the result.
   interface MessageBoxUndoChangesListener
   {
      fun onDialogUndoChanges(dialog: DialogFragment) // "Yes" was clicked
      fun onDialogContinueOrder(dialog: DialogFragment) // "No" was clicked
   }

   private lateinit var listener: MessageBoxUndoChangesListener
   private var title: String? = null
   private var message: String? = null

   // This method is called by the system to attach the listener from the Activity.
   override fun onAttach(context: Context)
   {
      super.onAttach(context)
      // Verify that the host activity implements the callback interface.
      try
      {
         // Instantiate the listener so we can send events to the host.
         listener = context as MessageBoxUndoChangesListener
      } catch (e: ClassCastException)
      {
         // The activity doesn't implement the interface, throw exception.
         throw ClassCastException(("$context must implement MessageBoxUndoChangesListener"))
      }
   }

   // 2. Main Dialog Creation Logic
   override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
   {
      // Retrieve arguments set by the newInstance method
      title = arguments?.getString(ARG_TITLE)
      message = arguments?.getString(ARG_MESSAGE)

      return activity?.let {
         val builder = AlertDialog.Builder(it)
         builder.setTitle(title)
         builder.setMessage(message)
            // Set the "Yes" button and its click listener
            .setPositiveButton("Yes") { _, _ ->
               listener.onDialogUndoChanges(this)
            }
            // Set the "No" button and its click listener
            .setNegativeButton("No") { _, _ ->
               listener.onDialogContinueOrder(this)
            }
         // Create the AlertDialog object and return it.
         builder.create()
      } ?: throw IllegalStateException("Activity cannot be null")
   }

   companion object
   {
      private const val ARG_TITLE = "Stop order"
      private const val ARG_MESSAGE = "message456"

      // 3. Factory Method: Use this to create and show the dialog
      fun newInstance(title: String, message: String): ModalDialogYesNo
      {
         val fragment = ModalDialogYesNo()
         val args = Bundle().apply {
            putString(ARG_TITLE, title)
            //putString(ARG_MESSAGE, message)
         }
         fragment.arguments = args
         return fragment
      }
   }
}
