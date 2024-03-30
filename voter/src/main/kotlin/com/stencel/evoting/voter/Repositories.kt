package com.stencel.evoting.voter

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SmartContractAddressRepository: CrudRepository<SmartContract, String>
