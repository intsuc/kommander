package dev.intsuc.kommander.context

import dev.intsuc.kommander.ImmutableStringReader

class StringRange(
    val start: Int,
    val end: Int,
) {
    fun get(reader: ImmutableStringReader): String = reader.string.substring(start, end)

    fun get(string: String): String = string.substring(start, end)

    fun isEmpty(): Boolean = start == end

    val length: Int get() = end - start

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is StringRange) {
            return false
        }
        return start == other.start && end == other.end
    }

    override fun hashCode(): Int {
        var result = start
        result = 31 * result + end
        return result
    }

    override fun toString(): String = "StringRange{start=$start, end=$end}"

    companion object {
        fun at(pos: Int): StringRange = StringRange(pos, pos)

        fun between(start: Int, end: Int): StringRange = StringRange(start, end)

        fun encompassing(a: StringRange, b: StringRange): StringRange {
            return StringRange(minOf(a.start, b.start), maxOf(a.end, b.end))
        }
    }
}
