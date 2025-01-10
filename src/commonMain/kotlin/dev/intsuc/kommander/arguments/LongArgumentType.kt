package dev.intsuc.kommander.arguments

import dev.intsuc.kommander.StringReader
import dev.intsuc.kommander.context.CommandContext
import dev.intsuc.kommander.exceptions.CommandSyntaxException

class LongArgumentType private constructor(
    val minimum: Long,
    val maximum: Long,
) : ArgumentType<Long> {
    override fun parse(reader: StringReader): Long {
        val start = reader.cursor
        val result = reader.readLong()
        if (result < minimum) {
            reader.cursor = start
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.longTooLow().createWithContext(reader, result, minimum)
        }
        if (result > maximum) {
            reader.cursor = start
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.longTooHigh().createWithContext(reader, result, maximum)
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LongArgumentType) return false

        return maximum == other.maximum && minimum == other.minimum
    }

    override fun hashCode(): Int {
        return 31 * minimum.hashCode() + maximum.hashCode()
    }

    override fun toString(): String = when {
        minimum == Long.MIN_VALUE && maximum == Long.MAX_VALUE -> "longArg()"
        maximum == Long.MAX_VALUE -> "longArg($minimum)"
        else -> "longArg($minimum, $maximum)"
    }

    override val examples: Collection<String> get() = EXAMPLES

    companion object {
        private val EXAMPLES: Collection<String> = listOf("0", "123", "-123")

        fun long(): LongArgumentType = long(Long.MIN_VALUE)

        fun long(min: Long): LongArgumentType = long(min, Long.MAX_VALUE)

        fun long(min: Long, max: Long): LongArgumentType = LongArgumentType(min, max)

        fun getLong(context: CommandContext<*>, name: String): Long = context.getArgument(name)
    }
}
