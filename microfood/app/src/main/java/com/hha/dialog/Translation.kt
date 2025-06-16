package com.hha.dialog

import com.hha.resources.Global
import com.hha.types.ETaal

object Translation {

    enum class TextId {
        TEXT_CHOOSE_TABLE,
        TEXT_ABOUT_US,
        TEXT_CALCULATOR,
        TEXT_CANCEL,
        TEXT_CHANGE_LANGUAGE,
        TEXT_ERROR,
        TEXT_EXIT_PROGRAM,
        TEXT_MIN_1,
        TEXT_NAME,
        TEXT_OK,
        TEXT_PLUS_1,
        TEXT_PORTION,
        TEXT_PRICE,
        TEXT_PRINTER,
        TEXT_PRINT_QUANTITY,
        TEXT_TABLE,
        TEXT_TABLE_OVERVIEW,
        TEXT_TAKEAWAY,
        TEXT_REMOVE_ITEM,
        TEXT_SEND_ALL,
        TEXT_SENDING_TABLES,
        TEXT_SETTINGS,
        TEXT_STOP,
        TEXT_STOP_PROGRAM,
        TEXT_UPDATE,
        TEXT_UPDATE_MENU,
        MAX_TEXT
    }

    private val m_lin = mapOf(
        TextId.TEXT_CHOOSE_TABLE to arrayOf("输入单", "KIES TAFEL BESTELLEN", "CHOOSE TABLE TO ORDER", "TISCH NAME", "PILIH MEJA PESANAN"),
        TextId.TEXT_ABOUT_US to arrayOf("关于", "OVER ONS", "ABOUT US", "WIR SIND", "TENTANG"),
        TextId.TEXT_CALCULATOR to arrayOf("计算器", "CALCULATOR", "CALCULATOR", "RECHNEN", "KALKULATOR"),
        TextId.TEXT_CANCEL to arrayOf("取消", "STOP", "STOP", "STOP", "BATAL"),
        TextId.TEXT_CHANGE_LANGUAGE to arrayOf("TAAL", "改变语言", "改变语言", "改变语言", "改变语言"),
        TextId.TEXT_ERROR to arrayOf("ERROR", "ERROR", "ERROR", "ERROR", "ERROR"),
        TextId.TEXT_EXIT_PROGRAM to arrayOf("停止软件", "STOP PROGRAMMA", "STOP PROGRAM", "PROGRAM STOP", "STOP PROGRAM"),
        TextId.TEXT_MIN_1 to arrayOf("减一", "MIN 1", "", "", "-1"),
        TextId.TEXT_NAME to arrayOf("换菜单名字", "NAMEN VERANDEREN", "MENU NAMES CHANGE", "MENU NAMEN ANDERN", "NAMA MENU GANTI", "換菜單名字"),
        TextId.TEXT_OK to arrayOf("好 !", "OK !", "OK !", "GUT !", "OK !"),
        TextId.TEXT_PLUS_1 to arrayOf("递增", "PLUS 1", "", "", "+1"),
        TextId.TEXT_PORTION to arrayOf("部分", "PORTIE", "PORTION", "PORTIE", "MELAYANI"),
        TextId.TEXT_PRICE to arrayOf("价格", "PRIJS", "PRICE", "PREIS", "HARGA"),
        TextId.TEXT_PRINTER to arrayOf("打印机", "AFDRUK", "PRINTER", "DRUCKER", "PRINTER"),
        TextId.TEXT_PRINT_QUANTITY to arrayOf("打印多少?", "AANTAL AFDRUKKEN?", "PRINT HOW,MANY TIMES?", "DRUCK ANZAHL", "PRINTER BERAPA KALI?"),
        TextId.TEXT_TABLE to arrayOf("餐桌", "TAFEL", "TABLE", "TISCH", "MEJA"),
        TextId.TEXT_TABLE_OVERVIEW to arrayOf("输入单", "TAFEL OVERZICHT", "CHOOSE TABLE TO ORDER", "TISCH NAME", "PILIH MEJA PESANAN"),
        TextId.TEXT_TAKEAWAY to arrayOf("新打包", "AFHALEN", "TAKEAWAY", "MITNEHMEN", "CINA TAKE AWAY"),
        TextId.TEXT_REMOVE_ITEM to arrayOf("删除", "WEGHALEN", "REMOVE", "ENTFERNEN", "DIHAPUS"),
        TextId.TEXT_SEND_ALL to arrayOf("发送", "VERZEND ORDERS", "SEND ORDERS", "UBERTRAGEN", "MENGIRIMKAN"),
        TextId.TEXT_SENDING_TABLES to arrayOf("互联网，请稍候", "BEZIG MET INTERNET\nWACHT TOT DE TAFEL VERSTUURD IS.", "SEND ORDERS, PLEASE WAIT", "WARTEN BITTE", "TUNGGU,INTERNET"),
        TextId.TEXT_SETTINGS to arrayOf("改变配置", "VERANDER CONFIGURATIE", "CHANGE CONFIGURATION", "ANDERN EINSTELLUNGEN", "GANTI KONFIGURASI"),
        TextId.TEXT_STOP to arrayOf("停止", "STOP", "STOP", "STOP", "STOP"),
        TextId.TEXT_STOP_PROGRAM to arrayOf("停止软件", "STOP PROGRAMMA", "STOP PROGRAM", "PROGRAM STOP", "STOP PROGRAM"),
        TextId.TEXT_UPDATE to arrayOf("更新", "UPDATE", "UPDATE", "UPDATE", "UPDATE"),
        TextId.TEXT_UPDATE_MENU to arrayOf("菜单更新", "UPDATE MENU KAART", "UPDATE MENU CARD", "MENU UPDATE", "MENU PEMBARUAN")
    )

    private const val DEFAULT_STRING = "??";

    fun get(id: TextId): String {
        val lang = Global.getInstance().language.value
        return m_lin[id]?.getOrElse(lang-1) { DEFAULT_STRING } ?: DEFAULT_STRING
    }

    fun nextLanguage(): ETaal {
        val global = Global.getInstance()
        val CFG = Global.getInstance().CFG

        while (true) {
            global.language = when (global.language) {
                ETaal.LANG_SIMPLIFIED -> ETaal.LANG_DUTCH
                ETaal.LANG_NONE -> ETaal.LANG_SIMPLIFIED
                ETaal.LANG_DUTCH -> ETaal.LANG_ENGLISH
                ETaal.LANG_ENGLISH -> ETaal.LANG_GERMAN
                ETaal.LANG_GERMAN -> ETaal.LANG_INDONESIAN
                else -> ETaal.LANG_SIMPLIFIED
            }
            when (global.language) {
                ETaal.LANG_SIMPLIFIED -> try {
                    if (CFG.getValue("chinese") != 0) return global.language
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                ETaal.LANG_DUTCH -> try {
                    if (CFG.getValue("dutch") != 0) return global.language
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                ETaal.LANG_INDONESIAN -> try {
                    if (CFG.getValue("indonesian") != 0) return global.language
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                else -> {}
            }
        }
    }
}