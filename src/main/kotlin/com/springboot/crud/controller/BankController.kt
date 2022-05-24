package com.springboot.crud.controller

import com.springboot.crud.model.Bank
import com.springboot.crud.model.Branch
import com.springboot.crud.repository.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/api")
class BankController (private val bankRepository: BankRepository,
                      private val branchRepository: BranchRepository) {

    @GetMapping("/banks")
    fun getAllBanks(): List<String> {
        var banks: MutableList<String> = mutableListOf()

        val bankCollection = bankRepository.findAll()

        bankCollection.forEach{
            banks.add(it.name)
        }
        return banks
    }

    @GetMapping("/bank")
    fun getBankDetails(@RequestParam name: String): Bank? {

        return bankRepository.findBankByName(name) ?: throw Exception("Bank with this name does not exist")
    }

    @GetMapping("/bank/branches")
    fun getBankBranches(@RequestParam bank: String): List<Branch>? {

        val bankObj = bankRepository.findBankByName(bank) ?: throw Exception("Bank with this name does not exist")

        return branchRepository.findByBank(bankObj)
    }

    @GetMapping("/bank/branch")
    fun getBranchByIfsc(@RequestParam ifsc: String): Branch? {

        return branchRepository.findByIfsc(ifsc) ?: throw Exception("Branch with this IFSC does not exist")
    }

    @PostMapping("/bank/add-branch")
    fun addNewBankBranch(@RequestBody payload: Map<String, String>): String {

        if (!payload.containsKey("bank")) {
            throw IllegalArgumentException("Bank name required")
        }

        var availableBanks: MutableList<String> = mutableListOf()

        val bankCollection = bankRepository.findAll()
        bankCollection.forEach {
            availableBanks.add(it.name)
        }

        var bank = bankRepository.findBankByName(payload["bank"].toString())
        if ( bank == null || payload["bank"].toString() !in availableBanks) {

            throw Exception("This bank does not exists. Please choose from these available banks: ${availableBanks.joinToString(",")}")
        }
        if (!payload.containsKey("ifsc") || payload["ifsc"].equals("") || payload["ifsc"].equals(null)) {
            throw IllegalArgumentException("Please provide a valid IFSC code.")
        }
        if (branchRepository.findByIfsc(payload["ifsc"].toString()) != null){
            throw Exception("A Branch with this IFSC code already exists")
        }

        var branch = Branch(bank=bank, ifsc=payload["ifsc"].toString(), address=payload["address"].toString(), branchManager=payload["manager"].toString())
        branchRepository.save(branch)

        return "Branch successfully created"
    }

    @PatchMapping("/bank/update-branch")
    fun updateBankBranch(@RequestBody payload: Map<String, String>): String {

        if (!payload.containsKey("id")) {
            throw IllegalArgumentException("Unique Id of the Branch to be updated is required")
        }

        var branch = payload["id"]?.let { branchRepository.findByIdOrNull(it.toInt()) } ?: throw NoSuchElementException("Branch with this Id does not exist")
        branch.ifsc = payload["ifsc"] ?: branch.ifsc
        branch.address = payload["address"] ?: branch.address
        branch.branchManager = payload["manager"] ?: branch.branchManager
        branchRepository.save(branch)

        return "Branch details updated successfully"
    }

    @DeleteMapping("/bank/delete-branch")
    fun deleteBankBranch(@RequestBody payload: Map<String, String>): String {

        if (!payload.containsKey("id")) {
            throw IllegalArgumentException("Id of the Branch to be deleted is required")
        }

        var branch = payload["id"]?.let { branchRepository.findByIdOrNull(it.toInt()) } ?: throw NoSuchElementException("Branch with this Id does not exist")
        branchRepository.delete(branch)

        return "Branch successfully deleted"
    }

    @PostMapping("/banks/add-bank")
    fun addNewBank(@RequestBody payload: Map<String, String>): String {

        if (!payload.containsKey("name") || payload["name"].equals("") || payload["name"].equals(null)) {
            throw IllegalArgumentException("Bank name required")
        }

        if (bankRepository.findBankByName(payload["name"].toString()) != null){
            throw Exception("Bank already exists")
        }

        var bank = Bank(UUID.randomUUID().toString(), payload["name"].toString(), payload["headquarter"].toString())
        bankRepository.save(bank)

        return "Bank successfully added"
    }

    @PatchMapping("/banks/update-bank")
    fun updateBankDetails(@RequestBody payload: Map<String, String>): String {

        if (!payload.containsKey("id")) {
            throw IllegalArgumentException("Unique Id of the Bank to be updated is required")
        }

        if (!payload.containsKey("name") || payload["name"].equals("") || payload["name"].equals(null)) {
            throw IllegalArgumentException("Please provide valid bank name")
        }

        val bankToUpdate = bankRepository.findByIdOrNull(payload["id"].toString()) ?: throw NoSuchElementException("Bank with this Id does not exist")

        bankToUpdate.name = payload["name"].toString()
        bankToUpdate.headquarter = payload["headquarter"].toString()

        bankRepository.save(bankToUpdate)

        return "Bank details updated successfully"
    }

    @DeleteMapping("/banks/delete-bank")
    fun deleteBank(@RequestBody payload: Map<String, String>): String {

        if (!payload.containsKey("name")) {
            throw IllegalArgumentException("Bank name required")
        }

        val bankToDelete = bankRepository.findBankByName(payload["name"].toString())
            ?: throw NoSuchElementException("Bank with this name does not exist")

        bankRepository.delete(bankToDelete)

        return "Bank successfully deleted"
    }
}