package de.uni_potsdam.hpi.openmensa.extension

import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

fun <X, Y> LiveData<X>.map(function: Function<X, Y>): LiveData<Y> {
    return Transformations.map(this, function)
}

fun <X, Y> LiveData<X>.map(function: (X) -> Y): LiveData<Y> {
    return Transformations.map(this, function)
}

fun <X, Y> LiveData<X>.switchMap(function: Function<X, LiveData<Y>>): LiveData<Y> {
    return Transformations.switchMap(this, function)
}

fun <X, Y> LiveData<X>.switchMap(function: (X) -> LiveData<Y>): LiveData<Y> {
    return Transformations.switchMap(this, function)
}
