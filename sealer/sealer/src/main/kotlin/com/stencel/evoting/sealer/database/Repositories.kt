package com.stencel.evoting.sealer.database

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SmartContractAddressRepository: CrudRepository<SmartContract, String>
