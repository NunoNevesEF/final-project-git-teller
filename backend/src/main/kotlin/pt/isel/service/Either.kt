package pt.isel.service

import pt.isel.service.Either.Left
import pt.isel.service.Either.Right

sealed class Either<out L, out R>{
    data class Left<out L>(val left: L) : Either<L, Nothing>()
    data class Right<out R>(val right: R) : Either<Nothing, R>()
}

fun <R> success(value: R) = Right(value)
fun <L> failure(error: L) = Left<L>(error)

typealias Success<S> = Either.Right<S>
typealias Failure<E> = Either.Left<E>