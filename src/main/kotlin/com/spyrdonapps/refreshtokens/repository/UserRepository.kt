package com.spyrdonapps.refreshtokens.repository

import com.spyrdonapps.refreshtokens.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
}

