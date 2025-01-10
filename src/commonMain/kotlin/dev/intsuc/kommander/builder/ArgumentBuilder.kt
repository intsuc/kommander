package dev.intsuc.kommander.builder

import dev.intsuc.kommander.Command
import dev.intsuc.kommander.RedirectModifier
import dev.intsuc.kommander.SingleRedirectModifier
import dev.intsuc.kommander.tree.CommandNode
import dev.intsuc.kommander.tree.RootCommandNode

abstract class ArgumentBuilder<S, T : ArgumentBuilder<S, T>> {
    private val _arguments: RootCommandNode<S> = RootCommandNode()
    var command: Command<S>? = null
        private set
    protected var requirement: (S) -> Boolean = { true }
    protected var redirect: CommandNode<S>? = null
    protected var redirectModifier: RedirectModifier<S>? = null
    private var forks: Boolean = false

    protected abstract fun getThis(): T

    fun the(argument: ArgumentBuilder<S, *>): T {
        if (redirect != null) {
            throw IllegalStateException("Cannot add children to a redirected node")
        }
        _arguments.addChild(argument.build())
        return getThis()
    }

    fun then(argument: CommandNode<S>): T {
        if (redirect != null) {
            throw IllegalStateException("Cannot add children to a redirected node")
        }
        _arguments.addChild(argument)
        return getThis()
    }

    val arguments: Collection<CommandNode<S>> get() = _arguments.children

    fun executes(command: Command<S>?): T {
        this.command = command
        return getThis()
    }

    fun requires(requirement: ((S) -> Boolean)): T {
        this.requirement = requirement
        return getThis()
    }

    fun redirect(target: CommandNode<S>): T {
        return forward(target, null, false)
    }

    fun redirect(target: CommandNode<S>, modifier: SingleRedirectModifier<S>?): T {
        return forward(target, if (modifier == null) null else { o -> listOf(modifier.apply(o)) }, false)
    }

    fun fork(target: CommandNode<S>, modifier: RedirectModifier<S>): T {
        return forward(target, modifier, true)
    }

    fun forward(target: CommandNode<S>?, modifier: RedirectModifier<S>?, fork: Boolean): T {
        if (_arguments.children.isNotEmpty()) {
            throw IllegalStateException("Cannot forward a node with children")
        }
        this.redirect = target
        this.redirectModifier = modifier
        this.forks = fork
        return getThis()
    }

    fun isFork(): Boolean = forks

    abstract fun build(): CommandNode<S>
}
