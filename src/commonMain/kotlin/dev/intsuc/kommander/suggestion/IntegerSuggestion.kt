package dev.intsuc.kommander.suggestion

import dev.intsuc.kommander.Message
import dev.intsuc.kommander.context.StringRange

class IntegerSuggestion(
    range: StringRange,
    val value: Int,
    tooltip: Message?,
) : Suggestion(range, value.toString(), tooltip) {
    constructor(range: StringRange, value: Int) : this(range, value, null)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is IntegerSuggestion) {
            return false
        }
        return value == other.value && super.equals(other)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value
        return result
    }

    override fun toString(): String = "IntegerSuggestion{value=$value, range=$range, text='$text', tooltip=$tooltip}"

    override fun compareTo(other: Suggestion): Int {
        if (other is IntegerSuggestion) {
            return value compareTo other.value
        }
        return super.compareTo(other)
    }

    override fun compareToIgnoreCase(other: Suggestion): Int = compareTo(other)
}
