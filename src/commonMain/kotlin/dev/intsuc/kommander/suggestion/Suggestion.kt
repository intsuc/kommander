package dev.intsuc.kommander.suggestion

import dev.intsuc.kommander.Message
import dev.intsuc.kommander.context.StringRange

open class Suggestion(
    val range: StringRange,
    val text: String,
    val tooltip: Message?,
) : Comparable<Suggestion> {
    constructor(range: StringRange, text: String) : this(range, text, null)

    fun apply(input: String): String {
        if (range.start == 0 && range.end == input.length) {
            return text
        }
        val result = StringBuilder()
        if (range.start > 0) {
            result.append(input.substring(0, range.start))
        }
        result.append(text)
        if (range.end < input.length) {
            result.append(input.substring(range.end))
        }
        return result.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Suggestion) {
            return false
        }
        return range == other.range && text == other.text && tooltip == other.tooltip
    }

    override fun hashCode(): Int {
        var result = range.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + tooltip.hashCode()
        return result
    }

    override fun toString(): String = "Suggestion{range=$range, text='$text', tooltip='$tooltip'}"

    override fun compareTo(other: Suggestion): Int = text compareTo other.text

    open fun compareToIgnoreCase(other: Suggestion): Int = text.compareTo(other.text, true)

    fun expand(command: String, range: StringRange): Suggestion {
        if (range == this.range) {
            return this
        }
        val result = StringBuilder()
        if (range.start < this.range.start) {
            result.append(command.substring(range.start, this.range.start))
        }
        result.append(text)
        if (range.end > this.range.end) {
            result.append(command.substring(this.range.end, range.end))
        }
        return Suggestion(range, result.toString(), tooltip)
    }
}
