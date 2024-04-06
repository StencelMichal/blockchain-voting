package com.stencel.evoting.voter.service

import com.stencel.evoting.smartcontracts.VotingState
import org.springframework.stereotype.Service
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger

@Service
class VotingService(
    private val contractAddressResolverService: ContractAddressResolverService,
    private val web3j: Web3j,
    private val gasProvider: ContractGasProvider,
) {

    fun vote(contract: VotingState, vote: VotingState.Vote): Boolean {
        return contract.vote(vote).send().isStatusOK
    }

    fun showResults(votingIdentifier: String, blockchainPrivateKey: String): Boolean {
        return resolveContractAndPerform(votingIdentifier, blockchainPrivateKey) { contract ->
            val result = contract.retrieveVotingResults().send()
            val candidates = contract.candidates.send() as List<String>
            println("Total votes: ${result.totalVotes}")
            candidates.zip(result.votes).forEach { (candidate, votes) ->
                println("$candidate: $votes votes  ---  ${votes * BigInteger.valueOf(100) / result.totalVotes}%")
            }
            true
        } ?: false
    }

    private fun <T> resolveContractAndPerform(
        votingIdentifier: String,
        blockchainPrivateKey: String,
        action: (VotingState) -> T,
    ): T? {
        val credentials = Credentials.create(blockchainPrivateKey)
        return contractAddressResolverService.resolveContractAddress(votingIdentifier)?.let { address ->
            val contract = VotingState.load(address, web3j, credentials, gasProvider)
            action(contract)
        }
    }

}