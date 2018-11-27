
package com.sample

import org.springframework.fu.kofu.application

val app = application {
	properties<SampleProperties>("sample")
	import(dataConfig)
	import(webConfig)
}

fun main() {
	app.run()
}
