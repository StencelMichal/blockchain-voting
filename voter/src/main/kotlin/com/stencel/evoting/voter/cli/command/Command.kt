package com.stencel.evoting.voter.cli.command

import java.util.concurrent.Callable

abstract class Command : Callable<Int> {

    abstract fun execute()

    override fun call(): Int {
        return try {
            execute()
            0
        } catch (e: Exception) {
            println("Error: ${e.message}")
            -1
        }
    }

}