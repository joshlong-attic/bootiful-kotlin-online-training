package com.example.basics

import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.*
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.support.beans
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class BasicsApplication/* {

	@Bean
	fun exposedSpringTransactionManager(ds: DataSource) = SpringTransactionManager(ds)

	@Bean
	fun transactionTemplate(txManager: PlatformTransactionManager) = TransactionTemplate(txManager)
}*/

@Service
@Transactional
class ExposedCustomerService(private val transactionTemplate: TransactionTemplate) : CustomerService, InitializingBean {

	override fun afterPropertiesSet() {
		this.transactionTemplate.execute {
			SchemaUtils.create(Customers)
		}
	}

	override fun all(): Collection<Customer> = Customers
			.selectAll()
			.map { Customer(id = it[Customers.id], name = it[Customers.name]) }

	override fun byId(id: Long): Customer? =
			Customers
					.select {
						Customers.id.eq(id)
					}
					.map { Customer(id = it[Customers.id], name = it[Customers.name]) }
					.singleOrNull()

	override fun insert(customer: Customer) {
		Customers.insert {
			it[Customers.name] = customer.name
		}
	}
}


@RestController
class CustomerRestController(private val customerService: CustomerService) {

	@GetMapping("/customers")
	fun all() = this.customerService.all()
}

object Customers : Table() {
	val id = long("id").autoIncrement().primaryKey()
	val name = varchar("name", 255)
}

//@Service
class JdbcCustomerService(private val jdbcTemplate: JdbcTemplate) : CustomerService {

	override fun all(): Collection<Customer> =
			this.jdbcTemplate.query("select * from CUSTOMERS") { rs, _ -> Customer(rs.getLong("ID"), rs.getString("NAME")) }

	override fun byId(id: Long): Customer? =
			this.jdbcTemplate.queryForObject("select * from CUSTOMERS where ID = ?", id) { rs, _ -> Customer(rs.getLong("ID"), rs.getString("NAME")) }

	override fun insert(customer: Customer) {
		this.jdbcTemplate.execute("insert into CUSTOMERS(NAME) values(?)") {
			it.setString(1, customer.name)
			it.execute()
		}
	}

}

@Component
class DataRunner(private val customerService: CustomerService) : ApplicationRunner {

	override fun run(args: ApplicationArguments?) {
		arrayOf("Josh", "Madhura", "Jennifer", "Cornelia", "Mark", "Dave", "Stephane", "Brian")
				.map { Customer(name = it) }
				.forEach { customerService.insert(it) }

		customerService.all().forEach {
			val id: Long = it.id!!
			println(customerService.byId(id))
		}
	}
}

interface CustomerService {

	fun all(): Collection<Customer>
	fun byId(id: Long): Customer?
	fun insert(customer: Customer)
}

data class Customer(val id: Long? = null, val name: String)

val beans = beans {

	bean {
		SpringTransactionManager(ref())
	}

	bean {
		TransactionTemplate(ref())
	}

}

fun main(args: Array<String>) {


	SpringApplicationBuilder()
			.initializers(beans)
			.sources(BasicsApplication::class.java)
			.run(*args)

//	SpringApplication.run(BasicsApplication::class.java, *args)
}

