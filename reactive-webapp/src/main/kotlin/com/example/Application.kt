package com.example

import org.springframework.beans.factory.InitializingBean
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.dropCollection
import org.springframework.data.mongodb.core.findAll
import org.springframework.fu.application
import org.springframework.fu.module.data.mongodb.mongodb
import org.springframework.fu.module.logging.LogLevel
import org.springframework.fu.module.logging.level
import org.springframework.fu.module.logging.logback.consoleAppender
import org.springframework.fu.module.logging.logback.logback
import org.springframework.fu.module.logging.logging
import org.springframework.fu.module.webflux.jackson.jackson
import org.springframework.fu.module.webflux.netty.netty
import org.springframework.fu.module.webflux.webflux
import org.springframework.fu.ref
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Flux
import java.util.*

val app = application {


	mongodb()
	bean<CustomerRepository>()
	bean {
		InitializingBean {
			val repo = ref<CustomerRepository>()
			repo.delete()
					.thenMany(Flux.just("Dave", "Tam Mie")
							.flatMap { repo.save(Customer(id = UUID.randomUUID().toString(), name = it)) })
					.thenMany(repo.all())
					.subscribe { println("found result ${it.id} with name ${it.name}") }
		}
	}

	logging {
		level(LogLevel.INFO)
		logback {
			consoleAppender()
		}
	}
	webflux {
		val port = if (profiles.contains("test")) 8181 else 8080
		server(netty(port)) {
			codecs { jackson() }
			router {
				val customerRepo = ref<CustomerRepository>()
				GET("/customers") {
					ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(customerRepo.all())
				}
				GET("/") { ok().syncBody("Hello world!") }
			}
		}
	}
}

class Customer(val id: String? = null, val name: String? = null)

class CustomerRepository(private val reactiveMongoTemplate: ReactiveMongoTemplate) {

	fun delete() = this.reactiveMongoTemplate.dropCollection<Customer>()

	fun save(r: Customer) = this.reactiveMongoTemplate.save(r)

	fun all() = this.reactiveMongoTemplate.findAll<Customer>()
}


fun main(args: Array<String>) = app.run(await = true)