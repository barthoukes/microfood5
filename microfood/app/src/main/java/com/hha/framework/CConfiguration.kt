package com.hha.framework

import com.hha.resources.Global
import org.json.JSONException
import org.json.JSONObject
import android.os.Environment
import com.hha.exceptions.ConfigNotFoundException
import java.io.*

class CConfiguration private constructor() {
    var fileName = "configuration.txt"
    private val totalValues = 12
    private val global = Global.getInstance()

    private val names = arrayOf(
        // name
        "name",
        "IP",
        "handheld_id",
        "password",
        "font_item",
        "font_text",
        "font_button",
        "font_splash",
        "chinese",
        "dutch",
        "restaurant_name",
        "demo"
    )

    private val defaults = arrayOf(
        // default values
        "Bart",
        "192.168.1.104",
        "1",
        "piet",
        "20",
        "24",
        "24",
        "24",
        "1",
        "1",
        "MeiWah",
        "1"
    )

    private val values = Array(totalValues) { defaults[it] }

    @Throws(ConfigNotFoundException::class)
    private fun getIndex(name: String): Int {
        for (a in names.indices) {
            if (name == names[a]) {
                return a
            }
        }
        throw ConfigNotFoundException()
    }

    @Throws(NumberFormatException::class, ConfigNotFoundException::class)
    fun     getValue(name: String): Int {
        val a = getIndex(name)
        return values[a].toInt()
    }

    @Throws(ConfigNotFoundException::class)
    fun getString(name: String): String {
        val a = getIndex(name)
        return values[a]
    }

    @Throws(ConfigNotFoundException::class)
    fun setString(name: String, value: String) {
        val a = getIndex(name)
        values[a] = value
    }

    @Throws(ConfigNotFoundException::class)
    fun setValue(name: String, value: Int) {
        val a = getIndex(name)
        values[a] = value.toString()
    }

    init {
        load(fileName)
    }

    private fun load(file: String) {
        // Set default values first
        for (a in 0 until totalValues) {
            values[a] = defaults[a]
        }

        try {
            var dir = Environment.getDataDirectory()
            dir = Environment.getExternalStorageDirectory()
            dir = global.myDir
            val loadFile = FileReader(File(dir, file))
            val buffer = BufferedReader(loadFile)
            val line = buffer.readLine()
            val parser = JSONObject(line)

            for (a in 0 until totalValues) {
                if (parser.has(names[a])) {
                    values[a] = parser.getString(names[a])
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            save(file)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun save(filename: String) {
        try {
            val parser = JSONObject()
            var dir = Environment.getExternalStorageDirectory()
            dir = global.myDir
            val writer = FileWriter(File(dir, filename))

            for (a in 0 until totalValues) {
                parser.put(names[a], values[a])
            }
            writer.write(parser.toString())
            writer.close()
        } catch (e1: IOException) {
            e1.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    val isDemo: Boolean
        get() = try {
            getValue("demo") > 0
        } catch (e: Exception) {
            false
        }

    companion object {
        private var instance: CConfiguration? = null

        val getInstance: CConfiguration
            get() {
                if (instance == null) {
                    instance = CConfiguration()
                }
                return instance!!
            }
    }
}