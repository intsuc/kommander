package dev.intsuc.kommander

import dev.intsuc.kommander.tree.CommandNode

fun interface AmbiguityConsumer<S> {
    fun ambiguous(parent: CommandNode<S>, child: CommandNode<S>, sibling: CommandNode<S>, inputs: Collection<String>)
}
