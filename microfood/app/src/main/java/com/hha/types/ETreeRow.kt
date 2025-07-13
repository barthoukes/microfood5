package com.hha.types

/**
 * Represents tree row types an list
 */
enum class ETreeRow(val value: Int) {
    TREE_ONLY_ITEM(0),                      ///< Item without subitems
    TREE_ITEM(1),                            ///< Item with subitems.
    TREE_NEXT_ONLY_SUBITEM(2),                ///< Subitem, with more subitems, no more subsub items.
    TREE_LAST_ONLY_SUBITEM(3),                ///< Subitem, without any subitems or subsub items.
    TREE_NEXT_SUBITEM(4),                    ///< Subitem, with more subitems and subsub items.
    TREE_LAST_SUBITEM(5),                    ///< Subitem with subsub items.
    TREE_NEXT_SUBITEM_NEXT_SUBSUBITEM(6),    ///< Subsubitem, more subitems follow, more subsubitems follow.
    TREE_NEXT_SUBITEM_LAST_SUBSUBITEM(7),    ///< Subsubitem, more subitems follow, no more subsubitems follow.
    TREE_LAST_SUBITEM_NEXT_SUBSUBITEM(8),    ///< Subsubitem, more subsubitems follow.
    TREE_LAST_SUBITEM_LAST_SUBSUBITEM(9);    ///< Subsubitem, the last one.

    companion object {
        /**
         * Get EOrderLevel from an integer value
         * @param value The integer value to look up
         * @return Corresponding EOrderLevel or null if not found
         */
        fun fromInt(value: Int): ETreeRow? {
            return values().firstOrNull { it.value == value }
        }
    }
}