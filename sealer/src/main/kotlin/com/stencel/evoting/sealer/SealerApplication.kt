package com.stencel.evoting.sealer

import com.stencel.evoting.sealer.daemon.VoteValidatorDaemon
import com.stencel.evoting.smartcontracts.VotingState
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestTemplate
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.tx.gas.ContractGasProvider
import kotlin.system.exitProcess


@SpringBootApplication(scanBasePackages = ["com.stencel.evoting.sealer", "com.stencel.evoting.smartcontracts"])
@EnableScheduling
class SealerApplication

fun main(args: Array<String>) {
    val credentialsStr = System.getenv("CREDENTIALS")
    val votingIdentifier = System.getenv("VOTING_IDENTIFIER")

    if (credentialsStr.isNullOrBlank() || votingIdentifier.isNullOrBlank()) {
        println("Missing credentials or identifier in environment variables.")
        return
    }

    val contractAddress = getContractAddress(votingIdentifier)

    val credentials = Credentials.create(credentialsStr)
    val springContext = runApplication<SealerApplication>(*args.drop(2).toTypedArray())

    val contract = loadContract(springContext, credentials, contractAddress)
    VoteValidatorDaemon.contractOpt = contract
}

private fun getPrivateKey(): Credentials {
    try {
        println("Provide sealer's private key:")
        val key = readln()
        return Credentials.create(key)
    } catch (e: Exception) {
        println("Cannot create sealer's credentials")
        exitProcess(-1)
    }
}

private fun getVotingIdentifier(): String {
    try {
        println("Provide voting identifier:")
        return readln()
    } catch (e: Exception) {
        println("Cannot read voting identifier")
        exitProcess(-1)
    }
}

private fun loadContract(
    springContext: ConfigurableApplicationContext,
    credentials: Credentials,
    contractAddress: String
): VotingState {
    val web3j = springContext.beanFactory.getBean(Web3j::class.java)
    val gasProvider = springContext.beanFactory.getBean(ContractGasProvider::class.java)
    return VotingState.load(
        contractAddress,
        web3j,
        credentials,
        gasProvider
    )
}

private fun getContractAddress(votingIdentifier: String): String {
    var address: String? = null
    while (address == null) {
        try {
            val baseUrl = "http://host.docker.internal:8081/contractAddress"
            val url = "$baseUrl?votingIdentifier=$votingIdentifier"
            address = RestTemplate().getForObject(url, String::class.java) as String
            println("Contract address: $address")
        } catch (e: Exception) {
            println("Cannot get contract address. Retrying... error: ${e.message}")
        }
        Thread.sleep(1000)
    }
    return address
}