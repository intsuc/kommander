package dev.intsuc.kommander

interface ImmutableStringReader {
    val string: String
    val remainingLength: Int
    val totalLength: Int
    val cursor: Int
    val scanned: String
    val remaining: String
    fun canRead(length: Int): Boolean
    fun canRead(): Boolean
    fun peek(): Char
    fun peek(offset: Int): Char
}
