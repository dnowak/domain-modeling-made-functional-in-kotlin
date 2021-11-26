package io.github.dnowak.order.taking.place.order.store

import arrow.core.Option
import arrow.core.getOrNone
import io.github.dnowak.order.taking.common.OrderId
import io.github.dnowak.order.taking.place.order.PricedOrder

typealias FindOrder = (OrderId) -> Option<PricedOrder>

typealias StoreOrder = (PricedOrder) -> Unit

class InMemoryOrderStore() {
    private val orders: MutableMap<OrderId, PricedOrder> = mutableMapOf()

    fun findOrder(id: OrderId): Option<PricedOrder> = orders.getOrNone(id)

    fun storeOrder(order: PricedOrder) {
        orders[order.orderId] = order
    }
}
