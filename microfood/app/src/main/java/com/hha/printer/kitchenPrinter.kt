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
import com.hha.types.EItemSort
import com.hha.types.ETaal
import com.hha.types.ETimeFrameIndex
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
   private var mPrintQuantity: Boolean = true
   private var mKitchenChinese: Boolean = false
   private var mKitchenLocal: Boolean = false
   private var mEnableCollectPrinter: Boolean = collectPrinter
   private var mPrintKitchenSorted: Boolean = false
   private val global = Global.getInstance()
   private val CFG = global.CFG
   private val userCFG = global.userCFG
   private var mEuroLang: ETaal = global.euroLang
   private var mPrintLang: ETaal = global.euroLang
   private var mTimeFrameIndex: ETimeFrameIndex = timeFrameIndex
   private var mOrderNumberPrinted: Boolean = false
   private var mTimeDelay: Int = timeDelay
   private var mQuantity: Int = 1
   private var mTakeaway: Boolean = false
   private var mRechaud: Boolean = false
   private var mPhone: Boolean = false
   private var mDeletedBlack: Boolean = false
   private var mEnableCutPrinter: Boolean = false
   private var mChineseFirst: Boolean = false

   private lateinit var mTransaction: CTransaction
   private var mTransactionId: Int = transactionId
   private var mOnlyText: Boolean = !CFG.getBoolean("kitchen_chinese")
   private var mTakeawayPrintAll: Boolean = CFG.getBoolean("kitchen_takeaway_print_all")
   private var mAlias: Boolean = CFG.getValue("kitchen_alias") > 0
   private var mFirstAlias: Boolean = CFG.getValue("kitchen_first_alias") > 0
   private var mLineItemLine: Int = CFG.getValue("kitchen_horizontal_line_line")
   private var mLinespace: Int = CFG.getValue("kitchen_linespace")
   private var mLinespaceBetweenArticles: Int = CFG.getValue("kitchen_linespace_between_articles")
   private var mLinespaceBetweenLanguages: Int = CFG.getValue("kitchen_linespace_between_languages")
   private var mLinespaceInArticle: Int = CFG.getValue("kitchen_linespace_article")
   private var mOverrideLocation: EPrinterLocation = EPrinterLocation.PRINTER_LOCATION_AUTOMATIC_PRN

   private val mKitchenTextFont = CFG.getValue("kitchen_text_font")
   private val mKitchenTextHeight = CFG.getValue("kitchen_text_height")
   private val mKitchenTextWidth = CFG.getValue("kitchen_text_width")
   private val mTable = Translation.get(TextId.TEXT_TABLE, mEuroLang)
   private val mTelephone = Translation.get(TextId.TEXT_TELEPHONE, mEuroLang)
   private val mTextAsap = Translation.get(TextId.TEXT_ASAP, mEuroLang)
   private val mTextDeliverTime = Translation.get(TextId.TEXT_DELIVER_TIME, mEuroLang)
   private val mTextDiscount = Translation.get(TextId.TEXT_DISCOUNT, mEuroLang)
   private val mTextEatInside = Translation.get(TextId.TEXT_EAT_INSIDE, mEuroLang)
   private val mTextKey = Translation.get(TextId.TEXT_KEY, mEuroLang)
   private val mTextList = Translation.get(TextId.TEXT_LIST, mEuroLang)
   private val mTextTable = Translation.get(TextId.TEXT_TABLE, mEuroLang)
   private val mTextTelephone = Translation.get(TextId.TEXT_TELEPHONE, mEuroLang)
   private val mTextTotal = Translation.get(TextId.TEXT_TOTAL, mEuroLang)
   private val mTextRechaud = Translation.get(TextId.TEXT_RECHAUD, mEuroLang)
   private val mTextReprint = Translation.get(TextId.TEXT_REPRINT, mEuroLang)
   private val mTextSubtotal = Translation.get(TextId.TEXT_SUBTOTAL, mEuroLang)
   private val mCBM231 = EPrinterType.CBM231.value
   private val mSP200 = EPrinterType.SP200.value
   private val mTestPrinter = EPrinterType.TEST_PRINTER.value
   private val mTM290 = EPrinterType.TM290.value
   private val mTM295 = EPrinterType.TM295.value

   private var mTotal: CMoney = CMoney(0)
   private var mBillMessage: String = ""
   private var mDelay = CTimestamp()
   private var mSortedItems = CSortedItemList()
   private var mCounter = GrpcServiceFactory.createDailyTimeFrameService()

   companion object
   {
      var mRedColour: Boolean = false
   }

   init
   {
      if (mLinespaceBetweenArticles < 0)
      {
         mLinespaceBetweenArticles = mLinespace
      }
      if (mLinespaceInArticle < 0)
      {
         mLinespaceInArticle = mLinespace
      }
      if (mLinespaceBetweenLanguages < 0)
      {
         mLinespaceBetweenLanguages = mLinespace
      }

      mPrintKitchenSorted = when
      {
         userCFG.getValue("user_print_kitchen_sorted") > 2 -> !CFG.getBoolean("kitchen_unsorted")
         else -> userCFG.getValue("user_print_kitchen_sorted") > 0
      }

      mKitchenLocal = when
      {
         userCFG.getValue("user_print_kitchen_local") > 2 -> CFG.getValue("kitchen_local") > 0
         else -> userCFG.getValue("user_print_kitchen_local") > 0
      }

      if (!mAlias)
      {
         mFirstAlias = false
      }
      if (!mOnlyText)
      {
         mKitchenChinese = true
      }
      if (CFG.getValue("kitchen_first_chinese") > 0)
      {
         mChineseFirst = true
      }
   }

   suspend fun createAndLoad(transactionId: Int)
   {
      mTransaction = CTransaction.createAndLoad(
         transactionId, EItemSort.SORT_NONE, mTimeFrameIndex)
   }

   fun forceToPrinter(location: EPrinterLocation)
   {
      mOverrideLocation = location
   }

   fun print(location: EItemLocation, printer: EPrinterLocation, quantity: Int)
   {
      val pr = CFG.getValue("kitchen_print_per_item")

      mEnableCutPrinter = ((pr and 1) != 0 && location == EItemLocation.ITEM_KITCHEN) ||
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
      var printLang = if (mKitchenLocal) mEuroLang else ETaal.LANG_SIMPLIFIED
      mPrintLang = printLang
      mEuroLang = ETaal.LANG_DUTCH // Assuming EuroLang is available

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
      if (actualPrinter == EPrinterLocation.PRINTER_LOCATION_KITCHEN_COLLECT_PRN && !mEnableCollectPrinter)
      {
         return
      }

      val startIndex = getStartIndex()

      if (!mSortedItems.setTransaction(
            mTransactionId.toLong(), startIndex,
            mTimeFrameIndex, locationId, true
         )
      )
      {
         return
      }

      val splitPoint = mSortedItems.splitPoint
      val f1 = PrinterFile()
      f1.clear()

      var size = mSortedItems.sortAndTryToMerge(mPrintKitchenSorted, splitPoint)
      if (size > 0 && splitPoint > 0)
      {
         size = mSortedItems.sortAndTryToMerge(mPrintKitchenSorted, 0)
      }

      if (size > 0)
      {
         printItems(f1, locationId)
         val printerLocation =
            if (mOverrideLocation == EPrinterLocation.PRINTER_LOCATION_AUTOMATIC_PRN) actualPrinter else
               mOverrideLocation
         val service = GrpcServiceFactory.createTicketPrinterService()
         service.addRawText(
            printerLocation.toPrinterLocation(),
            f1.getBuffer(), Priority.PRIORITY_5, 1
         )
      }
   }

   private fun getStartIndex(): ETimeFrameIndex
   {
      var timeFrameIndex = mTimeFrameIndex
      val transType = mTransaction.transType

      if (mTakeawayPrintAll &&
         (transType == ETransType.TRANS_TYPE_TAKEAWAY || transType == ETransType.TRANS_TYPE_TAKEAWAY_PHONE)
      )
      {
         timeFrameIndex.index = ETimeFrameIndex.TIME_FRAME0
      }
      return timeFrameIndex
   }

   private fun printItems(F: PrinterFile, location: EItemLocation)
   {
      mTotal = CMoney(0)
      val size = mSortedItems.size

      for (p in 0 until size)
      {
         val item: CSortedItem = mSortedItems.getSortedItem(p)
         if (item == null)
         {
            continue;
         }
         val mainItem = item[0]

         if (mainItem.paperCutPerItem && mEnableCutPrinter)
         {
            var quantity = mainItem.getQuantity()
            val sizeOne = if (quantity > 0) 1 else -1

            while (quantity != 0)
            {
               mPrintQuantity = false
               val idx = mCounter.nextKitchenIndex(mTransactionId, mTimeFrameIndex.index)

               var headerVal = 0
               if (userCFG.getValue("user_print_kitchen_index") > 0)
               {
                  headerVal = idx
               }

               if (userCFG.getValue("user_print_kitchen_index") > 0) idx else 0
               printHeader(F, location, if (userCFG.getValue("user_print_kitchen_index") > 0) idx else 0)
               printExtra(F, idx)

               var chinese = mKitchenChinese
               if (chinese && mKitchenLocal && mainItem.chinesePrinterName == mainItem.localPrinterName)
               {
                  chinese = false
               }

               val printLocal = mKitchenLocal || mOnlyText
               var printChinese = !mChineseFirst && chinese
               var distance = mLinespaceBetweenLanguages

               if (mChineseFirst && chinese)
               {
                  if (!printLocal)
                  {
                     distance = mLinespaceBetweenArticles
                  }
                  printOrder(F, item, true, true, distance)
               }
               if (printLocal)
               {
                  if (!printChinese)
                  {
                     distance = mLinespaceBetweenArticles
                  }
                  printOrder(F, item, false, true, distance)
               }
               if (printChinese)
               {
                  printOrder(F, item, true, true, mLinespaceBetweenArticles)
               }

               mTotal = mTotal + item.getTotal()
               printFooter(F)
               mPrintQuantity = true
               quantity -= sizeOne
            }
         }
      }

      var valid = false
      mPrintQuantity = true

      for (p in 0 until size)
      {
         val item = mSortedItems.getSortedItem(p)
         val mainItem = item[0]

         if (!mainItem.paperCutPerItem || !mEnableCutPrinter)
         {
            if (!valid)
            {
               val idx = mCounter.nextKitchenIndex(mTransactionId, mTimeFrameIndex.index)
               printHeader(F, location, if (userCFG.getValue("user_print_kitchen_index") > 0) idx else 0)
               printExtra(F, idx)
               valid = true
            }

            var chinese = mKitchenChinese
            if (chinese && mKitchenLocal && mainItem.chinesePrinterName == mainItem.localPrinterName)
            {
               chinese = false
            }

            val printLocal = mKitchenLocal || mOnlyText
            var printChinese = !mChineseFirst && chinese
            var distance = mLinespaceBetweenLanguages

            if (mChineseFirst && chinese)
            {
               if (!printLocal)
               {
                  distance = mLinespaceBetweenArticles
               }
               printOrder(F, item, true, false, distance)
            }
            if (printLocal)
            {
               if (!printChinese)
               {
                  distance = mLinespaceBetweenArticles
               }
               printOrder(F, item, false, false, distance)
            }
            if (printChinese)
            {
               printOrder(F, item, true, false, mLinespaceBetweenArticles)
            }

            mTotal = mTotal + item.getTotal()
         }
      }

      if (valid)
      {
         if (CFG.getValue("kitchen_transaction_total") > 0)
         {
            val total = mTransaction.getTotal(ETaxType.BTW_TYPE_LOW) + mTransaction.getTotal(ETaxType.BTW_TYPE_HIGH)
            mTotal = total
         }
         printTotal(F, mTotal)
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
      val discount = mTransaction.getDiscount()
      val total = subtotal - discount
      var str: String

      if (mPrintLang == ETaal.LANG_SIMPLIFIED || mPrintLang == ETaal.LANG_TRADITIONAL)
      {
         if (!discount.empty())
         {
            str = subtotal.toString(0, CMoney.MONEY_LINE)
            F.write(mTextSubtotal)
            val strLen = str.length
            F.write("[subtab:::$strLen]$str\n")
            str = discount.toString(0, CMoney.MONEY_LINE)
            F.write(mTextDiscount)
            F.write("[subtab:::$str]$str\n")
         }
         str = total.toString(0, CMoney.MONEY_LINE)
         F.write(mTextTotal)
         F.write("[subtab:::$str]$str\n")
      } else
      {
         F.write("[Text|Height:2|Width:2]")
         if (!discount.empty())
         {
            str = subtotal.toString(0, CMoney.MONEY_LINE)
            F.write(mTextSubtotal)
            F.write("[subtab:::$str]$str\n")
            str = discount.toString(0, CMoney.MONEY_LINE)
            F.write(mTextDiscount)
            F.write("[subtab:::$str]$str\n")
         }
         str = total.toString(0, CMoney.MONEY_LINE)
         val strLen = str.length
         F.write("[Text|Height:2|Width:2]$mTextTotal[subtab::$strLen]$str\n")
      }
   }

   private fun printColours(F: PrinterFile)
   {
      val isTakeaway = mTransaction.isTakeaway()

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

         CFG.getValue("kitchen_added_food_red") > 0 && isTakeaway && mTimeFrameIndex.index > 1 ->
         {
            F.write(if (mTimeFrameIndex.index > 1) "[red]" else "[black]")
         }

         CFG.getValue("kitchen_change_colours") > 0 ->
         {
            mRedColour = !mRedColour
            F.write(if (mRedColour) "[red]" else "[black]")
         }

         else ->
         {
            F.write("[black]")
         }
      }
   }

   private fun printExtra(F: PrinterFile, index: Int)
   {
      if (!mTakeaway || index <= 1)
      {
         return
      }

      val extra = CFG.getValue("kitchen_text_print_extra")
      when (extra)
      {
         1 ->
         {
            val idx = mTimeFrameIndex.index
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
      val transType = mTransaction.transType
      val lang = if (CFG.getValue("kitchen_chinese") > 0) ETaal.LANG_SIMPLIFIED else mEuroLang

      if (transType == ETransType.TRANS_TYPE_TAKEAWAY_PHONE)
      {
         mDelay = CTimestamp(mTransaction.customerTime)
      } else
      {
         mDelay = CTimestamp(mTransaction.deliverTime)
      }

      if (mDelay.year > 2000)
      {
         val time = if (mDelay.isAsap(CFG.getValue("kitchen_print_asap") > 0))
         {
            mTextAsap
         } else
         {
            mDelay.getShortTime()
         }
         done = true

         if (CFG.getValue("kitchen_chinese") > 0)
         {
            F.write("[graphics|font:deliver]$mTextDeliverTime $time\n\n")
         } else
         {
            F.write("[height:2]$mTextDeliverTime [width:2]$time[width:1|height:1]\n\n")
         }
      }
      if (!done && CFG.getValue("kitchen_print_asap") > 0)
      {
         F.write("[graphics|font:deliver]$mTextAsap\n\n")
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

      F.write("[Linespace:$mLinespace]")
      for (x in 0 until CFG.getValue("kitchen_beeps"))
      {
         F.write("[ring]")
      }
      F.write("[graphics|font:waiter]")

      if (CFG.getValue("print_reroute") > 0)
      {
         F.write("[Reprint|Font:16]$mTextReprint\n[Ifn:$mTM290|Ifn:$mTM295|CUT]\n[endif|endif|endif]")
      }

      printColours(F)

      mBillMessage = mTransaction.getBillRemarks()
      if (mBillMessage.length > 1)
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
         F.write("$mBillMessage\n\n")
      }

      F.write("[text|width:1|height:1|font:1]")
      printTime(F)
      F.write(String.format(Locale.ROOT, "#%07d", mTransactionId))

      if (CFG.getValue("kitchen_print_waiter") > 0)
      {
         if (CFG.getValue("entry_with_key") > 0)
         {
            val s = mTransaction.getEmployeeName()
            if (s.isNotEmpty())
            {
               F.write("[right:$mTextKey $s]")
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
         F.write("[ifn:$mSP200|ifn:$mTestPrinter|linespace:$mLinespaceInArticle]")
         printOrderChineseDetail(F, order, spices_chinese, extras_chinese, linespaceAfter)
         F.write("[endif|endif]")

         F.write("[if:$mTestPrinter|text]")
         printLocalOrderTextmode(F, order, order.chinesePrinterName, spices_chinese, extras_chinese, linespaceAfter)
         F.write("[endif]")
      } else
      {
         if (CFG.getValue("kitchen_chinese") > 0)
         {
            F.write("[If:$mSP200]")
         }
         printLocalOrderTextmode(F, order, order.localPrinterName, spices_local, extras_local, linespaceAfter)
         if (CFG.getValue("kitchen_chinese") > 0)
         {
            F.write("[endif]")
         }
         if (CFG.getValue("kitchen_chinese") > 0)
         {
            F.write("[ifn:$mSP200|ifn:$mTestPrinter]")
            printLocalOrderGraphmode(F, order, spices_local, extras_local, linespaceAfter)
            F.write("[endif|endif]")

            F.write("[if:$mTestPrinter|text]")
            printLocalOrderTextmode(F, order, order.localPrinterName, spices_local, extras_local, linespaceAfter)
            F.write("[endif]")
         }
      }
   }

   private fun printTafel(F: PrinterFile, location: EItemLocation, billNumber: Int)
   {
      val name = mTransaction.displayName
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

      mRechaud = mTransaction.isRechaud()
      mTakeaway = mTransaction.isTakeaway()
      mPhone = mTransaction.isTelephone()

      mDeletedBlack = CFG.getValue("kitchen_takeaway_red") > 0 && (mTakeaway || mPhone) &&
         CFG.getValue("kitchen_change_colours") == 0

      if (CFG.getValue("kitchen_line") > 0)
      {
         F.write("[Line:12]\n")
      }

      F.write("[Text|Font:1|Height:2|Width:2|Ifnchars:29|Font:0|Endif]")

      when
      {
         mRechaud ->
         {
            val nr = mTransaction.TAcount()
            F.write("[Ifchars:29]\t[endif]$mTextRechaud")
            if (nr > 0)
            {
               F.append(String.format(Locale.ROOT, ": %02d%s", nr, sub_nr))
            } else
            {
               F.write(": $sub_nr")
            }
         }

         mTakeaway ->
         {
            val tstr = when
            {
               CFG.getValue("takeaway_mode") > 0 -> mTextList
               mTransaction.transType == ETransType.TRANS_TYPE_TAKEAWAY ||
                  mTransaction.transType == ETransType.TRANS_TYPE_TAKEAWAY_PHONE ->
                  Translation.get(TextId.TEXT_TAKEAWAY, mEuroLang)

               else -> mTextEatInside
            }
            val takeaway_counter = mTransaction.TAcount()
            F.write("[Ifchars:29]\t[endif]$tstr")

            if (takeaway_counter > 0)
            {
               F.write(": $name$sub_nr")
            } else
            {
               F.write(": $sub_nr")
            }
         }

         mPhone ->
         {
            F.write("$mTextTelephone $name\n")
            s = mTransaction.getClient()
            if (s.length > 2)
            {
               F.write("[height:1|width:1]\n$s\n")
            }
         }

         mTransaction.isShop() ->
         {
            val ptr = mTransaction.getClient()
            F.write(ptr)
         }

         else ->
         {
            s = mTransaction.getClient()
            if (s.length > 2)
            {
               F.write("$mTextTelephone $name\n")
               F.write("[height:1|width:1]\n$s$sub_nr")
            } else
            {
               F.write("[Ifchars:29]\t[endif]$mTextTable: $name$sub_nr")
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
      F.write("[Line:16|ifn:$mSP200|ifn:$mCBM231]\n[endif|endif]")
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
      F.write("[Linespace:$mLinespaceInArticle]")
      if (order.level == EOrderLevel.LEVEL_ITEMGROUP && mLineItemLine > 0)
      {
         F.write("[width:1|height:1|font:kitchen_h1]")
         F.write("[line:$mLineItemLine]\n")
         if (order.localPrinterName.isEmpty() && order.chinesePrinterName.isEmpty())
         {
            return
         }
      } else
      {
         F.write("[width:1|height:1|font:kitchen_chinese]")
      }

      if (order.level != EOrderLevel.LEVEL_ITEMGROUP && mPrintQuantity)
      {
         val q = order.getQuantity()
         F.write("${q}x")
      }
      F.write("[tab::99x ]")

      if (mFirstAlias)
      {
         if (mAlias)
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
         if (mAlias)
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
      F.write("[Linespace:$mLinespaceInArticle]")
      F.write("[font:$mKitchenTextFont|height:$mKitchenTextHeight|width:$mKitchenTextWidth]")

      if (order.level != EOrderLevel.LEVEL_ITEMGROUP && mPrintQuantity)
      {
         val q = order.getQuantity()
         F.write("${q}x")
      }

      F.write("[tab:3|ifchars:26|tab:5|Endif]")
      if (mFirstAlias && mAlias)
      {
         F.write("${order.alias}[tab:8|ifchars:21|tab:10|endif]")
      }

      F.wordsCharOut(localPrinterName, 5, 0, mKitchenTextWidth, false)
      F.wordsCharOut(spices, 5, 0, mKitchenTextWidth, true)
      F.wordsCharOut(extras, 5, 0, mKitchenTextWidth, true)

      if (!mFirstAlias && mAlias)
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
      F.write("[Linespace:$mLinespaceInArticle]")
      F.write("[font:kitchen_local|Height:1|width:1]")

      if (order.level != EOrderLevel.LEVEL_ITEMGROUP && mPrintQuantity)
      {
         val q = order.getQuantity()
         F.write("${q}x")
      }
      F.write("[tab::99x ]")

      if (mFirstAlias)
      {
         if (mAlias)
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
         if (mAlias)
         {
            F.write("[right:${order.alias}]")
         }
      }
      F.write("[linespace:$linespaceAfter]\n")
   }

   private fun printFooter(F: PrinterFile)
   {
      F.write("[font:default]")
      F.write("[transaction:${mTransaction.transactionId}]")
      val s = CFG.getString("kitchen_footer")
      F.write(s)
      F.write("[If:$mSP200]\n\n\n\n[Endif]\n\n\n[cut|black]\n")
   }
}