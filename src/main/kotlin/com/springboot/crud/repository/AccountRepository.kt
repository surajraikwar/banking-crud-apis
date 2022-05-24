package com.springboot.crud.repository

import com.springboot.crud.model.Account
import com.springboot.crud.model.Bank
import com.springboot.crud.model.Branch
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.Repository

interface AccountRepository : CrudRepository<Account, Int> {

//    fun openBankAccount(account: Account): Account
//    fun saveAccount(account: Account): Account
    fun findAccountByAccountNumber(accountNumber: Long): Account?
    fun findAccountsByBranch(branch: Branch): List<Account>?
    fun findAccountsByBank(bank: Bank): List<Account>?
}