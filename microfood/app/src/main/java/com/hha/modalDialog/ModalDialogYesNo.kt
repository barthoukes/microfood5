package com.hha.modalDialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton // <-- Import ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import tech.hha.microfood.R

class ModalDialogYesNo : DialogFragment() {

   // Listener Interface (no change)
   interface MessageBoxYesNoListener {
      fun onDialogPositiveClick(dialog: DialogFragment, requestCode: Int)
      fun onDialogNegativeClick(dialog: DialogFragment, requestCode: Int)
   }

   private lateinit var mListener: MessageBoxYesNoListener
   private var mRequestCode: Int = 0

   // onAttach (no change)
   override fun onAttach(context: Context) {
      super.onAttach(context)
      try {
         mListener = context as MessageBoxYesNoListener
      } catch (e: ClassCastException) {
         throw ClassCastException(("$context must implement MessageBoxYesNoListener"))
      }
   }

   // --- Main Logic: Inflate your existing XML ---
   override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
   ): View {
      // 1. Inflate your specific XML file
      val view = inflater.inflate(R.layout.popup_cancel_ok, container, false)

      // 2. Get arguments from the bundle
      val title = arguments?.getString(ARG_TITLE)
      val message = arguments?.getString(ARG_MESSAGE) // Your layout has no message field, but we can set the title
      mRequestCode = arguments?.getInt(ARG_REQUEST_CODE) ?: 0

      // 3. Find views by their IDs from popup_cancel_ok.xml
      val titleTextView: TextView = view.findViewById(R.id.text_cancel)
      val okButton: Button = view.findViewById(R.id.button_ok)
      val cancelButton: ImageButton = view.findViewById(R.id.button_cancel)

      // 4. Set the content
      // Your layout has a static text "Cancel the order ?", so we'll use the passed 'title'.
      // If the title is not null or empty, use it. Otherwise, the default from the XML will be shown.
      if (!title.isNullOrEmpty()) {
         titleTextView.text = title
      }

      // Your layout doesn't have a separate message field, so the `message` argument will be ignored.
      // The title serves as the main text.

      // 5. Set click listeners
      okButton.setOnClickListener {
         // "OK" is the positive action
         mListener.onDialogPositiveClick(this, mRequestCode)
         dismiss() // Close the dialog
      }

      cancelButton.setOnClickListener {
         // "Cancel" is the negative action
         mListener.onDialogNegativeClick(this, mRequestCode)
         dismiss() // Close the dialog
      }

      return view
   }

   // Optional: To ensure the dialog background respects your layout's corners/style
   override fun onStart() {
      super.onStart()
      dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
   }

   // Companion object factory method (no change)
   companion object {
      private const val ARG_TITLE = "title"
      private const val ARG_MESSAGE = "message"
      private const val ARG_REQUEST_CODE = "requestCode"

      fun newInstance(title: String, message: String, requestCode: Int): ModalDialogYesNo {
         val fragment = ModalDialogYesNo()
         val args = Bundle().apply {
            putString(ARG_TITLE, title)
            putString(ARG_MESSAGE, message)
            putInt(ARG_REQUEST_CODE, requestCode)
         }
         fragment.arguments = args
         return fragment
      }
   }
}
