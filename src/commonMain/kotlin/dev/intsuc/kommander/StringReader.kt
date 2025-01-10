package dev.intsuc.kommander

import dev.intsuc.kommander.exceptions.CommandSyntaxException

class StringReader(
    override val string: String,
    override var cursor: Int,
) : ImmutableStringReader {
    constructor(other: StringReader) : this(other.string, other.cursor)

    constructor(string: String) : this(string, 0)

    override val remainingLength: Int get() = string.length - cursor

    override val totalLength: Int get() = string.length

    override val scanned: String get() = string.substring(0, cursor)

    override val remaining: String get() = string.substring(cursor)

    override fun canRead(length: Int): Boolean = cursor + length <= string.length

    override fun canRead(): Boolean = canRead(1)

    override fun peek(): Char = string[cursor]

    override fun peek(offset: Int): Char = string[cursor + offset]

    fun read(): Char = string[cursor++]

    fun skip() {
        cursor++
    }

    fun skipWhitespace() {
        while (canRead() && peek().isWhitespace()) {
            skip()
        }
    }

    fun readInt(): Int {
        val start = cursor
        while (canRead() && isAllowedNumber(peek())) {
            skip()
        }
        val number = string.substring(start, cursor)
        if (number.isEmpty()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedInt().createWithContext(this)
        }
        try {
            return number.toInt()
        } catch (ex: NumberFormatException) {
            cursor = start
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().createWithContext(this, number)
        }
    }

    fun readLong(): Long {
        val start = cursor
        while (canRead() && isAllowedNumber(peek())) {
            skip()
        }
        val number = string.substring(start, cursor)
        if (number.isEmpty()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedLong().createWithContext(this)
        }
        try {
            return number.toLong()
        } catch (ex: NumberFormatException) {
            cursor = start
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidLong().createWithContext(this, number)
        }
    }

    fun readDouble(): Double {
        val start = cursor
        while (canRead() && isAllowedNumber(peek())) {
            skip()
        }
        val number = string.substring(start, cursor)
        if (number.isEmpty()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedDouble().createWithContext(this)
        }
        try {
            return number.toDouble()
        } catch (ex: NumberFormatException) {
            cursor = start
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidDouble().createWithContext(this, number)
        }
    }

    fun readFloat(): Float {
        val start = cursor
        while (canRead() && isAllowedNumber(peek())) {
            skip()
        }
        val number = string.substring(start, cursor)
        if (number.isEmpty()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedFloat().createWithContext(this)
        }
        try {
            return number.toFloat()
        } catch (ex: NumberFormatException) {
            cursor = start
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidFloat().createWithContext(this, number)
        }
    }

    fun readUnquotedString(): String {
        val start = cursor
        while (canRead() && isAllowedInUnquotedString(peek())) {
            skip()
        }
        return string.substring(start, cursor)
    }

    fun readQuotedString(): String {
        if (!canRead()) {
            return ""
        }
        val next = peek()
        if (!isQuotedStringStart(next)) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedStartOfQuote().createWithContext(this)
        }
        skip()
        return readStringUntil(next)
    }

    fun readStringUntil(terminator: Char): String {
        val result = StringBuilder()
        var escaped = false
        while (canRead()) {
            val c = read()
            if (escaped) {
                if (c == terminator || c == SYNTAX_ESCAPE) {
                    result.append(c)
                    escaped = false
                } else {
                    cursor--
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape().createWithContext(this, c.toString())
                }
            } else if (c == SYNTAX_ESCAPE) {
                escaped = true
            } else if (c == terminator) {
                return result.toString()
            } else {
                result.append(c)
            }
        }

        throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedEndOfQuote().createWithContext(this)
    }

    fun readString(): String {
        if (!canRead()) {
            return ""
        }
        val next = peek()
        if (isQuotedStringStart(next)) {
            skip()
            return readStringUntil(next)
        }
        return readUnquotedString()
    }

    fun readBoolean(): Boolean {
        val start = cursor
        val value = readString()
        if (value.isEmpty()) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedBool().createWithContext(this)
        }
        return when (value) {
            "true" -> true
            "false" -> false
            else -> {
                cursor = start
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidBool().createWithContext(this, value)
            }
        }
    }

    fun expect(c: Char) {
        if (!canRead() || peek() != c) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedSymbol().createWithContext(this, c.toString())
        }
        skip()
    }

    companion object {
        private const val SYNTAX_ESCAPE: Char = '\\'
        private const val SYNTAX_DOUBLE_QUOTE: Char = '"'
        private const val SYNTAX_SINGLE_QUOTE: Char = '\''

        fun isAllowedNumber(c: Char): Boolean = c in '0'..'9' || c == '.' || c == '-'

        fun isQuotedStringStart(c: Char): Boolean = c == SYNTAX_DOUBLE_QUOTE || c == SYNTAX_SINGLE_QUOTE

        fun isAllowedInUnquotedString(c: Char): Boolean = c in '0'..'9'
                || c in 'A'..'Z'
                || c in 'a'..'z'
                || c == '_' || c == '-'
                || c == '.' || c == '+'
    }
}
