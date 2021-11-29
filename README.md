# domain-modeling-made-functional-in-kotlin
Examples from [Domain Modeling Made Functional Tackle Software Complexity with Domain-Driven Design and F#](https://pragprog.com/titles/swdddf/domain-modeling-made-functional/) book translated to [Kotlin](https://kotlinlang.org/)

## Idea

I was looking for production like examples of code based on Functional Programming paradigm. 
I found the aforementioned book which focuses on using Functional Programming with Domain Driven Design.
The book contains extensive code samples
So I found even more than I was looking for ;-).

I try to apply FP but in Kotlin. So I thought that it could be interesting to convert the code from book to Kotlin.

## Changes

It is not 1 to 1 copy. I have changed the original in several areas:
* implementation is based on [Arrow](https://arrow-kt.io/) - functional library for Kotlin
* application assembly and REST endpoint is based on Spring Boot
* data validation is based on arrow types
* base type for simple types to support comparison and string representation
* dropped *String50* type
* dependencies - only immediate dependencies are provided instead of dependencies of dependencies

## Original Code

The code in */src/original* directory was copied from sources attached to the book.

# Testing

## Start the Application

To start the application execute following command:

```shell
./gradlew bootRun
```

## Place Valid Order

Execute the following command in shell:

```shell
curl --location --request POST 'http://localhost:8080/orders' \
--header 'Content-Type: application/json' \
--data-raw '{
    "orderId": "orderId",
    "customerInfo": {
        "firstName": "Jan",
        "lastName": "Kowalski",
        "emailAddress": "jan@kowalski.com"
    },
    "shippingAddress": {
        "addressLine1": "Some Street 1",
        "city": "Los Angeles",
        "zipCode": "12345"
    },
    "billingAddress": {
        "addressLine1": "Other Street 4",
        "addressLine2": "Appatment 42",
        "city": "Beverly Hills",
        "zipCode": "90210"
    },
    "lines": [
        {
            "orderLineId": "line-1",
            "productCode": "W1234",
            "quantity": 10
        },
        {
            "orderLineId": "line-2",
            "productCode": "G123",
            "quantity": 0.75
        }
    ]
}
'
```

## Place Invalid Order

Execution of following command

```shell
curl --location --request POST 'http://localhost:8080/orders' \
--header 'Content-Type: application/json' \
--data-raw '{
    "orderId": "1345",
    "customerInfo": {
        "firstName": "Jan",
        "lastName": "Kowalski",
        "emailAddress": "jan@kowalski.com"
    },
    "shippingAddress": {
        "addressLine1": "Some Street 1",
        "city": "Los Angeles",
        "zipCode": "12345"
    },
    "billingAddress": {
        "addressLine1": "",
        "addressLine2": "Appatment 42",
        "city": "Beverly Hills",
        "zipCode": "90210a"
    },
    "lines": [
        {
            "orderLineId": "line-1",
            "productCode": "W1234",
            "quantity": 101111
        },
        {
            "orderLineId": "line-2",
            "productCode": "G123",
            "quantity": 0.001
        }
    ]
}
'
```

Should return validation errors:

```json
{
    "code": "ValidationError",
    "message": "Validation errors",
    "validationErrors": [
        {
            "path": "billingAddress/addressLine1",
            "message": "The length of <> should be between <1> and <50>"
        },
        {
            "path": "billingAddress/zipCode",
            "message": "'90210a' must match the pattern '\\d{5}'"
        },
        {
            "path": "lines[0]/quantity",
            "message": "The <101111> should be between <1> and <1000>"
        },
        {
            "path": "lines[1]/quantity",
            "message": "The <0.001> should be between <0.05> and <100.00>"
        }
    ]
}
```

# TODO

## Implement Address Validation 

The validation incorporates address check which is an asynchronous operation. Because of that it enforces that address 
and order validation become also synchronous. Async/suspend does not blend well with Validated. 
Need to find a smart way to solve the problem. 
