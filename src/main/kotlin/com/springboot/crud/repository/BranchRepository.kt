package com.springboot.crud.repository

import com.springboot.crud.model.Bank
import com.springboot.crud.model.Branch
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BranchRepository: CrudRepository<Branch, Int>{

    fun findByBank(bank: Bank): List<Branch>
    fun findByIfsc(ifsc: String): Branch?
}