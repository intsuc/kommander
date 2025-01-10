package dev.intsuc.kommander.arguments

import dev.intsuc.kommander.StringReader
import dev.intsuc.kommander.context.CommandContext
import dev.intsuc.kommander.exceptions.CommandSyntaxException

class FloatArgumentType private constructor(
    val minimum: Float,
    val maximum: Float,
) : ArgumentType<Float> {
    override fun parse(reader: StringReader): Float {
        val start = reader.cursor
        val result = reader.readFloat()
        if (result < minimum) {
            reader.cursor = start
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.floatTooLow().createWithContext(reader, result, minimum)
        }
        if (result > maximum) {
            reader.cursor = start
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.floatTooHigh().createWithContext(reader, result, maximum)
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FloatArgumentType) return false

        return maximum == other.maximum && minimum == other.minimum
    }

    override fun hashCode(): Int {
        return (31.0 * minimum + maximum).toInt()
    }

    override fun toString(): String = when {
        minimum == -Float.MAX_VALUE && maximum == Float.MAX_VALUE -> "float()"
        maximum == Float.MAX_VALUE -> "float($minimum)"
        else -> "float($minimum, $maximum)"
    }

    override val examples: Collection<String> get() = EXAMPLES

    companion object {
        private val EXAMPLES: Collection<String> = listOf("0", "1.2", ".5", "-1", "-.5", "-1234.56")

        fun float(): FloatArgumentType = float(-Float.MAX_VALUE)

        fun float(min: Float): FloatArgumentType = float(min, Float.MAX_VALUE)

        fun float(min: Float, max: Float): FloatArgumentType = FloatArgumentType(min, max)

        fun getFloat(context: CommandContext<*>, name: String): Float = context.getArgument(name)
    }
}
