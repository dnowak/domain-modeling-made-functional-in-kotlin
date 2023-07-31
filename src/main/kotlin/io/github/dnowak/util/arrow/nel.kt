package io.github.dnowak.util.arrow

import arrow.core.Nel

fun <E> distinct(nel: Nel<E>): Nel<E> = nel.distinct()

