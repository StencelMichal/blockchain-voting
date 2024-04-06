package com.stencel.evoting.voter.cli

import com.stencel.evoting.voter.cli.command.ShowResultsCommand
import com.stencel.evoting.voter.cli.command.VoteCommand
import org.springframework.stereotype.Component
import picocli.CommandLine
import java.util.*
import kotlin.system.exitProcess

@CommandLine.Command(
    name = "evoting",
    description = ["E-voting voter CLI"]
)
class CliSpec

@Component
class CLI(
    voteCommand: VoteCommand,
    showResultsCommand: ShowResultsCommand
) {

    private val scanner = Scanner(System.`in`)
    private val interpreter = CommandLine(CliSpec())
        .addSubcommand(voteCommand)
        .addSubcommand(showResultsCommand)

    fun interpret() {

        while (true) {
            print("> ")
            val input = scanner.nextLine()
            if (input.equals("exit", ignoreCase = true)) {
                exitProcess(0)
            }
            try {
                interpreter.execute(*input.split(" ").toTypedArray())
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }
        }

    }

}