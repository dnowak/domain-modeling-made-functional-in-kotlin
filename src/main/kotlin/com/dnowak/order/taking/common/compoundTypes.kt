package com.dnowak.order.taking.common

// ==================================
// Common compound types used throughout the OrderTaking domain
//
// Includes: customers, addresses, etc.
// Plus common errors.
//
// ==================================


// ==================================
// Customer-related types
// ==================================
/*
type PersonalName = {
    FirstName : String50
    LastName : String50
}
*/

data class PersonalName(
    val firstName: String,
    val lastName: String,
)

/*
type CustomerInfo = {
    Name : PersonalName
    EmailAddress : EmailAddress
}
 */

data class CustomerInfo(
    val name: PersonalName,
    val emailAddress: EmailAddress,
)

// ==================================
// Address-related
// ==================================

/*
type Address = {
    AddressLine1 : String50
    AddressLine2 : String50 option
    AddressLine3 : String50 option
    AddressLine4 : String50 option
    City : String50
    ZipCode : ZipCode
}
 */

data class City(val value: String)

data class Address(
    val addressLine1: String,
    val addressLine2: String?,
    val addressLine3: String?,
    val addressLine4: String?,
    val city: City,
    val zipCode: ZipCode
)

// ==================================
// Product-related types
// ==================================

// Note that the definition of a Product is in a different bounded
// context, and in this context, products are only represented by a ProductCode
// (see the SimpleTypes module).

