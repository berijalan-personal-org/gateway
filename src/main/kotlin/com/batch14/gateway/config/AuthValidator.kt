package com.batch14.gateway.config

import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import java.util.function.Predicate

@Component
class AuthValidator {
    //list url yang whitelist
    val whitelistUrl = listOf(
        "users/login",
        "users/register",
        "users/verify-otp"
    )

    //cek apakah request whitelist
    val isSecure: Predicate<ServerHttpRequest> =
        Predicate<ServerHttpRequest> {
            request ->
                whitelistUrl.stream().noneMatch { uri: String ->
                    request.uri.path.contains(uri)
                }
        }
}