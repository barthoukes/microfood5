package com.hha.framework

import com.hha.common.MenuItem
import com.hha.resources.TextSplitter
import com.hha.types.ENameType
import com.hha.types.ETaal

// Placeholder for the DuoShao function - implement its actual logic
fun duoShao(portion: Int, language: ETaal, isPrinter: Boolean): String {
    // Example: This needs to be your actual logic from C++ DuoShao
    if (portion == 1) return if (language == ETaal.LANG_SIMPLIFIED || language == ETaal.LANG_TRADITIONAL) "单" else "1x "
    if (portion == 2) return if (language == ETaal.LANG_SIMPLIFIED || language == ETaal.LANG_TRADITIONAL) "双" else "2x "
    // ... more portion logic
    return "" // Default if portion is not handled or 0
}

fun MenuItem.getWidth() : Int {
    return this.positionWidth
}

/**
 * Calculates a display name for the MenuItem.
 * This is an example, customize the logic based on your needs.
 */
fun MenuItem.name(language: ETaal, portion: Int, type: ENameType, trim: Boolean = false): String {
    var resultString: String
    val portionString = duoShao(portion, language, type == ENameType.NAME_PRINTER)
    return Name(language, portionString, this.localName,
        this.chineseName, type, trim)
}

fun Name(language: ETaal, portionString: String, currentLocalName: String,
         currentChineseName: String, type: ENameType, trim: Boolean = false): String {

    var local = when {
        currentLocalName.isEmpty() && type != ENameType.NAME_ORIGINAL -> currentChineseName
        else -> currentLocalName
    }
    var chinese = when {
        currentChineseName.isEmpty() && type != ENameType.NAME_ORIGINAL -> currentLocalName
        else -> currentChineseName
    }
    val baseName = if (language == ETaal.LANG_SIMPLIFIED || language == ETaal.LANG_TRADITIONAL) {
        chinese
    } else {
        local
    }
    // Prepend portion string only if it's not empty, similar to C++ string concatenation
    val str = if (portionString.isNotEmpty()) portionString + baseName else baseName

    val f1: String
    val f2: String // f2's exact role in CtextSplitter needs clarification for a perfect port

    when (type) {
        ENameType.NAME_ORIGINAL -> {
            f1 = " "
            f2 = " "
        }

        ENameType.NAME_PRINTER, ENameType.NAME_SCREEN -> {
            f1 = " "  // Assuming space is the primary delimiter for splitting into words
            f2 = "\n" // Its role in CtextSplitter might be for line wrapping or special formatting
        }
        // Kotlin requires 'else' in when if it's an expression, or all cases covered.
        // If ENameType has more values, handle them or add a default.
        // Assuming NAME_SCREEN is the default like in C++ `default:` case
        else -> {
            f1 = " "
            f2 = "\n"
        }
    }

    val textSplitter = TextSplitter(str, f1, f2, trim)
    val screenCase = 1
    val printerCase = 1

    when (type) {
        ENameType.NAME_SCREEN -> {
            when (screenCase) {
                    1 -> textSplitter.allLowerCase()
                    2 -> textSplitter.firstCharacterCapital()
                    3 -> textSplitter.allCapitals()
                }
            }
            ENameType.NAME_PRINTER -> {
                when (printerCase) {
                    1 -> textSplitter.allLowerCase()
                    2 -> textSplitter.firstCharacterCapital()
                    3 -> textSplitter.allCapitals()
                }
            }
            else -> {}
        }
        return textSplitter.join(0, " ")
    }








