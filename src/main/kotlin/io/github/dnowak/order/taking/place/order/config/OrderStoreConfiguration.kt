package io.github.dnowak.order.taking.place.order.config

import io.github.dnowak.order.taking.place.order.PlaceOrderEvent
import io.github.dnowak.order.taking.place.order.store.FindOrder
import io.github.dnowak.order.taking.place.order.store.InMemoryOrderStore
import io.github.dnowak.order.taking.place.order.store.StoreOrder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

@Configuration
class OrderStoreConfiguration {
    @Bean
    fun orderStore() = InMemoryOrderStore()

    @Bean
    fun findOderFun(store: InMemoryOrderStore): FindOrder = store::findOrder

    @Bean
    fun storeOrder(store: InMemoryOrderStore): StoreOrder = store::storeOrder

    @Bean
    fun storeOrderListener(storeOrder: StoreOrder) = StoreOrderListener(storeOrder)

}

class StoreOrderListener(private val storeOrder: StoreOrder) {

    @EventListener
    fun onEvent(event: PlaceOrderEvent.OrderPlaced) {
        storeOrder(event.payload)
    }
}
