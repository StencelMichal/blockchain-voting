package com.stencel.evoting.smartcontracts.service

import com.stencel.evoting.smartcontracts.VotingState
import org.springframework.stereotype.Service
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.tx.gas.ContractGasProvider

@Service
class DeploymentService(
    private val web3j: Web3j,
    private val gasProvider: ContractGasProvider,
) {

    fun deployContract(blockchainPrivateKey: String): VotingState {
        val credentials = Credentials.create(blockchainPrivateKey)
        return VotingState.deploy(web3j, credentials, gasProvider, credentials.address).send()
    }

}