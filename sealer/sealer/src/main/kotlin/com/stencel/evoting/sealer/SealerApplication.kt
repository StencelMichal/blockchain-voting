package com.stencel.evoting.sealer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SealerApplication

fun main(args: Array<String>) {
    runApplication<SealerApplication>(*args)
}
