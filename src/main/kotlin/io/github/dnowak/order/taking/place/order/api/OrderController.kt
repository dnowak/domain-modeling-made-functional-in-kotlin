package io.github.dnowak.order.taking.place.order.api

import arrow.core.traverseEither
import io.github.dnowak.order.taking.common.OrderId
import io.github.dnowak.order.taking.common.ValidationError
import io.github.dnowak.order.taking.event.PublishEvent
import io.github.dnowak.order.taking.place.order.OrderFormDto
import io.github.dnowak.order.taking.place.order.PlaceOrder
import io.github.dnowak.order.taking.place.order.fromDomain
import io.github.dnowak.order.taking.place.order.store.FindOrder
import io.github.dnowak.order.taking.place.order.toUnvalidatedOrder
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/orders")
class OrderController(
    private val publishEvent: PublishEvent,
    private val placeOrder: PlaceOrder,
    private val findOrder: FindOrder,
) {
    private val logger = KotlinLogging.logger {}

    @GetMapping
    suspend fun orders(): ResponseEntity<String> {
        logger.info("Orders")
        return ResponseEntity.ok("none")
    }

    @GetMapping("/{orderId}")
    fun order(@PathVariable orderId: String): ResponseEntity<Any> =
        OrderId.validate(orderId).map(findOrder).fold(
            { errors ->
                ResponseEntity.badRequest().body(errors.map(ValidationError::message).joinToString())
            }, { maybeOrder ->
                maybeOrder.fold(
                    { ResponseEntity.notFound().build() },
                    { order -> ResponseEntity.ok(fromDomain(order)) }
                )
            }
        )

    @PostMapping
    suspend fun placeOrder(@RequestBody form: OrderFormDto): ResponseEntity<Any> =
        placeOrder(toUnvalidatedOrder(form))
            .map { events ->
                //TODO: this should be done in usecase
                events.traverseEither(publishEvent)
                Unit
            }.fold(
                { error -> ResponseEntity.badRequest().body(fromDomain(error)) },
                { ResponseEntity.created(URI("/orders/${form.orderId}")).build() }
            )
}
