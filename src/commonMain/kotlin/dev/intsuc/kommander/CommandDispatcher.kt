package dev.intsuc.kommander

import dev.intsuc.kommander.builder.LiteralArgumentBuilder
import dev.intsuc.kommander.context.CommandContextBuilder
import dev.intsuc.kommander.context.ContextChain
import dev.intsuc.kommander.exceptions.CommandSyntaxException
import dev.intsuc.kommander.suggestion.Suggestions
import dev.intsuc.kommander.suggestion.SuggestionsBuilder
import dev.intsuc.kommander.tree.CommandNode
import dev.intsuc.kommander.tree.LiteralCommandNode
import dev.intsuc.kommander.tree.RootCommandNode

class CommandDispatcher<S>(
    val root: RootCommandNode<S>,
) {
    private val hasCommand: (CommandNode<S>?) -> Boolean = { input ->
        input != null && (input.command != null || input.children.any(hasCommand))
    }
    var consumer: ResultConsumer<S> = ResultConsumer { c, s, r -> }

    constructor() : this(RootCommandNode())

    fun register(command: LiteralArgumentBuilder<S>): LiteralCommandNode<S> {
        val build = command.build()
        root.addChild(build)
        return build
    }

    fun execute(input: String, source: S): Int = execute(StringReader(input), source)

    fun execute(input: StringReader, source: S): Int {
        val parse = parse(input, source)
        return execute(parse)
    }

    fun execute(parse: ParseResults<S>): Int {
        if (parse.reader.canRead()) {
            if (parse.exceptions.size == 1) {
                throw parse.exceptions.values.first()
            } else if (parse.context.range.isEmpty()) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parse.reader)
            } else {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(parse.reader)
            }
        }

        val command = parse.reader.string
        val original = parse.context.build(command)

        val flatContext = ContextChain.tryFlatten(original)
        if (flatContext == null) {
            consumer.onCommandComplete(original, false, 0)
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parse.reader)
        }

        return flatContext.executeAll(original.source, consumer)
    }

    fun parse(command: String, source: S): ParseResults<S> = parse(StringReader(command), source)

    fun parse(command: StringReader, source: S): ParseResults<S> {
        val context = CommandContextBuilder(this, source, root, command.cursor)
        return parseNodes(root, command, context)
    }

    private fun parseNodes(node: CommandNode<S>, originalReader: StringReader, contextSoFar: CommandContextBuilder<S>): ParseResults<S> {
        val source = contextSoFar.source
        var errors: MutableMap<CommandNode<S>, CommandSyntaxException>? = null
        var potentials: MutableList<ParseResults<S>>? = null
        val cursor: Int = originalReader.cursor

        for (child in node.getRelevantNodes(originalReader)) {
            if (!child.canUse(source)) {
                continue
            }
            val context = contextSoFar.copy()
            val reader = StringReader(originalReader)
            try {
                try {
                    child.parse(reader, context)
                } catch (ex: RuntimeException) {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().createWithContext(reader, ex.message)
                }
                if (reader.canRead()) {
                    if (reader.peek() != ARGUMENT_SEPARATOR_CHAR) {
                        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherExpectedArgumentSeparator().createWithContext(reader)
                    }
                }
            } catch (ex: CommandSyntaxException) {
                if (errors == null) {
                    errors = linkedMapOf()
                }
                errors[child] = ex
                reader.cursor = cursor
                continue
            }

            context.withCommand(child.command)
            if (reader.canRead(if (child.redirect == null) 2 else 1)) {
                reader.skip()
                if (child.redirect != null) {
                    val childContext = CommandContextBuilder(this, source, child.redirect, reader.cursor)
                    val parse = parseNodes(child.redirect, reader, childContext)
                    context.withChild(parse.context)
                    return ParseResults(context, parse.reader, parse.exceptions)
                } else {
                    val parse = parseNodes(child, reader, context)
                    if (potentials == null) {
                        potentials = ArrayList(1)
                    }
                    potentials += parse
                }
            } else {
                if (potentials == null) {
                    potentials = ArrayList(1)
                }
                potentials += ParseResults(context, reader, emptyMap())
            }
        }

        if (potentials != null) {
            if (potentials.size > 1) {
                potentials.sortWith { a, b ->
                    when {
                        !a.reader.canRead() && b.reader.canRead() -> -1
                        a.reader.canRead() && !b.reader.canRead() -> 1
                        a.exceptions.isEmpty() && !b.exceptions.isEmpty() -> -1
                        !a.exceptions.isEmpty() && b.exceptions.isEmpty() -> 1
                        else -> 0
                    }
                }
            }
            return potentials.first()
        }

        return ParseResults(contextSoFar, originalReader, errors ?: emptyMap())
    }

    fun getAllUsage(node:  CommandNode<S>, source: S, restricted: Boolean): Array<String> {
        val result = ArrayList<String>()
        getAllUsage(node, source, result, "", restricted)
        return result.toTypedArray()
    }

    private fun getAllUsage(node: CommandNode<S>, source: S, result: ArrayList<String>, prefix: String, restricted: Boolean) {
        if (restricted && !node.canUse(source)) {
            return
        }

        if (node.command != null) {
            result += prefix
        }

        if (node.redirect != null) {
            val redirect = if (node.redirect === root) "..." else "-> ${node.redirect.usageText}"
            result += if (prefix.isEmpty()) "${node.usageText}$ARGUMENT_SEPARATOR$redirect" else "$prefix$ARGUMENT_SEPARATOR$redirect"
        } else if (node.children.isNotEmpty()) {
            for (child in node.children) {
                getAllUsage(child, source, result, if (prefix.isEmpty()) child.usageText else "$prefix$ARGUMENT_SEPARATOR${child.usageText}", restricted)
            }
        }
    }

    fun getSmartUsage(node: CommandNode<S>, source: S): Map<CommandNode<S>, String> {
        val result = linkedMapOf<CommandNode<S>, String>()

        val optional = node.command != null
        for (child in node.children) {
            val usage = getSmartUsage(child, source, optional, false)
            if (usage != null) {
                result[child] = usage
            }
        }
        return result
    }

    private fun getSmartUsage(node: CommandNode<S>, source: S, optional: Boolean, deep: Boolean): String? {
        if (!node.canUse(source)) {
            return null
        }

        val self = if (optional) "$USAGE_OPTIONAL_OPEN${node.usageText}$USAGE_OPTIONAL_CLOSE" else node.usageText
        val childOptional = node.command != null
        val open = if (childOptional) USAGE_OPTIONAL_OPEN else USAGE_REQUIRED_OPEN
        val close = if (childOptional) USAGE_OPTIONAL_CLOSE else USAGE_REQUIRED_CLOSE

        if (!deep) {
            if (node.redirect != null) {
                val redirect = if (node.redirect === root) "..." else "-> ${node.redirect.usageText}"
                return "$self$ARGUMENT_SEPARATOR$redirect"
            } else {
                val children = node.children.filter { c -> c.canUse(source) }
                if (children.size == 1) {
                    val usage = getSmartUsage(children.first(), source, childOptional, childOptional)
                    if (usage != null) {
                        return "$self$ARGUMENT_SEPARATOR$usage"
                    }
                } else if (children.size > 1) {
                    val childUsage = linkedSetOf<String>()
                    for (child in children) {
                        val usage = getSmartUsage(child, source, childOptional, true)
                        if (usage != null) {
                            childUsage += usage
                        }
                    }
                    if (childUsage.size == 1) {
                        val usage = childUsage.first()
                        return "$self$ARGUMENT_SEPARATOR${if (childOptional) "$USAGE_OPTIONAL_OPEN$usage$USAGE_OPTIONAL_CLOSE" else usage}"
                    } else if (childUsage.size > 1) {
                        val builder = StringBuilder(open)
                        var count = 0
                        for (child in children)  {
                            if (count > 0) {
                                builder.append(USAGE_OR)
                            }
                            builder.append(child.usageText)
                            count++
                        }
                        if (count > 0) {
                            builder.append(close)
                            return "$self$ARGUMENT_SEPARATOR$builder"
                        }
                    }
                }
            }
        }

        return self
    }

    fun getCompletionSuggestions(parse: ParseResults<S>): Suggestions {
        return getCompletionSuggestions(parse, parse.reader.totalLength)
    }

    fun getCompletionSuggestions(parse: ParseResults<S>, cursor: Int): Suggestions {
        val context = parse.context

        val nodeBeforeCursor = context.findSuggestionContext(cursor)
        val parent = nodeBeforeCursor.parent
        val start = minOf(nodeBeforeCursor.startPos, cursor)

        val fullInput = parse.reader.string
        val truncatedInput = fullInput.substring(0, cursor)
        val truncatedInputLowerCase = truncatedInput.lowercase()
        val futures = Array<Suggestions?>(parent.children.size) { null }
        var i = 0
        for (node in parent.children) {
            var future = Suggestions.empty()
            try {
                future = node.listSuggestions(context.build(truncatedInput), SuggestionsBuilder(truncatedInput, truncatedInputLowerCase, start))
            } catch (_: CommandSyntaxException) {
            }
            futures[i++] = future
        }

        val suggestions = mutableListOf<Suggestions>()
        for (future in futures) {
            suggestions += future!!
        }
        return Suggestions.merge(fullInput, suggestions)
    }

    fun getPath(target: CommandNode<S>): Collection<String> {
        val nodes = mutableListOf<List<CommandNode<S>>>()
        addPaths(root, nodes, mutableListOf())

        for (list in nodes) {
            if (list.last() === target) {
                val result = mutableListOf<String>()
                for (node in list) {
                    if (node !== root) {
                        result += node.name
                    }
                }
                return result
            }
        }

        return emptyList()
    }

    fun findNode(path: Collection<String>): CommandNode<S>? {
        var node: CommandNode<S> = root
        for (name in path) {
            node = node.getChild(name) ?: return null
        }
        return node
    }

    fun findAmbiguities(consumer: AmbiguityConsumer<S>) {
        root.findAmbiguities(consumer)
    }

    private fun addPaths(node: CommandNode<S>, result: MutableList<List<CommandNode<S>>>, parents: List<CommandNode<S>>) {
        val current = parents.toMutableList()
        current += node
        result += current

        for (child in node.children) {
            addPaths(child, result, current)
        }
    }

    companion object {
        const val ARGUMENT_SEPARATOR: String = " "

        const val ARGUMENT_SEPARATOR_CHAR: Char = ' '

        private const val USAGE_OPTIONAL_OPEN: String = "["
        private const val USAGE_OPTIONAL_CLOSE: String = "]"
        private const val USAGE_REQUIRED_OPEN: String = "("
        private const val USAGE_REQUIRED_CLOSE: String = ")"
        private const val USAGE_OR: String = "|"
    }
}
