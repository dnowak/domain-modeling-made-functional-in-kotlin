package io.github.dnowak.order.taking.place.order.api

import io.github.dnowak.order.taking.place.order.OrderFormDto
import io.github.dnowak.order.taking.place.order.OrderPlacedDto
import io.github.dnowak.order.taking.place.order.PlaceOrder
import io.github.dnowak.order.taking.place.order.fromDomain
import io.github.dnowak.order.taking.place.order.toUnvalidatedOrder
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/orders")
class OrderController(private val placeOrder: PlaceOrder) {
    private val logger = KotlinLogging.logger {}

    @GetMapping
    suspend fun orders(): ResponseEntity<String> {
        logger.info("Orders")
        return ResponseEntity.ok("none")
    }

    @PostMapping
    suspend fun placeOrder(@RequestBody form: OrderFormDto): ResponseEntity<Any> =
        placeOrder(toUnvalidatedOrder(form))
            .fold(
                { error -> ResponseEntity.badRequest().body(fromDomain(error)) },
                { events -> ResponseEntity.ok().body(events.map(::fromDomain)) }
            )
}
