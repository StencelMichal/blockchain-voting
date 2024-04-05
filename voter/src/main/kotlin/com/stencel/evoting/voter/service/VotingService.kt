package com.stencel.evoting.voter.service

import com.stencel.evoting.smartcontracts.VotingState
import com.stencel.evoting.smartcontracts.service.VotingStateManagerService
import org.springframework.stereotype.Service
import java.math.BigInteger

@Service
class VotingService(
    private val votingStateManagerService: VotingStateManagerService
) {

    fun vote(votingIdentifier: String, privateKey: String, vote: VotingState.Vote): Boolean {
        return votingStateManagerService.contract.vote(vote).send().isStatusOK
    }

    fun showResults(votingIdentifier: String): Boolean {
        val votingStateContract = votingStateManagerService.contract
        val result = votingStateContract.retrieveVotingResults().send()
        val candidates = votingStateContract.candidates.send() as List<String>
        println("Total votes: ${result.totalVotes}")
        candidates.zip(result.votes).forEach { (candidate, votes) ->
            println("$candidate: $votes votes  ---  ${votes * BigInteger.valueOf(100) / result.totalVotes}%")
        }
        return true
    }

}