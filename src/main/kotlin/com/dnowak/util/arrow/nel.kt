package com.dnowak.util.arrow

import arrow.core.Nel
import arrow.core.NonEmptyList

fun <E> unique(nel: Nel<E>): Nel<E> = NonEmptyList.fromListUnsafe(nel.toSet().toList())

