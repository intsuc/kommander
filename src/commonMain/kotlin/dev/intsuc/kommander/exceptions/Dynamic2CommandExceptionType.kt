package dev.intsuc.kommander.exceptions

import dev.intsuc.kommander.ImmutableStringReader
import dev.intsuc.kommander.Message

class Dynamic2CommandExceptionType(
    private val function: (Any?, Any?) -> Message,
) : CommandExceptionType {
    fun create(a: Any?, b: Any?): CommandSyntaxException {
        return CommandSyntaxException(this, function(a, b))
    }

    fun createWithContext(reader: ImmutableStringReader, a: Any?, b: Any?): CommandSyntaxException {
        return CommandSyntaxException(this, function(a, b), reader.string, reader.cursor)
    }
}
