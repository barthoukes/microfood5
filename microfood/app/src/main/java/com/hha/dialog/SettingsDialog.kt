package com.hha.dialog

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.TextView
import com.hha.framework.CConfiguration
import com.hha.exceptions.ConfigNotFoundException
import tech.hha.microfood.R

class SettingsDialog : Activity() {
    private lateinit var mSettingsNaam: TextView
    private lateinit var mSettingsIP: TextView
    private lateinit var mSettingsHandHeld: TextView
    private lateinit var mSettingsPassword: TextView
    private lateinit var mSettingsFontFood: TextView
    private lateinit var mSettingsFontText: TextView
    private lateinit var mSettingsFontSplash: TextView
    private lateinit var mSettingsFontButton: TextView
    private lateinit var mSettingsDutch: CheckBox
    private lateinit var mSettingsChinese: CheckBox
    private lateinit var mSettingsDemo: CheckBox
    private val mConfig: CConfiguration = CConfiguration.getInstance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_layout)

        // Initialize views
        mSettingsNaam = findViewById(R.id.settingsEditName)
        mSettingsIP = findViewById(R.id.settingsEditIP)
        mSettingsHandHeld = findViewById(R.id.settingsEditSocket)
        mSettingsPassword = findViewById(R.id.settingsEditPassword)
        mSettingsFontFood = findViewById(R.id.settingsFontItem)
        mSettingsFontText = findViewById(R.id.settingsFontText)
        mSettingsDutch = findViewById(R.id.settingsDutch)
        mSettingsChinese = findViewById(R.id.settingsChinese)
        mSettingsFontSplash = findViewById(R.id.settingsFontSplash)
        mSettingsFontButton = findViewById(R.id.settingsFontButton)
        mSettingsDemo = findViewById(R.id.settingsDemo)

        setButtons()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setButtons() {
        try {
            mSettingsNaam.text = mConfig.getString("name")
            mSettingsIP.text = mConfig.getString("IP")
            mSettingsHandHeld.text = mConfig.getValue("handheld_id").toString()
            mSettingsPassword.text = mConfig.getString("password")
            mSettingsFontFood.text = mConfig.getValue("font_item").toString()
            mSettingsFontText.text = mConfig.getValue("font_text").toString()
            mSettingsDutch.isChecked = mConfig.getValue("dutch") == 1
            mSettingsChinese.isChecked = mConfig.getValue("chinese") == 1
            mSettingsDemo.isChecked = mConfig.getValue("demo") == 1
            mSettingsFontSplash.text = mConfig.getValue("font_splash").toString()
            mSettingsFontButton.text = mConfig.getValue("font_button").toString()
        } catch (e: ConfigNotFoundException) {
            e.printStackTrace()
        }
    }

    fun button_settings_cancel(view: View) {
        finish()
    }

    fun button_settings_ok(view: View) {
        try {
            mConfig.setString("name", mSettingsNaam.text.toString())
            mConfig.setString("IP", mSettingsIP.text.toString())
            mConfig.setString("handheld_id", mSettingsHandHeld.text.toString())

            if (mConfig.getValue("handheld_id") <= 0 || mConfig.getValue("handheld_id") > 10) {
                mConfig.setValue("handheld_id", 1)
            }

            mConfig.setString("password", mSettingsPassword.text.toString())
            mConfig.setString("font_item", mSettingsFontFood.text.toString())

            if (mConfig.getValue("font_item") < 10 || mConfig.getValue("font_item") > 40) {
                mConfig.setValue("font_item", 15)
            }

            mConfig.setString("font_text", mSettingsFontText.text.toString())

            if (mConfig.getValue("font_text") < 10 || mConfig.getValue("font_text") > 40) {
                mConfig.setValue("font_text", 15)
            }

            mConfig.setValue("dutch", if (mSettingsDutch.isChecked) 1 else 0)
            mConfig.setValue("chinese", if (mSettingsChinese.isChecked) 1 else 0)
            mConfig.setString("font_splash", mSettingsFontSplash.text.toString())
            mConfig.setString("font_button", mSettingsFontButton.text.toString())
            mConfig.setValue("demo", if (mSettingsDemo.isChecked) 1 else 0)
           // mConfig.Save(mConfig.fileName)

            // Send update...
            // val msg = Cmessage.getInstance()
        } catch (e: ConfigNotFoundException) {
            e.printStackTrace()
        }
        finish()
    }
}