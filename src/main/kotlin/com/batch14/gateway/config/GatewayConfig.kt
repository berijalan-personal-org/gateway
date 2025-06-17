package com.batch14.gateway.config

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.PredicateSpec
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GatewayConfig (
    private val authHeaderFilter: AuthHeaderFilter
){
    //ROUTING
    @Bean
    fun routes(builder: RouteLocatorBuilder): RouteLocator? {
        return builder.routes()
        //routing ke user service
            .route("user-service") {r: PredicateSpec ->
                r.path("/gateway/user-service/**")
                    .filters {f: GatewayFilterSpec ->
                        //rewrite path
                        f.rewritePath("/gateway/user-service/(?<segment>.*)",
                            "/user-service/\${segment}"
                        )
                        //apply filter
                        f.filter(authHeaderFilter)
                    }.uri("lb://user-service")
            }.build()
    }
}