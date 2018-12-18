package com.example1

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking


// Uses Kotlin 1.3.0 and
//
// <dependency>
//  <groupId>org.jetbrains.kotlinx</groupId>
//  <artifactId>kotlinx-coroutines-core</artifactId>
//  <version>1.0.1</version>
// </dependency>

data class Customer(val id: Int = 0, val name: String)

fun main(args: Array<String>) {

	val deferred = (1..1_000_000)
			.map { n ->
				GlobalScope.async {
					delay(1000)
					n
				}
			}

	runBlocking {
		val sum = deferred.sumBy { it.await() }
		println("the sum is $sum")
	}

	val myFunc: (String) -> Customer = { Customer(name = it) }

	arrayOf("Josh", "Madhura", "Olga", "Zhen")
			.map(myFunc)
			.forEach { println(it) }

}
