package com.spyrdonapps.refreshtokens.api

import com.auth0.jwt.JWT
import com.spyrdonapps.refreshtokens.domain.Role
import com.spyrdonapps.refreshtokens.domain.RoleToUserForm
import com.spyrdonapps.refreshtokens.domain.User
import com.spyrdonapps.refreshtokens.security.CustomAuthenticationFilter
import com.spyrdonapps.refreshtokens.security.CustomAuthorizationFilter
import com.spyrdonapps.refreshtokens.security.JsonUtils
import com.spyrdonapps.refreshtokens.security.JwtUtils
import com.spyrdonapps.refreshtokens.service.UserService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/api")
class UserController(
    private val userService: UserService
) {
    @GetMapping("/users")
    fun getUsers(): ResponseEntity<List<User>> = ResponseEntity.ok(userService.getUsers())

    @PostMapping("/user/save")
    fun saveUser(@RequestBody user: User): ResponseEntity<User> {
        val uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/save").toUriString())
        return ResponseEntity.created(uri).body(userService.saveUser(user))
    }

    @PostMapping("/role/save")
    fun saveRole(@RequestBody role: Role): ResponseEntity<Role> {
        val uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/role/save").toUriString())
        return ResponseEntity.created(uri).body(userService.saveRole(role))
    }

    @PostMapping("/role/addtouser")
    fun addRoleToUser(@RequestBody roleToUserForm: RoleToUserForm): ResponseEntity<Unit> =
        ResponseEntity.ok(userService.addRoleToUser(username = roleToUserForm.username, roleName = roleToUserForm.roleName))

    @GetMapping("/token/refresh") // todo - should be POST with refresh token in body, no headers
    fun refreshToken(request: HttpServletRequest, response: HttpServletResponse) {
        // todo extract duplicated code
        val authorizationHeader: String? = request.getHeader(HttpHeaders.AUTHORIZATION)
        // todo this assumes we put refresh token into header. this is not wanted - we should send refresh token in body instead!
        if (authorizationHeader?.startsWith(CustomAuthorizationFilter.BEARER_WITH_SPACE_PREFIX) == true) {
            try {
                val oldRefreshToken = authorizationHeader.substringAfter(CustomAuthorizationFilter.BEARER_WITH_SPACE_PREFIX)
                val algorithm = JwtUtils.algorithm
                val verifier = JWT.require(algorithm).build()
                val decodedJwt = verifier.verify(oldRefreshToken)
                val tokenType = decodedJwt.claims["token_type"]?.asString()
                if (tokenType != "refresh") {
                    throw IllegalArgumentException("Wrong token type: $tokenType") // todo saying too much to public, but for testing purposes
                }
                val username = decodedJwt.subject
                val user = userService.getUser(username) ?: error("User not found")

                // todo duplicated
                val accessToken = JWT.create()
                    .withSubject(user.username)
                    .withExpiresAt(Instant.now().plus(Duration.ofMinutes(CustomAuthenticationFilter.ACCESS_TOKEN_VALIDITY_MINUTES)).let { Date.from(it) })
                    .withIssuer(request.requestURL.toString())
                    .withClaim("roles", user.roles.map { it.name })
                    .withClaim("token_type", "access") // make it less verbose, also constant
                    .sign(algorithm)

                // todo revoke oldRefreshToken in database
                val newRefreshToken = JWT.create()
                    .withSubject(user.username)
                    .withExpiresAt(Instant.now().plus(Duration.ofMinutes(CustomAuthenticationFilter.REFRESH_TOKEN_VALIDITY_MINUTES)).let { Date.from(it) })
                    .withIssuer(request.requestURL.toString())
                    .withClaim("token_type", "refresh") // make it less verbose, also constant
                    .withClaim("roles", decodedJwt.claims["roles"]?.asList(String::class.java).orEmpty()) // the same as they were
                    .sign(algorithm)
                val tokens = hashMapOf<String, String>( // todo - this sucks, use a class
                    "access_token" to accessToken,
                    "refresh_token" to newRefreshToken
                )
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                JsonUtils.objectMapper.writeValue(response.outputStream, tokens)
            } catch (e: Exception) {
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                response.status = HttpServletResponse.SC_FORBIDDEN
                JsonUtils.objectMapper.writeValue(response.outputStream, mapOf("error_message" to e.message)) // todo - use class
            }
        } else {
            error("Refresh token is missing")
        }
    }
}