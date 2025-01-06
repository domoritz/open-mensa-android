package de.uni_potsdam.hpi.openmensa.extension

import de.uni_potsdam.hpi.openmensa.helpers.Option
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transformWhile

fun <T> Flow<Boolean>.whileTrue(other: Flow<T>): Flow<T> =
    combine(this, other) { valid, result ->
        if (valid) Option.Some<T>(result)
        else Option.None()
    }.transformWhile {
        if (it is Option.Some<*>) {
            emit(it.value as T)

            true
        }
        else false
    }