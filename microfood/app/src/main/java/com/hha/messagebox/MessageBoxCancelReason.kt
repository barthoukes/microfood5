package com.hha.messagebox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
//import androidx.paging.map
import com.hha.adapter.CancelReasonAdapter
import com.hha.dialog.Translation
import com.hha.dialog.Translation.TextId
import tech.hha.microfood.databinding.DialogCancelReasonBinding

class MessageBoxCancelReason : DialogFragment()
{

   private var _binding: DialogCancelReasonBinding? = null
   private val binding get() = _binding!!
   private final val reasons = arrayOf(TextId.TEXT_CANCEL_REASON_TRAINING,
      TextId.TEXT_CANCEL_REASON_PERSONNEL,
      TextId.TEXT_CANCEL_REASON_RUN_AWAY,
      TextId.TEXT_CANCEL_REASON_BAD_FOOD,
      TextId.TEXT_CANCEL_REASON_NO_MONEY,
      TextId.TEXT_CANCEL_REASON_WRONG_DISH,
      TextId.TEXT_CANCEL_REASON_NO_STOCK,
      TextId.TEXT_CANCEL_REASON_TECHNICAL,
      TextId.TEXT_CANCEL_REASON_TELEPHONE,
      TextId.TEXT_CANCEL_REASON_TOO_LATE)

   // Define a listener interface to send the result back to the Activity
   interface MessageBoxCancelReasonListener
   {
      fun onReasonSelected(reason: String)
      fun onDialogCancelled()
   }

   override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?,
   ): View
   {
      _binding = DialogCancelReasonBinding.inflate(inflater, container, false)
      return binding.root
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?)
   {
      super.onViewCreated(view, savedInstanceState)

      binding.dialogTitle.text = Translation.get(TextId.TEXT_CANCEL_REASON)

      isCancelable = false // Prevent closing by tapping outside

      // --- Fetch Data From Database ---
      // I'm assuming you have a class like CWhy to get the reasons
      val reasonStrings: List<String> = reasons.map { textId -> Translation.get(textId) }

      // --- Setup RecyclerView ---
      val adapter = CancelReasonAdapter(reasonStrings) { selectedReason ->
         // A reason was clicked, send it back to the activity and dismiss
         (activity as? MessageBoxCancelReasonListener)?.onReasonSelected(selectedReason)
         dismiss()
      }
      binding.reasonsRecyclerview.adapter = adapter

      // --- Setup Cancel Button ---
         binding.buttonCancel.setOnClickListener {
            // The cancel button was clicked
            (activity as? MessageBoxCancelReasonListener)?.onDialogCancelled()
            dismiss()
         }.toString()
   }

   override fun onDestroyView()
   {
      super.onDestroyView()
      _binding = null
   }
}
