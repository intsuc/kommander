package dev.intsuc.kommander.builder

import dev.intsuc.kommander.arguments.ArgumentType
import dev.intsuc.kommander.suggestion.SuggestionProvider
import dev.intsuc.kommander.tree.ArgumentCommandNode
import dev.intsuc.kommander.tree.CommandNode

class RequiredArgumentBuilder<S, T>(
    val name: String,
    val type: ArgumentType<T>,
) : ArgumentBuilder<S, RequiredArgumentBuilder<S, T>>() {
    var suggestionsProvider: SuggestionProvider<S>? = null
        private set

    fun suggests(provider: SuggestionProvider<S>?): RequiredArgumentBuilder<S, T> {
        suggestionsProvider = provider
        return this
    }

    override fun getThis(): RequiredArgumentBuilder<S, T> = this

    override fun build(): ArgumentCommandNode<S, T> {
        val result = ArgumentCommandNode(name, type, command, requirement, redirect, redirectModifier, isFork(), suggestionsProvider)

        for (child in arguments) {
            result.addChild(child)
        }

        return result
    }

    companion object {
        fun <S, T> argument(name: String, type: ArgumentType<T>): RequiredArgumentBuilder<S, T> = RequiredArgumentBuilder(name, type)
    }
}
