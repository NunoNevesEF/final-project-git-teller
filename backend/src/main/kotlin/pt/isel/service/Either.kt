package pt.isel.service

import pt.isel.service.Either.Left
import pt.isel.service.Either.Right

sealed class Either<out L, out R>{
    data class Left<out L>(val left: L) : Either<L, Nothing>()
    data class Right<out R>(val right: R) : Either<Nothing, R>()
}

fun <R> success(value: R) = Right(value)
fun <L> failure(error: L) = Left<L>(error)
fun <L,R> R?.toEither(left: () -> L): Either<L, R> =
    this?.let { success(it) } ?: failure(left())

fun <L,R> Either<L,R>.isSuccess() = this is Right<R>
fun <L,R> Either<L,R>.isFailure() = this is Left<L>

fun <L,R> Either<L,R>.rightOrNull() : R? = (this as? Right)?.right
fun <L,R> Either<L,R>.leftOrNull() : L? = (this as? Left)?.left

typealias Success<S> = Right<S>
typealias Failure<E> = Left<E>