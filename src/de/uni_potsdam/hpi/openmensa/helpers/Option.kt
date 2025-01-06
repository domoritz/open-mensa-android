package de.uni_potsdam.hpi.openmensa.helpers

sealed class Option<T> {
    class None<T>: Option<T>()
    data class Some<T>(val value: T): Option<T>()
}