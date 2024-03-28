package com.stencel.evoting.voter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.web3j.crypto.Credentials

@SpringBootApplication
class VoterApplication

fun main(args: Array<String>) {
    runApplication<VoterApplication>(*args)
    Credentials.create("0xcfcf3f2d7deb189365799e9ab4ef34fae2350e908bd64678619a849c7c827a06")
}



