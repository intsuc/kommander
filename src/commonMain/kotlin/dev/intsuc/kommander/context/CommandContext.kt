package dev.intsuc.kommander.context

import dev.intsuc.kommander.Command
import dev.intsuc.kommander.RedirectModifier
import dev.intsuc.kommander.tree.CommandNode

class CommandContext<S>(
    val source: S,
    val input: String,
    val arguments: Map<String, ParsedArgument<S, *>>,
    val command: Command<S>?,
    val rootNode: CommandNode<S>,
    val nodes: List<ParsedCommandNode<S>>,
    val range: StringRange?,
    val child: CommandContext<S>?,
    val redirectModifier: RedirectModifier<S>?,
    private val forks: Boolean,
) {
    fun copyFor(source: S): CommandContext<S> {
        if (this.source === source) {
            return this
        }
        return CommandContext(source, input, arguments, command, rootNode, nodes, range, child, redirectModifier, forks)
    }

    val lastChild: CommandContext<S>
        get() {
            var result = this
            while (result.child != null) {
                result = result.child
            }
            return result
        }

    inline fun <reified V> getArgument(name: String): V {
        val argument = arguments[name] ?: throw IllegalArgumentException("No such argument '$name' exists on this command")

        val result = argument.result
        return if (result is V) {
            result
        } else {
            throw IllegalArgumentException("Argument '$name' is defined as ${result!!::class.simpleName}, not ${V::class.simpleName}")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CommandContext<*>) return false

        if (arguments != other.arguments) return false
        if (rootNode != other.rootNode) return false
        if (nodes.size != other.nodes.size || nodes != other.nodes) return false
        if (if (command != null) command != other.command else other.command != null) return false
        if (source != other.source) return false
        if (if (child != null) child != other.child else other.child != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + arguments.hashCode()
        result = 31 * result + command.hashCode()
        result = 31 * result + rootNode.hashCode()
        result = 31 * result + nodes.hashCode()
        result = 31 * result + child.hashCode()
        return result
    }

    fun hasNodes(): Boolean = nodes.isNotEmpty()

    fun isForked(): Boolean = forks
}
