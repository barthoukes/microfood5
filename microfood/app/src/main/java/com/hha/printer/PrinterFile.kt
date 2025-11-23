package com.hha.printer

import java.io.File
import java.nio.charset.StandardCharsets

/**
 * A Kotlin class to construct a formatted file for a printer service.
 *
 * This class mimics the behavior of the original C++ CprinterFile, providing a
 * fluent API to build a command string for a printer. It replaces manual buffer
 * manipulation with a robust StringBuilder and offers clean, idiomatic Kotlin functions.
 *
 * @param content The initial string content. If it starts with "BART" and has the
 *                correct header size, the file is considered valid.
 */
class PrinterFile(content: String = "")
{

   // A companion object holds constants, similar to static members in C++.
   companion object
   {
      private const val PRINTER_FILE_HEADER_SIZE = 8 // Assumed size
      private const val PRINTER_FILE_MAGIC = "BART"
   }

   private var buffer = StringBuilder()
   var isValid: Boolean = false
      private set

   init
   {
      // This logic replaces the C++ constructor CprinterFile(const std::string &s)
      if (content.startsWith(PRINTER_FILE_MAGIC) && content.length >= PRINTER_FILE_HEADER_SIZE)
      {
         buffer.append(content)
         isValid = true
      } else
      {
         clear()
      }
   }

   /**
    * Appends a string to the internal buffer. This is the primary "write" method.
    */
   fun append(value: Any?): PrinterFile
   {
      buffer.append(value.toString())
      if (!isValid)
      {
         // Logic from C++ puts() to set header if buffer was initially empty.
         start()
         isValid = true
      }
      return this
   }

   fun write(value: String) = append(value)

   /**
    * Clears the buffer and initializes it with a default header.
    */
   fun clear()
   {
      buffer.clear()
      buffer.append(PRINTER_FILE_MAGIC)
      // Pad with spaces until the header size is reached.
      while (buffer.length < PRINTER_FILE_HEADER_SIZE)
      {
         buffer.append(' ')
      }
      setPriority(10)
      setDestination(1)
      setRepeat(1)
      isValid = false
   }

   /**
    * Prepares the file for reading by setting the internal cursor.
    * In this implementation, this is a conceptual placeholder as we don't use a manual cursor.
    */
   private fun start()
   {
      // C++ version moves a cursor. In Kotlin, this isn't needed for building the string.
      // The header is already set in clear().
   }

   /**
    * Splits a string into words based on common delimiters.
    * Replaces the C++ `doSplit` function.
    */
   private fun doSplit(s: String): List<String>
   {
      // Use a regular expression to split on space, tab, newline, and comma.
      // `filter(String::isNotEmpty)` removes empty strings resulting from multiple delimiters.
      return s.split(Regex("[ \t\n\r,]+")).filter(String::isNotEmpty)
   }

   /**
    * Formats words to wrap based on character count, using special printer commands.
    * Replaces `wordsCharOut`.
    */
   fun wordsCharOut(s: String, cursorL: Int, cursorR: Int, width: Int, hasLeadingSpace: Boolean)
   {
      val words = doSplit(s)
      var first = !hasLeadingSpace

      words.forEach { word ->
         val effectiveWidth = (word.length + (if (first) 0 else 1)) * width + cursorR
         append("[ifnchars:$effectiveWidth]\n[tab:$cursorL|")
         if (first)
         {
            append("endif]$word")
            first = false
         } else
         {
            append("else] [endif]$word")
         }
      }
   }

   /**
    * Formats words to wrap based on printer dot measurements, using special commands.
    * Replaces `wordsDotOut`. This implementation handles both Int and String for cursorL.
    */
   fun wordsDotOut(s: String, cursorL: Any, hasLeadingSpace: Boolean)
   {
      val words = doSplit(s)
      if (words.isEmpty()) return
      var first = !hasLeadingSpace

      words.forEach { word ->
         append("[ifndots::$word]\n[tab:$cursorL|")
         if (first)
         {
            append("endif]$word")
            first = false
         } else
         {
            append("else] [endif]$word")
         }
      }
   }

   /**
    * Saves the buffer's content to a file.
    */
   fun save(filename: String)
   {
      try
      {
         File(filename).writeText(buffer.toString(), StandardCharsets.UTF_8)
      } catch (e: Exception)
      {
         // Handle exceptions, e.g., log the error
         println("Error saving file $filename: ${e.message}")
      }
   }

   // --- Header Manipulation Methods ---

   fun setDestination(destination: Int)
   {
      require(destination in 0..9) { "Destination must be between 0 and 9." }
      buffer.setCharAt(4, Character.forDigit(destination, 10))
   }

   fun getDestination(): Int
   {
      val char = buffer.getOrNull(4)
      return if (char?.isDigit() == true) char.digitToInt() else 0
   }

   fun setPriority(priority: Int)
   {
      require(priority in 0..25) { "Priority must be between 0 and 25." }
      buffer.setCharAt(5, 'A' + priority)
   }

   fun setRepeat(repeat: Int)
   {
      require(repeat in 1..3) { "Repeat must be between 1 and 3." }
      buffer.setCharAt(6, Character.forDigit(repeat, 10))
   }

   fun getRepeat(): Int
   {
      val char = buffer.getOrNull(6)
      return if (char?.isDigit() == true) char.digitToInt() else 0
   }

   fun setPrinterLocation(location: EPrinterLocation)
   {
      buffer.setCharAt(7, 'A' + location.ordinal)
   }

   // --- Helper for fluent API ---

   fun ifPrinter(printerType: EPrinterType): PrinterFile
   {
      return append("[if:$printerType]")
   }

   /**
    * Returns the complete string content of the buffer.
    */
   fun getBuffer(): String = buffer.toString()
}
