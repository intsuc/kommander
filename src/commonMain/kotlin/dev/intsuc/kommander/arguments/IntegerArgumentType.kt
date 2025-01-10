package dev.intsuc.kommander.arguments

import dev.intsuc.kommander.StringReader
import dev.intsuc.kommander.context.CommandContext
import dev.intsuc.kommander.exceptions.CommandSyntaxException

class IntegerArgumentType private constructor(
    val minimum: Int,
    val maximum: Int,
) : ArgumentType<Int> {
    override fun parse(reader: StringReader): Int {
        val start = reader.cursor
        val result = reader.readInt()
        if (result < minimum) {
            reader.cursor = start
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(reader, result, minimum)
        }
        if (result > maximum) {
            reader.cursor = start
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().createWithContext(reader, result, maximum)
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntegerArgumentType) return false

        return maximum == other.maximum && minimum == other.minimum
    }

    override fun hashCode(): Int {
        return 31 * minimum + maximum
    }

    override fun toString(): String = when {
        minimum == Int.MIN_VALUE && maximum == Int.MAX_VALUE -> "integer()"
        maximum == Int.MAX_VALUE -> "integer($minimum)"
        else -> "integer($minimum, $maximum)"
    }

    override val examples: Collection<String> get() = EXAMPLES

    companion object {
        private val EXAMPLES: Collection<String> = listOf("0", "123", "-123")

        fun integer(): IntegerArgumentType = integer(Int.MIN_VALUE)

        fun integer(min: Int): IntegerArgumentType = integer(min, Int.MAX_VALUE)

        fun integer(min: Int, max: Int): IntegerArgumentType = IntegerArgumentType(min, max)

        fun getInt(context: CommandContext<*>, name: String): Int = context.getArgument(name)
    }
}
