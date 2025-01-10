package dev.intsuc.kommander

import dev.intsuc.kommander.exceptions.CommandSyntaxException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StringReaderTest {
    @Test
    fun canRead() {
        val reader = StringReader("abc")
        assertEquals(reader.canRead(), true)
        reader.skip() // 'a'
        assertEquals(reader.canRead(), true)
        reader.skip() // 'b'
        assertEquals(reader.canRead(), true)
        reader.skip() // 'c'
        assertEquals(reader.canRead(), false)
    }

    @Test
    fun remainingLength() {
        val reader = StringReader("abc")
        assertEquals(reader.remainingLength, 3)
        reader.cursor = 1
        assertEquals(reader.remainingLength, 2)
        reader.cursor = 2
        assertEquals(reader.remainingLength, 1)
        reader.cursor = 3
        assertEquals(reader.remainingLength, 0)
    }

    @Test
    fun canRead_length() {
        val reader = StringReader("abc")
        assertEquals(reader.canRead(1), true)
        assertEquals(reader.canRead(2), true)
        assertEquals(reader.canRead(3), true)
        assertEquals(reader.canRead(4), false)
        assertEquals(reader.canRead(5), false)
    }

    @Test
    fun peek() {
        val reader = StringReader("abc")
        assertEquals(reader.peek(), 'a')
        assertEquals(reader.cursor, 0)
        reader.cursor = 2
        assertEquals(reader.peek(), 'c')
        assertEquals(reader.cursor, 2)
    }

    @Test
    fun peek_length() {
        val reader = StringReader("abc")
        assertEquals(reader.peek(0), 'a')
        assertEquals(reader.peek(2), 'c')
        assertEquals(reader.cursor, 0)
        reader.cursor = 1
        assertEquals(reader.peek(1), 'c')
        assertEquals(reader.cursor, 1)
    }

    @Test
    fun read() {
        val reader = StringReader("abc")
        assertEquals(reader.read(), 'a')
        assertEquals(reader.read(), 'b')
        assertEquals(reader.read(), 'c')
        assertEquals(reader.cursor, 3)
    }

    @Test
    fun skip() {
        val reader = StringReader("abc")
        reader.skip()
        assertEquals(reader.cursor, 1)
    }

    @Test
    fun remaining() {
        val reader = StringReader("Hello!")
        assertEquals(reader.remaining, "Hello!")
        reader.cursor = 3
        assertEquals(reader.remaining, "lo!")
        reader.cursor = 6
        assertEquals(reader.remaining, "")
    }

    @Test
    fun scanned(){
        val reader = StringReader("Hello!")
        assertEquals(reader.scanned, "")
        reader.cursor = 3
        assertEquals(reader.scanned, "Hel")
        reader.cursor = 6
        assertEquals(reader.scanned, "Hello!")
    }

    @Test
    fun skipWhitespace_none() {
        val reader = StringReader("Hello!")
        reader.skipWhitespace()
        assertEquals(reader.cursor, 0)
    }

    @Test
    fun skipWhitespace_mixed() {
        val reader = StringReader(" \t \t\nHello!")
        reader.skipWhitespace()
        assertEquals(reader.cursor, 5)
    }

    @Test
    fun skipWhitespace_empty() {
        val reader = StringReader("")
        reader.skipWhitespace()
        assertEquals(reader.cursor, 0)
    }

    @Test
    fun readUnquotedString() {
        val reader = StringReader("hello world")
        assertEquals(reader.readUnquotedString(), "hello")
        assertEquals(reader.scanned, "hello")
        assertEquals(reader.remaining, " world")
    }

    @Test
    fun readUnquotedString_empty() {
        val reader = StringReader("")
        assertEquals(reader.readUnquotedString(), "")
        assertEquals(reader.scanned, "")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readUnquotedString_empty_withRemaining() {
        val reader = StringReader(" hello world")
        assertEquals(reader.readUnquotedString(), "")
        assertEquals(reader.scanned, "")
        assertEquals(reader.remaining, " hello world")
    }

    @Test
    fun readQuotedString() {
        val reader = StringReader("\"hello world\"")
        assertEquals(reader.readQuotedString(), "hello world")
        assertEquals(reader.scanned, "\"hello world\"")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readSingleQuotedString() {
        val reader = StringReader("'hello world'")
        assertEquals(reader.readQuotedString(), "hello world")
        assertEquals(reader.scanned, "'hello world'")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readMixedQuotedString_doubleInsideSingle() {
        val reader = StringReader("'hello \"world\"'")
        assertEquals(reader.readQuotedString(), "hello \"world\"")
        assertEquals(reader.scanned, "'hello \"world\"'")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readMixedQuotedString_singleInsideDouble() {
        val reader = StringReader("\"hello 'world'\"")
        assertEquals(reader.readQuotedString(), "hello 'world'")
        assertEquals(reader.scanned, "\"hello 'world'\"")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readQuotedString_empty() {
        val reader = StringReader("")
        assertEquals(reader.readQuotedString(), "")
        assertEquals(reader.scanned, "")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readQuotedString_emptyQuoted() {
        val reader = StringReader("\"\"")
        assertEquals(reader.readQuotedString(), "")
        assertEquals(reader.scanned, "\"\"")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readQuotedString_emptyQuoted_withRemaining() {
        val reader = StringReader("\"\" hello world")
        assertEquals(reader.readQuotedString(), "")
        assertEquals(reader.scanned, "\"\"")
        assertEquals(reader.remaining, " hello world")
    }

    @Test
    fun readQuotedString_withEscapedQuote() {
        val reader = StringReader("\"hello \\\"world\\\"\"")
        assertEquals(reader.readQuotedString(), "hello \"world\"")
        assertEquals(reader.scanned, "\"hello \\\"world\\\"\"")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readQuotedString_withEscapedEscapes() {
        val reader = StringReader("\"\\\\o/\"")
        assertEquals(reader.readQuotedString(), "\\o/")
        assertEquals(reader.scanned, "\"\\\\o/\"")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readQuotedString_withRemaining() {
        val reader = StringReader("\"hello world\" foo bar")
        assertEquals(reader.readQuotedString(), "hello world")
        assertEquals(reader.scanned, "\"hello world\"")
        assertEquals(reader.remaining, " foo bar")
    }

    @Test
    fun readQuotedString_withImmediateRemaining() {
        val reader = StringReader("\"hello world\"foo bar")
        assertEquals(reader.readQuotedString(), "hello world")
        assertEquals(reader.scanned, "\"hello world\"")
        assertEquals(reader.remaining, "foo bar")
    }

    @Test
    fun readQuotedString_noOpen() {
        try {
            StringReader("hello world\"").readQuotedString()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedStartOfQuote())
            assertEquals(ex.cursor, 0)
        }
    }

    @Test
    fun readQuotedString_noClose() {
        try {
            StringReader("\"hello world").readQuotedString()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedEndOfQuote())
            assertEquals(ex.cursor, 12)
        }
    }

    @Test
    fun readQuotedString_invalidEscape() {
        try {
            StringReader("\"hello\\nworld\"").readQuotedString()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape())
            assertEquals(ex.cursor, 7)
        }
    }

    @Test
    fun readQuotedString_invalidQuoteEscape() {
        try {
            StringReader("'hello\\\"\'world").readQuotedString()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidEscape())
            assertEquals(ex.cursor, 7)
        }
    }

    @Test
    fun readString_noQuotes() {
        val reader = StringReader("hello world")
        assertEquals(reader.readString(), "hello")
        assertEquals(reader.scanned, "hello")
        assertEquals(reader.remaining, " world")
    }

    @Test
    fun readString_singleQuotes() {
        val reader = StringReader("'hello world'")
        assertEquals(reader.readString(), "hello world")
        assertEquals(reader.scanned, "'hello world'")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readString_doubleQuotes() {
        val reader = StringReader("\"hello world\"")
        assertEquals(reader.readString(), "hello world")
        assertEquals(reader.scanned, "\"hello world\"")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readInt() {
        val reader = StringReader("1234567890")
        assertEquals(reader.readInt(), 1234567890)
        assertEquals(reader.scanned, "1234567890")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readInt_negative() {
        val reader = StringReader("-1234567890")
        assertEquals(reader.readInt(), -1234567890)
        assertEquals(reader.scanned, "-1234567890")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readInt_invalid() {
        try {
            StringReader("12.34").readInt()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt())
            assertEquals(ex.cursor, 0)
        }
    }

    @Test
    fun readInt_none() {
        try {
            StringReader("").readInt()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedInt())
            assertEquals(ex.cursor, 0)
        }
    }

    @Test
    fun readInt_withRemaining() {
        val reader = StringReader("1234567890 foo bar")
        assertEquals(reader.readInt(), 1234567890)
        assertEquals(reader.scanned, "1234567890")
        assertEquals(reader.remaining, " foo bar")
    }

    @Test
    fun readInt_withRemainingImmediate() {
        val reader = StringReader("1234567890foo bar")
        assertEquals(reader.readInt(), 1234567890)
        assertEquals(reader.scanned, "1234567890")
        assertEquals(reader.remaining, "foo bar")
    }

    @Test
    fun readLong() {
        val reader = StringReader("1234567890")
        assertEquals(reader.readLong(), 1234567890L)
        assertEquals(reader.scanned, "1234567890")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readLong_negative() {
        val reader = StringReader("-1234567890")
        assertEquals(reader.readLong(), -1234567890L)
        assertEquals(reader.scanned, "-1234567890")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readLong_invalid() {
        try {
            StringReader("12.34").readLong()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidLong())
            assertEquals(ex.cursor, 0)
        }
    }

    @Test
    fun readLong_none() {
        try {
            StringReader("").readLong()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedLong())
            assertEquals(ex.cursor, 0)
        }
    }

    @Test
    fun readLong_withRemaining() {
        val reader = StringReader("1234567890 foo bar")
        assertEquals(reader.readLong(), 1234567890L)
        assertEquals(reader.scanned, "1234567890")
        assertEquals(reader.remaining, " foo bar")
    }

    @Test
    fun readLong_withRemainingImmediate() {
        val reader = StringReader("1234567890foo bar")
        assertEquals(reader.readLong(), 1234567890L)
        assertEquals(reader.scanned, "1234567890")
        assertEquals(reader.remaining, "foo bar")
    }

    @Test
    fun readDouble() {
        val reader = StringReader("123")
        assertEquals(reader.readDouble(), 123.0)
        assertEquals(reader.scanned, "123")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readDouble_withDecimal() {
        val reader = StringReader("12.34")
        assertEquals(reader.readDouble(), 12.34)
        assertEquals(reader.scanned, "12.34")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readDouble_negative() {
        val reader = StringReader("-123")
        assertEquals(reader.readDouble(), -123.0)
        assertEquals(reader.scanned, "-123")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readDouble_invalid() {
        try {
            StringReader("12.34.56").readDouble()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidDouble())
            assertEquals(ex.cursor, 0)
        }
    }

    @Test
    fun readDouble_none() {
        try {
            StringReader("").readDouble()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedDouble())
            assertEquals(ex.cursor, 0)
        }
    }

    @Test
    fun readDouble_withRemaining() {
        val reader = StringReader("12.34 foo bar")
        assertEquals(reader.readDouble(), 12.34)
        assertEquals(reader.scanned, "12.34")
        assertEquals(reader.remaining, " foo bar")
    }

    @Test
    fun readDouble_withRemainingImmediate() {
        val reader = StringReader("12.34foo bar")
        assertEquals(reader.readDouble(), 12.34)
        assertEquals(reader.scanned, "12.34")
        assertEquals(reader.remaining, "foo bar")
    }

    @Test
    fun readFloat() {
        val reader = StringReader("123")
        assertEquals(reader.readFloat(), 123.0f)
        assertEquals(reader.scanned, "123")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readFloat_withDecimal() {
        val reader = StringReader("12.34")
        assertEquals(reader.readFloat(), 12.34f)
        assertEquals(reader.scanned, "12.34")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readFloat_negative() {
        val reader = StringReader("-123")
        assertEquals(reader.readFloat(), -123.0f)
        assertEquals(reader.scanned, "-123")
        assertEquals(reader.remaining, "")
    }

    @Test
    fun readFloat_invalid() {
        try {
            StringReader("12.34.56").readFloat()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidFloat())
            assertEquals(ex.cursor, 0)
        }
    }

    @Test
    fun readFloat_none() {
        try {
            StringReader("").readFloat()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedFloat())
            assertEquals(ex.cursor, 0)
        }
    }

    @Test
    fun readFloat_withRemaining() {
        val reader = StringReader("12.34 foo bar")
        assertEquals(reader.readFloat(), 12.34f)
        assertEquals(reader.scanned, "12.34")
        assertEquals(reader.remaining, " foo bar")
    }

    @Test
    fun readFloat_withRemainingImmediate() {
        val reader = StringReader("12.34foo bar")
        assertEquals(reader.readFloat(), 12.34f)
        assertEquals(reader.scanned, "12.34")
        assertEquals(reader.remaining, "foo bar")
    }

    @Test
    fun expect_correct() {
        val reader = StringReader("abc")
        reader.expect('a')
        assertEquals(reader.cursor, 1)
    }

    @Test
    fun expect_incorrect() {
        val reader = StringReader("bcd")
        try {
            reader.expect('a')
            fail()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedSymbol())
            assertEquals(ex.cursor, 0)
        }
    }

    @Test
    fun expect_none() {
        val reader = StringReader("")
        try {
            reader.expect('a')
            fail()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedSymbol())
            assertEquals(ex.cursor, 0)
        }
    }

    @Test
    fun readBoolean_correct() {
        val reader = StringReader("true")
        assertEquals(reader.readBoolean(), true)
        assertEquals(reader.scanned, "true")
    }

    @Test
    fun readBoolean_incorrect() {
        val reader = StringReader("tuesday")
        try {
            reader.readBoolean()
            fail()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidBool())
            assertEquals(ex.cursor, 0)
        }
    }

    @Test
    fun readBoolean_none() {
        val reader = StringReader("")
        try {
            reader.readBoolean()
            fail()
        } catch (ex: CommandSyntaxException) {
            assertEquals(ex.type, CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerExpectedBool())
            assertEquals(ex.cursor, 0)
        }
    }
}
