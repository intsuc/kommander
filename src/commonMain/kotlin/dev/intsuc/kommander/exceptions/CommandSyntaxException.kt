package dev.intsuc.kommander.exceptions

import dev.intsuc.kommander.Message

class CommandSyntaxException(
    val type: CommandExceptionType,
    private val _message: Message,
    val input: String?,
    val cursor: Int,
) : Exception(_message.string, null) {
    constructor(type: CommandExceptionType, message: Message) : this(type, message, null, -1)

    override val message: String
        get() {
            var message = _message.string
            val context = context
            if (context != null) {
                message += " at position $cursor: $context"
            }
            return message
        }

    val rawMessage: Message get() = _message

    val context: String?
        get() {
            if (input == null || cursor < 0) {
                return null
            }
            val builder = StringBuilder()
            val cursor = minOf(input.length, this.cursor)

            if (cursor > CONTEXT_AMOUNT) {
                builder.append("...")
            }

            builder.append(input.substring(maxOf(0, cursor - CONTEXT_AMOUNT), cursor))
            builder.append("<--[HERE]")

            return builder.toString()
        }

    companion object {
        const val CONTEXT_AMOUNT: Int = 10
        var ENABLE_COMMAND_STACK_TRACES: Boolean = true
        var BUILT_IN_EXCEPTIONS: BuiltInExceptionProvider = BuiltInExceptions()
    }
}
