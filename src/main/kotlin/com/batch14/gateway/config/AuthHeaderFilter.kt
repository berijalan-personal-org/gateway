package com.batch14.gateway.config

import com.batch14.gateway.domain.constant.Constant
import com.batch14.gateway.exception.CustomException
import com.batch14.gateway.util.JwtUtil
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthHeaderFilter (
    val authValidator: AuthValidator,
    val jwtUtil: JwtUtil
) : GatewayFilter {
    //cek apakah pada request ada header authorization

    private fun isAuthMissing(request: ServerHttpRequest): Boolean {
        return !request.headers.containsKey("Authorization")
    }

    //fungsi ambil JWT Token
    private fun getAuthHeader(request: ServerHttpRequest): String? {
        return if(request.headers
            .getOrEmpty("Authorization")[0]
                .split(" ").size == 2
            ){
                request.headers.getOrEmpty("Authorization")[0]
                    .split(" ")[1]
            } else {
                //jika tidak ada token, kembalikan string kosong
                ""
        }
    }

    //populate request header
    private fun populateRequestHeader(
        exchange: ServerWebExchange,
        token: String
    ) {
        //extract claims
        val claims = jwtUtil.decode(token)

        //populate header
        exchange.request.mutate()
            .header(Constant.HEADER_USER_ID, claims["idUser"].toString())
            .header(Constant.HEADER_USER_ROLE, claims["role"].toString())
            .build()
    }


    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain
    ): Mono<Void?>? {
        //ambil objek request
        val request: ServerHttpRequest = exchange.request as ServerHttpRequest

        //cet whitelist url
        if (authValidator.isSecure.test(request)) {
            //validasi token
            //1. cek apakah ada header authorization
            if (isAuthMissing(request)) {
                throw CustomException(
                    "You don't have permission",
                    HttpStatus.UNAUTHORIZED.value(),
                    401
                )
            }

            //2. cek apakah token ada di dalam request header Auth
            val token = if(getAuthHeader(request) != null) {
                getAuthHeader(request)!!
            } else {
                throw CustomException(
                    "You don't have permission",
                    HttpStatus.UNAUTHORIZED.value(),
                    401
                )
            }

            //3. populate request header
            jwtUtil.decode(token)
            populateRequestHeader(exchange, token)
        }
        return chain.filter(exchange)
    }

    //disable default behaviour dari spring security
    //disable csrf token
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity)
    : SecurityWebFilterChain {
        http.csrf {csrf -> csrf.disable()}
        return http.build()
    }

}