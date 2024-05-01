package com.stencel.evoting.voter.service

import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ContractAddressResolverService {

    private val baseUrl = "http://host.docker.internal:8081/contractAddress"
    private val restTemplate = RestTemplate()

    fun resolveContractAddress(votingIdentifier: String): String? {
        val url = "$baseUrl?votingIdentifier=$votingIdentifier"
        return restTemplate.getForObject(url, String::class.java) as String
    }
}