package com.hha.dialog

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import com.hha.resources.Global
import tech.hha.microfood.R
import java.io.DataOutputStream

class StopDialog : Activity() {
    val CFG = Global.getInstance().CFG

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stop_layout)

        val bigText = findViewById<TextView>(R.id.stopBig)

        try {
            bigText.textSize = CFG.getValue("font_splash").toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        Thread {
            try {
                Thread.sleep(5000)
                shutDown()
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(-1)
                finish()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    /**
     * Powers off the device
     * Note: This requires root permissions
     */
    private fun shutDown() {
        try {
            Runtime.getRuntime().exec("su").run {
                DataOutputStream(outputStream).use { out ->
                    out.writeBytes("reboot -p\n")
                    out.writeBytes("exit\n")
                    out.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}