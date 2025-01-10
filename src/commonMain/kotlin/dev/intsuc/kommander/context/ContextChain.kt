package dev.intsuc.kommander.context

import dev.intsuc.kommander.ResultConsumer
import dev.intsuc.kommander.exceptions.CommandSyntaxException

class ContextChain<S>(
    // TODO ideally those two would have separate types, but modifiers and executables expect full context
    private val modifiers: List<CommandContext<S>>,
    private val executable: CommandContext<S>,
) {
    private var nextStageCache: ContextChain<S>? = null

    init {
        if (executable.command == null) {
            throw IllegalArgumentException("Last command in chain must be executable")
        }
    }

    fun executeAll(source: S, resultConsumer: ResultConsumer<S>): Int {
        if (modifiers.isEmpty()) {
            // Fast path - just a single stage
            return runExecutable(executable, source, resultConsumer, false)
        }

        var forkedMode = false
        var currentSources = listOf(source)

        for (modifier in modifiers) {
            forkedMode = forkedMode or modifier.isForked()

            var nextSources = mutableListOf<S>()
            for (sourceToRun in currentSources) {
                nextSources += runModifier(modifier, sourceToRun, resultConsumer, forkedMode)
            }
            if (nextSources.isEmpty()) {
                return 0
            }
            currentSources = nextSources
        }

        var result = 0
        for (executionSource in currentSources) {
            result += runExecutable(executable, executionSource, resultConsumer, forkedMode)
        }

        return result
    }

    val stage: Stage get() = if (modifiers.isEmpty()) Stage.EXECUTE else Stage.MODIFY

    val topContext: CommandContext<S> get() {
        if (modifiers.isEmpty()) {
            return executable
        }
        return modifiers.first()
    }

    fun nextStage(): ContextChain<S>? {
        val modifierCount = modifiers.size
        if (modifierCount == 0) {
            return null
        }

        if (nextStageCache == null) {
            nextStageCache = ContextChain(modifiers.subList(1, modifierCount), executable)
        }
        return nextStageCache
    }

    enum class Stage {
        MODIFY,
        EXECUTE,
    }

    companion object {
        fun <S> tryFlatten(rootContext: CommandContext<S>): ContextChain<S>? {
            val modifiers = mutableListOf<CommandContext<S>>()

            var current: CommandContext<S> = rootContext

            while (true) {
                val child: CommandContext<S>? = current.child
                if (child == null) {
                    if (current.command == null) {
                        return null
                    }

                    return ContextChain(modifiers, current)
                }

                modifiers += current
                current = child
            }
        }

        fun <S> runModifier(modifier: CommandContext<S>, source: S, resultConsumer: ResultConsumer<S>, forkedMode: Boolean): Collection<S> {
            val sourceModifier = modifier.redirectModifier

            // Note: source currently in context is irrelevant at this point, since we might have updated it in one of earlier stages
            if (sourceModifier == null) {
                // Simple redirect, just propagate source to next node
                return listOf(source)
            }

            val contextToUse = modifier.copyFor(source)
            try {
                return sourceModifier.apply(contextToUse)
            } catch (ex: CommandSyntaxException) {
                resultConsumer.onCommandComplete(contextToUse, false, 0)
                if (forkedMode) {
                    return emptyList()
                }
                throw ex
            }
        }

        fun <S> runExecutable(executable: CommandContext<S>, source: S, resultConsumer: ResultConsumer<S>, forkedMode: Boolean): Int {
            val contextToUse = executable.copyFor(source)
            try {
                val result = executable.command!!.run(contextToUse)
                resultConsumer.onCommandComplete(contextToUse, true, result)
                return if (forkedMode) 1 else result
            } catch (ex: CommandSyntaxException) {
                resultConsumer.onCommandComplete(contextToUse, false, 0)
                if (forkedMode) {
                    return 0
                }
                throw ex
            }
        }
    }
}
