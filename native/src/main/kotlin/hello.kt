fun main(args: Array<String>) {

    data class Customer(val id: Int = 0, val name: String)

    val myFunc: (String) -> Customer = { Customer(name = it) }

    arrayOf("Josh", "Madhura", "Olga", "Zhen")
            .map(myFunc)
            .forEach { println(it) }


}
