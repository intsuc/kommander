package dev.intsuc.kommander.arguments

import dev.intsuc.kommander.StringReader
import dev.intsuc.kommander.context.CommandContext

class StringArgumentType(
    val type: StringType,
) : ArgumentType<String> {
    override fun parse(reader: StringReader): String {
        return when (type) {
            StringType.GREEDY_PHRASE -> {
                val text = reader.remaining
                reader.cursor = reader.totalLength
                text
            }

            StringType.SINGLE_WORD -> reader.readUnquotedString()
            StringType.QUOTABLE_PHRASE -> reader.readString()
        }
    }

    override fun toString(): String = "string()"

    override val examples: Collection<String> get() = type.examples

    enum class StringType(vararg examples: String) {
        SINGLE_WORD("word", "words_with_underscores"),
        QUOTABLE_PHRASE("\"quoted phrase\"", "word", "\"\""),
        GREEDY_PHRASE("word", "words with spaces", "\"and symbols\"");

        private val _examples: List<String> = examples.toList()
        val examples: Collection<String> get() = _examples
    }

    companion object {
        fun word(): StringArgumentType = StringArgumentType(StringType.SINGLE_WORD)

        fun string(): StringArgumentType = StringArgumentType(StringType.QUOTABLE_PHRASE)

        fun greedyString(): StringArgumentType = StringArgumentType(StringType.GREEDY_PHRASE)

        fun getString(context: CommandContext<*>, name: String): String = context.getArgument(name)

        fun escapeIfRequired(input: String): String {
            for (c in input) {
                if (!StringReader.isAllowedInUnquotedString(c)) {
                    return escape(input)
                }
            }
            return input
        }

        private fun escape(input: String): String {
            val result = StringBuilder("\"")

            for (c in input) {
                if (c == '\\' || c == '"') {
                    result.append('\\')
                }
                result.append(c)
            }

            result.append('"')
            return result.toString()
        }
    }
}
