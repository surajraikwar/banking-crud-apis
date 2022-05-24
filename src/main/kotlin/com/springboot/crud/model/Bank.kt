package com.springboot.crud.model

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
@Table(name="bank")
data class Bank(@Id val id: String,
                @get: NotBlank var name: String,
                var headquarter: String? = null
                )
