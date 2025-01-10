package dev.intsuc.kommander.suggestion

import dev.intsuc.kommander.context.StringRange

class Suggestions(
    val range: StringRange,
    val list: List<Suggestion>,
) {
    fun isEmpty(): Boolean = list.isEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Suggestions) {
            return false
        }
        return range == other.range && list == other.list
    }

    override fun hashCode(): Int {
        var result = range.hashCode()
        result = 31 * result + list.hashCode()
        return result
    }

    override fun toString(): String = "Suggestions{range=$range, suggestions=$list}"

    companion object {
        private val EMPTY: Suggestions = Suggestions(StringRange.at(0), emptyList())

        fun empty(): Suggestions = EMPTY

        fun merge(command: String, input: Collection<Suggestions>): Suggestions {
            if (input.isEmpty()) {
                return EMPTY
            } else if (input.size == 1) {
                return input.first()
            }

            val texts = hashSetOf<Suggestion>()
            for (suggestions in input) {
                texts += suggestions.list
            }
            return create(command, texts)
        }

        fun create(command: String, suggestions: Collection<Suggestion>): Suggestions {
            if (suggestions.isEmpty()) {
                return EMPTY
            }
            var start = Int.MAX_VALUE
            var end = Int.MIN_VALUE
            for (suggestion in suggestions) {
                start = minOf(suggestion.range.start, start)
                end = maxOf(suggestion.range.end, end)
            }
            val range = StringRange(start, end)
            val texts = hashSetOf<Suggestion>()
            for (suggestion in suggestions) {
                texts += suggestion.expand(command, range)
            }
            val sorted = texts.toMutableList()
            sorted.sortWith { a, b -> a.compareToIgnoreCase(b) }
            return Suggestions(range, sorted)
        }
    }
}
