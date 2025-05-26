package com.hha.dialog

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import tech.hha.microfood.R

class ViewPopup(activity: Activity, text: String) {
    private var popupWindow: PopupWindow? = null

    init {
        // Inflate the popup view
        val popupView = activity.layoutInflater.inflate(R.layout.popup_text, null)

        // Create and configure the popup window
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            // Set the text
            popupView.findViewById<TextView>(R.id.text_popup).text = text

            // Show the popup centered
            showAtLocation(popupView, Gravity.CENTER, 0, 0)

            // Set up the OK button click listener
            popupView.findViewById<Button>(R.id.button_ok).setOnClickListener {
                dismiss()
                popupWindow = null
            }
        }
    }

    fun dismiss() {
        popupWindow?.dismiss()
        popupWindow = null
    }

    fun isShowing(): Boolean = popupWindow?.isShowing ?: false
}