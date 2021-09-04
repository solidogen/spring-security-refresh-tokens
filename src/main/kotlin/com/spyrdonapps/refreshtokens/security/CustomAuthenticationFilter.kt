package com.spyrdonapps.refreshtokens.security

import com.auth0.jwt.JWT
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CustomAuthenticationFilter(authenticationManager: AuthenticationManager) :
    UsernamePasswordAuthenticationFilter(authenticationManager) {

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        // todo try to receive json body instead, use object mapper (jackson)
        val username = request.getParameter("username")
        val password = request.getParameter("password")
        val authenticationToken = UsernamePasswordAuthenticationToken(username, password)
        return authenticationManager.authenticate(authenticationToken)
    }

    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        authentication: Authentication
    ) {
        val user = authentication.principal as User
        val algorithm = JwtUtils.algorithm
        val accessToken = JWT.create()
            .withSubject(user.username)
            .withExpiresAt(Instant.now().plus(Duration.ofMinutes(ACCESS_TOKEN_VALIDITY_MINUTES)).let { Date.from(it) })
            .withIssuer(request.requestURL.toString())
            .withClaim("roles", user.authorities.map { it.authority })
            .withClaim("token_type", "access") // todo is this fine? I guess I should encrypt this value
            .sign(algorithm)
        val refreshToken = JWT.create()
            .withSubject(user.username)
            .withExpiresAt(Instant.now().plus(Duration.ofMinutes(REFRESH_TOKEN_VALIDITY_MINUTES)).let { Date.from(it) })
            .withIssuer(request.requestURL.toString())
            .withClaim("roles", user.authorities.map { it.authority }) // todo - this lets user use api with refresh_token, change this to empty
            .withClaim("token_type", "refresh") // todo is this fine? I guess I should encrypt this value
            .sign(algorithm)
        val tokens = hashMapOf<String, String>( // todo - this sucks, use a class
            "access_token" to accessToken,
            "refresh_token" to refreshToken
        )
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        JsonUtils.objectMapper.writeValue(response.outputStream, tokens)
    }

    companion object {
        const val ACCESS_TOKEN_VALIDITY_MINUTES: Long = 10
        const val REFRESH_TOKEN_VALIDITY_MINUTES: Long = 30
    }
}