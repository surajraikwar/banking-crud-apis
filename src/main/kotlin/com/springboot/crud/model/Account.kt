package com.springboot.crud.model

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import javax.persistence.*
import javax.validation.constraints.*

@Entity
@Table(name="account")
data class Account(@Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Int = Int.MAX_VALUE,
                   val accountNumber: Long,
                   @get: NotBlank var accountHolderName: String,
                   @get: NotNull var aadhaar: Long,
                   var pan: String,
                   var accountBalance: Double? = 0.0,
                   var accountType: AccountType,
                   @ManyToOne(cascade = arrayOf(CascadeType.PERSIST))
                   @OnDelete(action = OnDeleteAction.CASCADE)
                   @get: NotNull val bank: Bank,
                   @ManyToOne(cascade = arrayOf(CascadeType.PERSIST))
                   @OnDelete(action = OnDeleteAction.CASCADE)
                   @get: NotNull var branch: Branch
                   )
