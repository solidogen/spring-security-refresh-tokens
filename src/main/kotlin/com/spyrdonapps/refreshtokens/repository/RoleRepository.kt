package com.spyrdonapps.refreshtokens.repository

import com.spyrdonapps.refreshtokens.domain.Role
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository : JpaRepository<Role, Long> {
    fun findByName(name: String): Role?
}