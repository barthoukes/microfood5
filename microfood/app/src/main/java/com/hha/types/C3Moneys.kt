package com.hha.types

import com.hha.common.Money

class C3Moneys
{
   var taxLow: CMoney = CMoney(0)
   var taxHigh: CMoney = CMoney(0)
   var taxFree: CMoney = CMoney(0)

   // Constructors
   constructor() : this(CMoney(), CMoney(), CMoney())
   constructor(taxLow: CMoney, taxHigh: CMoney, taxFree: CMoney)
   {
      this.taxLow = taxLow
      this.taxHigh = taxHigh
      this.taxFree = taxFree
   }

   constructor(taxLow: Money, taxHigh: Money, taxFree: Money) :
      this(CMoney(taxLow), CMoney(taxHigh), CMoney(taxFree))

   constructor(taxLow: Int, taxHigh: Int, taxFree: Int)
   {
      this.taxLow = CMoney(taxLow)
      this.taxHigh = CMoney(taxHigh)
      this.taxFree = CMoney(taxFree)
   }

   constructor(taxLow: String, taxHigh: String, taxFree: String) :
      this(CMoney(taxLow), CMoney(taxHigh), CMoney(taxFree))

   operator fun plus(other: C3Moneys): C3Moneys
   {
      return C3Moneys(
         taxLow + other.taxLow,
         taxHigh + other.taxHigh,
         taxFree + other.taxFree
      )
   }

   operator fun minus(other: C3Moneys): C3Moneys
   {
      return C3Moneys(
         taxLow - other.taxLow,
         taxHigh - other.taxHigh,
         taxFree - other.taxFree
      )
   }

   // Similarly, we can have unary minus
   operator fun unaryMinus(): C3Moneys
   {
      return C3Moneys(-taxLow, -taxHigh, -taxFree)
   }

// We might want to multiply by a scalar (Int or Double) or by another triplet?
// Let's do scalar multiplication for now.

   operator fun times(multiplier: Int): C3Moneys
   {
      return C3Moneys(
         taxLow * multiplier,
         taxHigh * multiplier,
         taxFree * multiplier
      )
   }

   fun calculateTax(total: C3Moneys, taxPercentageLow: Double, taxPercentageHigh: Double) {
      // Formula for calculating tax FROM a total amount (inclusive of tax) is:
      // Tax = Total * (Percentage / (100 + Percentage))

      // Calculate tax for the 'low' tax bracket
      val taxLow = total.taxLow.cents() * (taxPercentageLow / (100.0 + taxPercentageLow))

      // Calculate tax for the 'high' tax bracket
      val taxHigh = total.taxHigh.cents() * (taxPercentageHigh / (100.0 + taxPercentageHigh))

      // Update the properties of the C3Moneys object this function is called on
      this.taxLow = CMoney(taxLow.toInt())
      this.taxHigh = CMoney(taxHigh.toInt())
      this.taxFree = CMoney(0) // Tax on the zero-rated amount is always 0
   }
   fun toMoney(): CMoney
   {
      return taxLow + taxHigh + taxFree
   }

   operator fun times(multiplier: Double): C3Moneys
   {
      return C3Moneys(
         taxLow * multiplier,
         taxHigh * multiplier,
         taxFree * multiplier
      )
   }

   operator fun div(divisor: Int): C3Moneys
   {
      return C3Moneys(
         taxLow / divisor,
         taxHigh / divisor,
         taxFree / divisor
      )
   }

   // Compound assignments
   operator fun plusAssign(other: C3Moneys)
   {
      taxLow = taxLow + other.taxLow
      taxHigh = taxHigh + other.taxHigh
      taxFree = taxFree + other.taxFree
   }

   operator fun minusAssign(other: C3Moneys)
   {
      taxLow = taxLow - other.taxLow
      taxHigh = taxHigh - other.taxHigh
      taxFree = taxFree - other.taxFree
   }

   operator fun timesAssign(multiplier: Int)
   {
      taxLow = taxLow * multiplier
      taxHigh = taxLow * multiplier
      taxFree = taxFree * multiplier
   }

   operator fun timesAssign(multiplier: Double)
   {
      taxLow = taxLow * multiplier
      taxHigh = taxHigh * multiplier
      taxFree = taxFree * multiplier
   }

   operator fun divAssign(divisor: Int)
   {
      taxLow = taxLow / divisor
      taxHigh = taxHigh / divisor
      taxFree = taxFree / divisor
   }

// Comparison: We can define compareTo if we want, but what does it mean to compare two triplets?
// Maybe we don't need that. Instead, we can provide a function to check if all are zero, etc.

   fun isZero(): Boolean = taxLow.isZero() && taxHigh.isZero() && taxFree.isZero()

   // We can also have a function to clear all
   fun clear()
   {
      taxLow.clear()
      taxHigh.clear()
      taxFree.clear()
   }

   // String representation
   override fun toString(): String
   {
      return "CMoneyTriplet(taxLow=${taxLow.toString()}, taxHigh=${taxHigh.toString()}, taxFree=${taxFree.toString()})"
   }

// We can also have a function to convert to a string in a specific format, but let's keep it simple for now.

// We might want to load from a file? The original CMoney had a load function. We can do similarly if needed.

   // We can also provide a function to get the total (if that makes sense) by adding all three?
   fun total(): CMoney = taxLow + taxHigh + taxFree

   // We can also provide a function to round each component
   fun round(gap: Int)
   {
      taxLow.round(gap)
      taxHigh.round(gap)
      taxFree.round(gap)
   }

   // We can also provide a function to check if any is negative, etc.
   fun isZeroOrNegative(): Boolean
   {
      return taxLow.isZeroOrNegative() && taxHigh.isZeroOrNegative() && taxFree.isZeroOrNegative()
   }

   // We can also provide a function to check if the triplet is empty (all zero)
   fun empty(): Boolean = isZero()

// We can also provide a function to get the cents of each? Maybe not, because we have the CMoney objects.

// We can also provide a function to convert to a string with a given format for each, but that might be overkill.

   // We can also provide a function to set from another triplet.
   fun set(other: C3Moneys)
   {
      taxLow = other.taxLow
      taxHigh = other.taxHigh
      taxFree = other.taxFree
   }

   // We can also provide a function to set from three CMoney.
   fun set(low: CMoney, high: CMoney, free: CMoney)
   {
      taxLow = low
      taxHigh = high
      taxFree = free
   }

   // We can also provide a function to set from three doubles.
   fun set(low: Double, high: Double, free: Double)
   {
      taxLow = CMoney(low)
      taxHigh = CMoney(high)
      taxFree = CMoney(free)
   }

   // We can also provide a function to set from three strings.
   fun set(low: String, high: String, free: String)
   {
      taxLow = CMoney(low)
      taxHigh = CMoney(high)
      taxFree = CMoney(free)
   }

// We can also provide a function to add a CMoney to a specific component?
// But we can do that by accessing the property directly.

// We can also provide a function to add a CMoney to all components? Maybe not, because they are distinct.

// We can also provide a function to multiply by a CMoney? That doesn't make sense.

// We can also provide a function to multiply by a triplet? Maybe, but let's not for now.

// We can also provide a function to divide by a triplet? Maybe not.

   // We can also provide a function to compare two triplets for equality.
   override fun equals(other: Any?): Boolean
   {
      if (this === other) return true
      if (other !is C3Moneys) return false
      return taxLow == other.taxLow && taxHigh == other.taxHigh && taxFree == other.taxFree
   }

   override fun hashCode(): Int
   {
      var result = taxLow.hashCode()
      result = 31 * result + taxHigh.hashCode()
      result = 31 * result + taxFree.hashCode()
      return result
   }
}