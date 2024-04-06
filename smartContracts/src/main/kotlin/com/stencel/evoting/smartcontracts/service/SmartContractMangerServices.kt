package com.stencel.evoting.smartcontracts.service

import com.stencel.evoting.smartcontracts.VotingState
import com.stencel.evoting.smartcontracts.database.SmartContractAddressRepository
import org.springframework.stereotype.Service
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.tx.gas.ContractGasProvider

@Service
class VotingStateManagerService(
    private val web3j: Web3j,
    private val credentials: Credentials,
    private val gasProvider: ContractGasProvider,
    private val smartContractAddressRepository: SmartContractAddressRepository
) : SmartContractManagerService<VotingState>(
    web3j = web3j,
    credentials = credentials,
    gasProvider = gasProvider,
    smartContractAddressRepository = smartContractAddressRepository,
    localBinary = VotingState.BINARY,
    deploy = { web3j, credentials -> VotingState.deploy(web3j, credentials, gasProvider, credentials.address) },
    load = { address, credentials, gasProvider -> VotingState.load(address, web3j, credentials, gasProvider) }
)