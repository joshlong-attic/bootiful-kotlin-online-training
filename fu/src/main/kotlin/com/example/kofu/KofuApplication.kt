package com.example.kofu

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findAll
import org.springframework.fu.kofu.application
import org.springframework.fu.kofu.mongo.mongodb
import org.springframework.fu.kofu.web.jackson
import org.springframework.fu.kofu.web.server
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Flux

val app = application {
	beans {
		bean<CustomerRepository>()
	}
	mongodb()
	server {
		codecs {
			jackson()
		}
		router {
			val repo = ref<CustomerRepository>()
			GET("/customers") { ok().body(repo.all()) }
			GET("/") { ok().syncBody("Hello world!") }
		}
	}
	listener<ApplicationReadyEvent> {

		val repo = ref<CustomerRepository>()

		Flux
				.just("A", "B", "C")
				.map { Customer(name = it) }
				.flatMap { repo.insert(it) }
				.subscribe { println("saving ${it.id}") }
	}
}

fun main(args: Array<String>) {
	app.run()
}

class CustomerRepository(private val rxTemplate: ReactiveMongoTemplate) {

	fun all(): Flux<Customer> = this.rxTemplate.findAll<Customer>()
	fun insert(c: Customer) = this.rxTemplate.save(c)
}

data class Customer(val id: String? = null, val name: String)