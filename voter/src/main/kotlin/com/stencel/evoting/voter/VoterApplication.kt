package com.stencel.evoting.voter

import com.stencel.evoting.voter.cli.CLI
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.stencel.evoting.voter", "com.stencel.evoting.smartcontracts"])
@EntityScan(basePackages = ["com.stencel.evoting.smartcontracts.database"])
@EnableJpaRepositories("com.stencel.evoting.smartcontracts.database")
class VoterApplication

fun main(args: Array<String>) {
    val context = runApplication<VoterApplication>(*args)
    val cli = context.getBean(CLI::class.java)
    cli.interpret()
}
