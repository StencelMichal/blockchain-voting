package com.stencel.evoting.smartcontracts.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.ContractGasProvider
import org.web3j.tx.gas.StaticGasProvider
import java.math.BigInteger

@Configuration
class Web3jConfiguration {

    @Value("\${rpcAddress}")
    private lateinit var rpcAddress: String

    @Bean
    fun web3j(): Web3j {
        return Web3j.build(HttpService("http://${rpcAddress}:8545"))
    }

    @Bean
    fun contractGasProvider(): ContractGasProvider {
        return StaticGasProvider(BigInteger.valueOf(20000000000), BigInteger.valueOf(9000000))
    }
}