package com.hha.resources

class TextSplitter(
    input: String,
    private val sign1: String,
    private val sign2: String,
    private val trimWords: Boolean = true
) {
    private val parts = mutableListOf<String>()

    init {
        var index = 0
        while (true) {
            val loc1 = input.indexOf(sign1, index)
            val loc2 = input.indexOf(sign2, index)

            val loc = when {
                loc1 == -1 -> loc2
                loc2 == -1 -> loc1
                else -> minOf(loc1, loc2)
            }

            if (loc == -1 || loc <= 0) {
                // Add remainder of string
                val word = input.substring(index)
                if (trim(word)) {
                    parts.add(word)
                }
                break
            }

            val word = input.substring(index, loc)
            if (trim(word)) {
                parts.add(word)
            }
            index = loc + 1
        }
    }

    private fun trim(word: String): Boolean {
        if (!trimWords) return true

        val trimmed = word.trim()
        return trimmed.isNotEmpty()
    }

    val size: Int
        get() = parts.size

    operator fun get(index: Int): String = parts[index]

    fun firstCharacterCapital() {
        parts.replaceAll { it.replaceFirstChar { first -> first.uppercase() } }
    }

    fun allCapitals() {
        parts.replaceAll { it.uppercase() }
    }

    fun allLowerCase() {
        parts.replaceAll { it.lowercase() }
    }

    fun trimTags() {
        parts.replaceAll { it.replace(Regex("<[^>]*>"), "") }
    }

    fun removeLeft(nrCharacters: Int) {
        parts.replaceAll {
            it.replace("&quot;", "-")
                .drop(nrCharacters)
        }
    }

    fun trimRoundBrackets() {
        parts.replaceAll {
            it.replace(Regex("^\\("), "")
                .replace(Regex("\\)$"), "")
        }
    }

    fun isEmpty(): Boolean = parts.isEmpty()

    fun erase(index: Int) {
        if (index < parts.size) {
            parts.removeAt(index)
        }
    }

    fun trim() {
        parts.replaceAll { it.trim() }
    }

    fun insert(y: Int, str: String) {
        while (y >= parts.size) {
            parts.add("")
        }
        parts[y] = str + parts[y]
    }

    fun join(start: Int, join: String): String {
        return parts.drop(start).joinToString(join)
    }
}