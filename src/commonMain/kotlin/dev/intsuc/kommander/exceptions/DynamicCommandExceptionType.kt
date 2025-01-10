package dev.intsuc.kommander.exceptions

import dev.intsuc.kommander.ImmutableStringReader
import dev.intsuc.kommander.Message

class DynamicCommandExceptionType(
    private val function: (Any?) -> Message,
) : CommandExceptionType {
    fun create(arg: Any?): CommandSyntaxException {
        return CommandSyntaxException(this, function(arg))
    }

    fun createWithContext(reader: ImmutableStringReader, arg: Any?): CommandSyntaxException {
        return CommandSyntaxException(this, function(arg), reader.string, reader.cursor)
    }
}
