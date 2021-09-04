package com.spyrdonapps.refreshtokens.security

import com.auth0.jwt.algorithms.Algorithm

object JwtUtils {
    // secret should be encrypted etc, maybe pass this as System property?
    val algorithm = Algorithm.HMAC256("secret".toByteArray())
}