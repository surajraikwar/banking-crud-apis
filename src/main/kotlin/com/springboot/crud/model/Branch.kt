package com.springboot.crud.model

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import javax.persistence.*
import javax.validation.constraints.*

@Entity
@Table(name="branch")
data class Branch(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Int = Int.MAX_VALUE,
    @get: NotBlank var ifsc: String,
    @ManyToOne(cascade = ([CascadeType.PERSIST]))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @get: NotNull val bank: Bank,
    var address: String,
    var branchManager: String
    )
