package com.stencel.evoting.voter

import com.n1analytics.paillier.PaillierPrivateKey
import com.stencel.evoting.voter.cli.CLI
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.stencel.evoting.voter", "com.stencel.evoting.smartcontracts"])
class VoterApplication

fun main(args: Array<String>) {
    val context = runApplication<VoterApplication>(*args)
    val cli = context.getBean(CLI::class.java)
    cli.interpret()
}
