package dev.intsuc.kommander.arguments

import dev.intsuc.kommander.StringReader
import dev.intsuc.kommander.context.CommandContext
import dev.intsuc.kommander.exceptions.CommandSyntaxException

class DoubleArgumentType private constructor(
    val minimum: Double,
    val maximum: Double,
) : ArgumentType<Double> {
    override fun parse(reader: StringReader): Double {
        val start = reader.cursor
        val result = reader.readDouble()
        if (result < minimum) {
            reader.cursor = start
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooLow().createWithContext(reader, result, minimum)
        }
        if (result > maximum) {
            reader.cursor = start
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooHigh().createWithContext(reader, result, maximum)
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DoubleArgumentType) return false

        return maximum == other.maximum && minimum == other.minimum
    }

    override fun hashCode(): Int {
        return (31.0 * minimum + maximum).toInt()
    }

    override fun toString(): String = when {
        minimum == -Double.MAX_VALUE && maximum == Double.MAX_VALUE -> "double()"
        maximum == Double.MAX_VALUE -> "double($minimum)"
        else -> "double($minimum, $maximum)"
    }

    override val examples: Collection<String> get() = EXAMPLES

    companion object {
        private val EXAMPLES: Collection<String> = listOf("0", "1.2", ".5", "-1", "-.5", "-1234.56")

        fun double(): DoubleArgumentType = double(-Double.MAX_VALUE)

        fun double(min: Double): DoubleArgumentType = double(min, Double.MAX_VALUE)

        fun double(min: Double, max: Double): DoubleArgumentType = DoubleArgumentType(min, max)

        fun getDouble(context: CommandContext<*>, name: String): Double = context.getArgument(name)
    }
}
