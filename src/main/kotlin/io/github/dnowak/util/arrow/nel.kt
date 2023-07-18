package io.github.dnowak.util.arrow

import arrow.core.Nel
import arrow.core.NonEmptyList

fun <E> distinct(nel: Nel<E>): Nel<E> = nel.distinct()

