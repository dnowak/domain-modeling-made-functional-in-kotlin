package io.github.dnowak.order.taking.place.order

import arrow.core.Either
import arrow.core.Nel
import io.github.dnowak.order.taking.common.Address
import io.github.dnowak.order.taking.common.BillingAmount
import io.github.dnowak.order.taking.common.CustomerInfo
import io.github.dnowak.order.taking.common.EmailAddress
import io.github.dnowak.order.taking.common.OrderId
import io.github.dnowak.order.taking.common.OrderLineId
import io.github.dnowak.order.taking.common.OrderQuantity
import io.github.dnowak.order.taking.common.Price
import io.github.dnowak.order.taking.common.ProductCode
import io.github.dnowak.order.taking.common.PropertyValidationError
import java.math.BigDecimal
import java.net.URI

// ==================================
// This file contains the definitions of PUBLIC types (exposed at the boundary of the bounded context)
// related to the PlaceOrder workflow
// ==================================

// ------------------------------------
// inputs to the workflow

/*
type UnvalidatedCustomerInfo = {
    FirstName : string
    LastName : string
    EmailAddress : string
}
 */

data class UnvalidatedCustomerInfo(
    val firstName: String,
    val lastName: String,
    val emailAddress: String,
)

/*
type UnvalidatedAddress = {
    AddressLine1 : string
    AddressLine2 : string
    AddressLine3 : string
    AddressLine4 : string
    City : string
    ZipCode : string
}
 */

data class UnvalidatedAddress(
    val addressLine1: String,
    val addressLine2: String?,
    val addressLine3: String?,
    val addressLine4: String?,
    val city: String,
    val zipCode: String
)

/*
type UnvalidatedOrderLine =  {
    OrderLineId : string
    ProductCode : string
    Quantity : decimal
}
*/

data class UnvalidatedOrderLine(
    val orderLineId: String,
    val productCode: String,
    val quantity: BigDecimal
)

/*
type UnvalidatedOrder = {
    OrderId : string
    CustomerInfo : UnvalidatedCustomerInfo
    ShippingAddress : UnvalidatedAddress
    BillingAddress : UnvalidatedAddress
    Lines : UnvalidatedOrderLine list
}
 */

data class UnvalidatedOrder(
    val orderId: String,
    val customerInfo: UnvalidatedCustomerInfo,
    val shippingAddress: UnvalidatedAddress,
    val billingAddress: UnvalidatedAddress,
    val lines: List<UnvalidatedOrderLine>,
)


// ------------------------------------
// outputs from the workflow (success case)

/// Event will be created if the Acknowledgment was successfully posted
/*
type OrderAcknowledgmentSent = {
    OrderId : OrderId
    EmailAddress : EmailAddress
}
 */

data class OrderAcknowledgmentSent(
    val orderId: OrderId,
    val emailAddress: EmailAddress,
)

// priced state
/*
type PricedOrderLine = {
    OrderLineId : OrderLineId
    ProductCode : ProductCode
    Quantity : OrderQuantity
    LinePrice : Price
}
 */
data class PricedOrderLine(
    val orderLineId: OrderLineId,
    val productCode: ProductCode,
    val quantity: OrderQuantity,
    val linePrice: Price,
)

/*
type PricedOrder = {
    OrderId : OrderId
    CustomerInfo : CustomerInfo
    ShippingAddress : Address
    BillingAddress : Address
    AmountToBill : BillingAmount
    Lines : PricedOrderLine list
}
 */

data class PricedOrder(
    val orderId: OrderId,
    val customerInfo: CustomerInfo,
    val shippingAddress: Address,
    val billingAddress: Address,
    val amountToBill: BillingAmount,
    val lines: List<PricedOrderLine>,
)

/// Event to send to shipping context
//type OrderPlaced = PricedOrder

typealias OrderPlaced = PricedOrder

/// Event to send to billing context
/// Will only be created if the AmountToBill is not zero
/*
type BillableOrderPlaced = {
    OrderId : OrderId
    BillingAddress: Address
    AmountToBill : BillingAmount
}
 */

data class BillableOrderPlaced(
    val orderId: OrderId,
    val billingAddress: Address,
    val amountToBill: BillingAmount,
)

/// The possible events resulting from the PlaceOrder workflow
/// Not all events will occur, depending on the logic of the workflow
/*
type PlaceOrderEvent =
| OrderPlaced of OrderPlaced
| BillableOrderPlaced of BillableOrderPlaced
| AcknowledgmentSent  of OrderAcknowledgmentSent
 */
//TODO: Fix it - is it OK?
sealed interface PlaceOrderEvent {
    data class OrderPlaced(val payload: io.github.dnowak.order.taking.place.order.OrderPlaced) : PlaceOrderEvent
    data class BillableOrderPlaced(val payload: io.github.dnowak.order.taking.place.order.BillableOrderPlaced) :
        PlaceOrderEvent

    data class AcknowledgmentSent(val payload: OrderAcknowledgmentSent) : PlaceOrderEvent
}

// ------------------------------------
// error outputs


/// All the things that can go wrong in this workflow
//type ValidationError = ValidationError of string
//TODO:  Check it - defined elsewhere

//type PricingError = PricingError of string
data class PricingError(val message: String)

/*
type ServiceInfo = {
    Name : string
    Endpoint: System.Uri
}
 */
data class ServiceInfo(
    val name: String,
    val endpoint: URI,
)

/*
type RemoteServiceError = {
    Service : ServiceInfo
    Exception : System.Exception
}
 */

data class RemoteServiceError(
    val service: ServiceInfo,
    val exception: Exception,
)

/*
type PlaceOrderError =
| Validation of ValidationError
| Pricing of PricingError
| RemoteService of RemoteServiceError
 */

sealed class PlaceOrderError {
    data class Validation(val errors: Nel<PropertyValidationError>) : PlaceOrderError()
    data class Pricing(val error: PricingError) : PlaceOrderError()
    data class RemoteService(val error: RemoteServiceError) : PlaceOrderError()
}


// ------------------------------------
// the workflow itself

/*
type PlaceOrder =
UnvalidatedOrder -> AsyncResult<PlaceOrderEvent list,PlaceOrderError>
 */

typealias PlaceOrder = suspend (UnvalidatedOrder) -> Either<PlaceOrderError, List<PlaceOrderEvent>>

