package com.spyrdonapps.refreshtokens.security

import com.auth0.jwt.JWT
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CustomAuthorizationFilter : OncePerRequestFilter() {

    private val objectMapper = jacksonObjectMapper()

    /**
    * todo - do something so refresh_token can be used only for refreshing, not everything else
    * */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (request.servletPath == "/api/login") {
            // no authorization
            filterChain.doFilter(request, response)
        } else {
            val authorizationHeader: String? = request.getHeader(HttpHeaders.AUTHORIZATION)
            if (authorizationHeader?.startsWith(BEARER_WITH_SPACE_PREFIX) == true) {
                try {
                    val token = authorizationHeader.substringAfter(BEARER_WITH_SPACE_PREFIX)
                    val algorithm = JwtUtils.algorithm
                    val verifier = JWT.require(algorithm).build()
                    val decodedJwt = verifier.verify(token)
                    val username = decodedJwt.subject
                    val roles = decodedJwt.claims["roles"]?.asList(String::class.java).orEmpty()
                    val authorities = roles.map { SimpleGrantedAuthority(it) }
                    val authenticationToken = UsernamePasswordAuthenticationToken(username, null, authorities)
                    SecurityContextHolder.getContext().authentication = authenticationToken
                    filterChain.doFilter(request, response)
                } catch (e: Exception) {
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.status = HttpServletResponse.SC_FORBIDDEN
                    objectMapper.writeValue(response.outputStream, mapOf("error_message" to e.message)) // todo - use class
                }
            } else {
                filterChain.doFilter(request, response)
            }
        }
    }

    companion object {
        private const val BEARER_WITH_SPACE_PREFIX = "Bearer "
    }
}