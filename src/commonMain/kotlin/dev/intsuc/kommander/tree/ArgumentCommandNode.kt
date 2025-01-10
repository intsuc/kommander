package dev.intsuc.kommander.tree

import dev.intsuc.kommander.Command
import dev.intsuc.kommander.RedirectModifier
import dev.intsuc.kommander.StringReader
import dev.intsuc.kommander.arguments.ArgumentType
import dev.intsuc.kommander.builder.RequiredArgumentBuilder
import dev.intsuc.kommander.context.CommandContext
import dev.intsuc.kommander.context.CommandContextBuilder
import dev.intsuc.kommander.context.ParsedArgument
import dev.intsuc.kommander.exceptions.CommandSyntaxException
import dev.intsuc.kommander.suggestion.SuggestionProvider
import dev.intsuc.kommander.suggestion.Suggestions
import dev.intsuc.kommander.suggestion.SuggestionsBuilder

class ArgumentCommandNode<S, T>(
    override val name: String,
    val type: ArgumentType<T>,
    command: Command<S>?,
    requirement: (S) -> Boolean,
    redirect: CommandNode<S>?,
    modifier: RedirectModifier<S>?,
    forks: Boolean,
    private val customSuggestions: SuggestionProvider<S>?,
) : CommandNode<S>(command, requirement, redirect, modifier, forks) {
    override val usageText: String get() = "$USAGE_ARGUMENT_OPEN$name$USAGE_ARGUMENT_CLOSE"

    override fun parse(reader: StringReader, contextBuilder: CommandContextBuilder<S>) {
        val start = reader.cursor
        val result = type.parse(reader, contextBuilder.source)
        val parsed = ParsedArgument<S, _>(start, reader.cursor, result)

        contextBuilder.withArgument(name, parsed)
        contextBuilder.withNode(this, parsed.range)
    }

    override fun listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): Suggestions {
        return customSuggestions?.getSuggestions(context, builder) ?: type.listSuggestions(context, builder)
    }

    override fun createBuilder(): RequiredArgumentBuilder<S, *> {
        val builder = RequiredArgumentBuilder.argument<S, _>(name, type)
        builder.requires(requirement)
        builder.forward(redirect, redirectModifier, isFork())
        builder.suggests(customSuggestions)
        if (command != null) {
            builder.executes(command)
        }
        return builder
    }

    override fun isValidInput(input: String): Boolean {
        try {
            val reader = StringReader(input)
            type.parse(reader)
            return !reader.canRead() || reader.peek() == ' '
        } catch (_: CommandSyntaxException) {
            return false
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArgumentCommandNode<*, *>) return false

        if (name != other.name) return false
        if (type != other.type) return false
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override val sortedKey: String get() = name

    override val examples: Collection<String> get() = type.examples

    override fun toString(): String = "<argument $name:$type>"

    companion object {
        private const val USAGE_ARGUMENT_OPEN: String = "<"
        private const val USAGE_ARGUMENT_CLOSE: String = ">"
    }
}
