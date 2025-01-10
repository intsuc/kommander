package dev.intsuc.kommander.builder

import dev.intsuc.kommander.tree.CommandNode
import dev.intsuc.kommander.tree.LiteralCommandNode

class LiteralArgumentBuilder<S>(
    val literal: String,
) : ArgumentBuilder<S, LiteralArgumentBuilder<S>>() {
    override fun getThis(): LiteralArgumentBuilder<S> = this

    override fun build(): LiteralCommandNode<S> {
        val result = LiteralCommandNode(literal, command, requirement, redirect, redirectModifier, isFork())

        for (argument in arguments) {
            result.addChild(argument)
        }

        return result
    }

    companion object {
        fun <S> literal(name: String): LiteralArgumentBuilder<S> = LiteralArgumentBuilder(name)
    }
}
