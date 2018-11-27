package com.example1


// Bruce Eckel has a great Kotlin book

data class Customer(val id: Int = 0, val name: String)

fun main(args: Array<String>) {

	val myFunc: (String) -> Customer = { Customer(name = it) }

	arrayOf("Josh", "Madhura", "Olga", "Zhen")
			.map(myFunc)
			.forEach { println(it) }

}
