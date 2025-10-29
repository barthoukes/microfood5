package com.hha.messagebox

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import tech.hha.microfood.databinding.PopupTextInputBinding

class MessageBoxTextInput: DialogFragment()
{
   private lateinit var binding: PopupTextInputBinding
   private lateinit var inputText: EditText

   interface OnTextEnteredListener
   {
      fun onTextEntered(text: String)
   }

   var listener: OnTextEnteredListener? = null

   override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?
   ): View
   {
      binding = PopupTextInputBinding.inflate(inflater, container, false)
      inputText = binding.inputText

      // REMOVED: setupKeyboard() is no longer needed

      setupActionButtons()
      setupInputText()

      return binding.root
   }

   override fun onStart()
   {
      super.onStart()
      dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
   }

   override fun onResume()
   {
      super.onResume()
      // Automatically focus the EditText and show the keyboard
      inputText.requestFocus()
      dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
   }

   private fun setupInputText()
   {
      // Optional: Allow the "Enter" key on the keyboard to submit the dialog
      inputText.setOnEditorActionListener { _, actionId, _ ->
         if (actionId == EditorInfo.IME_ACTION_DONE)
         {
            submitText()
            return@setOnEditorActionListener true
         }
         false
      }
   }

   private fun setupActionButtons()
   {
      binding.btnCancel.setOnClickListener {
         dismiss() // Close the dialog
      }

      binding.btnEnter.setOnClickListener {
         submitText()
      }
   }

   private fun submitText()
   {
      listener?.onTextEntered(inputText.text.toString())
      dismiss() // Close the dialog
   }

   // REMOVED: setupKeyboard() and onKeyClick() are no longer needed
}
