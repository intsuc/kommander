package dev.intsuc.kommander

import dev.intsuc.kommander.tree.RootCommandNode

class CommandDispatcher<S>(
    private val root: RootCommandNode<S>,
) {
    companion object {
        const val ARGUMENT_SEPARATOR: String = " "

        const val ARGUMENT_SEPARATOR_CHAR: Char = ' '

        private const val USAGE_OPTIONAL_OPEN: String = "["
        private const val USAGE_OPTIONAL_CLOSE: String = "]"
        private const val USAGE_REQUIRED_OPEN: String = "("
        private const val USAGE_REQUIRED_CLOSE: String = ")"
        private const val USAGE_OR: String = "|"
    }
}
