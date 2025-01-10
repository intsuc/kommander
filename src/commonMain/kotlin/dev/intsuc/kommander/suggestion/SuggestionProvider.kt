package dev.intsuc.kommander.suggestion

import dev.intsuc.kommander.context.CommandContext

fun interface SuggestionProvider<S> {
    fun getSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): Suggestions
}
