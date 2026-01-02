package com.hha.model // Or com.hha.util, depending on where you created the file

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents a one-time event.
 * This prevents the event from being fired again on configuration changes (like screen rotation).
 */
open class MyEvent<out T>(private val content: T) {

   var hasBeenHandled = false
      private set // Allow external read but not write

   /**
    * Returns the content and prevents its use again.
    */
   fun getContentIfNotHandled(): T? {
      return if (hasBeenHandled) {
         null
      } else {
         hasBeenHandled = true
         content
      }
   }

   /**
    * Returns the content, even if it's already been handled.
    * Useful for peeking at the data without consuming the event.
    */
   fun peekContent(): T = content
}
