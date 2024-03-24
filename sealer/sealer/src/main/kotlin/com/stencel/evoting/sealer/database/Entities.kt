package com.stencel.evoting.sealer.database

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class SmartContractAddress(
    @Id
    var contractName: String,
    @Column(nullable = false)
    var contractAddress: String
)