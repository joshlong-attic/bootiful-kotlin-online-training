package com.example.basics

import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.*
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.sql.DataSource

@SpringBootApplication
class BasicsApplication {

	@Bean
	fun exposedTransactionManager(ds: DataSource) = SpringTransactionManager(ds)

	@Bean
	fun transactionTemplate(txm: PlatformTransactionManager) = TransactionTemplate(txm)
}

fun main(args: Array<String>) {
	runApplication<BasicsApplication>(*args)
}


object Customers : Table() {
	val id = long("id").autoIncrement().primaryKey()
	val name = varchar("name", 255)
}

@Component
class SampleDataInitializer(private val customerService: CustomerService) {

	@EventListener(ApplicationReadyEvent::class)
	fun initialize() {

		val names = arrayOf("Tammie", "Kimly", "Madhura", "Olga", "Cornelia", "Jennifer", "Michelle", "Eva")
		names.map { Customer(name = it) }
				.forEach { customerService.insert(it) }

		customerService.all()
				.forEach {
					val id: Long = it.id!!
					val customer = customerService.byId(id)
					println(customer)
				}
	}
}

@Service
@Transactional
class ExposedCustomerService(private val transactionTemplate: TransactionTemplate) : CustomerService, InitializingBean {

	override fun afterPropertiesSet() {
		this.transactionTemplate.execute {
			SchemaUtils.create(Customers)
		}
	}

	override fun byId(id: Long): Customer? =
			Customers
					.select { Customers.id.eq(id) }
					.map { Customer(id = it[Customers.id], name = it[Customers.name]) }
					.singleOrNull()

	override fun insert(customer: Customer) {
		Customers.insert { it[Customers.name] = customer.name }
	}

	override fun all(): Collection<Customer> =
			Customers
					.selectAll()
					.map { Customer(id = it[Customers.id], name = it[Customers.name]) }
}


@RestController
class CustomerRestController(private val customerService: CustomerService) {

	@GetMapping("/customers")
	fun get() = this.customerService.all()

}

/*
@Service
class JdbcCustomerService(private val jdbcTemplate: JdbcTemplate) : CustomerService {

	private fun rsToCustomer(rs: ResultSet) = Customer(id = rs.getLong("ID"), name = rs.getString("NAME"))

	override fun all(): Collection<Customer> =
			this.jdbcTemplate.query("select * from CUSTOMERS") { rs, i ->
				rsToCustomer(rs)
			}

	override fun byId(id: Long): Customer? =
			this.jdbcTemplate.queryForObject("select * from CUSTOMERS where ID =?", id) { rs, i ->
				rsToCustomer(rs)
			}

	override fun insert(customer: Customer) {
		this.jdbcTemplate.execute("insert into CUSTOMERS(NAME) values(?)") {
			it.setString(1, customer.name)
			it.execute()
		}
	}
}
*/

interface CustomerService {
	fun all(): Collection<Customer>
	fun byId(id: Long): Customer?
	fun insert(customer: Customer)
}

data class Customer(val id: Long? = null, val name: String)