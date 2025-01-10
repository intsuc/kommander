package dev.intsuc.kommander.exceptions

import dev.intsuc.kommander.ImmutableStringReader
import dev.intsuc.kommander.Message

class DynamicNCommandExceptionType(
    private val function: (Array<out Any?>) -> Message,
) : CommandExceptionType {
    fun create(a: Any?, vararg args: Any?): CommandSyntaxException {
        return CommandSyntaxException(this, function(args))
    }

    fun createWithContext(reader: ImmutableStringReader, vararg args: Any?): CommandSyntaxException {
        return CommandSyntaxException(this, function(args), reader.string, reader.cursor)
    }
}
