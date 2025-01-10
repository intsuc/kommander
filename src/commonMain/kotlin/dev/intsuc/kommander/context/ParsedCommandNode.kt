package dev.intsuc.kommander.context

import dev.intsuc.kommander.tree.CommandNode

class ParsedCommandNode<S>(
    val node: CommandNode<S>,
    val range: StringRange,
) {
    override fun toString(): String = "$node@$range"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ParsedCommandNode<*>) return false
        return node == other.node && range == other.range
    }

    override fun hashCode(): Int {
        var result = node.hashCode()
        result = 31 * result + range.hashCode()
        return result
    }
}
