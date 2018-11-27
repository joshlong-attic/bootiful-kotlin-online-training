package com.sample

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.fu.kofu.application
import org.springframework.fu.kofu.configuration
import org.springframework.fu.kofu.mongo.embedded
import org.springframework.fu.kofu.mongo.mongodb
import org.springframework.fu.kofu.web.jackson
import org.springframework.fu.kofu.web.mustache
import org.springframework.fu.kofu.web.server
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Flux

class UserRepository(
		private val mongo: ReactiveMongoOperations,
		private val objectMapper: ObjectMapper) {

	fun findAll() = mongo.findAll<User>()

	fun save(user: User) = mongo.save(user)

	fun initSampleData() {

		val names = Flux.just("Olga", "Dr. Subramaniam", "Josh", "Sebastien", "Madhura", "Cornelia")
				.map { User(name = it) }
				.flatMap { this.mongo.save(it) }
		this.mongo
				.dropCollection(User::class.java)
				.thenMany(names)
				.thenMany(findAll())
				.subscribe { println("saved ${it.name}") }
	}
}

// Switch to data classes when https://github.com/spring-projects/spring-boot/issues/8762 is fixed
class SampleProperties {
	lateinit var message: String
}

@Document
data class User(@Id val id: String? = null, val name: String)

val dataConfig = configuration {
	beans {
		bean<UserRepository>()
	}
	listener<ApplicationReadyEvent> {
		ref<UserRepository>().initSampleData()
	}
	mongodb {
		embedded()
	}
}

class UserHandler(
		private val repository: UserRepository, private val configuration: SampleProperties) {

	fun listApi(request: ServerRequest) =
			ServerResponse
					.ok()
					.contentType(MediaType.APPLICATION_JSON_UTF8)
					.body(repository.findAll())

	fun conf(request: ServerRequest) = ServerResponse
			.ok()
			.syncBody(configuration.message)

}

val webConfig = configuration {
	beans {
		bean<UserHandler>()
	}
	server {
		port = 8080
		mustache()
		codecs {
			string()
			jackson()
		}
		router {
			val userHandler = ref<UserHandler>()
			GET("/api/user", userHandler::listApi)
			GET("/conf", userHandler::conf)
		}
	}
}

val app = application {
	properties<SampleProperties>("sample")
	import(dataConfig)
	import(webConfig)
}

fun main() {
	app.run()
}
