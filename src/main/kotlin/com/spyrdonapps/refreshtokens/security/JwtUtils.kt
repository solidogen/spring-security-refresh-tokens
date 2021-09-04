package com.spyrdonapps.refreshtokens.security

import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JwtUtils {
    // secret should be encrypted etc, maybe pass this as System property?
    val algorithm: Algorithm = Algorithm.HMAC256("secret".toByteArray())
}

object JsonUtils {
    // refactor
    val objectMapper = jacksonObjectMapper()
}