package dev.intsuc.kommander

import dev.intsuc.kommander.context.CommandContext

fun interface RedirectModifier<S> {
    fun apply(context: CommandContext<S>): Collection<S>
}
