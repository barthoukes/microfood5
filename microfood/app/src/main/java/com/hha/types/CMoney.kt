package com.hha.types

import java.io.File
import kotlin.math.abs

/**
 * Represents monetary values with cent precision.
 * @property cents The monetary value stored in cents (1/100 of the base unit)
 * Examples:
 * val money1 = CMoney("12.34")  // 1234 cents
 * val money2 = CMoney(5.67)     // 567 cents
 * val sum = money1 + money2     // 1801 cents
 * val product = money1 * 2      // 2468 cents
 * val negative = -money1        // -1234 cents
 */
class CMoney(private var cents: Int = 0) {
    companion object {
        const val MONEY_CENTS = 0x01
        const val MONEY_LINE = 0x02
        const val MONEY_APE = 0x04
        const val MONEY_LEADING_SPACE = 0x08
    }

    // Constructors
    constructor(value: Double) : this((value*100.0).toInt())
    constructor(value: String) : this(0) {
        parseString(value)
    }

    fun round(gap : Int) {
        cents = cents - (cents%gap)
    }

    fun cents() : Int {
        return cents;
    }

    fun empty() : Boolean {
        return cents == 0
    }

    // Parsing from string
    private fun parseString(m: String) {
        cents = 0
        var x = m
        var negative = false

        if (x.startsWith("-")) {
            negative = true
            x = x.substring(1)
        }

        // Parse whole number part
        var wholeNumber : Int = 0
        var i = 0
        while (i < x.length && x[i].isDigit()) {
            wholeNumber = wholeNumber * 10 + (x[i] - '0')
            i++
        }
        cents = wholeNumber * 100

        // Parse decimal part if exists
        if (i < x.length && (x[i] == '.' || x[i] == ',')) {
            i++
            if (i < x.length && x[i].isDigit()) {
                cents += 10 * (x[i] - '0')
                i++
                if (i < x.length && x[i].isDigit()) {
                    cents += (x[i] - '0')
                }
            }
        }

        if (negative) {
            cents = -cents
        }
    }

    // Basic operations
    operator fun plus(other: CMoney): CMoney = CMoney(cents + other.cents)
    operator fun minus(other: CMoney): CMoney = CMoney(cents - other.cents)
    operator fun unaryMinus(): CMoney = CMoney(-cents)

    operator fun times(multiplier: Int): CMoney = CMoney(cents * multiplier)
    operator fun times(multiplier: Double): CMoney = CMoney((cents * multiplier).toInt())
    operator fun div(divisor: Int): CMoney = CMoney(cents / divisor)

    // Compound assignments
    operator fun plusAssign(other: CMoney) { cents += other.cents }
    operator fun minusAssign(other: CMoney) { cents -= other.cents }
    operator fun timesAssign(multiplier: Int) { cents *= multiplier }
    operator fun timesAssign(multiplier: Double) { cents = (cents * multiplier).toInt() }
    operator fun divAssign(divisor: Int) { cents /= divisor }

    // Comparison operators
    operator fun compareTo(other: CMoney): Int = cents.compareTo(other.cents)
    override fun equals(other: Any?): Boolean = (other is CMoney) && (cents == other.cents)
    override fun hashCode(): Int = cents.hashCode()

    // Conversion functions
    fun toDouble(): Double = cents.toDouble()
    fun toLong(): Long = cents.toLong()
    fun isZero(): Boolean = cents == 0
    fun isNonZero(): Boolean = cents != 0

    fun negative() { cents = -cents }
    fun clear() { cents = 0 }

    // String representation
    fun toString(length: Int = 8, flags: Int = MONEY_CENTS, ape: String = ""): String {
        val builder = StringBuilder()

        if (abs(cents) > 1_000_000 && length < 9 && (flags and MONEY_CENTS) == 0) {
            builder.append(cents / 100)
        } else if ((flags and MONEY_LINE) != 0 && cents == 0) {
            builder.append("-.--")
        } else {
            if (cents < 0) builder.append("-")
            val absCents = abs(cents)
            builder.append(absCents / 100).append(".")
            val remainingCents = absCents % 100
            builder.append(remainingCents / 10).append(remainingCents % 10)
        }

        var result = builder.toString()

        if ((flags and MONEY_APE) != 0) {
            result = ape + result
        }

        if ((flags and MONEY_LEADING_SPACE) != 0) {
            result = " $result"
        }

        if (result.length < length) {
            result = " ".repeat(length - result.length) + result
        }

        return result
    }

    override fun toString(): String = toString(8, MONEY_CENTS)

    // File operations (simplified from original C++ version)
    fun load(file: File) {
        // Simplified implementation - would need proper binary reading in real code
        val bytes = file.readBytes()
        if (bytes.size >= 3) {
            cents = (bytes[0].toInt() and 0xFF) +
                    ((bytes[1].toInt() and 0xFF) shl 8)
            cents = cents * 100 + (bytes[2].toInt() and 0xFF)
        }
    }
}

// Extension functions for operator overloading with Int first
operator fun Int.times(money: CMoney): CMoney = money * this