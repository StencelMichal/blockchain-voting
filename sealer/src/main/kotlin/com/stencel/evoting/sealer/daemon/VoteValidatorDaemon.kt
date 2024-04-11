package com.stencel.evoting.sealer.daemon

import com.stencel.evoting.sealer.service.VoteValidatorService
import com.stencel.evoting.smartcontracts.VotingState
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import scala.xml.dtd.ValidationException

@Component
class VoteValidatorDaemon(
    private val voteValidatorService: VoteValidatorService,
) {

    @Scheduled(fixedRate = 1000) // Run every 1000 milliseconds (1 second)
    fun runTask() {
        contractOpt?.let { contract ->
            try {
                val voteToValidate = contract.voteAwaitingForValidation.send()
                validateVote(voteToValidate, contract)
            } catch (_: Exception) {
                // Swallow exception
            }
        }
    }

    private fun validateVote(vote: VotingState.Vote, contract: VotingState) {
        try {
            val validationResult = voteValidatorService.validateVote(vote)
            if (validationResult) {
                contract.confirmVoteValidity(vote).send()
            } else {
                contract.invalidateVote(vote).send()
            }
        } catch (_: ValidationException) {
            contract.invalidateVote(vote).send()
        }
    }

    companion object {
        var contractOpt: VotingState? = null
    }
}
