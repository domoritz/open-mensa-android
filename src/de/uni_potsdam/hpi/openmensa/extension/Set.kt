package de.uni_potsdam.hpi.openmensa.extension

fun <T> MutableSet<T>.toggle(item: T) {
    if (this.remove(item)) {
        // it was in the list
    } else {
        // it was not in the list
        this.add(item)
    }
}