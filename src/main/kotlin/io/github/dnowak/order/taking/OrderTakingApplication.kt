package io.github.dnowak.order.taking

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication(scanBasePackages = ["io.github.dnowak"])
class OrderTakingApplication

fun main(args: Array<String>) {
    SpringApplication.run(OrderTakingApplication::class.java, *args)
}
