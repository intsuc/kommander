package dev.intsuc.kommander

import dev.intsuc.kommander.context.CommandContextBuilder
import dev.intsuc.kommander.exceptions.CommandSyntaxException
import dev.intsuc.kommander.tree.CommandNode

class ParseResults<S>(
    private val context: CommandContextBuilder<S>,
    private val reader: ImmutableStringReader,
    private val exceptions: Map<CommandNode<S>, CommandSyntaxException>,
) {
    constructor(context: CommandContextBuilder<S>) : this(context, StringReader(""), emptyMap())
}
