package dev.intsuc.kommander.exceptions

import dev.intsuc.kommander.ImmutableStringReader
import dev.intsuc.kommander.Message

class Dynamic4CommandExceptionType(
    private val function: (Any?, Any?, Any?, Any?) -> Message,
) : CommandExceptionType {
    fun create(a: Any?, b: Any?, c: Any?, d: Any?): CommandSyntaxException {
        return CommandSyntaxException(this, function(a, b, c, d))
    }

    fun createWithContext(reader: ImmutableStringReader, a: Any?, b: Any?, c: Any?, d: Any?): CommandSyntaxException {
        return CommandSyntaxException(this, function(a, b, c, d), reader.string, reader.cursor)
    }
}
