package com.stencel.evoting.voter.cli.command

import com.stencel.evoting.voter.service.VotingService
import org.springframework.stereotype.Component
import picocli.CommandLine
import java.util.concurrent.Callable

@Component
@CommandLine.Command(
    name = "showResults",
    description = ["Show voting results"]
)
class ShowResultsCommand(
    private val votingService: VotingService
) : Callable<Int> {

    @CommandLine.Option(names = ["--id"], required = false, description = ["Identifier of voting"])
    private var votingIdentifier: String = "test"

    override fun call(): Int {
        votingService.showResults(votingIdentifier)
        return 0
    }

}