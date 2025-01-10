package dev.intsuc.kommander.context

class ParsedArgument<S, T>(
    val range: StringRange,
    val result: T,
) {
    constructor(start: Int, end: Int, result: T) : this(StringRange.between(start, end), result)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is ParsedArgument<*, *>) {
            return false
        }
        return range == other.range && result == other.result
    }

    override fun hashCode(): Int {
        var result = range.hashCode()
        result = 31 * result + this.result.hashCode()
        return result
    }
}
