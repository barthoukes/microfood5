package com.hha.messagebox

import android.R
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.hha.framework.COpenClientsHandler

class StringDialog() : DialogFragment() {

    // Configuration options with defaults
    private var title: String = ""
    private var hint: String? = null
    private var initialText: String = ""
    private var listener: StringListener? = null

    private var allowChineseInput: Boolean = true

    interface StringListener {
        fun onStringConfirmed(text: String)
        fun onStringCancelled()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inputLayout = TextInputLayout(requireContext()).apply {
            hint = this@StringDialog.hint
            addView(TextInputEditText(context).apply {
                setText(initialText)
                imeOptions = EditorInfo.IME_ACTION_DONE
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        confirmInput()
                        true
                    } else {
                        false
                    }
                }
            })
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(inputLayout)
            .setPositiveButton(R.string.ok) { _, _ -> confirmInput() }
            .setNegativeButton(R.string.cancel) { _, _ -> listener?.onStringCancelled() }
            .apply {
                if (allowChineseInput) {
                    setNeutralButton("中文") { _, _ ->
                        (inputLayout.editText as? TextInputEditText)?.inputType =
                            EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_NORMAL
                    }
                }
            }
            .create()
    }

    private fun confirmInput() {
        val text = (dialog as? AlertDialog)
            ?.findViewById<TextInputEditText>(R.id.text1)
            ?.text?.toString()?.trim()

        text?.takeIf { it.isNotEmpty() }?.let {
            listener?.onStringConfirmed(it)
        } ?: run {
            listener?.onStringCancelled()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as? StringListener ?:
                context as? StringListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    class Builder {
        private val dialog = StringDialog()

        fun setTitle(title: String): Builder {
            dialog.title = title
            return this
        }

        fun setHint(hint: String): Builder {
            dialog.hint = hint
            return this
        }

        fun setInitialText(text: String): Builder {
            dialog.initialText = text
            return this
        }

        fun allowChineseInput(allow: Boolean): Builder {
            dialog.allowChineseInput = allow
            return this
        }

        fun setListener(listener: StringListener): Builder {
            dialog.listener = listener
            return this
        }

        fun show() {
            val listener = dialog.listener ?: return

            when (listener) {
                is FragmentActivity -> {
                    dialog.show(
                        (listener as FragmentActivity).supportFragmentManager,
                        "StringDialog"
                    )
                }

                is Fragment -> {
                    dialog.show((listener as Fragment).childFragmentManager, "StringDialog")
                }

                else -> throw IllegalStateException("Listener must be a FragmentActivity or Fragment")
            }
        }
    }
}

//Example
//// In your Activity or Fragment:
//class MyActivity : AppCompatActivity(), StringDialog.StringListener {
//
//    fun showDialog() {
//        StringDialog.Builder(this)
//            .setTitle("Enter your text")
//            .setHint("Optional hint")
//            .setInitialText("Default text")
//            .allowChineseInput(true)
//            .setListener(this)
//            .show()
//    }
//
//    override fun onStringConfirmed(text: String) {
//        // Handle the confirmed text
//        Toast.makeText(this, "Entered: $text", Toast.LENGTH_SHORT).show()
//    }
//
//    override fun onStringCancelled() {
//        // Handle cancellation
//        Toast.makeText(this, "Input cancelled", Toast.LENGTH_SHORT).show()
//    }
//}
//
