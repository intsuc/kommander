package dev.intsuc.kommander

import dev.intsuc.kommander.context.CommandContext

fun interface ResultConsumer<S> {
    fun onCommandComplete(context: CommandContext<S>, success: Boolean, result: Int)
}
