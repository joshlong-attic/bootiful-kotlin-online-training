package com.example.reactive

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux

@SpringBootApplication
class ReactiveApplication

fun main(args: Array<String>) {
	SpringApplicationBuilder()
			.sources(ReactiveApplication::class.java)
			.initializers(beans {
				bean {
					router {
						GET("/hi") {
							ServerResponse.ok().body(Flux.just("Hello, functional reactive"), String::class.java)
						}
					}
				}
				bean {
					ref<RouteLocatorBuilder>().routes {
						route {
							path("/guides")
							uri("http://spring.io:80/guides")
						}
					}
				}
			})
			.run(*args)
}