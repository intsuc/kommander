package dev.intsuc.kommander.exceptions

interface BuiltInExceptionProvider {
    fun doubleTooLow(): Dynamic2CommandExceptionType

    fun doubleTooHigh(): Dynamic2CommandExceptionType

    fun floatTooLow(): Dynamic2CommandExceptionType

    fun floatTooHigh(): Dynamic2CommandExceptionType

    fun integerTooLow(): Dynamic2CommandExceptionType

    fun integerTooHigh(): Dynamic2CommandExceptionType

    fun longTooLow(): Dynamic2CommandExceptionType

    fun longTooHigh(): Dynamic2CommandExceptionType

    fun literalIncorrect(): DynamicCommandExceptionType

    fun readerExpectedStartOfQuote(): SimpleCommandExceptionType

    fun readerExpectedEndOfQuote(): SimpleCommandExceptionType

    fun readerInvalidEscape(): DynamicCommandExceptionType

    fun readerInvalidBool(): DynamicCommandExceptionType

    fun readerInvalidInt(): DynamicCommandExceptionType

    fun readerExpectedInt(): SimpleCommandExceptionType

    fun readerInvalidLong(): DynamicCommandExceptionType

    fun readerExpectedLong(): SimpleCommandExceptionType

    fun readerInvalidDouble(): DynamicCommandExceptionType

    fun readerExpectedDouble(): SimpleCommandExceptionType

    fun readerInvalidFloat(): DynamicCommandExceptionType

    fun readerExpectedFloat(): SimpleCommandExceptionType

    fun readerExpectedBool(): SimpleCommandExceptionType

    fun readerExpectedSymbol(): DynamicCommandExceptionType

    fun dispatcherUnknownCommand(): SimpleCommandExceptionType

    fun dispatcherUnknownArgument(): SimpleCommandExceptionType

    fun dispatcherExpectedArgumentSeparator(): SimpleCommandExceptionType

    fun dispatcherParseException(): DynamicCommandExceptionType
}
