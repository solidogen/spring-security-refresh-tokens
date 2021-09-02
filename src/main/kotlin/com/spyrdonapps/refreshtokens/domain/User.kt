package com.spyrdonapps.refreshtokens.domain

import javax.persistence.*

@Entity(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,
    val name: String,
    val username: String,
    val password: String,
    @ManyToMany(fetch = FetchType.EAGER)
    val roles: MutableList<Role> = mutableListOf()
)