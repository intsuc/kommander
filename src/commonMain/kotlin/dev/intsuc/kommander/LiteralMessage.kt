package dev.intsuc.kommander

class LiteralMessage(override val string: String) : Message {
    override fun toString(): String = string
}
