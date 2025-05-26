import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tech.hha.microfood.R
import tech.hha.microfood.databinding.DialogMainMenuBinding

class MainMenuDialog : DialogFragment() {
    // View binding
    private var _binding: DialogMainMenuBinding? = null
    private val binding get() = _binding!!

    // Connection status colors
    private val connectionColors = listOf(
        0xFF004400, 0xFF00CC00, 0xFF005500, 0xFF00DD00,
        0xFF006600, 0xFF00EE00, 0xFF007700, 0xFF00FF00
    )
    private var connectionColorIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMainMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDialog()
        setupClickListeners()
        setupObservers()
        startConnectionUpdates()
    }

    private fun setupDialog() {
        // Set dialog window properties
        dialog?.window?.apply {
            setBackgroundDrawableResource(R.drawable.backrepeat)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    private fun setupClickListeners() {
        // Table overview button
        binding.tafelOverzicht.apply {
            setOnClickListener { navigateToTableOverview() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.sitin)
        }

        // Takeaway button
        binding.buttonTakeaway.apply {
            setOnClickListener { navigateToTakeaway() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.bag)
        }

        // Settings button
        binding.buttonSettings.apply {
            setOnClickListener { navigateToSettings() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.settings48)
        }

        // Update button
        binding.buttonUpdate.apply {
            setOnClickListener { updateMenuCard() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.menu)
        }

        // Calculator button
        binding.buttonCalculator.apply {
            setOnClickListener { openCalculator() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.icon_calculator)
        }

        // Language button
        binding.buttonChangeLanguage.apply {
            setOnClickListener { changeLanguage() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.languages)
        }

        // Stop button
        binding.buttonStop.apply {
            setOnClickListener { confirmExit() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.shutdown)
        }

        // About button
        binding.aboutUs.apply {
            setOnClickListener { showAbout() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.info48)
        }
    }

    private fun setupObservers() {
//        viewModel.restaurantName.observe(viewLifecycleOwner) { name ->
//            binding.textInfo.text = name
//        }
//
//        viewModel.connectionStatus.observe(viewLifecycleOwner) { isConnected ->
//            updateConnectionStatus(isConnected)
//        }
//
//        viewModel.bufferStatus.observe(viewLifecycleOwner) { level ->
//            updateBufferStatus(level)
//        }
    }

    private fun startConnectionUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            while (true) {
                //viewModel.checkConnectionStatus()
                delay(10_000) // Update every 10 seconds
            }
        }
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        binding.connectionColour.setBackgroundColor(
            if (isConnected) {
                connectionColorIndex = (connectionColorIndex + 1) % connectionColors.size
                connectionColors[connectionColorIndex].toInt()
            } else {
                Color.RED
            }
        )
    }

    private fun updateBufferStatus(level: Int) {
        val r = level * 8
        val g = 192 - level * 6
        val b = 32
        binding.bufferColour.setBackgroundColor(Color.rgb(r, g, b))
    }

    // Navigation functions
    private fun navigateToTableOverview() {
        //findNavController().navigate(R.id.action_to_table_overview)
    }

    private fun navigateToTakeaway() {
        //findNavController().navigate(R.id.action_to_takeaway)
    }

    private fun navigateToSettings() {
        //findNavController().navigate(R.id.action_to_settings)
    }

    private fun updateMenuCard() {
        viewLifecycleOwner.lifecycleScope.launch {
        // @todo create a class that retrieves the menu card every minute.
        //viewModel.updateMenuCard()
            showToast(R.string.menu_update_started)
        }
    }

    private fun openCalculator() {
        try {
            startActivity(requireContext().packageManager.getLaunchIntentForPackage("com.android.calculator2"))
        } catch (e: Exception) {
            //showToast(R.string.calculator_not_found)
        }
    }

    private fun changeLanguage() {
        //viewModel.rotateLanguage()
        updateTexts()
    }

    private fun confirmExit() {
//        MaterialAlertDialogBuilder(requireContext())
//            .setTitle(R.string.confirm_exit_title)
//            .setMessage(R.string.confirm_exit_message)
//            .setPositiveButton(R.string.exit) { _, _ ->
//                requireActivity().finish()
//            }
//            .setNegativeButton(R.string.cancel, null)
//            .show()
    }

    private fun showAbout() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://www.houkes-horeca-applications.nl")))
        } catch (e: ActivityNotFoundException) {
            //showToast(R.string.no_browser_found)
        }
    }

    private fun updateTexts() {
        binding.tafelOverzicht.text = getString(R.string.table_overview)
        binding.buttonTakeaway.text = getString(R.string.takeaway)
        binding.buttonSettings.text = getString(R.string.configuration)
        binding.buttonUpdate.text = getString(R.string.update_name_card)
        binding.buttonStop.text = getString(R.string.stop)
        binding.aboutUs.text = getString(R.string.about_us)
    }

    private fun showToast(@StringRes messageRes: Int) {
        Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = MainMenuDialog()
    }
}
