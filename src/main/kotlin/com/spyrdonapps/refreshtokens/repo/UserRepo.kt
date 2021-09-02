package com.spyrdonapps.refreshtokens.repo

import com.spyrdonapps.refreshtokens.domain.Role
import com.spyrdonapps.refreshtokens.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepo : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
}

interface RoleRepo : JpaRepository<Role, Long> {
    fun findByName(name: String): Role?
}