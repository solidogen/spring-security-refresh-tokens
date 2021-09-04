package com.spyrdonapps.refreshtokens.service

import com.spyrdonapps.refreshtokens.domain.Role
import com.spyrdonapps.refreshtokens.domain.User
import org.springframework.security.core.userdetails.User as SpringSecUser
import com.spyrdonapps.refreshtokens.repository.RoleRepository
import com.spyrdonapps.refreshtokens.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
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
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder
) : UserService, UserDetailsService {

    override fun saveUser(user: User): User {
        user.password = passwordEncoder.encode(user.password)
        return userRepository.save(user)
    }

    override fun saveRole(role: Role): Role = roleRepository.save(role)

    override fun addRoleToUser(username: String, roleName: String) {
        val user = userRepository.findByUsername(username) ?: error("No user with username: $username")
        val role = roleRepository.findByName(roleName) ?: error("No role with name: $roleName")
        user.roles.add(role)
    }

    override fun getUser(username: String): User? = userRepository.findByUsername(username)

    override fun getUsers(): List<User> = userRepository.findAll()

    @OptIn(ExperimentalStdlibApi::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("No user with username: $username")
        val authorities: List<SimpleGrantedAuthority> = buildList {
            user.roles.forEach { role -> add(SimpleGrantedAuthority(role.name)) }
        }
        return SpringSecUser(user.username, user.password, authorities)
    }

}