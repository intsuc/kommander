package dev.intsuc.kommander.arguments

import dev.intsuc.kommander.StringReader
import dev.intsuc.kommander.context.CommandContext
import dev.intsuc.kommander.suggestion.Suggestions
import dev.intsuc.kommander.suggestion.SuggestionsBuilder

class BoolArgumentType private constructor() : ArgumentType<Boolean> {
    override fun parse(reader: StringReader): Boolean {
        return reader.readBoolean()
    }

    override fun <S> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): Suggestions {
        if ("true".startsWith(builder.remainingLowerCase)) {
            builder.suggest("true")
        }
        if ("false".startsWith(builder.remainingLowerCase)) {
            builder.suggest("false")
        }
        return builder.buildFuture()
    }

    override val examples: Collection<String> get() = EXAMPLES

    companion object {
        private val EXAMPLES: Collection<String> = listOf("true", "false")

        fun bool(): BoolArgumentType = BoolArgumentType()

        fun getBool(context: CommandContext<*>, name: String): Boolean = context.getArgument(name)
    }
}
