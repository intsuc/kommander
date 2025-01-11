package dev.intsuc.kommander.arguments

import dev.intsuc.kommander.StringReader
import dev.intsuc.kommander.context.CommandContext
import dev.intsuc.kommander.suggestion.Suggestions
import dev.intsuc.kommander.suggestion.SuggestionsBuilder

interface ArgumentType<T> {
    fun parse(reader: StringReader): T

    fun <S> parse(reader: StringReader, source: S): T {
        return parse(reader)
    }

    suspend fun <S> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): Suggestions {
        return Suggestions.empty()
    }

    val examples: Collection<String> get() = emptyList()
}
