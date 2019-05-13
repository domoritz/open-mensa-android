package de.uni_potsdam.hpi.openmensa

import java.util.concurrent.Executors

object Threads {
    val network = Executors.newFixedThreadPool(4)
}