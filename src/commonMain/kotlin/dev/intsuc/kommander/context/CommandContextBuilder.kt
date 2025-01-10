package dev.intsuc.kommander.context

import dev.intsuc.kommander.Command
import dev.intsuc.kommander.CommandDispatcher
import dev.intsuc.kommander.RedirectModifier
import dev.intsuc.kommander.tree.CommandNode

class CommandContextBuilder<S> {
    private val _arguments: MutableMap<String, ParsedArgument<S, *>> = linkedMapOf()
    private val rootNode: CommandNode<S>
    private val _nodes: MutableList<ParsedCommandNode<S>> = mutableListOf()
    val dispatcher: CommandDispatcher<S>
    var source: S
    var command: Command<S>? = null
        private set
    private var child: CommandContextBuilder<S>? = null
    var range: StringRange
        private set
    private var modifier: RedirectModifier<S>? = null
    private var forks: Boolean = false

    constructor(dispatcher: CommandDispatcher<S>, source: S, rootNode: CommandNode<S>, start: Int) {
        this.rootNode = rootNode
        this.dispatcher = dispatcher
        this.source = source
        this.range = StringRange.at(start)
    }

    fun withSource(source: S): CommandContextBuilder<S> {
        this.source = source
        return this
    }

    fun withArgument(name: String, argument: ParsedArgument<S, *>): CommandContextBuilder<S> {
        _arguments[name] = argument
        return this
    }

    val arguments: Map<String, ParsedArgument<S, *>> get() = _arguments

    fun withCommand(command: Command<S>?): CommandContextBuilder<S> {
        this.command = command
        return this
    }

    fun withNode(node: CommandNode<S>, range: StringRange): CommandContextBuilder<S> {
        _nodes.add(ParsedCommandNode(node, range))
        this.range = StringRange.encompassing(this.range, range)
        this.modifier = node.redirectModifier
        this.forks = node.isFork()
        return this
    }

    fun copy(): CommandContextBuilder<S> {
        val copy = CommandContextBuilder(dispatcher, source!!, rootNode, range.start)
        copy.command = command
        copy._arguments += _arguments
        copy._nodes += _nodes
        copy.child = child
        copy.range = range
        copy.forks = forks
        return copy
    }

    fun withChild(child: CommandContextBuilder<S>): CommandContextBuilder<S> {
        this.child = child
        return this
    }

    val lastChild: CommandContextBuilder<S>
        get() {
            var result = this
            while (result.child != null) {
                result = result.child!!
            }
            return result
        }

    val nodes: List<ParsedCommandNode<S>> get() = _nodes

    fun build(input: String): CommandContext<S> {
        return CommandContext(source, input, arguments, command, rootNode, nodes, range, child?.build(input), modifier, forks)
    }

    fun findSuggestionContext(cursor: Int): SuggestionContext<S> {
        if (range.start <= cursor) {
            if (range.end < cursor) {
                if (child != null) {
                    return child!!.findSuggestionContext(cursor)
                } else if (_nodes.isNotEmpty()) {
                    val last = _nodes.last()
                    return SuggestionContext(last.node, last.range.end + 1)
                } else {
                    return SuggestionContext(rootNode, range.start)
                }
            } else {
                var prev = rootNode
                for (node in _nodes) {
                    val nodeRange = node.range
                    if (nodeRange.start <= cursor && cursor < nodeRange.end) {
                        return SuggestionContext(prev, nodeRange.start)
                    }
                    prev = node.node
                }
                // if (prev == null) {
                //     throw IllegalStateException("Can't find node before cursor")
                // }
                return SuggestionContext(prev, range.start)
            }
        }
        throw IllegalStateException("Can't find node before cursor")
    }
}
