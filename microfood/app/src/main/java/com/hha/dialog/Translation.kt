package com.hha.dialog

import com.hha.resources.Global
import com.hha.types.ETaal

object Translation {

    enum class TextId {
        TEXT_CHOOSE_TABLE,
        TEXT_ABOUT_US,
        TEXT_ALREADY_PAYED,
        TEXT_ASAP,
        TEXT_KEY_BAD,
        TEXT_BILL_HEADER,
        TEXT_BILL_OPTION,
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
        TEXT_CASH,
        TEXT_CASH_OUT,
        TEXT_CHANGE_LANGUAGE,
        TEXT_CHOOSE_TO_ORDER,
        TEXT_CLEAN_TABLE,
        TEXT_CLIENT_AMOUNT,
        TEXT_CLIENT_PAYS_WITH,
        TEXT_DELIVER_TIME,
        TEXT_DISCOUNT,
        TEXT_EAT_INSIDE,
        TEXT_EMPLOYEE,
        TEXT_ERROR,
        TEXT_EXCHANGE_MONEY,
        TEXT_EXIT_PROGRAM,
        TEXT_EXTRA,
        TEXT_FINISH_BILL,
        TEXT_FLOOR_PLAN,
        TEXT_ITEM_DRINKS,
        TEXT_ITEM_KITCHEN,
        TEXT_KEY,
        TEXT_LIST,
        TEXT_MIN_1,
        TEXT_NAME,
        TEXT_MORE,
        TEXT_OK,
        TEXT_ORDER_TELEPHONE,
        TEXT_PAGE_ORDER,
        TEXT_PAID,
        TEXT_PAY,
        TEXT_PAYED,
        TEXT_PLUS_1,
        TEXT_PORTION,
        TEXT_PRICE,
        TEXT_PRINTER,
        TEXT_PRINT_OFFER,
        TEXT_PRINT_QUANTITY,
        TEXT_PRINT_SLIP,
        TEXT_PRINT_ROLL,
        TEXT_QUANTITY,
        TEXT_RECHAUD,
        TEXT_REMARK,
        TEXT_REMOVE_ITEM,
        TEXT_REPRINT,
        TEXT_SHOP,
        TEXT_TABLE,
        TEXT_TABLE_OVERVIEW,
        TEXT_TAKEAWAY,
        TEXT_TELEPHONE,
        TEXT_TRANSPORT,
        TEXT_SEND_ALL,
        TEXT_SEND_BILL,
        TEXT_SENDING_TABLES,
        TEXT_SETTINGS,
        TEXT_STOP,
        TEXT_STOP_PROGRAM,
        TEXT_SUBTOTAL,
        TEXT_TOTAL,
        TEXT_UPDATE,
        TEXT_UPDATE_MENU,
        TEXT_WOK,
        MAX_TEXT
    }

    fun TextId.str(): String
    {
        return get(this)
    }

    private val m_lin = mapOf(
        TextId.TEXT_ABOUT_US to listOf("关于", "OVER ONS", "ABOUT US", "WIR SIND", "TENTANG", "HAKKIMIZDA"),
        TextId.TEXT_ALREADY_PAYED to listOf("一切都付了", "ALLES REEDS BETAALD", "ALL IS PAYED", "ALLES IST BEZAHLT",
            "SEMUA DIBAYARKAN", "TAMAMI ÖDENDİ"),
        TextId.TEXT_ASAP to listOf("尽快", "SNEL", "ASAP", "SCHNELL", "SEPAT", "HEMEN"),
        TextId.TEXT_KEY_BAD to listOf( "请用别条匙", "ANDERE SLEUTEL GEBRUIKEN!",
            " PLEASE USE OTHER KEY!", "NICHT MEHR DIESE SCHLÜSSEL!",
            "MOHON GUNAKAN, KUNCI LAINNYA", "LÜTFEN BAŞKA ANAHTAR KULLANIN!"),
        TextId.TEXT_BILL_HEADER to listOf("买单", "AFREKENEN", "BILL MODE", "RECHNUNG", "BON", "HESAP MODU"),
        TextId.TEXT_BILL_OPTION to listOf("哪里买单", "KIES AFREKENEN", "CHOOSE BILL",
            "ZUR KASSE", "PILIH NOTA", "ÖDEME YERİ"),
        TextId.TEXT_BILL_PAYMENTS to listOf("付款", "BETAALWIJZE", "PAY METHOD", "BEZAHLUNG", "UANG", "ÖDEME YÖNTEMİ"),
        TextId.TEXT_CHOOSE_TABLE to listOf("输入单", "KIES TAFEL BESTELLEN", "CHOOSE TABLE TO ORDER", "TISCH NAME", "PILIH MEJA PESANAN", "SİPARİŞ İÇİN MASA SEÇ"),
        TextId.TEXT_CHOOSE_TO_ORDER to listOf("输入单", "KIES BESTELWIJZE", "CHOOSE TO ORDER", "BESTELLEN", "PILIH " +
           "PESANAN", "SİPARİŞ VERMEK İÇİN SEÇ"),
        TextId.TEXT_CALCULATOR to listOf("计算器", "CALCULATOR", "CALCULATOR", "RECHNEN", "KALKULATOR", "HESAP MAKİNESİ"),
        TextId.TEXT_CANCEL to listOf("取消", "STOP", "STOP", "STOP", "BATAL", "İPTAL"),
        TextId.TEXT_CANCEL_REASON to listOf("取消原因", "REDEN WAAROM DE BESTELLING STOPT", "REASON WHY THE ORDER IS " +
           "CANCELLED", "CANCEL GRUND", "ALASAN MENGAPA PESANAN DI STOP", "İPTAL NEDENİ"),
        TextId.TEXT_CANCEL_REASON_BAD_FOOD to listOf("取消原因变质的食物", "HET ETEN SMAAKT SLECHT",
            "FOOD TASTE BAD", "CANCEL GRUND BAD FOOD", "MAKANAN TIDAK ENAK", "KÖTÜ YEMEK"),
        TextId.TEXT_CANCEL_REASON_NO_MONEY to listOf("客户没有钱", "KLANT HEEFT GEEN GELD",
            "CUSTOMER HAS NO MONEY", "KUNDE HAT KEIN GELD", "PELANGGAN TIADA UANG", "MÜŞTERİNİN PARASI YOK"),
        TextId.TEXT_CANCEL_REASON_NO_STOCK to listOf("无库存", "GEEN VOORRAAD OF INGREDIENTEN",
            "NO STOCK OR FOOD", "NICHTS AUF LAGER", "TIDAK ADA STOK", "STOK YOK"),
        TextId.TEXT_CANCEL_REASON_PERSONNEL to listOf("食物的人员", "ETEN VOOR HET PERSONEEL",
            "FOOD FOR PERSONNEL", "ESSEN FUR DAS PERSONAL", "MAKANAN UNTUK PERSONIL", "PERSONEL YEMEĞİ"),
        TextId.TEXT_CANCEL_REASON_RUN_AWAY to listOf("客户跑掉了", "KLANT WEGGEREND ZONDER BETALEN",
            "CUSTOMER RAN AWAY", "KUNDEN LIEF WEG", "PELANGGAN PERGI TANPA BAYAR", "MÜŞTERİ KAÇTI"),
        TextId.TEXT_CANCEL_REASON_TECHNICAL to listOf("技術原因不能準備食物",
            "TECHNISCHE REDEN, KAN HET ETEN NIET MAKEN",
            "CANNOT COOK FOR TECHNICAL REASONS", "ESSEN ZUBEREITEN NICHT MOGLICH JETZT",
            "ALASAN TEKNIS TIDAK BISA SIAPKAN MAKANAN", "TEKNİK NEDEN"),
        TextId.TEXT_CANCEL_REASON_TELEPHONE to listOf("无需客户电话订购", "KLANT HAALT BESTELLING NIET OP", "TELEPHONE ORDER WITHOUT CUSTOMER",
            "TELEFON BESTELLEN OHNE KUNDEN", "PESANAN TIDAK DI AMBIL", "TELEFON SİPARİŞİ ALINMADI"),
        TextId.TEXT_CANCEL_REASON_TOO_LATE to listOf("食物送得太晚", "ETEN TE LAAT BEZORGD",
            "FOOD DELIVERED TOO LATE", "ESSEN ZU SPÄT GELIEFERT",
            "MAKANAN DIKIRIM TERLAMBAT", "YEMEK GEÇ GELDİ"),
        TextId.TEXT_CANCEL_REASON_TRAINING to listOf("交易培训", "TRAINING TRANSACTIE",
            "TRAINING TRANSACTION", "TRAINING TRANSACTION", "TRANSAKSI PERCOBAAN", "EĞİTİM İŞLEMİ"),
        TextId.TEXT_CANCEL_REASON_WRONG_DISH to listOf("错误的食物送呈", "VERKEERDE ETEN BEZORGD",
            "WRONG FOOD PREPARED", "FALSCH GELIEFERT FOOD", "SALAH PESANAN MAKANAN", "YANLIŞ YEMEK"),
        TextId.TEXT_CASH to listOf("现金", "CONTANT", "CASH", "KASSE", "KAS", "NAKİT"),
        TextId.TEXT_CASH_OUT to listOf("续回", "TERUG", "EXCHANGE", "ZUR]CKGABE", "PERUBAHAN", "PARA ÜSTÜ"),
        TextId.TEXT_CHANGE_LANGUAGE to listOf("TAAL", "改变语言", "改变语言", "改变语言", "改变语言", "DİL DEĞİŞTİR"),
        TextId.TEXT_CLEAN_TABLE to listOf("擦桌子", "TAFEL OPRUIMEN", "CLEAN TABLE", "TISCH SAUBER", "BERSIH MEJA", "MASAYI TEMİZLE"),
        TextId.TEXT_CLIENT_AMOUNT to listOf("顾客买单", "KLANT BEDRAG", "CLIENT AMOUNT", "KUNDE BETRAG", "JUMLAH PELANGGAN", "MÜŞTERİ TUTARI"),
        TextId.TEXT_CLIENT_PAYS_WITH to listOf("客户付钱", "KLANT BETAALT", "CUSTOMER PAYS", "KUNDE BEZAHLT", "MEMBAYAR", "MÜŞTERİ ÖDEMESİ"),
        TextId.TEXT_DELIVER_TIME to listOf("出餐", "LEVERTIJD", "TIME NOTE", "LIEFERZEIT", "JAM", "TESLİM SÜRESİ"),
        TextId.TEXT_DISCOUNT to listOf("折扣", "KORTING", "DISCOUNT", "RABBAT", "DISKON", "İNDİRİM"),
        TextId.TEXT_EAT_INSIDE to listOf("里面吃", "HIER ETEN", "EAT HERE", "ISS HIER", "MAKAN DISINI", "BURADA YE"),
        TextId.TEXT_EMPLOYEE to listOf("同事", "MEDEWERKER", "COLLEAGUE", "KOLLEGE", "KOLEGA", "PERSONEL"),
        TextId.TEXT_ERROR to listOf("ERROR", "ERROR", "ERROR", "ERROR", "ERROR", "HATA"),
        TextId.TEXT_EXCHANGE_MONEY to listOf("底钱", "WISSELGELD", "EXCHANGE MONEY", "WECHSELGELT", "UANG EXCHANGE", "PARA ÜSTÜ"),
        TextId.TEXT_EXIT_PROGRAM to listOf("停止软件", "STOP PROGRAMMA", "STOP PROGRAM", "PROGRAM STOP", "STOP PROGRAM", "PROGRAMI KAPAT"),
        TextId.TEXT_EXTRA to listOf("改餐", "EXTRA", "EXTRA", "EXTRA", "EXTRA", "EKSTRA"),
        TextId.TEXT_FINISH_BILL to listOf("打印票据", "REKENING KLAAR AFDRUK ?",
            "FINISH BILL ?", "RECHNUNG FERTIG AFDRUKKEN ?", "NOTA OKE ADRUKKEN? ", "FATURAYI TAMAMLA?"),
        TextId.TEXT_FLOOR_PLAN to listOf("平面图", "PLATTEGROND", "FLOORPLAN", "GRUNDRISS", "PLATTEGROND RESTORAN",
            "KAT PLANI"),
        TextId.TEXT_ITEM_DRINKS to listOf("饮料", "DRANK", "DRANK", "DRANK", "DRANK", "İÇECEKLER"),
        TextId.TEXT_KEY to listOf( "匙", "SLEUTEL", "KEY", "SCHLÜSSEL", "KUNCI", "ANAHTAR"),
        TextId.TEXT_ITEM_KITCHEN to listOf("厨房", "KEUKEN", "KITCHEN", "KUCHE", "DAPUR", "MUTFAK"),
        TextId.TEXT_LIST to listOf("买主 ", "KLANT ", "CLIENT ", "KUNDE ", "KLIEN", "MÜŞTERİ"),
        TextId.TEXT_MIN_1 to listOf("减一", "MIN 1", "", "", "-1", "-1"),
        TextId.TEXT_NAME to listOf("换菜单名字", "NAMEN VERANDEREN", "MENU NAMES CHANGE", "MENU NAMEN ANDERN", "NAMA MENU GANTI", "MENÜ ADI DEĞİŞTİR"),
        TextId.TEXT_MORE to listOf("加餐", "MEER", "MORE", "MEHR", "LEBIH", "DAHA FAZLA"),
        TextId.TEXT_OK to listOf("好 !", "OK !", "OK !", "GUT !", "OK !", "TAMAM !"),
        TextId.TEXT_ORDER_TELEPHONE to listOf("送食物", "INFORMEER KLANT", "INFORM CUSTOMER", "INFORMIER KUNDE", "INFORM" +
           " PELANGGAN", "MÜŞTERİYİ BİLGİLENDİR"),
        TextId.TEXT_PAGE_ORDER to listOf("餐类入单", "BESTELLEN MET KEUZEPAGINA", "ORDER WITH PAGES", "ORDER MIT PAGINA",
            "ORDER DENGAN PESANAN", "SAYFALARLA SİPARİŞ"),
        TextId.TEXT_PAID to listOf("一切都付了", "AL BETAALD", "PAID", "BEZAHLT", "DIBAYARKAN", "ÖDENDİ"),
        TextId.TEXT_PAY to listOf("埋单", "BETALEN", "PAY", "BEZAHLEN", "BAYAR", "ÖDE"),
        TextId.TEXT_PAYED to listOf("已付账单", "BETAALD", "BEZAHLT", "PAYED", "DIBAYAR", "ÖDENDİ"),
        TextId.TEXT_PLUS_1 to listOf("递增", "PLUS 1", "", "", "+1", "+1"),
        TextId.TEXT_PORTION to listOf("部分", "PORTIE", "PORTION", "PORTIE", "MELAYANI", "PORSİYON"),
        TextId.TEXT_PRICE to listOf("价格", "PRIJS", "PRICE", "PREIS", "HARGA", "FİYAT"),
        TextId.TEXT_PRINTER to listOf("打印机", "AFDRUK", "PRINTER", "DRUCKER", "PRINTER", "YAZICI"),
        TextId.TEXT_PRINT_OFFER to listOf("打印报价?", "OFFERTE AFDRUKKEN?", "PRINT OFFER?", "OFFERTE DRUCKEN?",
            "MAU PENAWARAN?", "TEKLİF YAZDIRILSIN MI?"),
        TextId.TEXT_PRINT_QUANTITY to listOf("打印多少?", "AANTAL AFDRUKKEN?", "PRINT HOW,MANY TIMES?", "DRUCK ANZAHL", "PRINTER BERAPA KALI?", "KAÇ TANE YAZDIRILSIN?"),
        TextId.TEXT_PRINT_SLIP to listOf("打印单", "SLIP PRINTER", "SLIP BILL", "SLIP DRUCKER", "PRINTER SLIP", "FİŞ YAZDIR"),
        TextId.TEXT_PRINT_ROLL to listOf("卷打印机", "REKENING ROL PRINTER" , "PRINT ROLL BILL", "ROLL DRUCKER", "PRINTER PERAN NOTA", "RULO YAZDIR"),
        TextId.TEXT_QUANTITY to listOf("数量", "HOEVEELHEID", "QUANTITY", "QUANTITAT", "KUANTITAS", "MİKTAR"),
        TextId.TEXT_RECHAUD to listOf("炭烧盘", "RECHAUD", "RECHAUD", "RECHAUD", "RECHAUD", "REŞO"),
        TextId.TEXT_REMARK to listOf("写备注", "OPMERKING", "REMARK", "NOTITZEN", "KETERANGAN", "NOT"),
        TextId.TEXT_REPRINT to listOf("重印", "HERDRUK", "REPRINT", "NEUDRUCK", "PRINT LAGI", "YENİDEN YAZDIR"),
        TextId.TEXT_TABLE to listOf("餐桌", "TAFEL", "TABLE", "TISCH", "MEJA", "MASA"),
        TextId.TEXT_TABLE_OVERVIEW to listOf("输入单", "TAFEL OVERZICHT", "CHOOSE TABLE TO ORDER", "TISCH NAME", "PILIH MEJA PESANAN", "MASA GENEL BAKIŞ"),
        TextId.TEXT_TAKEAWAY to listOf("新打包", "AFHALEN", "TAKEAWAY", "MITNEHMEN", "CINA TAKE AWAY", "PAKET SERVİS"),
        TextId.TEXT_TELEPHONE to listOf("电话", "TEL.", "TEL.", "TEL.", "TEL.", "TEL."),
        TextId.TEXT_TOTAL to listOf( "总数", "TOTAAL", "TOTAL", "TOTAL", "JUMLAH", "TOPLAM"),
        TextId.TEXT_TRANSPORT to listOf("转结燎", "TRANSPORT", "TRANSPORT", "TRANSPORT", "TRANSPORTASI", "TRANSFER"),
        TextId.TEXT_REMOVE_ITEM to listOf("删除", "WEGHALEN", "REMOVE", "ENTFERNEN", "DIHAPUS", "SİL"),
        TextId.TEXT_SEND_ALL to listOf("发送", "VERZEND ORDERS", "SEND ORDERS", "UBERTRAGEN", "MENGIRIMKAN", "TÜMÜNÜ GÖNDER"),
        TextId.TEXT_SEND_BILL to listOf("银行", "OP REKENING", "ACCOUNT", "ACCOUNT", "POS", "HESABA GÖNDER"),
        TextId.TEXT_SENDING_TABLES to listOf("互联网，请稍候", "BEZIG MET INTERNET\nWACHT TOT DE TAFEL VERSTUURD IS.", "SEND ORDERS, PLEASE WAIT", "WARTEN BITTE", "TUNGGU,INTERNET", "SİPARİŞ GÖNDERİLİYOR, LÜTFEN BEKLEYİN"),
        TextId.TEXT_SETTINGS to listOf("改变配置", "VERANDER CONFIGURATIE", "CHANGE CONFIGURATION", "ANDERN EINSTELLUNGEN", "GANTI KONFIGURASI", "AYARLAR"),
        TextId.TEXT_SHOP to listOf("商店", "WINKEL", "SHOP", "GESCHÄFT", "TOKO", "MAĞAZA"),
        TextId.TEXT_STOP to listOf("停止", "STOP", "STOP", "STOP", "STOP", "DUR"),
        TextId.TEXT_STOP_PROGRAM to listOf("停止软件", "STOP PROGRAMMA", "STOP PROGRAM", "PROGRAM STOP", "STOP PROGRAM", "PROGRAMI DURDUR"),
        TextId.TEXT_SUBTOTAL to listOf("合计", "SUBTOTAAL", "SUBTOTAL", "SUBTOTAL", "KHUSUS", "ARA TOPLAM"),
        TextId.TEXT_UPDATE to listOf("更新", "UPDATE", "UPDATE", "UPDATE", "UPDATE", "GÜNCELLE"),
        TextId.TEXT_UPDATE_MENU to listOf("菜单更新", "UPDATE MENU KAART", "UPDATE MENU CARD", "MENU UPDATE", "MENU " +
           "PEMBARUAN", "MENÜYÜ GÜNCELLE"),
        TextId.TEXT_WOK to listOf("炒锅", "WOK", "WOK", "WOK", "WAJAN", "WOK")
    )

    private const val DEFAULT_STRING = "??";

    fun get(id: TextId): String {
        val lang = Global.getInstance().language.value
        return m_lin[id]?.getOrElse(lang-1) { DEFAULT_STRING } ?: DEFAULT_STRING
    }

    fun get(id: TextId, lang: ETaal): String
    {
        return m_lin[id]?.getOrElse(lang.value - 1) { DEFAULT_STRING } ?: DEFAULT_STRING
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
                ETaal.LANG_INDONESIAN -> ETaal.LANG_TURKISH
                // You'll need to add logic for when to switch to Turkish here
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
                ETaal.LANG_TURKISH -> try {
                    if (CFG.getOption("turkish")) break
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                // Also add a case for Turkish here
                else -> {}
            }
        }
        if (global.language != ETaal.LANG_SIMPLIFIED)
            global.euroLang = global.language
        return global.language
    }
}
