package dev.intsuc.kommander.tree

import dev.intsuc.kommander.AmbiguityConsumer
import dev.intsuc.kommander.Command
import dev.intsuc.kommander.RedirectModifier
import dev.intsuc.kommander.StringReader
import dev.intsuc.kommander.builder.ArgumentBuilder
import dev.intsuc.kommander.context.CommandContext
import dev.intsuc.kommander.context.CommandContextBuilder
import dev.intsuc.kommander.suggestion.Suggestions
import dev.intsuc.kommander.suggestion.SuggestionsBuilder

abstract class CommandNode<S>(
    command: Command<S>?,
    val requirement: (S) -> Boolean,
    val redirect: CommandNode<S>?,
    val redirectModifier: RedirectModifier<S>?,
    private val forks: Boolean
) : Comparable<CommandNode<S>> {
    private val _children: MutableMap<String, CommandNode<S>> = linkedMapOf()
    private val literals: MutableMap<String, LiteralCommandNode<S>> = linkedMapOf()
    private val arguments: MutableMap<String, ArgumentCommandNode<S, *>> = linkedMapOf()
    var command: Command<S>? = command
        private set

    val children: Collection<CommandNode<S>> get() = _children.values

    fun getChild(name: String): CommandNode<S>? = _children[name]

    fun canUse(source: S): Boolean = requirement(source)

    fun addChild(node: CommandNode<S>) {
        if (node is RootCommandNode) {
            throw UnsupportedOperationException("Cannot add a RootCommandNode as a child to any other CommandNode")
        }

        val child = _children[node.name]
        if (child != null) {
            // We've found something to merge onto
            if (node.command != null) {
                child.command = node.command
            }
            for (grandchild in node.children) {
                child.addChild(grandchild)
            }
        } else {
            _children[node.name] = node
            if (node is LiteralCommandNode) {
                literals[node.name] = node
            } else if (node is ArgumentCommandNode<S, *>) {
                arguments[node.name] = node
            }
        }
    }

    fun findAmbiguities(consumer: AmbiguityConsumer<S>) {
        var matches = hashSetOf<String>()

        for (child in _children.values) {
            for (sibling in _children.values) {
                if (child === sibling) {
                    continue
                }

                for (input in child.examples) {
                    if (sibling.isValidInput(input)) {
                        matches += input
                    }
                }

                if (matches.isNotEmpty()) {
                    consumer.ambiguous(this, child, sibling, matches)
                    matches = hashSetOf()
                }
            }

            child.findAmbiguities(consumer)
        }
    }

    protected abstract fun isValidInput(input: String): Boolean

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CommandNode<*>) return false

        if (_children != other._children) return false
        if (if (command != null) command != other.command else other.command != null) return false

        return true
    }

    override fun hashCode(): Int {
        return 31 * _children.hashCode() + (command?.hashCode() ?: 0)
    }

    abstract val name: String

    abstract val usageText: String

    abstract fun parse(reader: StringReader, contextBuilder: CommandContextBuilder<S>)

    abstract suspend fun listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): Suggestions

    abstract fun createBuilder(): ArgumentBuilder<S, *>

    protected abstract val sortedKey: String

    fun getRelevantNodes(input: StringReader): Collection<CommandNode<S>> {
        if (literals.isNotEmpty()) {
            val cursor = input.cursor
            while (input.canRead() && input.peek() != ' ') {
                input.skip()
            }
            val text = input.string.substring(cursor, input.cursor)
            input.cursor = cursor
            val literal = literals[text]
            if (literal != null) {
                return listOf(literal)
            } else {
                return arguments.values
            }
        } else {
            return arguments.values
        }
    }

    override fun compareTo(other: CommandNode<S>): Int {
        if (this is LiteralCommandNode == other is LiteralCommandNode) {
            return sortedKey compareTo other.sortedKey
        }

        return if (other is LiteralCommandNode) 1 else -1
    }

    fun isFork(): Boolean = forks

    abstract val examples: Collection<String>
}
