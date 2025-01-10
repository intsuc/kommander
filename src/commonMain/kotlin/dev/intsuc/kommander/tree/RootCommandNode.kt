package dev.intsuc.kommander.tree

import dev.intsuc.kommander.StringReader
import dev.intsuc.kommander.builder.ArgumentBuilder
import dev.intsuc.kommander.context.CommandContext
import dev.intsuc.kommander.context.CommandContextBuilder
import dev.intsuc.kommander.suggestion.Suggestions
import dev.intsuc.kommander.suggestion.SuggestionsBuilder

class RootCommandNode<S> : CommandNode<S>(
    null,
    { true },
    null,
    { s -> listOf(s.source) },
    false,
) {
    override val name: String get() = ""

    override val usageText: String get() = ""

    override fun parse(reader: StringReader, contextBuilder: CommandContextBuilder<S>) = Unit

    override fun listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder) = Suggestions.empty()

    override fun isValidInput(input: String): Boolean = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RootCommandNode<*>) return false
        return super.equals(other)
    }

    override fun createBuilder(): ArgumentBuilder<S, *> {
        throw IllegalStateException("Cannot convert root into a builder")
    }

    override val sortedKey: String get() = ""

    override val examples: Collection<String> get() = emptyList()

    override fun toString(): String = "<root>"
}
