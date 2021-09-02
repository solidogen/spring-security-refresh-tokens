package com.spyrdonapps.refreshtokens

import com.spyrdonapps.refreshtokens.domain.Role
import com.spyrdonapps.refreshtokens.domain.User
import com.spyrdonapps.refreshtokens.service.UserService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@SpringBootApplication
class RefreshtokensApplication {

	@Bean
	fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

	@Bean
	fun init(userService: UserService) = CommandLineRunner { args ->
		userService.saveRole(Role(id = 0, name = "ROLE_USER"))
		userService.saveRole(Role(id = 0, name = "ROLE_MANAGER"))
		userService.saveRole(Role(id = 0, name = "ROLE_ADMIN"))
		userService.saveRole(Role(id = 0, name = "ROLE_SUPERADMIN"))

		userService.saveUser(User(id = 0, name = "John Travolta", username = "john", password = "1234", roles = mutableListOf()))
		userService.saveUser(User(id = 0, name = "Will Smith", username = "will", password = "1234", roles = mutableListOf()))
		userService.saveUser(User(id = 0, name = "Jim Carrey", username = "jim", password = "1234", roles = mutableListOf()))
		userService.saveUser(User(id = 0, name = "Arnold Schwarzenegger", username = "arnold", password = "1234", roles = mutableListOf()))

        userService.addRoleToUser("john", "ROLE_USER")
        userService.addRoleToUser("john", "ROLE_MANAGER")
        userService.addRoleToUser("will", "ROLE_MANAGER")
        userService.addRoleToUser("jim", "ROLE_ADMIN")
        userService.addRoleToUser("arnold", "ROLE_SUPERADMIN")
        userService.addRoleToUser("arnold", "ROLE_ADMIN")
        userService.addRoleToUser("arnold", "ROLE_USER")
	}
}

fun main(args: Array<String>) {
	runApplication<RefreshtokensApplication>(*args)
}
