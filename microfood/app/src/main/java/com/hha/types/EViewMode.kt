/**
 * Ways to view the tree
 */
enum class EViewMode(val value: Int)
{
   VIEW_MODE_UNDEFINED(0),
   VIEW_MODE_TRANSACTION(1),
   VIEW_MODE_SPLIT(2),
   VIEW_MODE_PREVIEW(3);

   companion object
   {
      fun fromValue(value: Int): EViewMode?
      {
         return values().find { it.value == value }
      }
   }
}
