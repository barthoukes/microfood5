package com.hha.framework

data class CCursor(var position: Int)
{
   init
   {
      require(position >= -1) { "Cursor position cannot be negative" }
   }

   fun move(offset: Int)
   {
      position = (position + offset).coerceAtLeast(0)
   }

   fun next(): CCursor
   {
      return CCursor(position + 1)
   }

   fun previous(): CCursor
   {
      return CCursor(position - 1)
   }

   fun set(newPosition: Int)
   {
      require(newPosition >= -1) { "Cursor position cannot be negative" }
      position = newPosition
   }
}