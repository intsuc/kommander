package dev.intsuc.kommander.builder

import dev.intsuc.kommander.tree.CommandNode
import dev.intsuc.kommander.tree.LiteralCommandNode

class LiteralArgumentBuilder<S>(
    val literal: String,
) : ArgumentBuilder<S, LiteralArgumentBuilder<S>>() {
    override fun getThis(): LiteralArgumentBuilder<S> = this

    override fun build(): CommandNode<S> {
        val result = LiteralCommandNode(literal, command, requirement, redirect, redirectModifier, isFork())

        for (argument in arguments) {
            result.addChild(argument)
        }

        return result
    }

    companion object {
        fun literal(name: String): LiteralArgumentBuilder<String> = LiteralArgumentBuilder(name)
    }
}
