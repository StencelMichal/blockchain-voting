package com.stencel.evoting.smartcontracts.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.ContractGasProvider
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.tx.gas.StaticGasProvider
import java.math.BigInteger

@Configuration
class Web3jConfiguration {

    @Bean
    fun web3j(): Web3j {
        return Web3j.build(HttpService("http://localhost:8545"))
    }

    @Bean
    fun credentials(): Credentials {
        return Credentials.create("0xcfcf3f2d7deb189365799e9ab4ef34fae2350e908bd64678619a849c7c827a06")
    }

    @Bean
    fun contractGasProvider(): ContractGasProvider {
        return StaticGasProvider(BigInteger.valueOf(20000000000), BigInteger.valueOf(9000000))
//        return DefaultGasProvider()

    }
}