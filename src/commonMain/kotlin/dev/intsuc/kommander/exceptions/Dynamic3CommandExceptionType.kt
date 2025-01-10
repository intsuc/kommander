package dev.intsuc.kommander.exceptions

import dev.intsuc.kommander.ImmutableStringReader
import dev.intsuc.kommander.Message

class Dynamic3CommandExceptionType(
    private val function: (Any?, Any?, Any?) -> Message,
) : CommandExceptionType {
    fun create(a: Any?, b: Any?, c: Any?): CommandSyntaxException {
        return CommandSyntaxException(this, function(a, b, c))
    }

    fun createWithContext(reader: ImmutableStringReader, a: Any?, b: Any?, c: Any?): CommandSyntaxException {
        return CommandSyntaxException(this, function(a, b, c), reader.string, reader.cursor)
    }
}
