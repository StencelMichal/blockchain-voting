package com.stencel.evoting.sealer.database

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class SmartContract(
    @Id
    val contractName: String,
    val contractAddress: String,
    val hash: String
)