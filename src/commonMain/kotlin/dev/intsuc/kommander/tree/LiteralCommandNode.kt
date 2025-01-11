package dev.intsuc.kommander.tree

import dev.intsuc.kommander.Command
import dev.intsuc.kommander.RedirectModifier
import dev.intsuc.kommander.StringReader
import dev.intsuc.kommander.builder.ArgumentBuilder
import dev.intsuc.kommander.builder.LiteralArgumentBuilder
import dev.intsuc.kommander.context.CommandContext
import dev.intsuc.kommander.context.CommandContextBuilder
import dev.intsuc.kommander.context.StringRange
import dev.intsuc.kommander.exceptions.CommandSyntaxException
import dev.intsuc.kommander.suggestion.Suggestions
import dev.intsuc.kommander.suggestion.SuggestionsBuilder

class LiteralCommandNode<S>(
    private val literal: String,
    command: Command<S>?,
    requirement: (S) -> Boolean,
    redirect: CommandNode<S>?,
    modifier: RedirectModifier<S>?,
    forks: Boolean,
) : CommandNode<S>(command, requirement, redirect, modifier, forks) {
    private val literalLowerCase: String = literal.lowercase()

    override val name: String get() = literal

    override fun parse(reader: StringReader, contextBuilder: CommandContextBuilder<S>) {
        val start = reader.cursor
        val end = parse(reader)
        if (end > -1) {
            contextBuilder.withNode(this, StringRange.between(start, end))
            return
        }

        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().createWithContext(reader, literal)
    }

    private fun parse(reader: StringReader): Int {
        val start = reader.cursor
        if (reader.canRead(literal.length)) {
            val end = start + literal.length
            if (reader.string.substring(start, end) == literal) {
                reader.cursor = end
                if (!reader.canRead() || reader.peek() == ' ') {
                    return end
                } else {
                    reader.cursor = start
                }
            }
        }
        return -1
    }

    override suspend fun listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): Suggestions {
        return if (literalLowerCase.startsWith(builder.remainingLowerCase)) {
            builder.suggest(literal).build()
        } else {
            Suggestions.empty()
        }
    }

    override fun isValidInput(input: String): Boolean {
        return parse(StringReader(input)) > -1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LiteralCommandNode<*>) return false

        if (literal != other.literal) return false
        return super.equals(other)
    }

    override val usageText: String get() = literal

    override fun hashCode(): Int {
        var result = literal.hashCode()
        result = 31 * result + super.hashCode()
        return result
    }

    override fun createBuilder(): ArgumentBuilder<S, *> {
        val builder = LiteralArgumentBuilder.literal<S>(literal)
        builder.requires(requirement)
        builder.forward(redirect, redirectModifier, isFork())
        if (command != null) {
            builder.executes(command)
        }
        return builder
    }

    override val sortedKey: String get() = literal

    override val examples: Collection<String> get() = listOf(literal)

    override fun toString(): String = "<literal $literal>"
}
