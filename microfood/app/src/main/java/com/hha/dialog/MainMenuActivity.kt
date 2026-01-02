package com.hha.dialog

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import tech.hha.microfood.R
import tech.hha.microfood.databinding.DialogMainActivityBinding

import com.hha.dialog.Translation
import com.hha.dialog.Translation.TextId
import com.hha.activities.AboutActivity
import com.hha.framework.CMenuCards
import com.hha.framework.COpenClientsHandler.createNewTakeawayTransaction
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import com.hha.service.AddressService


class MainMenuActivity : BaseActivity() {
    // View binding
    private lateinit var binding: DialogMainActivityBinding

    // Connection status colors
    private val connectionColors = listOf(
        0xFF004400, 0xFF00CC00, 0xFF005500, 0xFF00DD00,
        0xFF006600, 0xFF00EE00, 0xFF007700, 0xFF00FF00
    )
    private var connectionColorIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogMainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActivity()
        setupClickListeners()
        setupObservers()
        startConnectionUpdates()
        updateTexts()
    }

    private fun setupActivity() {
        // Set window properties if needed
        window.setBackgroundDrawableResource(R.drawable.backrepeat)
    }

    private fun setupClickListeners() {
        // Table overview button
        binding.tafelOverzicht.apply {
            setOnClickListener { navigateToTableOverview() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.sitin_yellow)
        }

        // Takeaway button
        binding.buttonTakeaway.apply {
            setOnClickListener { navigateToTakeaway() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.bag_yellow)
        }

        // Settings button
        binding.buttonSettings.apply {
            setOnClickListener { navigateToSettings() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.settings_yellow)
        }

        // Update button
        binding.buttonUpdate.apply {
            setOnClickListener { updateMenuCard() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.menu_yellow)
        }

        // Calculator button
        binding.buttonCalculator.apply {
            setOnClickListener { openCalculator() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.icon_calculator_yellow)
        }

        // Language button
        binding.buttonChangeLanguage.apply {
            setOnClickListener { changeLanguage() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.languages_yellow)
        }

        // Stop button
        binding.buttonStop.apply {
            setOnClickListener { confirmExit() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.shutdown_yellow)
        }

        // About button
        binding.aboutUs.apply {
            setOnClickListener { showAbout() }
            setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.info_icon_yellow)
        }
    }

    private fun setupObservers() {
//        viewModel.restaurantName.observe(this) { name ->
//            binding.textInfo.text = name
//        }
//
//        viewModel.connectionStatus.observe(this) { isConnected ->
//            updateConnectionStatus(isConnected)
//        }
//
//        viewModel.bufferStatus.observe(this) { level ->
//            updateBufferStatus(level)
//        }
    }

    private fun startConnectionUpdates() {
        lifecycleScope.launch {
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
        val addressService : AddressService = GrpcServiceFactory.createAddressService()
        val response = addressService.getAllAddressLines()
        Log.i("FirstFragment", "Response: $response")

        // You could also update UI here with the response
        startActivity(Intent(this, AskTransactionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })

        //startActivity(Intent(this, TableOverviewActivity::class.java))
    }

    private fun navigateToTakeaway()
    {
        // Todo new takeaway
        val useBag = true
        val user = Global.getInstance().rfidKeyId
        val transactionId : Int =
            createNewTakeawayTransaction(
                0, useBag, user, false);

        if (transactionId <= 0) {
            return
        }
        CMenuCards.getInstance().loadTakeaway()
        navigateToPageOrderActivity(transactionId)
    }

    private fun navigateToPageOrderActivity(transactionId: Int)
    {
        val intent = Intent(this, PageOrderActivity::class.java)
        intent.putExtra("TRANSACTION_ID", transactionId)
        startActivity(intent.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
       // finish()
    }

    private fun navigateToSettings() {
        //startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun updateMenuCard() {
        lifecycleScope.launch {
            // @todo create a class that retrieves the menu card every minute.
            //viewModel.updateMenuCard()
            showToast(R.string.menu_update_started)
        }
    }

    private fun openCalculator() {
        try {
            startActivity(packageManager.getLaunchIntentForPackage("com.android.calculator2"))
        } catch (e: Exception) {
            //showToast(R.string.calculator_not_found)
        }
    }

    private fun changeLanguage() {
        //viewModel.rotateLanguage()
        Translation.nextLanguage()
        updateTexts()
    }

    private fun confirmExit() {
        finish()
//        MaterialAlertDialogBuilder(this)
//            .setTitle(R.string.confirm_exit_title)
//            .setMessage(R.string.confirm_exit_message)
//            .setPositiveButton(R.string.exit) { _, _ ->
//                finish()
//            }
//            .setNegativeButton(R.string.cancel, null)
//            .show()
    }

    private fun showAbout() {
        startActivity(Intent(this, AboutActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
//        try {
//            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://www.houkes-horeca-applications.nl")))
//        } catch (e: ActivityNotFoundException) {
//            //showToast(R.string.no_browser_found)
//        }
    }

    private fun updateTexts() {
        binding.tafelOverzicht.text = Translation.get(TextId.TEXT_TABLE_OVERVIEW)
        binding.buttonTakeaway.text = Translation.get(TextId.TEXT_TAKEAWAY)
        binding.buttonSettings.text = Translation.get(TextId.TEXT_SETTINGS)
        binding.buttonUpdate.text = Translation.get(TextId.TEXT_UPDATE)
        binding.buttonStop.text = Translation.get(TextId.TEXT_STOP)
        binding.aboutUs.text = Translation.get(TextId.TEXT_ABOUT_US)
        binding.buttonCalculator.text = Translation.get(TextId.TEXT_CALCULATOR)
    }

    private fun showToast(@StringRes messageRes: Int) {
        Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show()
    }

//    companion object {
//        fun start(context: Context) {
//            context.startActivity(Intent(context, MainMenuActivity::class.java))
//        }
    //}
}