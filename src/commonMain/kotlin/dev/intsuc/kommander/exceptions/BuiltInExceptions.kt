package dev.intsuc.kommander.exceptions

import dev.intsuc.kommander.LiteralMessage


class BuiltInExceptions : BuiltInExceptionProvider {
    override fun doubleTooLow(): Dynamic2CommandExceptionType = DOUBLE_TOO_SMALL
    override fun doubleTooHigh(): Dynamic2CommandExceptionType = DOUBLE_TOO_BIG
    override fun floatTooLow(): Dynamic2CommandExceptionType = FLOAT_TOO_SMALL
    override fun floatTooHigh(): Dynamic2CommandExceptionType = FLOAT_TOO_BIG
    override fun integerTooLow(): Dynamic2CommandExceptionType = INTEGER_TOO_SMALL
    override fun integerTooHigh(): Dynamic2CommandExceptionType = INTEGER_TOO_BIG
    override fun longTooLow(): Dynamic2CommandExceptionType = LONG_TOO_SMALL
    override fun longTooHigh(): Dynamic2CommandExceptionType = LONG_TOO_BIG
    override fun literalIncorrect(): DynamicCommandExceptionType = LITERAL_INCORRECT
    override fun readerExpectedStartOfQuote(): SimpleCommandExceptionType = READER_EXPECTED_START_OF_QUOTE
    override fun readerExpectedEndOfQuote(): SimpleCommandExceptionType = READER_EXPECTED_END_OF_QUOTE
    override fun readerInvalidEscape(): DynamicCommandExceptionType = READER_INVALID_ESCAPE
    override fun readerInvalidBool(): DynamicCommandExceptionType = READER_INVALID_BOOL
    override fun readerInvalidInt(): DynamicCommandExceptionType = READER_INVALID_INT
    override fun readerExpectedInt(): SimpleCommandExceptionType = READER_EXPECTED_INT
    override fun readerInvalidLong(): DynamicCommandExceptionType = READER_INVALID_LONG
    override fun readerExpectedLong(): SimpleCommandExceptionType = READER_EXPECTED_LONG
    override fun readerInvalidDouble(): DynamicCommandExceptionType = READER_INVALID_DOUBLE
    override fun readerExpectedDouble(): SimpleCommandExceptionType = READER_EXPECTED_DOUBLE
    override fun readerInvalidFloat(): DynamicCommandExceptionType = READER_INVALID_FLOAT
    override fun readerExpectedFloat(): SimpleCommandExceptionType = READER_EXPECTED_FLOAT
    override fun readerExpectedBool(): SimpleCommandExceptionType = READER_EXPECTED_BOOL
    override fun readerExpectedSymbol(): DynamicCommandExceptionType = READER_EXPECTED_SYMBOL
    override fun dispatcherUnknownCommand(): SimpleCommandExceptionType = DISPATCHER_UNKNOWN_COMMAND
    override fun dispatcherUnknownArgument(): SimpleCommandExceptionType = DISPATCHER_UNKNOWN_ARGUMENT
    override fun dispatcherExpectedArgumentSeparator(): SimpleCommandExceptionType = DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR
    override fun dispatcherParseException(): DynamicCommandExceptionType = DISPATCHER_PARSE_EXCEPTION

    companion object {
        private val DOUBLE_TOO_SMALL: Dynamic2CommandExceptionType = Dynamic2CommandExceptionType { found, min -> LiteralMessage("Double must not be less than $min, found $found") }
        private val DOUBLE_TOO_BIG: Dynamic2CommandExceptionType = Dynamic2CommandExceptionType { found, max -> LiteralMessage("Double must not be more than $max, found $found") }

        private val FLOAT_TOO_SMALL: Dynamic2CommandExceptionType = Dynamic2CommandExceptionType { found, min -> LiteralMessage("Float must not be less than $min, found $found") }
        private val FLOAT_TOO_BIG: Dynamic2CommandExceptionType = Dynamic2CommandExceptionType { found, max -> LiteralMessage("Float must not be more than $max, found $found") }

        private val INTEGER_TOO_SMALL: Dynamic2CommandExceptionType = Dynamic2CommandExceptionType { found, min -> LiteralMessage("Integer must not be less than $min, found $found") }
        private val INTEGER_TOO_BIG: Dynamic2CommandExceptionType = Dynamic2CommandExceptionType { found, max -> LiteralMessage("Integer must not be more than $max, found $found") }

        private val LONG_TOO_SMALL: Dynamic2CommandExceptionType = Dynamic2CommandExceptionType { found, min -> LiteralMessage("Long must not be less than $min, found $found") }
        private val LONG_TOO_BIG: Dynamic2CommandExceptionType = Dynamic2CommandExceptionType { found, max -> LiteralMessage("Long must not be more than $max, found $found") }

        private val LITERAL_INCORRECT: DynamicCommandExceptionType = DynamicCommandExceptionType { expected -> LiteralMessage("Expected literal $expected") }

        private val READER_EXPECTED_START_OF_QUOTE: SimpleCommandExceptionType = SimpleCommandExceptionType(LiteralMessage("Expected quote to start a string"))
        private val READER_EXPECTED_END_OF_QUOTE: SimpleCommandExceptionType = SimpleCommandExceptionType(LiteralMessage("Unclosed quoted string"))
        private val READER_INVALID_ESCAPE: DynamicCommandExceptionType = DynamicCommandExceptionType { character -> LiteralMessage("Invalid escape sequence '$character' in quoted string") }
        private val READER_INVALID_BOOL: DynamicCommandExceptionType = DynamicCommandExceptionType { value -> LiteralMessage("Invalid bool, expected true or false but found '$value'") }
        private val READER_INVALID_INT: DynamicCommandExceptionType = DynamicCommandExceptionType { value -> LiteralMessage("Invalid integer '$value'") }
        private val READER_EXPECTED_INT: SimpleCommandExceptionType = SimpleCommandExceptionType(LiteralMessage("Expected integer"))
        private val READER_INVALID_LONG: DynamicCommandExceptionType = DynamicCommandExceptionType { value -> LiteralMessage("Invalid long '$value'") }
        private val READER_EXPECTED_LONG: SimpleCommandExceptionType = SimpleCommandExceptionType((LiteralMessage("Expected long")))
        private val READER_INVALID_DOUBLE: DynamicCommandExceptionType = DynamicCommandExceptionType { value -> LiteralMessage("Invalid double '$value'") }
        private val READER_EXPECTED_DOUBLE: SimpleCommandExceptionType = SimpleCommandExceptionType(LiteralMessage("Expected double"))
        private val READER_INVALID_FLOAT: DynamicCommandExceptionType = DynamicCommandExceptionType { value -> LiteralMessage("Invalid float '$value'") }
        private val READER_EXPECTED_FLOAT: SimpleCommandExceptionType = SimpleCommandExceptionType(LiteralMessage("Expected float"))
        private val READER_EXPECTED_BOOL: SimpleCommandExceptionType = SimpleCommandExceptionType(LiteralMessage("Expected bool"))
        private val READER_EXPECTED_SYMBOL: DynamicCommandExceptionType = DynamicCommandExceptionType { symbol -> LiteralMessage("Expected '$symbol'") }

        private val DISPATCHER_UNKNOWN_COMMAND: SimpleCommandExceptionType = SimpleCommandExceptionType(LiteralMessage("Unknown command"))
        private val DISPATCHER_UNKNOWN_ARGUMENT: SimpleCommandExceptionType = SimpleCommandExceptionType(LiteralMessage("Incorrect argument for command"))
        private val DISPATCHER_EXPECTED_ARGUMENT_SEPARATOR: SimpleCommandExceptionType = SimpleCommandExceptionType(LiteralMessage("Expected whitespace to end one argument, but found trailing data"))
        private val DISPATCHER_PARSE_EXCEPTION: DynamicCommandExceptionType = DynamicCommandExceptionType { message -> LiteralMessage("Could not parse command: $message") }
    }
}
