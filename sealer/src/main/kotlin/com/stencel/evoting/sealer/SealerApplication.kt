package com.stencel.evoting.sealer

import com.stencel.evoting.sealer.daemon.VoteValidatorDaemon
import com.stencel.evoting.smartcontracts.VotingState
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestTemplate
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.tx.gas.ContractGasProvider
import kotlin.system.exitProcess


@SpringBootApplication(scanBasePackages = ["com.stencel.evoting.sealer", "com.stencel.evoting.smartcontracts"])
@EntityScan(basePackages = ["com.stencel.evoting.smartcontracts.database"])
@EnableJpaRepositories("com.stencel.evoting.smartcontracts.database")
@EnableScheduling
class SealerApplication

fun main(args: Array<String>) {
    val springContext = runApplication<SealerApplication>(*args)
    val credentials = getPrivateKey()
    val votingIdentifier = getVotingIdentifier()
    val contract = getContract(springContext, votingIdentifier, credentials)
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

private fun getContract(
    springContext: ConfigurableApplicationContext,
    votingIdentifier: String,
    credentials: Credentials,
): VotingState {
    val contractAddress = getContractAddress(votingIdentifier)
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
    val baseUrl = "http://localhost:8081/contractAddress"
    val url = "$baseUrl?votingIdentifier=$votingIdentifier"
    return RestTemplate().getForObject(url, String::class.java) as String
}