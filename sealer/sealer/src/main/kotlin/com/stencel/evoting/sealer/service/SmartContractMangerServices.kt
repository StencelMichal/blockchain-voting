package com.stencel.evoting.sealer.service

import com.stencel.evoting.sealer.BigNumbers
import com.stencel.evoting.sealer.VotingState
import com.stencel.evoting.sealer.database.SmartContractAddressRepository
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
    binary = VotingState.BINARY,
    deploy = { web3j, credentials -> VotingState.deploy(web3j, credentials, gasProvider) },
    load = { address, credentials, gasProvider -> VotingState.load(address, web3j, credentials, gasProvider) }
)

@Service
class BigNumbersManagerService(
    private val web3j: Web3j,
    private val credentials: Credentials,
    private val gasProvider: ContractGasProvider,
    private val smartContractAddressRepository: SmartContractAddressRepository
) : SmartContractManagerService<BigNumbers>(
    web3j = web3j,
    credentials = credentials,
    gasProvider = gasProvider,
    smartContractAddressRepository = smartContractAddressRepository,
    binary = BigNumbers.BINARY,
    deploy = { web3j, credentials -> BigNumbers.deploy(web3j, credentials, gasProvider) },
    load = { address, credentials, gasProvider -> BigNumbers.load(address, web3j, credentials, gasProvider) }
)
