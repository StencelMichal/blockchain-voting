package com.stencel.evoting.sealer.service

import com.stencel.evoting.sealer.database.SmartContractAddress
import com.stencel.evoting.sealer.database.SmartContractAddressRepository
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.RemoteCall
import org.web3j.tx.Contract
import org.web3j.tx.gas.ContractGasProvider
import java.lang.reflect.ParameterizedType
import kotlin.jvm.optionals.getOrNull

abstract class SmartContractManagerService<T : Contract>(
    private val web3j: Web3j,
    private val credentials: Credentials,
    private val gasProvider: ContractGasProvider,
    private val smartContractAddressRepository: SmartContractAddressRepository,
    private val binary: String,
    private val deploy: (Web3j, Credentials) -> RemoteCall<T>,
    private val load: (String, Credentials, ContractGasProvider) -> T
) {

    private val contractName = getNameOfSmartContract()

    val contract: T = initializeContract()

    private fun initializeContract(): T {
        return loadContractAddress()
            ?.let { load(it, credentials, gasProvider) }
            ?.let { if (!hasContractChanged(it.contractAddress)) it else null }
            ?: deployContract()
    }

    private fun loadContractAddress(): String? {
        return smartContractAddressRepository.findById(contractName).getOrNull()?.contractAddress
    }

    private fun saveContractAddress(address: String) {
        val smartContractAddress = SmartContractAddress(contractName, address)
        smartContractAddressRepository.save(smartContractAddress)
    }

    private fun hasContractChanged(contractAddress: String): Boolean {
        val deployedContractBinary = load(contractAddress, credentials, gasProvider).contractBinary
        return deployedContractBinary != binary
    }

    private fun deployContract(): T {
        val contract = deploy(web3j, credentials).send()
        println("Contract deployed: ${contract.contractAddress}")
        saveContractAddress(contract.contractAddress)
        return contract
    }

    private fun getNameOfSmartContract(): String {
        val clazz = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
        return clazz.simpleName
    }
}
