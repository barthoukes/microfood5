package com.hha.dialog

import com.hha.resources.Global
import com.hha.types.ETaal

object Translation {

    enum class TextId {
        TEXT_CHOOSE_TABLE,
        TEXT_ABOUT_US,
        TEXT_BILL_HEADER,
        TEXT_BILL_PAYMENTS,
        TEXT_CALCULATOR,
        TEXT_CANCEL,
        TEXT_CANCEL_REASON,
        TEXT_CANCEL_REASON_BAD_FOOD,
        TEXT_CANCEL_REASON_NO_MONEY,
        TEXT_CANCEL_REASON_NO_STOCK,
        TEXT_CANCEL_REASON_PERSONNEL,
        TEXT_CANCEL_REASON_RUN_AWAY,
        TEXT_CANCEL_REASON_TECHNICAL,
        TEXT_CANCEL_REASON_TELEPHONE,
        TEXT_CANCEL_REASON_TOO_LATE,
        TEXT_CANCEL_REASON_TRAINING,
        TEXT_CANCEL_REASON_WRONG_DISH,
        TEXT_CHANGE_LANGUAGE,
        TEXT_DISCOUNT,
        TEXT_EAT_INSIDE,
        TEXT_ERROR,
        TEXT_EXIT_PROGRAM,
        TEXT_MIN_1,
        TEXT_NAME,
        TEXT_MORE,
        TEXT_OK,
        TEXT_PAY,
        TEXT_PLUS_1,
        TEXT_PORTION,
        TEXT_PRICE,
        TEXT_PRINTER,
        TEXT_PRINT_QUANTITY,
        TEXT_PRINT_SLIP,
        TEXT_PRINT_ROLL,
        TEXT_REMARK,
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
        TextId.TEXT_BILL_HEADER to listOf("买单", "AFREKENEN", "BILL MODE", "RECHNUNG", "BON"),
        TextId.TEXT_BILL_PAYMENTS to listOf("付款", "BETAALWIJZE", "PAY METHOD", "BEZAHLUNG", "UANG"),
        TextId.TEXT_CHOOSE_TABLE to listOf("输入单", "KIES TAFEL BESTELLEN", "CHOOSE TABLE TO ORDER", "TISCH NAME", "PILIH MEJA PESANAN"),
        TextId.TEXT_ABOUT_US to listOf("关于", "OVER ONS", "ABOUT US", "WIR SIND", "TENTANG"),
        TextId.TEXT_CALCULATOR to listOf("计算器", "CALCULATOR", "CALCULATOR", "RECHNEN", "KALKULATOR"),
        TextId.TEXT_CANCEL to listOf("取消", "STOP", "STOP", "STOP", "BATAL"),
        TextId.TEXT_CANCEL_REASON to listOf("取消原因", "REDEN WAAROM DE BESTELLING STOPT", "REASON WHY THE ORDER IS " +
           "CANCELLED", "CANCEL GRUND", "ALASAN MENGAPA PESANAN DI STOP"),
        TextId.TEXT_CANCEL_REASON_BAD_FOOD to listOf("取消原因变质的食物", "HET ETEN SMAAKT SLECHT",
            "FOOD TASTE BAD", "CANCEL GRUND BAD FOOD", "MAKANAN TIDAK ENAK"),
        TextId.TEXT_CANCEL_REASON_NO_MONEY to listOf("客户没有钱", "KLANT HEEFT GEEN GELD",
            "CUSTOMER HAS NO MONEY", "KUNDE HAT KEIN GELD", "PELANGGAN TIADA UANG"),
        TextId.TEXT_CANCEL_REASON_NO_STOCK to listOf("无库存", "GEEN VOORRAAD OF INGREDIENTEN",
            "NO STOCK OR FOOD", "NICHTS AUF LAGER", "TIDAK ADA STOK"),
        TextId.TEXT_CANCEL_REASON_PERSONNEL to listOf("食物的人员", "ETEN VOOR HET PERSONEEL",
            "FOOD FOR PERSONNEL", "ESSEN FUR DAS PERSONAL", "MAKANAN UNTUK PERSONIL"),
        TextId.TEXT_CANCEL_REASON_RUN_AWAY to listOf("客户跑掉了", "KLANT WEGGEREND ZONDER BETALEN",
            "CUSTOMER RAN AWAY", "KUNDEN LIEF WEG", "PELANGGAN PERGI TANPA BAYAR"),
        TextId.TEXT_CANCEL_REASON_TECHNICAL to listOf("技術原因不能準備食物",
            "TECHNISCHE REDEN, KAN HET ETEN NIET MAKEN",
            "CANNOT COOK FOR TECHNICAL REASONS", "ESSEN ZUBEREITEN NICHT MOGLICH JETZT",
            "ALASAN TEKNIS TIDAK BISA SIAPKAN MAKANAN"),
        TextId.TEXT_CANCEL_REASON_TELEPHONE to listOf("无需客户电话订购", "KLANT HAALT BESTELLING NIET OP", "TELEPHONE ORDER WITHOUT CUSTOMER",
            "TELEFON BESTELLEN OHNE KUNDEN", "PESANAN TIDAK DI AMBIL"),
        TextId.TEXT_CANCEL_REASON_TOO_LATE to listOf("食物送得太晚", "ETEN TE LAAT BEZORGD",
            "FOOD DELIVERED TOO LATE", "ESSEN ZU SPÄT GELIEFERT",
            "MAKANAN DIKIRIM TERLAMBAT"),
        TextId.TEXT_CANCEL_REASON_TRAINING to listOf("交易培训", "TRAINING TRANSACTIE",
            "TRAINING TRANSACTION", "TRAINING TRANSACTION", "TRANSAKSI PERCOBAAN"),
        TextId.TEXT_CANCEL_REASON_WRONG_DISH to listOf("错误的食物送呈", "VERKEERDE ETEN BEZORGD",
            "WRONG FOOD PREPARED", "FALSCH GELIEFERT FOOD", "SALAH PESANAN MAKANAN"),
        TextId.TEXT_CHANGE_LANGUAGE to listOf("TAAL", "改变语言", "改变语言", "改变语言", "改变语言"),
        TextId.TEXT_DISCOUNT to listOf("折扣", "KORTING", "DISCOUNT", "RABBAT", "DISKON"),
        TextId.TEXT_EAT_INSIDE to listOf("里面吃", "HIER ETEN", "EAT HERE", "ISS HIER", "MAKAN DISINI"),
        TextId.TEXT_ERROR to listOf("ERROR", "ERROR", "ERROR", "ERROR", "ERROR"),
        TextId.TEXT_EXIT_PROGRAM to listOf("停止软件", "STOP PROGRAMMA", "STOP PROGRAM", "PROGRAM STOP", "STOP PROGRAM"),
        TextId.TEXT_MIN_1 to listOf("减一", "MIN 1", "", "", "-1"),
        TextId.TEXT_NAME to listOf("换菜单名字", "NAMEN VERANDEREN", "MENU NAMES CHANGE", "MENU NAMEN ANDERN", "NAMA MENU GANTI", "換菜單名字"),
        TextId.TEXT_MORE to listOf("加餐", "MEER", "MORE", "MEHR", "LEBIH"),
        TextId.TEXT_OK to listOf("好 !", "OK !", "OK !", "GUT !", "OK !"),
        TextId.TEXT_PAY to listOf( "埋单", "BETALEN", "PAY", "BEZAHLEN", "BAYAR"),
        TextId.TEXT_PLUS_1 to listOf("递增", "PLUS 1", "", "", "+1"),
        TextId.TEXT_PORTION to listOf("部分", "PORTIE", "PORTION", "PORTIE", "MELAYANI"),
        TextId.TEXT_PRICE to listOf("价格", "PRIJS", "PRICE", "PREIS", "HARGA"),
        TextId.TEXT_PRINTER to listOf("打印机", "AFDRUK", "PRINTER", "DRUCKER", "PRINTER"),
        TextId.TEXT_PRINT_QUANTITY to listOf("打印多少?", "AANTAL AFDRUKKEN?", "PRINT HOW,MANY TIMES?", "DRUCK ANZAHL", "PRINTER BERAPA KALI?"),
        TextId.TEXT_PRINT_SLIP to listOf("打印单", "SLIP PRINTER", "SLIP BILL", "SLIP DRUCKER", "PRINTER SLIP"),
        TextId.TEXT_PRINT_ROLL to listOf("卷打印机", "REKENING ROL PRINTER" , "PRINT ROLL BILL", "ROLL DRUCKER", "PRINTER PERAN NOTA"),
        TextId.TEXT_REMARK to listOf("写备注", "OPMERKING", "REMARK", "NOTITZEN", "KETERANGAN"),
        TextId.TEXT_TABLE to listOf("餐桌", "TAFEL", "TABLE", "TISCH", "MEJA"),
        TextId.TEXT_TABLE_OVERVIEW to listOf("输入单", "TAFEL OVERZICHT", "CHOOSE TABLE TO ORDER", "TISCH NAME", "PILIH MEJA PESANAN"),
        TextId.TEXT_TAKEAWAY to listOf("新打包", "AFHALEN", "TAKEAWAY", "MITNEHMEN", "CINA TAKE AWAY"),
        TextId.TEXT_REMOVE_ITEM to listOf("删除", "WEGHALEN", "REMOVE", "ENTFERNEN", "DIHAPUS"),
        TextId.TEXT_SEND_ALL to listOf("发送", "VERZEND ORDERS", "SEND ORDERS", "UBERTRAGEN", "MENGIRIMKAN"),
        TextId.TEXT_SENDING_TABLES to listOf("互联网，请稍候", "BEZIG MET INTERNET\nWACHT TOT DE TAFEL VERSTUURD IS.", "SEND ORDERS, PLEASE WAIT", "WARTEN BITTE", "TUNGGU,INTERNET"),
        TextId.TEXT_SETTINGS to listOf("改变配置", "VERANDER CONFIGURATIE", "CHANGE CONFIGURATION", "ANDERN EINSTELLUNGEN", "GANTI KONFIGURASI"),
        TextId.TEXT_STOP to listOf("停止", "STOP", "STOP", "STOP", "STOP"),
        TextId.TEXT_STOP_PROGRAM to listOf("停止软件", "STOP PROGRAMMA", "STOP PROGRAM", "PROGRAM STOP", "STOP PROGRAM"),
        TextId.TEXT_UPDATE to listOf("更新", "UPDATE", "UPDATE", "UPDATE", "UPDATE"),
        TextId.TEXT_UPDATE_MENU to listOf("菜单更新", "UPDATE MENU KAART", "UPDATE MENU CARD", "MENU UPDATE", "MENU PEMBARUAN")
    )

    private const val DEFAULT_STRING = "??";

    fun get(id: TextId): String {
        val lang = Global.getInstance().language.value
        return m_lin[id]?.getOrElse(lang-1) { DEFAULT_STRING } ?: DEFAULT_STRING
    }

    fun nextLanguage(): ETaal {
        val global = Global.getInstance()
        val CFG = Global.getInstance().CFG
        var retry = 7

        while (--retry>0) {
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
                    if (CFG.getOption("simplified")) break
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                ETaal.LANG_DUTCH -> try {
                    if (CFG.getOption("nederlands")) break
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                ETaal.LANG_INDONESIAN -> try {
                    if (CFG.getOption("indonesian")) break
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                ETaal.LANG_ENGLISH -> try {
                    if (CFG.getOption("english")) break
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                ETaal.LANG_GERMAN -> try {
                    if (CFG.getOption("duits")) break
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                else -> {}
            }
        }
        return global.language
    }
}