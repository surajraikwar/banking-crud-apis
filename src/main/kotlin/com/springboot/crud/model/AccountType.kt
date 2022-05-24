package com.springboot.crud.model

enum class AccountType(val accountType: String) {
    SAVINGS("Savings"),
    CURRENT("Current"),
    SALARY("Salary"),
    FIXED_DEPOSIT("Fixed Deposit")
}