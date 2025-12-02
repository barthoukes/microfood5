/*============================================================================*/
/** @file     kitchen_printer.kt
 ** @brief    Handle kitchen details.
 **
 ** Wait for kitchen printer.
 **
 ** @author   mensfort
 */
/*------------------------------------------------------------------------------
 ** Copyright (c) Bart Houkes, 8 apr 2011
 **
 ** Copyright notice:
 ** This software is property of Bart Houkes.
 ** Unauthorized duplication and disclosure to third parties is forbidden.
 **===========================================================================*/

package com.hha.printer

import com.hha.common.ItemLocation
import com.hha.dialog.Translation
import com.hha.dialog.Translation.TextId
import com.hha.framework.CItem
import com.hha.framework.CSortedItem
import com.hha.framework.CSortedItemList
import com.hha.framework.CTransaction
import com.hha.grpc.GrpcServiceFactory
import com.hha.resources.Global
import com.hha.resources.CTimestamp
import com.hha.types.CMoney
import com.hha.types.EItemLocation
import com.hha.printer.BillMessage
import com.hha.types.EItemSort
import com.hha.types.ETaal
import com.hha.types.ETimeFrameIndex
import com.hha.printer.EPrinterType
import com.hha.printer.EPrinterLocation
import com.hha.types.EOrderLevel
import com.hha.types.ETaxType
import com.hha.types.ETransType
import java.util.Locale

class KitchenPrinter(
   transactionId: Int,
   timeFrameIndex: ETimeFrameIndex,
   timeDelay: Int,
   collectPrinter: Boolean
)
{
   private var m_printQuantity: Boolean = true
   private var m_kitchenChinese: Boolean = false
   private var m_kitchenLocal: Boolean = false
   private var m_enableCollectPrinter: Boolean = collectPrinter
   private var m_printKitchenSorted: Boolean = false
   private val global = Global.getInstance()
   private val CFG = global.CFG
   private val userCFG = global.userCFG
   private var m_euroLang: ETaal = global.euroLang
   private var m_printLang: ETaal = global.euroLang
   private var m_timeFrameIndex: ETimeFrameIndex = timeFrameIndex
   private var m_orderNumberPrinted: Boolean = false
   private var m_timeDelay: Int = timeDelay
   private var m_quantity: Int = 1
   private var m_takeaway: Boolean = false
   private var m_rechaud: Boolean = false
   private var m_phone: Boolean = false
   private var m_deletedBlack: Boolean = false
   private var m_enableCutPrinter: Boolean = false
   private var m_chineseFirst: Boolean = false

   private var m_transaction: CTransaction = CTransaction(transactionId, EItemSort.SORT_NONE, timeFrameIndex)
   private var m_transactionId: Int = transactionId
   private var m_onlyText: Boolean = !CFG.getBoolean("kitchen_chinese")
   private var m_takeawayPrintAll: Boolean = CFG.getBoolean("kitchen_takeaway_print_all")
   private var m_alias: Boolean = CFG.getValue("kitchen_alias") > 0
   private var m_firstAlias: Boolean = CFG.getValue("kitchen_first_alias") > 0
   private var m_lineItemLine: Int = CFG.getValue("kitchen_horizontal_line_line")
   private var m_linespace: Int = CFG.getValue("kitchen_linespace")
   private var m_linespaceBetweenArticles: Int = CFG.getValue("kitchen_linespace_between_articles")
   private var m_linespaceBetweenLanguages: Int = CFG.getValue("kitchen_linespace_between_languages")
   private var m_linespaceInArticle: Int = CFG.getValue("kitchen_linespace_article")
   private var m_overrideLocation: EPrinterLocation = EPrinterLocation.PRINTER_LOCATION_AUTOMATIC_PRN

   private val m_kitchenTextFont = CFG.getValue("kitchen_text_font")
   private val m_kitchenTextHeight = CFG.getValue("kitchen_text_height")
   private val m_kitchenTextWidth = CFG.getValue("kitchen_text_width")
   private val m_table = Translation.get(TextId.TEXT_TABLE, m_euroLang)
   private val m_telephone = Translation.get(TextId.TEXT_TELEPHONE, m_euroLang)
   private val m_textAsap = Translation.get(TextId.TEXT_ASAP, m_euroLang)
   private val m_textDeliverTime = Translation.get(TextId.TEXT_DELIVER_TIME, m_euroLang)
   private val m_textDiscount = Translation.get(TextId.TEXT_DISCOUNT, m_euroLang)
   private val m_textEatInside = Translation.get(TextId.TEXT_EAT_INSIDE, m_euroLang)
   private val m_textKey = Translation.get(TextId.TEXT_KEY, m_euroLang)
   private val m_textList = Translation.get(TextId.TEXT_LIST, m_euroLang)
   private val m_textTable = Translation.get(TextId.TEXT_TABLE, m_euroLang)
   private val m_textTelephone = Translation.get(TextId.TEXT_TELEPHONE, m_euroLang)
   private val m_textTotal = Translation.get(TextId.TEXT_TOTAL, m_euroLang)
   private val m_textRechaud = Translation.get(TextId.TEXT_RECHAUD, m_euroLang)
   private val m_textReprint = Translation.get(TextId.TEXT_REPRINT, m_euroLang)
   private val m_textSubtotal = Translation.get(TextId.TEXT_SUBTOTAL, m_euroLang)
   private val m_CBM231 = EPrinterType.CBM231.value
   private val m_SP200 = EPrinterType.SP200.value
   private val m_TEST_PRINTER = EPrinterType.TEST_PRINTER.value
   private val m_TM290 = EPrinterType.TM290.value
   private val m_TM295 = EPrinterType.TM295.value

   private var m_total: CMoney = CMoney(0)
   private var m_billMessage: String = ""
   private var m_delay = CTimestamp()
   private var m_sortedItems = CSortedItemList()
   private var m_counter = GrpcServiceFactory.createDailyTimeFrameService()

   companion object
   {
      var m_redColour: Boolean = false
   }

   init
   {
      if (m_linespaceBetweenArticles < 0)
      {
         m_linespaceBetweenArticles = m_linespace
      }
      if (m_linespaceInArticle < 0)
      {
         m_linespaceInArticle = m_linespace
      }
      if (m_linespaceBetweenLanguages < 0)
      {
         m_linespaceBetweenLanguages = m_linespace
      }

      m_printKitchenSorted = when
      {
         userCFG.getValue("user_print_kitchen_sorted") > 2 -> !CFG.getBoolean("kitchen_unsorted")
         else -> userCFG.getValue("user_print_kitchen_sorted") > 0
      }

      m_kitchenLocal = when
      {
         userCFG.getValue("user_print_kitchen_local") > 2 -> CFG.getValue("kitchen_local") > 0
         else -> userCFG.getValue("user_print_kitchen_local") > 0
      }

      if (!m_alias)
      {
         m_firstAlias = false
      }
      if (!m_onlyText)
      {
         m_kitchenChinese = true
      }
      if (CFG.getValue("kitchen_first_chinese") > 0)
      {
         m_chineseFirst = true
      }
   }

   fun forceToPrinter(location: EPrinterLocation)
   {
      m_overrideLocation = location
   }

   fun print(location: EItemLocation, printer: EPrinterLocation, quantity: Int)
   {
      val pr = CFG.getValue("kitchen_print_per_item")

      m_enableCutPrinter = ((pr and 1) != 0 && location == EItemLocation.ITEM_KITCHEN) ||
         ((pr and 2) != 0 && location == EItemLocation.ITEM_KITCHEN2) ||
         ((pr and 4) != 0 && location == EItemLocation.ITEM_KITCHEN3) ||
         ((pr and 8) != 0 && location == EItemLocation.ITEM_KITCHEN4) ||
         ((pr and 16) != 0 && location == EItemLocation.ITEM_KITCHEN5)

      var q = quantity
      while (q-- > 0)
      {
         print(location, printer)
      }
   }

   fun print(locationId: EItemLocation, printer: EPrinterLocation)
   {
      var printLang = if (m_kitchenLocal) m_euroLang else ETaal.LANG_SIMPLIFIED
      m_printLang = printLang
      m_euroLang = ETaal.LANG_DUTCH // Assuming EuroLang is available

      var actualPrinter = printer
      if (printer == EPrinterLocation.PRINTER_LOCATION_AUTOMATIC_PRN)
      {
         actualPrinter = EPrinterLocation.fromItemLocation(locationId)
      }

      if ((actualPrinter == EPrinterLocation.PRINTER_LOCATION_KITCHEN_PRN1 ||
            actualPrinter == EPrinterLocation.PRINTER_LOCATION_KITCHEN_PRN2 ||
            actualPrinter == EPrinterLocation.PRINTER_LOCATION_BILL_PRN ||
            actualPrinter == EPrinterLocation.PRINTER_LOCATION_KITCHEN_PRN3 ||
            actualPrinter == EPrinterLocation.PRINTER_LOCATION_KITCHEN_PRN4 ||
            actualPrinter == EPrinterLocation.PRINTER_LOCATION_KITCHEN_COLLECT_PRN) &&
         CFG.getValue("kitchen_print") == 0
      )
      {
         return
      }
      if (actualPrinter == EPrinterLocation.PRINTER_LOCATION_KITCHEN_COLLECT_PRN && !m_enableCollectPrinter)
      {
         return
      }

      val startIndex = getStartIndex()

      if (!m_sortedItems.setTransaction(
            m_transactionId.toLong(), startIndex,
            m_timeFrameIndex, locationId, true
         )
      )
      {
         return
      }

      val splitPoint = m_sortedItems.splitPoint
      val f1 = PrinterFile()
      f1.clear()

      var size = m_sortedItems.sortAndTryToMerge(m_printKitchenSorted, splitPoint)
      if (size > 0 && splitPoint > 0)
      {
         size = m_sortedItems.sortAndTryToMerge(m_printKitchenSorted, 0)
      }

      if (size > 0)
      {
         printItems(f1, locationId)
         val printerLocation =
            if (m_overrideLocation == EPrinterLocation.PRINTER_LOCATION_AUTOMATIC_PRN) actualPrinter else
               m_overrideLocation
         val service = GrpcServiceFactory.createTicketPrinterService()
         service.addRawText(
            printerLocation.toPrinterLocation(),
            f1.getBuffer(), Priority.PRIORITY_5, 1
         )
      }
   }

   private fun getStartIndex(): ETimeFrameIndex
   {
      var timeFrameIndex = m_timeFrameIndex
      val transType = m_transaction.transType

      if (m_takeawayPrintAll &&
         (transType == ETransType.TRANS_TYPE_TAKEAWAY || transType == ETransType.TRANS_TYPE_TAKEAWAY_PHONE)
      )
      {
         timeFrameIndex.index = ETimeFrameIndex.TIME_FRAME0
      }
      return timeFrameIndex
   }

   private fun printItems(F: PrinterFile, location: EItemLocation)
   {
      m_total = CMoney(0)
      val size = m_sortedItems.size

      for (p in 0 until size)
      {
         val item: CSortedItem = m_sortedItems.getSortedItem(p)
         if (item == null)
         {
            continue;
         }
         val mainItem = item[0]

         if (mainItem.paperCutPerItem && m_enableCutPrinter)
         {
            var quantity = mainItem.getQuantity()
            val sizeOne = if (quantity > 0) 1 else -1

            while (quantity != 0)
            {
               m_printQuantity = false
               val idx = m_counter.nextKitchenIndex(m_transactionId, m_timeFrameIndex.index)

               var headerVal = 0
               if (userCFG.getValue("user_print_kitchen_index") > 0)
               {
                  headerVal = idx
               }

               if (userCFG.getValue("user_print_kitchen_index") > 0) idx else 0
               printHeader(F, location, if (userCFG.getValue("user_print_kitchen_index") > 0) idx else 0)
               printExtra(F, idx)

               var chinese = m_kitchenChinese
               if (chinese && m_kitchenLocal && mainItem.chinesePrinterName == mainItem.localPrinterName)
               {
                  chinese = false
               }

               val printLocal = m_kitchenLocal || m_onlyText
               var printChinese = !m_chineseFirst && chinese
               var distance = m_linespaceBetweenLanguages

               if (m_chineseFirst && chinese)
               {
                  if (!printLocal)
                  {
                     distance = m_linespaceBetweenArticles
                  }
                  printOrder(F, item, true, true, distance)
               }
               if (printLocal)
               {
                  if (!printChinese)
                  {
                     distance = m_linespaceBetweenArticles
                  }
                  printOrder(F, item, false, true, distance)
               }
               if (printChinese)
               {
                  printOrder(F, item, true, true, m_linespaceBetweenArticles)
               }

               m_total = m_total + item.getTotal()
               printFooter(F)
               m_printQuantity = true
               quantity -= sizeOne
            }
         }
      }

      var valid = false
      m_printQuantity = true

      for (p in 0 until size)
      {
         val item = m_sortedItems.getSortedItem(p)
         val mainItem = item[0]

         if (!mainItem.paperCutPerItem || !m_enableCutPrinter)
         {
            if (!valid)
            {
               val idx = m_counter.nextKitchenIndex(m_transactionId, m_timeFrameIndex.index)
               printHeader(F, location, if (userCFG.getValue("user_print_kitchen_index") > 0) idx else 0)
               printExtra(F, idx)
               valid = true
            }

            var chinese = m_kitchenChinese
            if (chinese && m_kitchenLocal && mainItem.chinesePrinterName == mainItem.localPrinterName)
            {
               chinese = false
            }

            val printLocal = m_kitchenLocal || m_onlyText
            var printChinese = !m_chineseFirst && chinese
            var distance = m_linespaceBetweenLanguages

            if (m_chineseFirst && chinese)
            {
               if (!printLocal)
               {
                  distance = m_linespaceBetweenArticles
               }
               printOrder(F, item, true, false, distance)
            }
            if (printLocal)
            {
               if (!printChinese)
               {
                  distance = m_linespaceBetweenArticles
               }
               printOrder(F, item, false, false, distance)
            }
            if (printChinese)
            {
               printOrder(F, item, true, false, m_linespaceBetweenArticles)
            }

            m_total = m_total + item.getTotal()
         }
      }

      if (valid)
      {
         if (CFG.getValue("kitchen_transaction_total") > 0)
         {
            val total = m_transaction.getTotal(ETaxType.BTW_TYPE_LOW) + m_transaction.getTotal(ETaxType.BTW_TYPE_HIGH)
            m_total = total
         }
         printTotal(F, m_total)
         printFooter(F)
      }
   }

   private fun printOrder(F: PrinterFile, item: CSortedItem, chinese: Boolean, perItem: Boolean, linespaceAfter: Int)
   {
      val spices_local = item.getPrinterSpices(ETaal.LANG_DUTCH, perItem)
      val extras_local = item.getPrinterExtras(ETaal.LANG_DUTCH, perItem)
      val spices_chinese = item.getPrinterSpices(ETaal.LANG_SIMPLIFIED, perItem)
      val extras_chinese = item.getPrinterExtras(ETaal.LANG_SIMPLIFIED, perItem)

      print1Order(F, item[0], spices_local, extras_local, spices_chinese, extras_chinese, chinese, linespaceAfter)
   }

   private fun printTotal(F: PrinterFile, subtotal: CMoney)
   {
      if (CFG.getValue("kitchen_total") == 0) return

      F.write("[Line:16]\n")
      val discount = m_transaction.getDiscount()
      val total = subtotal - discount
      var str: String

      if (m_printLang == ETaal.LANG_SIMPLIFIED || m_printLang == ETaal.LANG_TRADITIONAL)
      {
         if (!discount.empty())
         {
            str = subtotal.toString(0, CMoney.MONEY_LINE)
            F.write(m_textSubtotal)
            val strLen = str.length
            F.write("[subtab:::$strLen]$str\n")
            str = discount.toString(0, CMoney.MONEY_LINE)
            F.write(m_textDiscount)
            F.write("[subtab:::$str]$str\n")
         }
         str = total.toString(0, CMoney.MONEY_LINE)
         F.write(m_textTotal)
         F.write("[subtab:::$str]$str\n")
      } else
      {
         F.write("[Text|Height:2|Width:2]")
         if (!discount.empty())
         {
            str = subtotal.toString(0, CMoney.MONEY_LINE)
            F.write(m_textSubtotal)
            F.write("[subtab:::$str]$str\n")
            str = discount.toString(0, CMoney.MONEY_LINE)
            F.write(m_textDiscount)
            F.write("[subtab:::$str]$str\n")
         }
         str = total.toString(0, CMoney.MONEY_LINE)
         val strLen = str.length
         F.write("[Text|Height:2|Width:2]$m_textTotal[subtab::$strLen]$str\n")
      }
   }

   private fun printColours(F: PrinterFile)
   {
      val isTakeaway = m_transaction.isTakeaway()

      when
      {
         CFG.getValue("kitchen_takeaway_red") > 0 && isTakeaway ->
         {
            F.write(if (CFG.getValue("kitchen_takeaway_red") > 1) "[black]" else "[red]")
         }

         CFG.getValue("kitchen_sitin_red") > 0 && !isTakeaway ->
         {
            F.write(if (CFG.getValue("kitchen_sitin_red") > 1) "[black]" else "[red]")
         }

         CFG.getValue("kitchen_added_food_red") > 0 && isTakeaway && m_timeFrameIndex.index > 1 ->
         {
            F.write(if (m_timeFrameIndex.index > 1) "[red]" else "[black]")
         }

         CFG.getValue("kitchen_change_colours") > 0 ->
         {
            m_redColour = !m_redColour
            F.write(if (m_redColour) "[red]" else "[black]")
         }

         else ->
         {
            F.write("[black]")
         }
      }
   }

   private fun printExtra(F: PrinterFile, index: Int)
   {
      if (!m_takeaway || index <= 1)
      {
         return
      }

      val extra = CFG.getValue("kitchen_text_print_extra")
      when (extra)
      {
         1 ->
         {
            val idx = m_timeFrameIndex.index
            if (idx <= 1) return
         }

         2 ->
         { /* Continue */
         }

         3 ->
         {
            if (index > 1)
            {
               F.write("time-frame-index $index\n")
            }
         }

         else -> return
      }

      val txt = if (CFG.getValue("kitchen_chinese") > 0)
      {
         "[graphics]" + Translation.get(Translation.TextId.TEXT_EXTRA, ETaal.LANG_SIMPLIFIED)
      } else
      {
         "[height:2|width:2]" + Translation.get(Translation.TextId.TEXT_EXTRA, ETaal.LANG_DUTCH)
      }
      F.write(txt)
      if (index > 1)
      {
         F.write(" ($index):\n")
      }
      F.write("\n")
   }

   private fun printTime(F: PrinterFile)
   {
      var done = false
      val transType = m_transaction.transType
      val lang = if (CFG.getValue("kitchen_chinese") > 0) ETaal.LANG_SIMPLIFIED else m_euroLang

      if (transType == ETransType.TRANS_TYPE_TAKEAWAY_PHONE)
      {
         m_delay = CTimestamp(m_transaction.customerTime)
      } else
      {
         m_delay = CTimestamp(m_transaction.deliverTime)
      }

      if (m_delay.year > 2000)
      {
         val time = if (m_delay.isAsap(CFG.getValue("kitchen_print_asap") > 0))
         {
            m_textAsap
         } else
         {
            m_delay.getShortTime()
         }
         done = true

         if (CFG.getValue("kitchen_chinese") > 0)
         {
            F.write("[graphics|font:deliver]$m_textDeliverTime $time\n\n")
         } else
         {
            F.write("[height:2]$m_textDeliverTime [width:2]$time[width:1|height:1]\n\n")
         }
      }
      if (!done && CFG.getValue("kitchen_print_asap") > 0)
      {
         F.write("[graphics|font:deliver]$m_textAsap\n\n")
      }
   }

   private fun printHeader(F: PrinterFile, location: EItemLocation, billNumber: Int)
   {
      val lines = CFG.getValue("kitchen_header_lines")
      if (lines > 0)
      {
         F.write("[text]")
         for (i in 0 until lines)
         {
            F.write(" \n")
         }
      }
      if (CFG.getValue("kitchen_header") == 0)
      {
         return
      }

      F.write("[Linespace:$m_linespace]")
      for (x in 0 until CFG.getValue("kitchen_beeps"))
      {
         F.write("[ring]")
      }
      F.write("[graphics|font:waiter]")

      if (CFG.getValue("print_reroute") > 0)
      {
         F.write("[Reprint|Font:16]$m_textReprint\n[Ifn:$m_TM290|Ifn:$m_TM295|CUT]\n[endif|endif|endif]")
      }

      printColours(F)

      m_billMessage = m_transaction.getBillRemarks()
      if (m_billMessage.length > 1)
      {
         val kitchen_height = CFG.getValue("kitchen_message_size")
         if (kitchen_height > 3)
         {
            F.write("[graphics|font:tel]")
         } else
         {
            F.write("[text|font:1]")
            if (kitchen_height and 1 != 0)
            {
               F.write("[height:2]")
            }
            if (kitchen_height and 2 != 0)
            {
               F.write("[width:2]")
            }
         }
         F.write("$m_billMessage\n\n")
      }

      F.write("[text|width:1|height:1|font:1]")
      printTime(F)
      F.write(String.format(Locale.ROOT, "#%07d", m_transactionId))

      if (CFG.getValue("kitchen_print_waiter") > 0)
      {
         if (CFG.getValue("entry_with_key") > 0)
         {
            val s = m_transaction.getEmployeeName()
            if (s.isNotEmpty())
            {
               F.write("[right:$m_textKey $s]")
            }
         }
      }

      F.write("\n[font:header]")
      printTafel(F, location, billNumber)

      if (CFG.getValue("kitchen_chinese") > 0)
      {
         F.write("[Width:1|Height:1|font:kitchen_chinese]")
      } else
      {
         F.write("[Text|Font:0|ifchars:29|Width:2|Endif]")
         F.write("[text]")
      }
   }

   private fun print1Order(
      F: PrinterFile,
      order: CItem,
      spices_local: String,
      extras_local: String,
      spices_chinese: String,
      extras_chinese: String,
      chinese: Boolean,
      linespaceAfter: Int
   )
   {
      if (chinese)
      {
         F.write("[ifn:$m_SP200|ifn:$m_TEST_PRINTER|linespace:$m_linespaceInArticle]")
         printOrderChineseDetail(F, order, spices_chinese, extras_chinese, linespaceAfter)
         F.write("[endif|endif]")

         F.write("[if:$m_TEST_PRINTER|text]")
         printLocalOrderTextmode(F, order, order.chinesePrinterName, spices_chinese, extras_chinese, linespaceAfter)
         F.write("[endif]")
      } else
      {
         if (CFG.getValue("kitchen_chinese") > 0)
         {
            F.write("[If:$m_SP200]")
         }
         printLocalOrderTextmode(F, order, order.localPrinterName, spices_local, extras_local, linespaceAfter)
         if (CFG.getValue("kitchen_chinese") > 0)
         {
            F.write("[endif]")
         }
         if (CFG.getValue("kitchen_chinese") > 0)
         {
            F.write("[ifn:$m_SP200|ifn:$m_TEST_PRINTER]")
            printLocalOrderGraphmode(F, order, spices_local, extras_local, linespaceAfter)
            F.write("[endif|endif]")

            F.write("[if:$m_TEST_PRINTER|text]")
            printLocalOrderTextmode(F, order, order.localPrinterName, spices_local, extras_local, linespaceAfter)
            F.write("[endif]")
         }
      }
   }

   private fun printTafel(F: PrinterFile, location: EItemLocation, billNumber: Int)
   {
      val name = m_transaction.displayName
      var s: String
      val printTime = true
      val sub_nr: String

      sub_nr = if (billNumber > 0)
      {
         if (location == EItemLocation.ITEM_KITCHEN5)
         {
            " < $billNumber >\n"
         } else
         {
            "-$billNumber\n"
         }
      } else
      {
         "\n"
      }

      m_rechaud = m_transaction.isRechaud()
      m_takeaway = m_transaction.isTakeaway()
      m_phone = m_transaction.isTelephone()

      m_deletedBlack = CFG.getValue("kitchen_takeaway_red") > 0 && (m_takeaway || m_phone) &&
         CFG.getValue("kitchen_change_colours") == 0

      if (CFG.getValue("kitchen_line") > 0)
      {
         F.write("[Line:12]\n")
      }

      F.write("[Text|Font:1|Height:2|Width:2|Ifnchars:29|Font:0|Endif]")

      when
      {
         m_rechaud ->
         {
            val nr = m_transaction.TAcount()
            F.write("[Ifchars:29]\t[endif]$m_textRechaud")
            if (nr > 0)
            {
               F.append(String.format(Locale.ROOT, ": %02d%s", nr, sub_nr))
            } else
            {
               F.write(": $sub_nr")
            }
         }

         m_takeaway ->
         {
            val tstr = when
            {
               CFG.getValue("takeaway_mode") > 0 -> m_textList
               m_transaction.transType == ETransType.TRANS_TYPE_TAKEAWAY ||
                  m_transaction.transType == ETransType.TRANS_TYPE_TAKEAWAY_PHONE ->
                  Translation.get(TextId.TEXT_TAKEAWAY, m_euroLang)

               else -> m_textEatInside
            }
            val takeaway_counter = m_transaction.TAcount()
            F.write("[Ifchars:29]\t[endif]$tstr")

            if (takeaway_counter > 0)
            {
               F.write(": $name$sub_nr")
            } else
            {
               F.write(": $sub_nr")
            }
         }

         m_phone ->
         {
            F.write("$m_textTelephone $name\n")
            s = m_transaction.getClient()
            if (s.length > 2)
            {
               F.write("[height:1|width:1]\n$s\n")
            }
         }

         m_transaction.isShop() ->
         {
            val ptr = m_transaction.getClient()
            F.write(ptr)
         }

         else ->
         {
            s = m_transaction.getClient()
            if (s.length > 2)
            {
               F.write("$m_textTelephone $name\n")
               F.write("[height:1|width:1]\n$s$sub_nr")
            } else
            {
               F.write("[Ifchars:29]\t[endif]$m_textTable: $name$sub_nr")
            }
         }
      } // when

      val a = BillMessage()
      val str = a.print(0, 0)
      F.write(str)

      if (CFG.getValue("kitchen_time") > 0 && printTime)
      {
         F.write("[font:waiter|Ifchars:29]\t[endif|time] [date]\n")
      }
      F.write("[Line:16|ifn:$m_SP200|ifn:$m_CBM231]\n[endif|endif]")
      F.write("[Width:1|Graphics]")
   }

   private fun printOrderChineseDetail(
      F: PrinterFile,
      order: CItem,
      spices: String,
      extras: String,
      linespaceAfter: Int
   )
   {
      F.write("[Linespace:$m_linespaceInArticle]")
      if (order.level == EOrderLevel.LEVEL_ITEMGROUP && m_lineItemLine > 0)
      {
         F.write("[width:1|height:1|font:kitchen_h1]")
         F.write("[line:$m_lineItemLine]\n")
         if (order.localPrinterName.isEmpty() && order.chinesePrinterName.isEmpty())
         {
            return
         }
      } else
      {
         F.write("[width:1|height:1|font:kitchen_chinese]")
      }

      if (order.level != EOrderLevel.LEVEL_ITEMGROUP && m_printQuantity)
      {
         val q = order.getQuantity()
         F.write("${q}x")
      }
      F.write("[tab::99x ]")

      if (m_firstAlias)
      {
         if (m_alias)
         {
            F.write(order.alias)
            F.write(" [tab::99x ABC]")
         }
         F.wordsDotOut(order.chinesePrinterName, "99x ", false)
         if (spices.isNotEmpty() || extras.isNotEmpty())
         {
            F.write("[font:kitchen_extra_chinese]")
            F.wordsDotOut(spices, "99x ", true)
            F.wordsDotOut(extras, "99x ", true)
         }
      } else
      {
         F.wordsDotOut(order.chinesePrinterName, "99x ", false)
         if (spices.isNotEmpty() || extras.isNotEmpty())
         {
            F.write("[font:kitchen_extra_chinese]")
            F.wordsDotOut(spices, "99x ", true)
            F.wordsDotOut(extras, "99x ", true)
            F.write("[font:kitchen_chinese]")
         }
         if (m_alias)
         {
            F.write("[right:${order.alias}]")
         }
      }
      F.write("[linespace:$linespaceAfter]\n")
   }

   private fun printLocalOrderTextmode(
      F: PrinterFile,
      order: CItem,
      localPrinterName: String,
      spices: String,
      extras: String,
      linespaceAfter: Int
   )
   {
      F.write("[Linespace:$m_linespaceInArticle]")
      F.write("[font:$m_kitchenTextFont|height:$m_kitchenTextHeight|width:$m_kitchenTextWidth]")

      if (order.level != EOrderLevel.LEVEL_ITEMGROUP && m_printQuantity)
      {
         val q = order.getQuantity()
         F.write("${q}x")
      }

      F.write("[tab:3|ifchars:26|tab:5|Endif]")
      if (m_firstAlias && m_alias)
      {
         F.write("${order.alias}[tab:8|ifchars:21|tab:10|endif]")
      }

      F.wordsCharOut(localPrinterName, 5, 0, m_kitchenTextWidth, false)
      F.wordsCharOut(spices, 5, 0, m_kitchenTextWidth, true)
      F.wordsCharOut(extras, 5, 0, m_kitchenTextWidth, true)

      if (!m_firstAlias && m_alias)
      {
         var len = order.alias.length
         if (CFG.getValue("kitchen_text_width") > 0)
         {
            len *= 2
         }
         val alias = order.alias
         F.write("[ifnchars:$len]\n[subtab:$len|endif]$alias")
      }
      F.write("[linespace:linespaceAfter]\n")
   }

   private fun printLocalOrderGraphmode(
      F: PrinterFile,
      order: CItem,
      spices: String,
      extras: String,
      linespaceAfter: Int
   )
   {
      F.write("[Linespace:$m_linespaceInArticle]")
      F.write("[font:kitchen_local|Height:1|width:1]")

      if (order.level != EOrderLevel.LEVEL_ITEMGROUP && m_printQuantity)
      {
         val q = order.getQuantity()
         F.write("${q}x")
      }
      F.write("[tab::99x ]")

      if (m_firstAlias)
      {
         if (m_alias)
         {
            F.write("${order.alias} [tab::99x ABCD]")
         }
         F.wordsDotOut(order.localPrinterName, "99x ", false)
         F.wordsDotOut(spices, "99x ", true)
         F.wordsDotOut(extras, "99x ", true)
      } else
      {
         F.write("[tab::99x ]")
         F.wordsDotOut(order.localPrinterName, "99x ", false)
         if (spices.isNotEmpty())
         {
            F.write(" ")
            F.wordsDotOut(spices, "99x ", true)
         }
         if (extras.isNotEmpty())
         {
            F.write(" ")
            F.wordsDotOut(extras, "99x ", true)
         }
         if (m_alias)
         {
            F.write("[right:${order.alias}]")
         }
      }
      F.write("[linespace:$linespaceAfter]\n")
   }

   private fun printFooter(F: PrinterFile)
   {
      F.write("[font:default]")
      F.write("[transaction:${m_transaction.transactionId}]")
      val s = CFG.getString("kitchen_footer")
      F.write(s)
      F.write("[If:$m_SP200]\n\n\n\n[Endif]\n\n\n[cut|black]\n")
   }
}