package dev.intsuc.kommander

import dev.intsuc.kommander.arguments.IntegerArgumentType.Companion.integer
import dev.intsuc.kommander.arguments.StringArgumentType.Companion.word
import dev.intsuc.kommander.builder.LiteralArgumentBuilder.Companion.literal
import dev.intsuc.kommander.builder.RequiredArgumentBuilder.Companion.argument
import dev.intsuc.kommander.context.StringRange
import dev.intsuc.kommander.suggestion.Suggestion
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CommandSuggestionsTest {
    private lateinit var subject: CommandDispatcher<Any>
    private val source: Any = Any()

    @BeforeTest
    fun setUp() {
        subject = CommandDispatcher()
    }

    private suspend fun testSuggestions(contents: String, cursor: Int, range: StringRange, vararg suggestions: String) {
        val result = subject.getCompletionSuggestions(subject.parse(contents, source), cursor)
        assertEquals(result.range, range)

        val expected = mutableListOf<Suggestion>()
        for (suggestion in suggestions) {
            expected.add(Suggestion(range, suggestion))
        }

        assertEquals(result.list, expected)
    }

    companion object {
        fun inputWithOffset(input: String, offset: Int): StringReader {
            val result = StringReader(input)
            result.cursor = offset
            return result
        }
    }

    @Test
    fun getCompletionSuggestions_rootCommands() = runTest {
        subject.register(literal("foo"))
        subject.register(literal("bar"))
        subject.register(literal("baz"))

        val result = subject.getCompletionSuggestions(subject.parse("", source))

        assertEquals(result.range, StringRange.at(0))
        assertEquals(result.list, listOf(Suggestion(StringRange.at(0), "bar"), Suggestion(StringRange.at(0), "baz"), Suggestion(StringRange.at(0), "foo")))
    }

    @Test
    fun getCompletionSuggestions_rootCommands_withInputOffset() = runTest {
        subject.register(literal("foo"))
        subject.register(literal("bar"))
        subject.register(literal("baz"))

        val result = subject.getCompletionSuggestions(subject.parse(inputWithOffset("OOO", 3), source))

        assertEquals(result.range, StringRange.at(3))
        assertEquals(result.list, listOf(Suggestion(StringRange.at(3), "bar"), Suggestion(StringRange.at(3), "baz"), Suggestion(StringRange.at(3), "foo")))
    }

    @Test
    fun getCompletionSuggestions_rootCommands_partial() = runTest {
        subject.register(literal("foo"))
        subject.register(literal("bar"))
        subject.register(literal("baz"))

        val result = subject.getCompletionSuggestions(subject.parse("b", source))

        assertEquals(result.range, StringRange.between(0, 1))
        assertEquals(result.list, listOf(Suggestion(StringRange.between(0, 1), "bar"), Suggestion(StringRange.between(0, 1), "baz")))
    }

    @Test
    fun getCompletionSuggestions_rootCommands_partial_withInputOffset() = runTest {
        subject.register(literal("foo"))
        subject.register(literal("bar"))
        subject.register(literal("baz"))

        val result = subject.getCompletionSuggestions(subject.parse(inputWithOffset("Zb", 1), source))

        assertEquals(result.range, StringRange.between(1, 2))
        assertEquals(result.list, listOf(Suggestion(StringRange.between(1, 2), "bar"), Suggestion(StringRange.between(1, 2), "baz")))
    }

    @Test
    fun getCompletionSuggestions_subCommands() = runTest {
        subject.register(
            literal<Any>("parent")
                .then(literal("foo"))
                .then(literal("bar"))
                .then(literal("baz"))
        )

        val result = subject.getCompletionSuggestions(subject.parse("parent ", source))

        assertEquals(result.range, StringRange.at(7))
        assertEquals(result.list, listOf(Suggestion(StringRange.at(7), "bar"), Suggestion(StringRange.at(7), "baz"), Suggestion(StringRange.at(7), "foo")))
    }

    @Test
    fun getCompletionSuggestions_movingCursor_subCommands() = runTest {
        subject.register(
            literal<Any>("parent_one")
                .then(literal("faz"))
                .then(literal("fbz"))
                .then(literal("gaz"))
        )

        subject.register(
            literal("parent_two")
        )

        testSuggestions("parent_one faz ", 0, StringRange.at(0), "parent_one", "parent_two")
        testSuggestions("parent_one faz ", 1, StringRange.between(0, 1), "parent_one", "parent_two")
        testSuggestions("parent_one faz ", 7, StringRange.between(0, 7), "parent_one", "parent_two")
        testSuggestions("parent_one faz ", 8, StringRange.between(0, 8), "parent_one")
        testSuggestions("parent_one faz ", 10, StringRange.at(0))
        testSuggestions("parent_one faz ", 11, StringRange.at(11), "faz", "fbz", "gaz")
        testSuggestions("parent_one faz ", 12, StringRange.between(11, 12), "faz", "fbz")
        testSuggestions("parent_one faz ", 13, StringRange.between(11, 13), "faz")
        testSuggestions("parent_one faz ", 14, StringRange.at(0))
        testSuggestions("parent_one faz ", 15, StringRange.at(0))
    }

    @Test
    fun getCompletionSuggestions_subCommands_partial() = runTest {
        subject.register(
            literal<Any>("parent")
                .then(literal("foo"))
                .then(literal("bar"))
                .then(literal("baz"))
        )

        val parse = subject.parse("parent b", source)
        val result = subject.getCompletionSuggestions(parse)

        assertEquals(result.range, StringRange.between(7, 8))
        assertEquals(result.list, listOf(Suggestion(StringRange.between(7, 8), "bar"), Suggestion(StringRange.between(7, 8), "baz")))
    }

    @Test
    fun getCompletionSuggestions_subCommands_partial_withInputOffset() = runTest {
        subject.register(
            literal<Any>("parent")
                .then(literal("foo"))
                .then(literal("bar"))
                .then(literal("baz"))
        )

        val parse = subject.parse(inputWithOffset("junk parent b", 5), source)
        val result = subject.getCompletionSuggestions(parse)

        assertEquals(result.range, StringRange.between(12, 13))
        assertEquals(result.list, listOf(Suggestion(StringRange.between(12, 13), "bar"), Suggestion(StringRange.between(12, 13), "baz")))
    }

    @Test
    fun getCompletionSuggestions_redirect() = runTest {
        val actual = subject.register(literal<Any>("actual").then(literal("sub")))
        subject.register(literal<Any>("redirect").redirect(actual))

        val parse = subject.parse("redirect ", source)
        val result = subject.getCompletionSuggestions(parse)

        assertEquals(result.range, StringRange.at(9))
        assertEquals(result.list, listOf(Suggestion(StringRange.at(9), "sub")))
    }

    @Test
    fun getCompletionSuggestions_redirectPartial() = runTest {
        val actual = subject.register(literal<Any>("actual").then(literal("sub")))
        subject.register(literal<Any>("redirect").redirect(actual))

        val parse = subject.parse("redirect s", source)
        val result = subject.getCompletionSuggestions(parse)

        assertEquals(result.range, StringRange.between(9, 10))
        assertEquals(result.list, listOf(Suggestion(StringRange.between(9, 10), "sub")))
    }

    @Test
    fun getCompletionSuggestions_movingCursor_redirect() = runTest {
        val actualOne = subject.register(
            literal<Any>("actual_one")
                .then(literal("faz"))
                .then(literal("fbz"))
                .then(literal("gaz"))
        )

        subject.register(literal("actual_two"))

        subject.register(literal<Any>("redirect_one").redirect(actualOne))
        subject.register(literal<Any>("redirect_two").redirect(actualOne))

        testSuggestions("redirect_one faz ", 0, StringRange.at(0), "actual_one", "actual_two", "redirect_one", "redirect_two")
        testSuggestions("redirect_one faz ", 9, StringRange.between(0, 9), "redirect_one", "redirect_two")
        testSuggestions("redirect_one faz ", 10, StringRange.between(0, 10), "redirect_one")
        testSuggestions("redirect_one faz ", 12, StringRange.at(0))
        testSuggestions("redirect_one faz ", 13, StringRange.at(13), "faz", "fbz", "gaz")
        testSuggestions("redirect_one faz ", 14, StringRange.between(13, 14), "faz", "fbz")
        testSuggestions("redirect_one faz ", 15, StringRange.between(13, 15), "faz")
        testSuggestions("redirect_one faz ", 16, StringRange.at(0))
        testSuggestions("redirect_one faz ", 17, StringRange.at(0))
    }

    @Test
    fun getCompletionSuggestions_redirectPartial_withInputOffset() = runTest {
        val actual = subject.register(literal<Any>("actual").then(literal("sub")))
        subject.register(literal<Any>("redirect").redirect(actual))

        val parse = subject.parse(inputWithOffset("/redirect s", 1), source)
        val result = subject.getCompletionSuggestions(parse)

        assertEquals(result.range, StringRange.between(10, 11))
        assertEquals(result.list, listOf(Suggestion(StringRange.between(10, 11), "sub")))
    }

    @Test
    fun getCompletionSuggestions_redirect_lots() = runTest {
        val loop = subject.register(literal("redirect"))
        subject.register(
            literal<Any>("redirect")
                .then(
                    literal<Any>("loop")
                        .then(
                            argument<Any, _>("loop", integer())
                                .redirect(loop)
                        )
                )
        )

        val result = subject.getCompletionSuggestions(subject.parse("redirect loop 1 loop 02 loop 003 ", source))

        assertEquals(result.range, StringRange.at(33))
        assertEquals(result.list, listOf(Suggestion(StringRange.at(33), "loop")))
    }

    @Test
    fun getCompletionSuggestions_execute_simulation() = runTest {
        val execute = subject.register(literal("execute"))
        subject.register(
            literal<Any>("execute")
                .then(
                    literal<Any>("as")
                        .then(
                            argument<Any, _>("name", word())
                                .redirect(execute)
                        )
                )
                .then(
                    literal<Any>("store")
                        .then(
                            argument<Any, _>("name", word())
                                .redirect(execute)
                        )
                )
                .then(
                    literal<Any>("run")
                        .executes { c -> 0 }
                )
        )

        val parse = subject.parse("execute as Dinnerbone as", source)
        val result = subject.getCompletionSuggestions(parse)

        assertEquals(result.isEmpty(), true)
    }

    @Test
    fun getCompletionSuggestions_execute_simulation_partial() = runTest {
        val execute = subject.register(literal("execute"))
        subject.register(
            literal<Any>("execute")
                .then(
                    literal<Any>("as")
                        .then(literal<Any>("bar").redirect(execute))
                        .then(literal<Any>("baz").redirect(execute))
                )
                .then(
                    literal<Any>("store")
                        .then(
                            argument<Any, _>("name", word())
                                .redirect(execute)
                        )
                )
                .then(
                    literal<Any>("run")
                        .executes { c -> 0 }
                )
        )

        val parse = subject.parse("execute as bar as ", source)
        val result = subject.getCompletionSuggestions(parse)

        assertEquals(result.range, StringRange.at(18))
        assertEquals(result.list, listOf(Suggestion(StringRange.at(18), "bar"), Suggestion(StringRange.at(18), "baz")))
    }
}
