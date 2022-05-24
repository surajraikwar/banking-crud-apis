package com.springboot.crud.repository

import com.springboot.crud.model.Bank
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BankRepository : CrudRepository<Bank, String>{

    fun findBankByName(name: String): Bank?
}



