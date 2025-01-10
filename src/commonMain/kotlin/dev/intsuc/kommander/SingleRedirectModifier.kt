package dev.intsuc.kommander

import dev.intsuc.kommander.context.CommandContext

fun interface SingleRedirectModifier<S> {
    fun apply(context: CommandContext<S>): S
}
