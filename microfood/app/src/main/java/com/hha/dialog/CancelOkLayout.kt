package com.hha.dialog

import android.app.Activity
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import tech.hha.microfood.R

class CancelOkLayout(
    private val activity: Activity,
    private val handler: Handler,
    text: String
) {
    companion object {
        const val MESSAGE_OK = 0x124434
        const val MESSAGE_CANCEL = 0xffffff
    }

    private var popupView: View = activity.layoutInflater.inflate(R.layout.popup_cancel_ok, null)
    private var popupWindow: PopupWindow? = PopupWindow(
        popupView,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        true
    ).apply {
        showAtLocation(popupView, Gravity.CENTER, 0, 0)
    }

    init {
        val orderOk = popupView.findViewById<Button>(R.id.button_ok)
        val header = popupView.findViewById<TextView>(R.id.text_cancel).apply {
            setText(text)
        }
        val orderCancel = popupView.findViewById<ImageButton>(R.id.button_cancel)

        // OK button click handler
        orderOk.setOnClickListener {
            handler.sendMessage(handler.obtainMessage(1, "OK"))
            dismissPopup()
        }

        // Cancel button click handler
        orderCancel.setOnClickListener {
            handler.sendMessage(handler.obtainMessage(0, "CANCEL"))
            dismissPopup()
        }
    }

    private fun dismissPopup() {
        popupWindow?.dismiss()
        popupWindow = null
    }

    fun isShowing(): Boolean = popupWindow?.isShowing ?: false
}