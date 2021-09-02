package com.spyrdonapps.refreshtokens.service

import com.spyrdonapps.refreshtokens.domain.Role
import com.spyrdonapps.refreshtokens.domain.User
import com.spyrdonapps.refreshtokens.repo.RoleRepo
import com.spyrdonapps.refreshtokens.repo.UserRepo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface UserService {
    fun saveUser(user: User): User
    fun saveRole(role: Role): Role
    fun addRoleToUser(username: String, roleName: String)
    fun getUser(username: String): User?
    fun getUsers(): List<User> // pagination in real app
}

@Service
@Transactional
class DefaultUserService(
    private val userRepo: UserRepo,
    private val roleRepo: RoleRepo
) : UserService {

    override fun saveUser(user: User): User = userRepo.save(user)

    override fun saveRole(role: Role): Role = roleRepo.save(role)

    override fun addRoleToUser(username: String, roleName: String) {
        val user = userRepo.findByUsername(username) ?: error("No user with username: $username")
        val role = roleRepo.findByName(roleName) ?: error("No role with name: $roleName")
        user.roles.add(role)
    }

    override fun getUser(username: String): User? = userRepo.findByUsername(username)

    override fun getUsers(): List<User> = userRepo.findAll()

}