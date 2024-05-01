package com.stencel.evoting.smartcontracts.util

import com.fasterxml.jackson.databind.ObjectMapper
import java.math.BigInteger

data class VoteContent(
    val encryptedVotes: List<String>,
    val encryptedExponents: List<BigInteger>,
    val voterEncryptedAnswers: List<String>,
    val encodedAnswersExponents: List<BigInteger>,
) {
    private val mapper = ObjectMapper()

    fun toJson(): String {
        return mapper.writeValueAsString(this)
    }
}
