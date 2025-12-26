package com.hha.modalDialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import tech.hha.microfood.databinding.ModalDialogDelayBinding
import java.util.Locale

/**
 * A dialog for adjusting a time delay in 10-minute increments.
 *
 * The dialog allows increasing or decreasing a delay value, which starts at an initial
 * value provided upon creation. The dialog closes when the user presses "OK" or "Stop",
 * returning the final delay value and the action taken.
 */
class ModalDialogDelay : DialogFragment() {

   // 1. Listener interface to communicate the result back
   interface ModalDialogDelayListener {
      fun onDelayFinalized(action: Action, finalDelay: Int)
   }

   // 2. Enum to define the possible closing actions
   enum class Action {
      OK, STOP
   }

   // --- Class Properties ---

   private var _binding: ModalDialogDelayBinding? = null
   private val binding get() = _binding!!

   private var listener: ModalDialogDelayListener? = null

   // Internal state to hold the current delay value in minutes
   private var currentDelay: Int = 0

   // --- Dialog Lifecycle and Setup ---

   override fun onAttach(context: Context) {
      super.onAttach(context)
      // Attach the listener from the host (Activity or parent Fragment)
      listener = parentFragment as? ModalDialogDelayListener ?: activity as? ModalDialogDelayListener
      if (listener == null) {
         throw ClassCastException("$context must implement ModalDialogDelayListener")
      }
   }

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      // Retrieve the initial delay value from the arguments bundle
      currentDelay = arguments?.getInt(ARG_INITIAL_DELAY) ?: 0
   }

   override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?
   ): View {
      _binding = ModalDialogDelayBinding.inflate(inflater, container, false)
      return binding.root
   }

   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
      setupClickListeners()
      updateDelayDisplay()
   }

   private fun setupClickListeners() {
      // --- Delay Adjustment Buttons ---
      binding.buttonPlus10.setOnClickListener {
         currentDelay += 10
         updateDelayDisplay()
      }

      binding.buttonMinus10.setOnClickListener {
         currentDelay -= 10
         updateDelayDisplay()
      }

      // --- Final Action Buttons ---
      binding.buttonStop.setOnClickListener {
         // "Stop" is interpreted as setting the delay to 0
         listener?.onDelayFinalized(Action.STOP, 0)
         dismiss()
      }

      binding.buttonOk.setOnClickListener {
         listener?.onDelayFinalized(Action.OK, currentDelay)
         dismiss()
      }
   }

   /**
    * Updates the central TextView to display the current delay value.
    * Formats the text to show "+X min", "Y min", or "0 min".
    */
   private fun updateDelayDisplay() {
      val sign = if (currentDelay > 0) "+" else ""
      binding.delayTimeDisplay.text = String.format(Locale.getDefault(), "%s%d min", sign, currentDelay)
   }

   // --- Boilerplate for DialogFragment ---

   override fun onDestroyView() {
      super.onDestroyView()
      _binding = null // Avoid memory leaks
   }

   // This applies your custom rounded corners background
   override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
   {
      val dialog = super.onCreateDialog(savedInstanceState)
      dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
      return dialog
   }

   // 3. Companion object with a newInstance pattern for safe argument passing
   companion object
   {
      const val TAG = "ModalDialogDelay"
      private const val ARG_INITIAL_DELAY = "initial_delay"

      /**
       * Creates a new instance of the ModalDialogDelay.
       * @param initialDelay The starting delay value, in minutes.
       * @return A new, configured instance of the dialog.
       */
      fun newInstance(initialDelay: Int): ModalDialogDelay
      {
         val args = Bundle()
         args.putInt(ARG_INITIAL_DELAY, initialDelay)
         val fragment = ModalDialogDelay()
         fragment.arguments = args
         return fragment
      }
   }
}
