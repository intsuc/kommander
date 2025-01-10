package dev.intsuc.kommander

import dev.intsuc.kommander.context.CommandContext

fun interface Command<S> {
    fun run(context: CommandContext<S>): Int

    companion object {
        const val SINGLE_SUCCESS: Int = 1
    }
}
