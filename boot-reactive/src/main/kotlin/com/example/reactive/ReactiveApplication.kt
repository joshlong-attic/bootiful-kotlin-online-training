package com.example.reactive

import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RouterFunctions.route
import reactor.core.publisher.Flux
import java.util.*

@SpringBootApplication
class ReactiveApplication {

	@Bean
	fun routes(cr: CustomerRepository) = router {
		GET("/customers") {
			ServerResponse.ok().body(cr.findAll())
		}
	}

	@Bean
	fun gateway(rlb: RouteLocatorBuilder) = rlb.routes {
		route {
			path("/proxy")
			uri("http://spring.io:80/guides")
		}
	}


	/* rlb
	.routes()
	.route { rSpec ->
		rSpec
				.path("/proxy")
				.uri("http://spring.io:80/guides")
	}
	.build()*/

	@Bean
	fun runner(cr: CustomerRepository) = ApplicationRunner {
		cr
				.deleteAll()
				.thenMany(Flux
						.just("Mary", "Peter", "Jonah", "Gwen")
						.map { Customer(id = UUID.randomUUID().toString(), name = it) }
						.flatMap { cr.save(it) }
				)
				.thenMany(cr.findAll())
				.subscribe { println(it) }
	}
}

/*
@RestController
class CustomerRestController(private val customerRepository: CustomerRepository) {

	@GetMapping("/customer")
	fun customers() = this.customerRepository.findAll()
}*/

interface CustomerRepository : ReactiveMongoRepository<Customer, String>

@Document
data class Customer(val id: String? = null, val name: String? = null)

fun main(args: Array<String>) {
	runApplication<ReactiveApplication>(*args)
}

