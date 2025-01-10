package dev.intsuc.kommander.context

import dev.intsuc.kommander.tree.CommandNode

class SuggestionContext<S>(
    val parent: CommandNode<S>,
    val startPos: Int,
)
