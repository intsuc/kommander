package dev.intsuc.kommander.suggestion

import dev.intsuc.kommander.Message
import dev.intsuc.kommander.context.StringRange

class SuggestionsBuilder(
    val input: String,
    private val inputLowerCase: String,
    val start: Int,
) {
    val remaining: String = input.substring(start)
    val remainingLowerCase: String = inputLowerCase.substring(start)
    val result: MutableList<Suggestion> = mutableListOf()

    constructor(input: String, start: Int) : this(input, input.lowercase(), start)

    fun build(): Suggestions {
        return Suggestions.create(input, result)
    }

    fun suggest(text: String): SuggestionsBuilder {
        if (text == remaining) {
            return this
        }
        result += Suggestion(StringRange.between(start, input.length), text)
        return this
    }

    fun suggest(text: String, tooltip: Message): SuggestionsBuilder {
        if (text == remaining) {
            return this
        }
        result += Suggestion(StringRange.between(start, input.length), text, tooltip)
        return this
    }

    fun suggest(value: Int): SuggestionsBuilder {
        result += IntegerSuggestion(StringRange.between(start, input.length), value)
        return this
    }

    fun suggest(value: Int, tooltip: Message): SuggestionsBuilder {
        result += IntegerSuggestion(StringRange.between(start, input.length), value, tooltip)
        return this
    }

    fun add(other: SuggestionsBuilder): SuggestionsBuilder {
        result += other.result
        return this
    }

    fun createOffset(start: Int): SuggestionsBuilder {
        return SuggestionsBuilder(input, inputLowerCase, start)
    }

    fun restart(): SuggestionsBuilder {
        return createOffset(start)
    }
}
