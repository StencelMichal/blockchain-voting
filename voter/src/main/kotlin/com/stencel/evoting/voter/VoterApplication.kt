package com.stencel.evoting.voter

import com.n1analytics.paillier.PaillierPublicKey
import com.stencel.evoting.smartcontracts.util.CryptographyUtils.Companion.generateRsaKeyPair
import com.stencel.evoting.smartcontracts.ringSignature.RingSignature
import com.stencel.evoting.smartcontracts.VotingState
import com.stencel.evoting.smartcontracts.configuration.Web3jConfiguration
import com.stencel.evoting.smartcontracts.service.VotingStateManagerService
import com.stencel.evoting.voter.cli.CLI
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.web3j.crypto.Credentials
import org.web3j.tuples.generated.Tuple2
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPair
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.*

@SpringBootApplication(scanBasePackages = ["com.stencel.evoting.voter", "com.stencel.evoting.smartcontracts"])
@EntityScan(basePackages = ["com.stencel.evoting.smartcontracts.database"])
@EnableJpaRepositories("com.stencel.evoting.smartcontracts.database")
class VoterApplication

fun main(args: Array<String>) {
    val context = runApplication<VoterApplication>(*args)
    val cli = context.getBean(CLI::class.java)
    cli.interpret()
}
