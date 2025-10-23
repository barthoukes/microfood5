package com.hha.dialog

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.TextView
import com.hha.exceptions.ConfigNotFoundException
import tech.hha.microfood.R
import com.hha.resources.Global

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
    val CFG = Global.getInstance().CFG

    override fun onCreate(savedInstanceState: Bundle?)
    {
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

    private fun setButtons()
    {
        try
        {
            mSettingsNaam.text = CFG.getString("name")
            mSettingsIP.text = CFG.getString("IP")
            mSettingsHandHeld.text = CFG.getString("handheld_id")
            mSettingsPassword.text = CFG.getString("password")
            mSettingsFontFood.text = CFG.getString("font_item")
            mSettingsFontText.text = CFG.getString("font_text")
            mSettingsDutch.isChecked = CFG.getOption("dutch")
            mSettingsChinese.isChecked = CFG.getOption("chinese")
            mSettingsDemo.isChecked = CFG.getOption("demo")
            mSettingsFontSplash.text = CFG.getString("font_splash")
            mSettingsFontButton.text = CFG.getString("font_button")
        } catch (e: ConfigNotFoundException)
        {
            e.printStackTrace()
        }
    }

    fun button_settings_cancel(view: View)
    {
        finish()
    }

    fun button_settings_ok(view: View)
    {
        try
        {
            CFG.setString("name", mSettingsNaam.text.toString())
            CFG.setString("IP", mSettingsIP.text.toString())
            CFG.setString("handheld_id", mSettingsHandHeld.text.toString())

            if (CFG.getValue("handheld_id") <= 0 || CFG.getValue("handheld_id") > 10)
            {
                CFG.setValue("handheld_id", 1)
            }

            CFG.setString("password", mSettingsPassword.text.toString())
            CFG.setString("font_item", mSettingsFontFood.text.toString())

            if (CFG.getValue("font_item") < 10 || CFG.getValue("font_item") > 40)
            {
                CFG.setValue("font_item", 15)
            }

            CFG.setString("font_text", mSettingsFontText.text.toString())

            if (CFG.getValue("font_text") < 10 || CFG.getValue("font_text") > 40)
            {
                CFG.setValue("font_text", 15)
            }

            CFG.setValue("dutch", if (mSettingsDutch.isChecked) 1 else 0)
            CFG.setValue("chinese", if (mSettingsChinese.isChecked) 1 else 0)
            CFG.setString("font_splash", mSettingsFontSplash.text.toString())
            CFG.setString("font_button", mSettingsFontButton.text.toString())
            CFG.setValue("demo", if (mSettingsDemo.isChecked) 1 else 0)
            // mConfig.Save(mConfig.fileName)

            // Send update...
            // val msg = Cmessage.getInstance()
        } catch (e: ConfigNotFoundException)
        {
            e.printStackTrace()
        }
        finish()
    }
}