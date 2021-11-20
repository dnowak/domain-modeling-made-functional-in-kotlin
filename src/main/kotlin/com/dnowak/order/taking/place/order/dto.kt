package com.dnowak.order.taking.place.order

import arrow.core.ValidatedNel
import arrow.core.zip
import com.dnowak.order.taking.common.Address
import com.dnowak.order.taking.common.City
import com.dnowak.order.taking.common.CustomerInfo
import com.dnowak.order.taking.common.EmailAddress
import com.dnowak.order.taking.common.OrderQuantity
import com.dnowak.order.taking.common.PersonalName
import com.dnowak.order.taking.common.ProductCode
import com.dnowak.order.taking.common.Property
import com.dnowak.order.taking.common.PropertyValidationError
import com.dnowak.order.taking.common.ZipCode
import com.dnowak.order.taking.common.assign
import com.dnowak.order.taking.common.validateNullableString50
import com.dnowak.order.taking.common.validateString50
import java.math.BigDecimal

//===============================================
// DTO for CustomerInfo
//===============================================

/*
type CustomerInfoDto = {
    FirstName : string
    LastName : string
    EmailAddress : string
    VipStatus : string
}
 */

data class CustomerInfoDto(
    val firstName: String,
    val lastName: String,
    val emailAddress: String,
)

/// Functions for converting between the DTO and corresponding domain object
//module internal CustomerInfoDto =

/// Convert the DTO into a UnvalidatedCustomerInfo object.
/// This always succeeds because there is no validation.
/// Used when importing an OrderForm from the outside world into the domain.
/*
let toUnvalidatedCustomerInfo (dto:CustomerInfoDto) : UnvalidatedCustomerInfo =

// sometimes it's helpful to use an explicit type annotation
// to avoid ambiguity between records with the same field names.
let domainObj : UnvalidatedCustomerInfo = {

    // this is a simple 1:1 copy which always succeeds
    FirstName = dto.FirstName
    LastName = dto.LastName
    EmailAddress = dto.EmailAddress
    VipStatus = dto.VipStatus

}
domainObj
 */
fun toUnvalidatedCustomerInfo(dto: CustomerInfoDto): UnvalidatedCustomerInfo =
    dto.run {
        UnvalidatedCustomerInfo(firstName, lastName, emailAddress)
    }

/// Convert the DTO into a CustomerInfo object
/// Used when importing from the outside world into the domain, eg loading from a database
/*
let toCustomerInfo (dto:CustomerInfoDto) :Result<CustomerInfo,string> =
result {
    // get each (validated) simple type from the DTO as a success or failure
    let!first = dto.FirstName |> String50.create "FirstName"
    let!last = dto.LastName |> String50.create "LastName"
    let!email = dto.EmailAddress|> EmailAddress.create "EmailAddress"
    let!vipStatus = dto.VipStatus |> VipStatus.create "VipStatus"
    // combine the components to create the domain object
    let name = { FirstName = first; LastName = last }
    let info = { Name = name; EmailAddress = email; VipStatus = vipStatus }
    return info
}
*/

fun toCustomerInfo(dto: CustomerInfoDto): ValidatedNel<PropertyValidationError, CustomerInfo> {
    val validatedFirstName = validateString50(dto.firstName)
        .assign(Property("firstName"))
    val validatedLastName = validateString50(dto.lastName)
        .assign(Property("lastName"))
    val validatedEmailAddress = EmailAddress.validate(dto.emailAddress)
        .assign(Property("emailAddress"))

    return validatedFirstName.zip(
        validatedLastName,
        validatedEmailAddress,
    ) { firstName, lastName, emailAddress ->
        CustomerInfo(PersonalName(firstName, lastName), emailAddress)
    }
}

/// Convert a CustomerInfo object into the corresponding DTO.
/// Used when exporting from the domain to the outside world.
/*
let fromCustomerInfo (domainObj:CustomerInfo) :CustomerInfoDto =
// this is a simple 1:1 copy
{
    FirstName = domainObj.Name.FirstName |> String50.value
    LastName = domainObj.Name.LastName |> String50.value
    EmailAddress = domainObj.EmailAddress |> EmailAddress.value
    VipStatus = domainObj.VipStatus |> VipStatus.value
}
 */

fun fromCustomerInfo(info: CustomerInfo): CustomerInfoDto =
    info.run {
        CustomerInfoDto(
            firstName = name.firstName,
            lastName = name.lastName,
            emailAddress = emailAddress.value,
        )
    }

//===============================================
// DTO for Address
//===============================================

/*
type AddressDto = {
    AddressLine1 : string
    AddressLine2 : string
    AddressLine3 : string
    AddressLine4 : string
    City : string
    ZipCode : string
    State : string
    Country : string
}
*/

data class AddressDto(
    val addressLine1: String,
    val addressLine2: String?,
    val addressLine3: String?,
    val addressLine4: String?,
    val city: String,
    val zipCode: String,
)

/// Functions for converting between the DTO and corresponding domain object
//module internal AddressDto =

/// Convert the DTO into a UnvalidatedAddress
/// This always succeeds because there is no validation.
/// Used when importing an OrderForm from the outside world into the domain.
/*
let toUnvalidatedAddress (dto:AddressDto) :UnvalidatedAddress =
// this is a simple 1:1 copy
{
    AddressLine1 = dto.AddressLine1
    AddressLine2 = dto.AddressLine2
    AddressLine3 = dto.AddressLine3
    AddressLine4 = dto.AddressLine4
    City = dto.City
    ZipCode = dto.ZipCode
    State = dto.State
    Country = dto.Country
}
 */

fun toUnvalidatedAddress(dto: AddressDto): UnvalidatedAddress =
    dto.run {
        UnvalidatedAddress(
            addressLine1,
            addressLine2,
            addressLine3,
            addressLine4,
            city,
            zipCode
        )
    }

/// Convert the DTO into a Address object
/// Used when importing from the outside world into the domain, eg loading from a database.
/*
let toAddress (dto:AddressDto) :Result<Address,string> =
result {
    // get each (validated) simple type from the DTO as a success or failure
    let!addressLine1 = dto.AddressLine1 |> String50.create "AddressLine1"
    let!addressLine2 = dto.AddressLine2 |> String50.createOption "AddressLine2"
    let!addressLine3 = dto.AddressLine3 |> String50.createOption "AddressLine3"
    let!addressLine4 = dto.AddressLine4 |> String50.createOption "AddressLine4"
    let!city = dto.City |> String50.create "City"
    let!zipCode = dto.ZipCode |> ZipCode.create "ZipCode"
    let!state = dto.State |> UsStateCode.create "State"
    let!country = dto.Country |> String50.create "Country"

    // combine the components to create the domain object
    let address : Common . Address = {
        AddressLine1 = addressLine1
        AddressLine2 = addressLine2
        AddressLine3 = addressLine3
        AddressLine4 = addressLine4
        City = city
        ZipCode = zipCode
        State = state
        Country = country
    }
    return address
}
*/

fun toAddress(dto: AddressDto): ValidatedNel<PropertyValidationError, Address> {
    val validatedLine1 = validateString50(dto.addressLine1)
        .assign(Property("addressLine1"))
    val validatedLine2 = validateNullableString50(dto.addressLine2)
        .assign(Property("addressLine2"))
    val validatedLine3 = validateNullableString50(dto.addressLine3)
        .assign(Property("addressLine3"))
    val validatedLine4 = validateNullableString50(dto.addressLine4)
        .assign(Property("addressLine4"))
    val validatedCity = validateString50(dto.city)
        .map(::City)
        .assign(Property("city"))
    val validatedZipCode = ZipCode.validate(dto.zipCode)
        .assign(Property("zipCode"))

    return validatedLine1.zip(
        validatedLine2,
        validatedLine3,
        validatedLine4,
        validatedCity,
        validatedZipCode,
    ) { line1, line2, line3, line4, city, zipCode ->
        Address(line1, line2, line3, line4, city, zipCode)
    }
}

/// Convert a Address object into the corresponding DTO.
/// Used when exporting from the domain to the outside world.
/*
let fromAddress (domainObj:Address) :AddressDto =
// this is a simple 1:1 copy
{
    AddressLine1 = domainObj.AddressLine1 |> String50.value
    AddressLine2 = domainObj.AddressLine2 |> Option.map String50.value |> defaultIfNone null
    AddressLine3 = domainObj.AddressLine3 |> Option.map String50.value |> defaultIfNone null
    AddressLine4 = domainObj.AddressLine4 |> Option.map String50.value |> defaultIfNone null
    City = domainObj.City |> String50.value
    ZipCode = domainObj.ZipCode |> ZipCode.value
    State = domainObj.State |> UsStateCode.value
    Country = domainObj.Country |> String50.value
}
*/

fun fromAddress(address: Address): AddressDto = address.run {
    AddressDto(addressLine1, addressLine2, addressLine3, addressLine4, city.value, zipCode.value)
}

//===============================================
// DTOs for OrderLines
//===============================================

/// From the order form used as input
/*
type OrderFormLineDto =  {
    OrderLineId : string
    ProductCode : string
    Quantity : decimal
}
*/

data class OrderFormLineDto(
    val orderLineId: String,
    val productCode: String,
    //TODO: Is it a correct type?
    val quantity: BigDecimal
)

/// Functions relating to the OrderLine DTOs
//module internal OrderLineDto =

/// Convert the OrderFormLine into a UnvalidatedOrderLine
/// This always succeeds because there is no validation.
/// Used when importing an OrderForm from the outside world into the domain.
/*
let toUnvalidatedOrderLine (dto:OrderFormLineDto) :UnvalidatedOrderLine =
// this is a simple 1:1 copy
{
    OrderLineId = dto.OrderLineId
    ProductCode = dto.ProductCode
    Quantity = dto.Quantity
}
*/

fun toUnvalidatedOrderLine(dto: OrderFormLineDto): UnvalidatedOrderLine = dto.run {
    UnvalidatedOrderLine(orderLineId, productCode, quantity)
}

//===============================================
// DTOs for PricedOrderLines
//===============================================

/// Used in the output of the workflow
/*
type PricedOrderLineDto =  {
    OrderLineId : string
    ProductCode : string
    Quantity : decimal
    LinePrice : decimal
}
*/

data class PricedOrderLineDto(
    val orderLineId: String,
    val productCode: String,
    val quantity: BigDecimal,
    val lineprice: BigDecimal,
)

//module internal PricedOrderLineDto =
/// Convert a PricedOrderLine object into the corresponding DTO.
/// Used when exporting from the domain to the outside world.
/*
let fromDomain (domainObj:PricedOrderLine) :PricedOrderLineDto =
// this is a simple 1:1 copy
{
    OrderLineId = domainObj.OrderLineId |> OrderLineId.value
    ProductCode = domainObj.ProductCode  |> ProductCode.value
    Quantity = domainObj.Quantity |> OrderQuantity.value
    LinePrice = domainObj.LinePrice |> Price.value
}
*/

private fun value(productCode: ProductCode): String = when (productCode) {
    is ProductCode.Gizmo -> productCode.code.value
    is ProductCode.Widget -> productCode.code.value
}

private fun value(quantity: OrderQuantity): BigDecimal = TODO()

fun fromDomain(line: PricedOrderLine): PricedOrderLineDto = line.run {
    PricedOrderLineDto(
        orderLineId = orderLineId.value,
        productCode = value(productCode),
        quantity = value(quantity),
        lineprice = linePrice.value,
    )
}

//===============================================
// DTO for OrderForm
//===============================================
/*
type OrderFormDto = {
    OrderId : string
    CustomerInfo : CustomerInfoDto
    ShippingAddress : AddressDto
    BillingAddress : AddressDto
    Lines : OrderFormLineDto list
}
*/

data class OrderFormDto(
    val orderId: String,
    val customerInfo: CustomerInfoDto,
    val shippingAddress: AddressDto,
    val billingAddress: AddressDto,
    val lines: List<OrderFormLineDto>,
)

/// Functions relating to the Order DTOs
//module internal OrderFormDto =

/// Convert the OrderForm into a UnvalidatedOrder
/// This always succeeds because there is no validation.
/*
let toUnvalidatedOrder (dto:OrderFormDto) :UnvalidatedOrder =
{
    OrderId = dto.OrderId
    CustomerInfo = dto.CustomerInfo |> CustomerInfoDto.toUnvalidatedCustomerInfo
    ShippingAddress = dto.ShippingAddress |> AddressDto.toUnvalidatedAddress
    BillingAddress = dto.BillingAddress |> AddressDto.toUnvalidatedAddress
    Lines = dto.Lines |> List.map OrderLineDto.toUnvalidatedOrderLine
}
*/

fun toUnvalidatedOrder(dto: OrderFormDto): UnvalidatedOrder = dto.run {
    UnvalidatedOrder(
        orderId = orderId,
        customerInfo = toUnvalidatedCustomerInfo(customerInfo),
        shippingAddress = toUnvalidatedAddress(shippingAddress),
        billingAddress = toUnvalidatedAddress(billingAddress),
        lines = lines.map(::toUnvalidatedOrderLine),
    )
}

//===============================================
// DTO for OrderPlaced event
//===============================================

/// Event to send to shipping context
/*
type OrderPlacedDto = {
    OrderId : string
    CustomerInfo : CustomerInfoDto
    ShippingAddress : AddressDto
    BillingAddress : AddressDto
    AmountToBill : decimal
    Lines : PricedOrderLineDto list
}
*/

data class OrderPlacedDto(
    val orderId: String,
    val customerInfo: CustomerInfoDto,
    val shippingAddress: AddressDto,
    val billingAddress: AddressDto,
    val amountToBill: BigDecimal,
    val lines: List<PricedOrderLineDto>,
)

//module internal OrderPlacedDto =

/// Convert a OrderPlaced object into the corresponding DTO.
/// Used when exporting from the domain to the outside world.
/*
let fromDomain (domainObj:OrderPlaced) :OrderPlacedDto =
{
    OrderId = domainObj.OrderId |> OrderId.value
    CustomerInfo = domainObj.CustomerInfo |> CustomerInfoDto.fromCustomerInfo
    ShippingAddress = domainObj.ShippingAddress |> AddressDto.fromAddress
    BillingAddress = domainObj.BillingAddress |> AddressDto.fromAddress
    AmountToBill = domainObj.AmountToBill |> BillingAmount.value
    Lines = domainObj.Lines |> List.map PricedOrderLineDto.fromDomain
}
*/

fun fromDomain(order: OrderPlaced): OrderPlacedDto = order.run {
    OrderPlacedDto(
        orderId = orderId.value,
        customerInfo = fromCustomerInfo(customerInfo),
        shippingAddress = fromAddress(shippingAddress),
        billingAddress = fromAddress(billingAddress),
        amountToBill = amountToBill.value,
        lines = lines.map(::fromDomain),
    )
}

//===============================================
// DTO for BillableOrderPlaced event
//===============================================

/// Event to send to billing context
/*
type BillableOrderPlacedDto = {
    OrderId : string
    BillingAddress: AddressDto
    AmountToBill : decimal
}
*/

data class BillableOrderPlacedDto(
    val orderId: String,
    val billingAddress: AddressDto,
    val amountToBill: BigDecimal
)


//module internal BillableOrderPlacedDto =

/// Convert a BillableOrderPlaced object into the corresponding DTO.
/// Used when exporting from the domain to the outside world.
/*
let fromDomain (domainObj:BillableOrderPlaced ) :BillableOrderPlacedDto =
{
    OrderId = domainObj.OrderId |> OrderId.value
    BillingAddress = domainObj.BillingAddress |> AddressDto.fromAddress
    AmountToBill = domainObj.AmountToBill |> BillingAmount.value
}
*/

fun fromDomain(event: BillableOrderPlaced): BillableOrderPlacedDto = event.run {
    BillableOrderPlacedDto(
        orderId = orderId.value,
        billingAddress = fromAddress(billingAddress),
        amountToBill = amountToBill.value,
    )
}

//===============================================
// DTO for OrderAcknowledgmentSent event
//===============================================

/// Event to send to other bounded contexts
/*
type OrderAcknowledgmentSentDto = {
    OrderId : string
    EmailAddress : string
}
*/

data class OrderAcknowledgmentSentDto(
    val orderId: String,
    val emailAddress: String,
)

//module internal OrderAcknowledgmentSentDto =

/// Convert a OrderAcknowledgmentSent object into the corresponding DTO.
/// Used when exporting from the domain to the outside world.
/*
let fromDomain (domainObj:OrderAcknowledgmentSent) :OrderAcknowledgmentSentDto =
{
    OrderId = domainObj.OrderId |> OrderId.value
    EmailAddress = domainObj.EmailAddress |> EmailAddress.value
}
*/

fun fromDomain(event: OrderAcknowledgmentSent): OrderAcknowledgmentSentDto = event.run {
    OrderAcknowledgmentSentDto(
        orderId = orderId.value,
        emailAddress = emailAddress.value,
    )
}

//===============================================
// DTO for PlaceOrderEvent
//===============================================

/// Use a dictionary representation of a PlaceOrderEvent, suitable for JSON
/// See "Serializing Records and Choice Types Using Maps" in chapter 11
//type PlaceOrderEventDto = IDictionary<string,obj>

typealias PlaceOrderEventDto = Map<String, Any>

//module internal PlaceOrderEventDto =

/// Convert a PlaceOrderEvent into the corresponding DTO.
/// Used when exporting from the domain to the outside world.
/*
let fromDomain (domainObj:PlaceOrderEvent) :PlaceOrderEventDto =
match domainObj with
| OrderPlaced orderPlaced ->
let obj = orderPlaced |> OrderPlacedDto.fromDomain |> box // use "box" to cast into an object
let key = "OrderPlaced"
[(key,obj)] |> dict
| BillableOrderPlaced billableOrderPlaced ->
let obj = billableOrderPlaced |> BillableOrderPlacedDto.fromDomain |> box
let key = "BillableOrderPlaced"
[(key,obj)] |> dict
| AcknowledgmentSent orderAcknowledgmentSent ->
let obj = orderAcknowledgmentSent |> OrderAcknowledgmentSentDto.fromDomain |> box
let key = "OrderAcknowledgmentSent"
[(key,obj)] |> dict
*/

fun fromDomain(event: PlaceOrderEvent): PlaceOrderEventDto = when (event) {
    is PlaceOrderEvent.OrderPlaced -> mapOf("OrderPlaced" to fromDomain(event.payload))
    is PlaceOrderEvent.BillableOrderPlaced -> mapOf("BillableOrderPlaced" to fromDomain(event.payload))
    is PlaceOrderEvent.AcknowledgmentSent -> mapOf("OrderAcknowledgementSent" to fromDomain(event.payload))
}

//===============================================
// DTO for PlaceOrderError
//===============================================
/*
type PlaceOrderErrorDto = {
    Code : string
    Message : string
}
*/

data class PropertyValidationErrorDto(
    val path: String,
    val message: String,
)

fun fromDomain(error: PropertyValidationError): PropertyValidationErrorDto {
    val path = error.path.map(Property::value).joinToString(separator = "/")
    return PropertyValidationErrorDto(path, error.message)
}

data class PlaceOrderErrorDto(
    val code: String,
    val message: String,
    val validationErrors: List<PropertyValidationErrorDto>? = null
)

//module internal PlaceOrderErrorDto =

/*
let fromDomain (domainObj:PlaceOrderError ) :PlaceOrderErrorDto =
match domainObj with
| Validation validationError ->
let (ValidationError msg) = validationError
{
    Code = "ValidationError"
    Message = msg
}
| Pricing pricingError ->
let (PricingError msg) = pricingError
{
    Code = "PricingError"
    Message = msg
}
| RemoteService remoteServiceError ->
let msg = sprintf "%s: %s" remoteServiceError.Service.Name remoteServiceError.Exception.Message
{
    Code = "RemoteServiceError"
    Message = msg
}
*/

fun fromDomain(error: PlaceOrderError): PlaceOrderErrorDto = when (error) {
    is PlaceOrderError.Pricing -> PlaceOrderErrorDto("PricingError", error.error.message)
    is PlaceOrderError.RemoteService ->
        PlaceOrderErrorDto("RemoteServiceError", "${error.error.service}: ${error.error.exception}")
    is PlaceOrderError.Validation ->
        PlaceOrderErrorDto("ValidationError", "Validation errors", error.errors.map(::fromDomain))
}
