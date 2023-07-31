package io.github.dnowak.order.taking.place.order.implementation

import arrow.core.Either
import arrow.core.Either.Companion.zipOrAccumulate
import arrow.core.EitherNel
import arrow.core.None
import arrow.core.Option
import arrow.core.flatMap
import arrow.core.flatten
import arrow.core.flattenOrAccumulate
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import arrow.core.some
import arrow.core.toEitherNel
import arrow.core.traverseEither
import io.github.dnowak.order.taking.common.Address
import io.github.dnowak.order.taking.common.BillingAmount
import io.github.dnowak.order.taking.common.City
import io.github.dnowak.order.taking.common.CustomerInfo
import io.github.dnowak.order.taking.common.EmailAddress
import io.github.dnowak.order.taking.common.OrderId
import io.github.dnowak.order.taking.common.OrderLineId
import io.github.dnowak.order.taking.common.OrderQuantity
import io.github.dnowak.order.taking.common.PersonalName
import io.github.dnowak.order.taking.common.Price
import io.github.dnowak.order.taking.common.ProductCode
import io.github.dnowak.order.taking.common.Property
import io.github.dnowak.order.taking.common.PropertyValidationError
import io.github.dnowak.order.taking.common.ValidationError
import io.github.dnowak.order.taking.common.ZipCode
import io.github.dnowak.order.taking.common.assign
import io.github.dnowak.order.taking.common.prepend
import io.github.dnowak.order.taking.common.validateNullableString50
import io.github.dnowak.order.taking.common.validateString50
import io.github.dnowak.order.taking.place.order.BillableOrderPlaced
import io.github.dnowak.order.taking.place.order.OrderAcknowledgmentSent
import io.github.dnowak.order.taking.place.order.OrderPlaced
import io.github.dnowak.order.taking.place.order.PlaceOrderError
import io.github.dnowak.order.taking.place.order.PlaceOrderEvent
import io.github.dnowak.order.taking.place.order.PricedOrder
import io.github.dnowak.order.taking.place.order.PricedOrderLine
import io.github.dnowak.order.taking.place.order.PricingError
import io.github.dnowak.order.taking.place.order.UnvalidatedAddress
import io.github.dnowak.order.taking.place.order.UnvalidatedCustomerInfo
import io.github.dnowak.order.taking.place.order.UnvalidatedOrder
import io.github.dnowak.order.taking.place.order.UnvalidatedOrderLine
import io.github.dnowak.util.arrow.distinct
import mu.KotlinLogging
import java.math.BigDecimal
import java.math.RoundingMode

private val logger = KotlinLogging.logger {}

// ======================================================
// This file contains the final implementation for the PlaceOrder workflow
//
// This represents the code in chapter 10, "Working with Errors"
//
// There are two parts:
// * the first section contains the (type-only) definitions for each step
// * the second section contains the implementations for each step
//   and the implementation of the overall workflow
// ======================================================


// ======================================================
// Section 1 : Define each step in the workflow using types
// ======================================================

// ---------------------------
// Validation step
// ---------------------------

// Product validation

//type CheckProductCodeExists = ProductCode -> bool

typealias CheckProductCodeExists = (ProductCode) -> Boolean

// Address validation
/*
type AddressValidationError =
| InvalidFormat
| AddressNotFound
 */

sealed interface AddressValidationError {
    object InvalidFormat : AddressValidationError
    object AddressNotFound : AddressValidationError
}

//type CheckedAddress = CheckedAddress of UnvalidatedAddress

typealias CheckedAddress = UnvalidatedAddress

//type CheckAddressExists = UnvalidatedAddress -> AsyncResult<CheckedAddress,AddressValidationError>

typealias CheckAddressExists = suspend (UnvalidatedAddress) -> Either<AddressValidationError, CheckedAddress>

// ---------------------------
// Either Order
// ---------------------------

/*
type ValidatedOrderLine =  {
    OrderLineId : OrderLineId
    ProductCode : ProductCode
    Quantity : OrderQuantity
}
*/

data class ValidatedOrderLine(
    val orderLineId: OrderLineId,
    val productCode: ProductCode,
    val quantity: OrderQuantity,
)

/*
type ValidatedOrder = {
    OrderId : OrderId
    CustomerInfo : CustomerInfo
    ShippingAddress : Address
    BillingAddress : Address
    Lines : ValidatedOrderLine list
}
*/

data class ValidatedOrder(
    val orderId: OrderId,
    val customerInfo: CustomerInfo,
    val shippingAddress: Address,
    val billingAddress: Address,
    val lines: List<ValidatedOrderLine>,
)

/*
type ValidateOrder =
CheckProductCodeExists  // dependency
-> CheckAddressExists  // dependency
-> UnvalidatedOrder    // input
-> AsyncResult<ValidatedOrder, ValidationError> // output
*/

//CHANGE: dropped dependencies from type definition, validation result
typealias ValidateOrder = (UnvalidatedOrder) -> EitherNel<PropertyValidationError, ValidatedOrder>

// ---------------------------
// Pricing step
// ---------------------------

//type GetProductPrice = ProductCode -> Price

typealias GetProductPrice = (ProductCode) -> Price

// priced state is defined Domain.WorkflowTypes
/*
type PriceOrder =
GetProductPrice     // dependency
-> ValidatedOrder  // input
-> Result<PricedOrder, PricingError>  // output
*/

typealias PriceOrder = (ValidatedOrder) -> Either<PricingError, PricedOrder>

// ---------------------------
// Send OrderAcknowledgment
// ---------------------------

//type HtmlString = HtmlString of string

data class HtmlString(val value: String)

/*
type OrderAcknowledgment = {
    EmailAddress : EmailAddress
    Letter : HtmlString
}
*/

data class OrderAcknowledgement(
    val emailAddress: EmailAddress,
    val letter: HtmlString,
)

//type CreateOrderAcknowledgmentLetter = PricedOrder -> HtmlString

typealias CreateOrderAcknowledgementLetter = (PricedOrder) -> HtmlString

/// Send the order acknowledgement to the customer
/// Note that this does NOT generate an Result-type error (at least not in this workflow)
/// because on failure we will continue anyway.
/// On success, we will generate a OrderAcknowledgmentSent event,
/// but on failure we won't.

//type SendResult = Sent | NotSent

sealed interface SendResult {
    object Sent : SendResult
    object NotSent : SendResult
}

//type SendOrderAcknowledgment = OrderAcknowledgment -> SendResult

typealias SendOrderAcknowledgment = (OrderAcknowledgement) -> SendResult

/*
type AcknowledgeOrder =
CreateOrderAcknowledgmentLetter  // dependency
-> SendOrderAcknowledgment      // dependency
-> PricedOrder                  // input
-> OrderAcknowledgmentSent option // output
 */

typealias AcknowledgeOrder = (PricedOrder) -> OrderAcknowledgmentSent?

// ---------------------------
// Create events
// ---------------------------

/*
type CreateEvents =
PricedOrder                           // input
-> OrderAcknowledgmentSent option    // input (event from previous step)
-> PlaceOrderEvent list              // output
 */

typealias CreateEvents = (PricedOrder, OrderAcknowledgmentSent?) -> List<PlaceOrderEvent>

// ======================================================
// Section 2 : Implementation
// ======================================================

// ---------------------------
// ValidateOrder step
// ---------------------------
/*
let toCustomerInfo (unvalidatedCustomerInfo: UnvalidatedCustomerInfo) =
result {
    let! firstName =
    unvalidatedCustomerInfo.FirstName
    |> String50.create "FirstName"
    |> Result.mapError ValidationError // convert creation error into ValidationError
    let! lastName =
    unvalidatedCustomerInfo.LastName
    |> String50.create "LastName"
    |> Result.mapError ValidationError // convert creation error into ValidationError
    let! emailAddress =
    unvalidatedCustomerInfo.EmailAddress
    |> EmailAddress.create "EmailAddress"
    |> Result.mapError ValidationError // convert creation error into ValidationError
    let customerInfo = {
        Name = {FirstName=firstName; LastName=lastName}
        EmailAddress = emailAddress
    }
    return customerInfo
}
 */

typealias ValidateCustomerInfo = (UnvalidatedCustomerInfo) -> EitherNel<PropertyValidationError, CustomerInfo>

fun validateCustomerInfo(info: UnvalidatedCustomerInfo): EitherNel<PropertyValidationError, CustomerInfo> {
    val validatedFirstName = validateString50(info.firstName)
        .assign(Property("firstName"))
    val validatedLastName = validateString50(info.lastName)
        .assign(Property("lastName"))
    val validatedEmailAddress = EmailAddress.validate(info.emailAddress)
        .assign(Property("emailAddress"))
    return zipOrAccumulate(
        validatedFirstName,
        validatedLastName,
        validatedEmailAddress
    ) { firstName, lastName, emailAddress ->
        CustomerInfo(PersonalName(firstName, lastName), emailAddress)
    }
}

/*
let toAddress (CheckedAddress unvalidatedAddress) =
result {
    let! addressLine1 =
    unvalidatedAddress.AddressLine1
    |> String50.create "AddressLine1"
    |> Result.mapError ValidationError // convert creation error into ValidationError
    let! addressLine2 =
    unvalidatedAddress.AddressLine2
    |> String50.createOption "AddressLine2"
    |> Result.mapError ValidationError // convert creation error into ValidationError
    let! addressLine3 =
    unvalidatedAddress.AddressLine3
    |> String50.createOption "AddressLine3"
    |> Result.mapError ValidationError // convert creation error into ValidationError
    let! addressLine4 =
    unvalidatedAddress.AddressLine4
    |> String50.createOption "AddressLine4"
    |> Result.mapError ValidationError // convert creation error into ValidationError
    let! city =
    unvalidatedAddress.City
    |> String50.create "City"
    |> Result.mapError ValidationError // convert creation error into ValidationError
    let! zipCode =
    unvalidatedAddress.ZipCode
    |> ZipCode.create "ZipCode"
    |> Result.mapError ValidationError // convert creation error into ValidationError
    let address : Address = {
        AddressLine1 = addressLine1
        AddressLine2 = addressLine2
        AddressLine3 = addressLine3
        AddressLine4 = addressLine4
        City = city
        ZipCode = zipCode
    }
    return address
}
*/

typealias ValidateAddress = (UnvalidatedAddress) -> EitherNel<PropertyValidationError, Address>

fun validateAddress(address: CheckedAddress): EitherNel<PropertyValidationError, Address> {
    val validatedAddresLine1 = validateString50(address.addressLine1)
        .assign(Property("addressLine1"))
    val validatedAddressLine2 = validateNullableString50(address.addressLine2)
        .assign(Property("addressLine2"))
    val validatedAddressLine3 = validateNullableString50(address.addressLine3)
        .assign(Property("addressLine3"))
    val validatedAddressLine4 = validateNullableString50(address.addressLine4)
        .assign(Property("addressLine4"))
    val validatedCity = validateString50(address.city).map(::City)
        .assign(Property("city"))
    val validatedZipCode = ZipCode.validate(address.zipCode)
        .assign(Property("zipCode"))

    return zipOrAccumulate(
        validatedAddresLine1,
        validatedAddressLine2,
        validatedAddressLine3,
        validatedAddressLine4,
        validatedCity,
        validatedZipCode
    ) { addressLine1, addressLine2, addressLine3, addressLine4, city, zipCode ->
        Address(addressLine1, addressLine2, addressLine3, addressLine4, city, zipCode)
    }
}

/*
/// Call the checkAddressExists and convert the error to a ValidationError
let toCheckedAddress (checkAddress:CheckAddressExists) address =
address
|> checkAddress
|> AsyncResult.mapError (fun addrError ->
match addrError with
| AddressNotFound -> ValidationError "Address not found"
| InvalidFormat -> ValidationError "Address has bad format"
)
*/

suspend fun toCheckedAddress(
    checkAddressExists: CheckAddressExists,
    address: UnvalidatedAddress,
): Either<ValidationError, CheckedAddress> =
    checkAddressExists(address)
        .mapLeft { error ->
            when (error) {
                is AddressValidationError.AddressNotFound -> ValidationError("Address not found")
                is AddressValidationError.InvalidFormat -> ValidationError("Address has bad format")
            }
        }

/*
let toOrderId orderId =
orderId
|> OrderId.create "OrderId"
|> Result.mapError ValidationError // convert creation error into ValidationError

// Already defined as OrderId.validate

/// Helper function for validateOrder
let toOrderLineId orderId =
orderId
|> OrderLineId.create "OrderLineId"
|> Result.mapError ValidationError // convert creation error into ValidationError

// Already defined as OrderId.validate

/// Helper function for validateOrder
let toProductCode (checkProductCodeExists:CheckProductCodeExists) productCode =

    // create a ProductCode -> Result<ProductCode,...> function
    // suitable for using in a pipeline
    let checkProduct productCode  =
        if checkProductCodeExists productCode then
            Ok productCode
        else
            let msg = sprintf "Left: %A" productCode
            Error (ValidationError msg)

    // assemble the pipeline
    productCode
    |> ProductCode.create "ProductCode"
    |> Result.mapError ValidationError // convert creation error into ValidationError
    |> Result.bind checkProduct
*/

//TODO: switch to validate & check
/* split into validation and checking
fun toProductCode(
    checkProductCodeExists: CheckProductCodeExists,
    productCode: String,
): ValidatedNel<ValidationError, ProductCode> {
    fun checkProduct(productCode: ProductCode): ValidatedNel<ValidationError, ProductCode> =
        if (checkProductCodeExists(productCode)) {
            productCode.right()
        } else {
            ValidationError("Left product code: <$productCode>").left().toEitherNel()
        }
    return .toEither().flatMap { code -> checkProduct(code).toEither() }.toValidated()
}
*/

fun checkProductCode(
    checkProductCodeExists: CheckProductCodeExists,
    productCode: ProductCode,
): EitherNel<ValidationError, ProductCode> =
    if (checkProductCodeExists(productCode)) {
        productCode.right()
    } else {
        ValidationError("Left product code: <$productCode>").left().toEitherNel()
    }

/*
/// Helper function for validateOrder
let toOrderQuantity productCode quantity =
OrderQuantity.create "OrderQuantity" productCode quantity
|> Result.mapError ValidationError // convert creation error into ValidationError

/// Helper function for validateOrder
let toValidatedOrderLine checkProductExists (unvalidatedOrderLine:UnvalidatedOrderLine) =
result {
    let!orderLineId =
    unvalidatedOrderLine.OrderLineId
    |> toOrderLineId
    let!productCode =
    unvalidatedOrderLine.ProductCode
    |> toProductCode checkProductExists
    let!quantity =
    unvalidatedOrderLine.Quantity
    |> toOrderQuantity productCode
    let validatedOrderLine = {
        OrderLineId = orderLineId
        ProductCode = productCode
        Quantity = quantity
    }
    return validatedOrderLine
}
*/

typealias ValidateProductCode = (String) -> EitherNel<ValidationError, ProductCode>

typealias ValidateOrderLine = (UnvalidatedOrderLine) -> EitherNel<PropertyValidationError, ValidatedOrderLine>

fun validateOrderLine(
    validateOrderLineId: (String) -> EitherNel<ValidationError, OrderLineId>,
    validateProductCode: ValidateProductCode,
    validateOrderQuantity: (ProductCode, BigDecimal) -> EitherNel<ValidationError, OrderQuantity>,
    line: UnvalidatedOrderLine,
): EitherNel<PropertyValidationError, ValidatedOrderLine> {
    val validatedOrderLineId = validateOrderLineId(line.orderLineId)
        .assign(Property("orderLineId"))
    val validatedProductCode = validateProductCode(line.productCode)
        .assign(Property("productCode"))
    val validatedQuantity = validatedProductCode.flatMap { code ->
        validateOrderQuantity(code, line.quantity).assign(Property("quantity"))
    }

    return either {
        ValidatedOrderLine(validatedOrderLineId.bind(), validatedProductCode.bind(), validatedQuantity.bind())
    }.mapLeft(::distinct)
}

/*
let validateOrder : ValidateOrder =
fun checkProductCodeExists checkAddressExists unvalidatedOrder ->
asyncResult {
    let!orderId =
    unvalidatedOrder.OrderId
    |> toOrderId
    |> AsyncResult.ofResult
    let!customerInfo =
    unvalidatedOrder.CustomerInfo
    |> toCustomerInfo
    |> AsyncResult.ofResult
    let!checkedShippingAddress =
    unvalidatedOrder.ShippingAddress
    |> toCheckedAddress checkAddressExists
    let!shippingAddress =
    checkedShippingAddress
    |> toAddress
    |> AsyncResult.ofResult
    let!checkedBillingAddress =
    unvalidatedOrder.BillingAddress
    |> toCheckedAddress checkAddressExists
    let!billingAddress =
    checkedBillingAddress
    |> toAddress
    |> AsyncResult.ofResult
    let!lines =
    unvalidatedOrder.Lines
    |> List.map (toValidatedOrderLine checkProductCodeExists)
    |> Result.sequence // convert list of Results to a single Result
    |> AsyncResult.ofResult
    let validatedOrder : ValidatedOrder = {
        OrderId = orderId
        CustomerInfo = customerInfo
        ShippingAddress = shippingAddress
        BillingAddress = billingAddress
        Lines = lines
    }
    return validatedOrder
}
*/

fun validateOrder(
    validateCustomerInfo: ValidateCustomerInfo,
    validateAddress: ValidateAddress,
    validateOrderLine: ValidateOrderLine,
    order: UnvalidatedOrder,
): EitherNel<PropertyValidationError, ValidatedOrder> {
    val validatedOrderId = OrderId.validate(order.orderId)
        .assign(Property("orderId"))
    val validatedCustomerInfo = validateCustomerInfo(order.customerInfo)
        .prepend(Property("customerInfo"))
    val validatedShippingAddress = validateAddress(order.shippingAddress)
        .prepend(Property("shippingAddress"))
    val validatedBillingAddress = validateAddress(order.billingAddress)
        .prepend(Property("billingAddress"))
    val validatedLines = order.lines.mapIndexed { index, line ->
        validateOrderLine(line)
            .prepend(Property("lines[$index]"))
    }.flattenOrAccumulate()

    return zipOrAccumulate(
        validatedOrderId,
        validatedCustomerInfo,
        validatedShippingAddress,
        validatedBillingAddress,
        validatedLines
    ) { orderId, customerInfo, shippingAddress, billingAddress, lines ->
        ValidatedOrder(orderId, customerInfo, shippingAddress, billingAddress, lines)
    }
}

/*
// ---------------------------
// PriceOrder step
// ---------------------------

let toPricedOrderLine (getProductPrice:GetProductPrice) (validatedOrderLine:ValidatedOrderLine) =
result {
    let qty = validatedOrderLine . Quantity | > OrderQuantity.value
    let price = validatedOrderLine . ProductCode | > getProductPrice
    let!linePrice =
    Price.multiply qty price
    |> Result.mapError PricingError // convert to PlaceOrderError
    let pricedLine : PricedOrderLine = {
        OrderLineId = validatedOrderLine.OrderLineId
        ProductCode = validatedOrderLine.ProductCode
        Quantity = validatedOrderLine.Quantity
        LinePrice = linePrice
    }
    return pricedLine
}
*/

typealias PriceOrderLine = (ValidatedOrderLine) -> Either<PricingError, PricedOrderLine>

fun priceOrderLine(getProductPrice: GetProductPrice, line: ValidatedOrderLine): Either<PricingError, PricedOrderLine> =
    either {
        val price = getProductPrice(line.productCode)
        val linePrice = (price * line.quantity).bind()
        line.run {
            PricedOrderLine(orderLineId, productCode, line.quantity, linePrice)
        }
    }

private operator fun Price.times(quantity: OrderQuantity): Either<PricingError, Price> =
    Price.validate(value.multiply(quantity.quantity).setScale(2, RoundingMode.HALF_UP))
        .mapLeft { errors -> PricingError(errors.map { error -> error.message }.joinToString()) }

/*
let priceOrder : PriceOrder =
fun getProductPrice validatedOrder ->
result {
    let!lines =
    validatedOrder.Lines
    |> List.map (toPricedOrderLine getProductPrice)
    |> Result.sequence // convert list of Results to a single Result
    let!amountToBill =
    lines
    |> List.map (fun line -> line.LinePrice)  // get each line price
    |> BillingAmount.sumPrices                // add them together as a BillingAmount
    |> Result.mapError PricingError           // convert to PlaceOrderError
    let pricedOrder : PricedOrder = {
        OrderId = validatedOrder.OrderId
        CustomerInfo = validatedOrder.CustomerInfo
        ShippingAddress = validatedOrder.ShippingAddress
        BillingAddress = validatedOrder.BillingAddress
        Lines = lines
        AmountToBill = amountToBill
    }
    return pricedOrder
}
*/

//TODO: Consider gathering pricing error from all lines -> would require switch to Either
fun priceOrder(priceOrderLine: PriceOrderLine, order: ValidatedOrder): Either<PricingError, PricedOrder> =
    either {
        val lines = order.lines.traverseEither(priceOrderLine).bind()
        val billingAmount = BillingAmount.validate(lines.sumOf { it.linePrice.value }.setScale(2, RoundingMode.HALF_UP))
            .mapLeft { errors -> PricingError("amountToBill: " + errors.map { it.message }.joinToString()) }
            .bind()
        order.run {
            PricedOrder(orderId, customerInfo, shippingAddress, billingAddress, billingAmount, lines)
        }
    }

/*
// ---------------------------
// AcknowledgeOrder step
// ---------------------------

let acknowledgeOrder : AcknowledgeOrder =
    fun createAcknowledgmentLetter sendAcknowledgment pricedOrder ->
        let letter = createAcknowledgmentLetter pricedOrder
        let acknowledgment = {
            EmailAddress = pricedOrder.CustomerInfo.EmailAddress
            Letter = letter
            }

        // if the acknowledgement was successfully sent,
        // return the corresponding event, else return None
        match sendAcknowledgment acknowledgment with
        | Sent ->
            let event = {
                OrderId = pricedOrder.OrderId
                EmailAddress = pricedOrder.CustomerInfo.EmailAddress
                }
            Some event
        | NotSent ->
            None
*/

fun acknowledgeOrder(
    createOrderAcknowledgementLetter: CreateOrderAcknowledgementLetter,
    sendAcknowledgement: SendOrderAcknowledgment,
    order: PricedOrder,
): OrderAcknowledgmentSent? {
    val letter = createOrderAcknowledgementLetter(order)
    val orderAcknowledgement = OrderAcknowledgement(order.customerInfo.emailAddress, letter)
    return when (sendAcknowledgement(orderAcknowledgement)) {
        SendResult.Sent -> OrderAcknowledgmentSent(order.orderId, order.customerInfo.emailAddress)
        SendResult.NotSent -> null
    }
}

/*
// ---------------------------
// Create events
// ---------------------------
let createOrderPlacedEvent (placedOrder:PricedOrder) : OrderPlaced =
    placedOrder
*/

fun createOrderPlacedEvent(placedOrder: PricedOrder): OrderPlaced = placedOrder

/*
let createBillingEvent (placedOrder:PricedOrder) : BillableOrderPlaced option =
    let billingAmount = placedOrder.AmountToBill |> BillingAmount.value
    if billingAmount > 0M then
        {
        OrderId = placedOrder.OrderId
        BillingAddress = placedOrder.BillingAddress
        AmountToBill = placedOrder.AmountToBill
        } |> Some
    else
        None
*/

fun createBillingEvent(placedOrder: PricedOrder): Option<PlaceOrderEvent.BillableOrderPlaced> {
    val billingAmount = placedOrder.amountToBill.value
    return if (billingAmount > BigDecimal.ZERO) {
        placedOrder.run {
            //TODO: nasty nesting ;-)
            PlaceOrderEvent.BillableOrderPlaced(BillableOrderPlaced(orderId, billingAddress, amountToBill)).some()
        }
    } else {
        None
    }
}

/*
/// helper to convert an Option into a List
let listOfOption opt =
    match opt with
    | Some x -> [x]
    | None -> []

let createEvents : CreateEvents =
    fun pricedOrder acknowledgmentEventOpt ->
        let acknowledgmentEvents =
            acknowledgmentEventOpt
            |> Option.map PlaceOrderEvent.AcknowledgmentSent
            |> listOfOption
        let orderPlacedEvents =
            pricedOrder
            |> createOrderPlacedEvent
            |> PlaceOrderEvent.OrderPlaced
            |> List.singleton
        let billingEvents =
            pricedOrder
            |> createBillingEvent
            |> Option.map PlaceOrderEvent.BillableOrderPlaced
            |> listOfOption

        // return all the events
        [
        yield! acknowledgmentEvents
        yield! orderPlacedEvents
        yield! billingEvents
        ]
*/

fun createEvents(pricedOrder: PricedOrder, orderAcknowledgmentSent: OrderAcknowledgmentSent?): List<PlaceOrderEvent> {
    val acknowledgmentEvents =
        orderAcknowledgmentSent?.let { sent -> listOf(PlaceOrderEvent.AcknowledgmentSent(sent)) } ?: emptyList()
    val orderPlacedEvents = listOf(PlaceOrderEvent.OrderPlaced(createOrderPlacedEvent(pricedOrder)))
    val billingEvents = createBillingEvent(pricedOrder)
        .map { event -> listOf<PlaceOrderEvent>(event) }.getOrElse { emptyList() }
    return listOf(acknowledgmentEvents, orderPlacedEvents, billingEvents).flatten()
}

/*
// ---------------------------
// overall workflow
// ---------------------------

let placeOrder
    checkProductExists // dependency
    checkAddressExists // dependency
    getProductPrice    // dependency
    createOrderAcknowledgmentLetter  // dependency
    sendOrderAcknowledgment // dependency
    : PlaceOrder =       // definition of function

    fun unvalidatedOrder ->
        asyncResult {
            let! validatedOrder =
                validateOrder checkProductExists checkAddressExists unvalidatedOrder
                |> AsyncResult.mapError PlaceOrderError.Validation
            let! pricedOrder =
                priceOrder getProductPrice validatedOrder
                |> AsyncResult.ofResult
                |> AsyncResult.mapError PlaceOrderError.Pricing
            let acknowledgementOption =
                acknowledgeOrder createOrderAcknowledgmentLetter sendOrderAcknowledgment pricedOrder
            let events =
                createEvents pricedOrder acknowledgementOption
            return events
        }
*/

//TODO: add checking (product codes, addresses)
suspend fun placeOrder(
    validateOrder: ValidateOrder,
    priceOrder: PriceOrder,
    acknowledgeOrder: AcknowledgeOrder,
    createEvents: CreateEvents,
    unvalidatedOrder: UnvalidatedOrder,
): Either<PlaceOrderError, List<PlaceOrderEvent>> = either {
    val validatedOrder = validateOrder(unvalidatedOrder).mapLeft(PlaceOrderError::Validation).bind()
    val pricedOrder = priceOrder(validatedOrder).mapLeft(PlaceOrderError::Pricing).bind()
    val acknowledgement = acknowledgeOrder(pricedOrder)
    val events = createEvents(pricedOrder, acknowledgement)
    events
}
