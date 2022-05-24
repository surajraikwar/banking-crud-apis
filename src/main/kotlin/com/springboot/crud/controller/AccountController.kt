package com.springboot.crud.controller

import com.springboot.crud.model.Account
import com.springboot.crud.model.AccountType
import com.springboot.crud.model.Branch
import com.springboot.crud.repository.AccountRepository
import com.springboot.crud.repository.BankRepository
import com.springboot.crud.repository.BranchRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Payload
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class AccountController(private val accountRepository: AccountRepository,
                        private val branchRepository: BranchRepository,
                        private val bankRepository: BankRepository) {

    @GetMapping("/accounts")
    fun getAllAccounts(): MutableIterable<Account> =
        accountRepository.findAll()

    @GetMapping("/bank/accounts")
    fun getAccountsByBank(@RequestParam bank: String): List<Account>? {

        val bank = bankRepository.findBankByName(bank) ?: throw Exception("Invalid bank")
        return accountRepository.findAccountsByBank(bank)
    }

    @GetMapping("/bank/branch/accounts")
    fun getAccountsByIfsc(@RequestParam ifsc: String): List<Account>? {

        val branch = branchRepository.findByIfsc(ifsc) ?: throw Exception("Branch with this IFSC does not exist")
        return accountRepository.findAccountsByBranch(branch)
    }

    @GetMapping("/account")
    fun getAccountByAccountNumber(@RequestParam account_number: Long): Account? {
        return accountRepository.findAccountByAccountNumber(account_number) ?: throw NoSuchElementException("Account with this account_number does not exist")
    }

    @PostMapping("/account/open-account")
    fun createNewAccount(@RequestBody payload: Map<String, String>): String{

        if (!payload.containsKey("name") || payload["name"].equals("") || payload["name"].equals(null)) {
            throw IllegalArgumentException("Name of the person required")
        }
        if (!payload.containsKey("ifsc") || payload["ifsc"].equals("") || payload["ifsc"].equals(null)) {
            throw IllegalArgumentException("Ifsc code of the bank branch is required")
        }
        if (!payload.containsKey("account_type") || payload["account_type"].equals("") || payload["account_type"].equals(null)) {
            throw IllegalArgumentException("account_type is required")
        }
        if (!payload.containsKey("aadhaar") || payload["aadhaar"].equals("") || payload["aadhaar"].equals(null)) {
            throw IllegalArgumentException("AADHAAR number of the person is mandatory")
        }

        var accountNumber: Long
        while (true){
            accountNumber = (10000000000..999999999999).random()

            accountRepository.findAccountByAccountNumber(accountNumber) ?: break
        }
        val aadhaar = payload["aadhaar"]?.toLong() ?: 0
        val branch = payload["ifsc"]?.let { branchRepository.findByIfsc(it.toString()) } ?: throw NoSuchElementException("Branch with this Ifsc does not exist")
        val accountType = when (payload["account_type"]) {
            "Savings" -> AccountType.SAVINGS
            "Current" -> AccountType.CURRENT
            "Salary" -> AccountType.SALARY
            "Fixed Deposit" -> AccountType.FIXED_DEPOSIT
            else -> throw IllegalArgumentException("Invalid Account type. Choose from available following account types: Savings, Current, Salary, Fixed Deposit")
        }

        var account = Account(accountHolderName = payload["name"].toString(),
                accountNumber = accountNumber,
                branch = branch,
                bank = branch.bank,
                aadhaar = aadhaar,
                pan = payload["pan"].toString(),
                accountType = accountType,
                accountBalance = payload["balance"]?.toDoubleOrNull() ?: 0.0
        )
        accountRepository.save(account)
        return "Account opened successfully"
    }

    @PostMapping("/account/update-balance")
    fun updateAccountBalance(@RequestBody payload: Map<String, String>): String{
        if (!payload.containsKey("account_number") || payload["account_number"].equals("") || payload["account_number"].equals(null)) {
            throw IllegalArgumentException("account_number is required")
        }
        var account = payload["account_number"]?.toLong()?.let { accountRepository.findAccountByAccountNumber(it) }
            ?: throw NoSuchElementException("Invalid account number")

        if (!payload.containsKey("transaction_type") || payload["transaction_type"].equals("") || payload["transaction_type"].equals(null)) {
            throw IllegalArgumentException("Please provide the transaction_type: credit/debit")
        }
        if (payload.containsKey("amount")) {
            if (payload["amount"].equals("") || payload["amount"].equals(null) || payload["amount"]!!.toDouble() < 1){
                throw IllegalArgumentException("Please provide valid amount greater than 1")
            }
        }else{
            throw IllegalArgumentException("Please provide valid amount")
        }

        var accountBalance: Double = account.accountBalance ?: 0.0

        if (payload["transaction_type"] == "debit"){

            if ( payload["amount"]!!.toDouble() > accountBalance){
                throw Exception("Requested amount is higher than available account balance. Please request a lower amount")
            }
            account.accountBalance = accountBalance - payload["amount"]!!.toDouble()
        }else{
            account.accountBalance = accountBalance + payload["amount"]!!.toDouble()
        }
        accountRepository.save(account)

        return "Transaction Successfull"
    }

    @PatchMapping("/account/update-details")
    fun updateAccountDetails(@RequestBody payload: Map<String, String>): String {

        if (!payload.containsKey("account_number") || payload["account_number"].equals("") || payload["account_number"].equals(null)) {
            throw IllegalArgumentException("account_number is required")
        }

        var account = payload["account_number"]?.toLong()?.let { accountRepository.findAccountByAccountNumber(it) }
            ?: throw NoSuchElementException("Invalid account number")

        if (account.aadhaar != payload["aadhaar"]?.toLong()){
            throw Exception("You have provided incorrect AADHAAR details. Only account holder can update account details.")
        }


        account.accountHolderName = payload["name"] ?: account.accountHolderName
        account.pan = payload["pan"] ?: account.pan

        if (payload.containsKey("account_type")) {
            if (payload["account_type"].equals("") || payload["account_type"].equals(null)){
                throw IllegalArgumentException("Invalid account_type")
            }else{
                account.accountType = when (payload["account_type"]) {
                    "Savings" -> AccountType.SAVINGS
                    "Current" -> AccountType.CURRENT
                    "Salary" -> AccountType.SALARY
                    "Fixed Deposit" -> AccountType.FIXED_DEPOSIT
                    else -> throw IllegalArgumentException("Invalid Account type. Choose from available following account types: Savings, Current, Salary, Fixed Deposit")
                }
            }
        }
        if (payload.containsKey("ifsc")) {
            if (payload["ifsc"].equals("") || payload["ifsc"].equals(null)){
                throw IllegalArgumentException("Invalid Ifsc code")
            }else{
                var branch = branchRepository.findByIfsc(payload["ifsc"].toString()) ?: throw Exception("Branch with this IFSC does not exist")
                account.branch = branch
            }
        }
        accountRepository.save(account)
        return "Account details updated successfully"
    }

    @DeleteMapping("/account/close-account")
    fun deleteAccount(@RequestBody payload: Map<String, String>): String {

        if (!payload.containsKey("account_number") || payload["account_number"].equals("") || payload["account_number"].equals(null)) {
            throw IllegalArgumentException("account_number is required")
        }

        var branch: Branch
        if (!payload.containsKey("ifsc") || payload["ifsc"].equals("") || payload["ifsc"].equals(null)) {
            throw IllegalArgumentException("Ifsc code of the bank branch is required")
        }else{
            branch = branchRepository.findByIfsc(payload["ifsc"].toString()) ?: throw Exception("Branch with this IFSC does not exist")
        }

        if (!payload.containsKey("aadhaar") || payload["aadhaar"].equals("") || payload["aadhaar"].equals(null)) {
            throw IllegalArgumentException("AADHAAR number of the person is mandatory")
        }

        var account = payload["account_number"]?.toLong()?.let { accountRepository.findAccountByAccountNumber(it) }
            ?: throw NoSuchElementException("Invalid account number")

        if (account.branch != branch){
            throw Exception("The IFSC provided does not match with the account IFSC")
        }
        if (account.aadhaar != payload["aadhaar"]?.toLong()){
            throw Exception("Please provide the correct AADHAAR number of the user")
        }

        accountRepository.delete(account)
        return "Account Successfully deleted"
    }
}