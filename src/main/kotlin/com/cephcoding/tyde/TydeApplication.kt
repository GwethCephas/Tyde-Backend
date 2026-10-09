package com.cephcoding.tyde

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class TydeApplication

fun main(args: Array<String>) {
	runApplication<TydeApplication>(*args)
}
