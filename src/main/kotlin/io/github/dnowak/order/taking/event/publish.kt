package io.github.dnowak.order.taking.event

import arrow.core.Either
import org.springframework.context.ApplicationEventPublisher

data class PublishError(val message: String)

typealias PublishEvent = (Any) -> Either<PublishError, Unit>

fun publishToSpring(eventPublisher: ApplicationEventPublisher, event: Any): Either<PublishError, Unit> =
    Either.catch {
        eventPublisher.publishEvent(event)
    }.mapLeft { throwable -> PublishError(throwable.toString()) }
