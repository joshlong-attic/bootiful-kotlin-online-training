
object Greetings {
	
  fun greet( name : String ) = "Hello, ${ name }!"
}

fun main(args: Array<String>) {
    println(Greetings.greet("World"))
}
