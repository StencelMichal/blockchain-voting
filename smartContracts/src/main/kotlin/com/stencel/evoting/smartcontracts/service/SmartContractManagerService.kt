package com.stencel.evoting.smartcontracts.service

import com.stencel.evoting.smartcontracts.CryptographyUtils.Companion.cryptoHash
import com.stencel.evoting.smartcontracts.database.SmartContract
import com.stencel.evoting.smartcontracts.database.SmartContractAddressRepository
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
    private val localBinary: String,
    private val deploy: (Web3j, Credentials) -> RemoteCall<T>,
    private val load: (String, Credentials, ContractGasProvider) -> T
) {

    private val contractName = getNameOfSmartContract()

    val contract: T = initializeContract()

    private fun initializeContract(): T {
        return loadContract()
            ?.takeIf { !hasContractChanged(it) }
            ?.let { load(it.contractAddress, credentials, gasProvider) }
            ?: deployContract()
    }

    private fun loadContract(): SmartContract? {
        return smartContractAddressRepository.findById(contractName).getOrNull()
    }

    private fun saveContractAddress(address: String) {
        val contractHash = cryptoHash(localBinary).toString()
        val smartContractAddress = SmartContract(contractName, address, contractHash)
        smartContractAddressRepository.save(smartContractAddress)
    }

    private fun hasContractChanged(smartContract: SmartContract): Boolean {
        val currentHash = cryptoHash(localBinary).toString()
        return smartContract.hash != currentHash
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
