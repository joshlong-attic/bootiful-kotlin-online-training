package com.example.augmented

import org.reactivestreams.Publisher
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.cloud.gateway.route.builder.filters
import org.springframework.cloud.gateway.route.builder.routes
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux

@SpringBootApplication
class AugmentedApplication


class CustomerRepository {
	fun all() = Flux.just(
			Customer(1L, "Josh"),
			Customer(2L, "Jane"))
}

class CustomerService(val cr: CustomerRepository) {

	fun customers(): Publisher<Customer> = cr.all()
}

data class Customer(val id: Long, val name: String)

val beans = beans {

	bean {
		val rlb = ref<RouteLocatorBuilder>()
		rlb.routes {

			route {
				path("/proxy")
				filters {
					setPath("/guides")
				}
				uri("http://spring.io")
			}
		}
	}

	bean {
		val cs = ref<CustomerService>()
		router {
			GET("/customers") {
				ServerResponse.ok().body(cs.customers())
			}
		}
	}

	bean<CustomerRepository>()

	bean {
		val cr = ref<CustomerRepository>()
		CustomerService(cr)
	}
}

fun main(args: Array<String>) {
	runApplication<AugmentedApplication>(*args) {
		addInitializers(beans)
	}
}
