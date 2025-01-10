package dev.intsuc.kommander.exceptions

import dev.intsuc.kommander.ImmutableStringReader
import dev.intsuc.kommander.Message

class SimpleCommandExceptionType(
    private val message: Message,
) : CommandExceptionType {
    fun create(): CommandSyntaxException {
        return CommandSyntaxException(this, message)
    }

    fun createWithContext(reader: ImmutableStringReader): CommandSyntaxException {
        return CommandSyntaxException(this, message, reader.string, reader.cursor)
    }

    override fun toString(): String = message.toString()
}
