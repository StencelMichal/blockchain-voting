package com.stencel.evoting.voter.cli.command

import com.stencel.evoting.voter.service.VotingService
import org.springframework.stereotype.Component
import picocli.CommandLine

@Component
@CommandLine.Command(
    name = "showResults",
    description = ["Show voting results"]
)
class ShowResultsCommand(
    private val votingService: VotingService,
) : Command() {

    @CommandLine.Option(names = ["--id"], required = false, description = ["Identifier of voting"])
    private var votingIdentifier: String = "test"

    @CommandLine.Option(names = ["--pk"], required = false, description = ["Voter private key"])
    private var privateKey: String = "0xda8d4ec5adbbf950d78b93eb73469361b73683b073eae8ac14c0cc025dbc8deb"

    override fun execute() {
        votingService.showResults(votingIdentifier, privateKey)
    }

}