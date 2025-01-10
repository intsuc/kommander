package dev.intsuc.kommander

import dev.intsuc.kommander.builder.LiteralArgumentBuilder.Companion.literal
import dev.intsuc.kommander.tree.CommandNode
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class CommandDispatcherUsagesTest {
    private lateinit var subject: CommandDispatcher<Any>
    private val source: Any = Any()
    private val command: Command<Any> = Command { c -> 0 }

    @BeforeTest
    fun setUp() {
        subject = CommandDispatcher()
        subject.register(
            literal<Any>("a")
                .then(
                    literal<Any>("1")
                        .then(literal<Any>("i").executes(command))
                        .then(literal<Any>("ii").executes(command))
                )
                .then(
                    literal<Any>("2")
                        .then(literal<Any>("i").executes(command))
                        .then(literal<Any>("ii").executes(command))
                )
        )
        subject.register(literal<Any>("b").then(literal<Any>("1").executes(command)))
        subject.register(literal<Any>("c").executes(command))
        subject.register(literal<Any>("d").requires { s -> false }.executes(command))
        subject.register(
            literal<Any>("e")
                .executes(command)
                .then(
                    literal<Any>("1")
                        .executes(command)
                        .then(literal<Any>("i").executes(command))
                        .then(literal<Any>("ii").executes(command))
                )
        )
        subject.register(
            literal<Any>("f")
                .then(
                    literal<Any>("1")
                        .then(literal<Any>("i").executes(command))
                        .then(literal<Any>("ii").executes(command).requires { s -> false })
                )
                .then(
                    literal<Any>("2")
                        .then(literal<Any>("i").executes(command).requires { s -> false })
                        .then(literal<Any>("ii").executes(command))
                )
        )
        subject.register(
            literal<Any>("g")
                .executes(command)
                .then(literal<Any>("1").then(literal<Any>("i").executes(command)))
        )
        subject.register(
            literal<Any>("h")
                .executes(command)
                .then(literal<Any>("1").then(literal<Any>("i").executes(command)))
                .then(literal<Any>("2").then(literal<Any>("i").then(literal<Any>("ii").executes(command))))
                .then(literal<Any>("3").executes(command))
        )
        subject.register(
            literal<Any>("i")
                .executes(command)
                .then(literal<Any>("1").executes(command))
                .then(literal<Any>("2").executes(command))
        )
        subject.register(
            literal<Any>("j")
                .redirect(subject.root)
        )
        subject.register(
            literal<Any>("k")
                .redirect(get("h"))
        )
    }

    private fun get(command: String): CommandNode<Any> {
        return subject.parse(command, source).context.nodes.last().node
    }

    private fun get(command: StringReader): CommandNode<Any> {
        return subject.parse(command, source).context.nodes.last().node
    }

    @Test
    fun testAllUsage_noCommands() {
        subject = CommandDispatcher()
        val results = subject.getAllUsage(subject.root, source, true)
        assertContentEquals(results, emptyArray())
    }

    @Test
    fun testSmartUsage_noCommands() {
        subject = CommandDispatcher()
        val results = subject.getSmartUsage(subject.root, source)
        assertEquals(results.entries, emptySet())
    }

    @Test
    fun testAllUsage_root() {
        val results = subject.getAllUsage(subject.root, source, true)
        assertContentEquals(
            results, arrayOf(
                "a 1 i",
                "a 1 ii",
                "a 2 i",
                "a 2 ii",
                "b 1",
                "c",
                "e",
                "e 1",
                "e 1 i",
                "e 1 ii",
                "f 1 i",
                "f 2 ii",
                "g",
                "g 1 i",
                "h",
                "h 1 i",
                "h 2 i ii",
                "h 3",
                "i",
                "i 1",
                "i 2",
                "j ...",
                "k -> h",
            )
        )
    }

    @Test
    fun testSmartUsage_root() {
        val results = subject.getSmartUsage(subject.root, source)
        assertEquals(
            results, mapOf(
                get("a") to "a (1|2)",
                get("b") to "b 1",
                get("c") to "c",
                get("e") to "e [1]",
                get("f") to "f (1|2)",
                get("g") to "g [1]",
                get("h") to "h [1|2|3]",
                get("i") to "i [1|2]",
                get("j") to "j ...",
                get("k") to "k -> h",
            )
        )
    }

    @Test
    fun testSmartUsage_h() {
        val results = subject.getSmartUsage(get("h"), source)
        assertEquals(
            results, mapOf(
                get("h 1") to "[1] i",
                get("h 2") to "[2] i ii",
                get("h 3") to "[3]",
            )
        )
    }

    @Test
    fun testSmartUsage_offsetH() {
        val offsetH = StringReader("/|/|/h")
        offsetH.cursor = 5

        val results = subject.getSmartUsage(get(offsetH), source)
        assertEquals(
            results, mapOf(
                get("h 1") to "[1] i",
                get("h 2") to "[2] i ii",
                get("h 3") to "[3]",
            )
        )
    }
}
