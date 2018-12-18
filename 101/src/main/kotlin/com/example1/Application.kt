package com.example1

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong


// Uses Kotlin 1.3.0 and
//
// <dependency>
//  <groupId>org.jetbrains.kotlinx</groupId>
//  <artifactId>kotlinx-coroutines-core</artifactId>
//  <version>1.0.1</version>
// </dependency>

val globalId = AtomicLong()

data class Customer(val id: Int = 0, val name: String)

fun main(args: Array<String>) {

	//  extension methods
	fun Customer.isValid(): Boolean = this.name.isNotEmpty()

	// overloaded methods
	val names = mutableListOf<String>()
	names += "Bob"
	println("the size of the names array is ${names.size} ")

	// functions as a first class citizen
	val myFunc: (String) -> Customer = { Customer(name = it) }

	arrayOf("Josh", "Madhura", "Olga", "Zhen", "Kimly", "Tammie")
			.map(myFunc)
			.forEach {
				println(it)
				println(it.isValid())
			}

	// coroutines
	val deferred = (1..1_000_000).map { n ->
		val di: Deferred<Int> = GlobalScope.async {
			delay(1000)
			n
		}
		di
	}

	runBlocking {
		val sum = deferred.sumBy { it.await() }
		println("the sum is $sum")
	}


}
