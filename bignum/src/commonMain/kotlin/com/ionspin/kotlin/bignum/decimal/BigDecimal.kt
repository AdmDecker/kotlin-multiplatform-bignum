/*
 *    Copyright 2019 Ugljesa Jovanovic
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.ionspin.kotlin.bignum.decimal

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import kotlin.math.absoluteValue

/**
 * Created by Ugljesa Jovanovic
 * ugljesa.jovanovic@ionspin.com
 * on 23-Mar-2019
 */

@ExperimentalUnsignedTypes
class BigDecimal private constructor(
    val significand: BigInteger,
    val exponent: BigInteger = BigInteger.ZERO,
    val decimalMode: DecimalMode = DecimalMode()
) : Comparable<Any> {

    companion object {
        val ZERO = BigDecimal(BigInteger.ZERO)
        val ONE = BigDecimal(BigInteger.ONE)

        private fun roundOrDont(significand: BigInteger, exponent: BigInteger, decimalMode: DecimalMode): BigDecimal {
            return if (decimalMode.roundingMode != RoundingMode.NONE) {
                round(significand, exponent, decimalMode)
            } else {
                BigDecimal(significand, exponent)
            }
        }

        private fun round(significand: BigInteger, exponent: BigInteger, decimalMode: DecimalMode): BigDecimal {
            val significandDigits = significand.numberOfDigits()
            if (decimalMode.precision < significandDigits) {

            } else {

            }
            TODO()
        }

        fun fromLong(long: Long) = BigDecimal(BigInteger.fromLong(long), BigInteger.ZERO)
        fun fromInt(int: Int) = BigDecimal(BigInteger.fromInt(int), BigInteger.ZERO)
        fun fromShort(short: Short) = BigDecimal(BigInteger.fromShort(short), BigInteger.ZERO)
        fun fromByte(byte: Byte) = BigDecimal(BigInteger.fromByte(byte), BigInteger.ZERO)

        fun fromLongWithExponent(long: Long, exponent: BigInteger): BigDecimal {
            val bigint = BigInteger.fromLong(long)
            val preparedExponent = bigint.numberOfDigits() - 1
            return BigDecimal(bigint, exponent + preparedExponent)
        }

        fun fromIntWithExponent(int: Int, exponent: BigInteger): BigDecimal {
            val bigint = BigInteger.fromInt(int)
            val preparedExponent = bigint.numberOfDigits()
            return BigDecimal(bigint, exponent + preparedExponent)
        }

        fun fromShortWithExponent(short: Short, exponent: BigInteger): BigDecimal {
            val bigint = BigInteger.fromShort(short)
            val preparedExponent = bigint.numberOfDigits()
            return BigDecimal(bigint, exponent + preparedExponent)
        }

        fun fromByteWithExponent(byte: Byte, exponent: BigInteger): BigDecimal {
            val bigint = BigInteger.fromByte(byte)
            val preparedExponent = bigint.numberOfDigits()
            return BigDecimal(bigint, exponent + preparedExponent)
        }

        fun fromLongWithExponent(long: Long, exponent: Int): BigDecimal = fromLongWithExponent(long, exponent.toBigInteger())
        fun fromIntWithExponent(int: Int, exponent: Int): BigDecimal = fromIntWithExponent(int, exponent.toBigInteger())
        fun fromShortWithExponent(short: Short, exponent: Int): BigDecimal = fromShortWithExponent(short, exponent.toBigInteger())
        fun fromByteWithExponent(byte: Byte, exponent: Int): BigDecimal = fromByteWithExponent(byte, exponent.toBigInteger())
    }

    val isExponentLong = exponent.numberOfWords == 0
    val longExponent = exponent.magnitude[0]


    fun plus(other: BigDecimal, decimalMode: DecimalMode = DecimalMode()): BigDecimal {

        val (first, second) = bringToSameExponent(this, other)
        val newSignificand = first.significand + second.significand
        val newExponent = BigInteger.max(first.exponent, second.exponent) + newSignificand.numberOfDigits() - 1

        return roundOrDont(newSignificand, newExponent, decimalMode)
    }

    fun minus(other: BigDecimal, decimalMode: DecimalMode = DecimalMode()): BigDecimal {
        val (first, second) = bringToSameExponent(this, other)
        val newSignificand = first.significand - second.significand
        val newExponent = BigInteger.max(first.exponent, second.exponent) + newSignificand.numberOfDigits() - 1
        return roundOrDont(newSignificand, newExponent, decimalMode)
    }


    internal fun multiply(other: BigDecimal, decimalMode: DecimalMode = DecimalMode()): BigDecimal {
//        val (first, second) = bringToSameExponent(this, other)
        val newSignificand = this.significand * other.significand
        val newExponent = this.exponent + other.exponent
        return roundOrDont(newSignificand, newExponent, decimalMode)

    }


    fun div(other: BigDecimal, decimalMode: DecimalMode = DecimalMode()): BigDecimal {
        val (first, second) = bringToSameExponent(this, other)
        val newExponent = first.exponent - second.exponent
        val newSignificand = first.significand / second.significand //TODO
        return roundOrDont(newSignificand, newExponent, decimalMode)
    }

    //TODO
    fun integerDiv(other: BigDecimal, decimalMode: DecimalMode = DecimalMode()): BigDecimal {
        val (first, second) = bringToSameExponent(this, other)
        val newExponent = first.exponent - second.exponent
        val newSignificand = first.significand / second.significand
        return roundOrDont(newSignificand, newExponent, decimalMode)
    }

    fun rem(other: BigDecimal, decimalMode: DecimalMode = DecimalMode()): BigDecimal {
        val (first, second) = bringToSameExponent(this, other)
        val newExponent = first.exponent - second.exponent
        val newSignificand = first.significand % second.significand
        return roundOrDont(newSignificand, newExponent, decimalMode)
    }

    //TODO
    fun divrem(other: BigDecimal, decimalMode: DecimalMode = DecimalMode()): Pair<BigDecimal, BigDecimal> {
        val newExponent = BigInteger.max(this.exponent, other.exponent)
        val newSignificand = this.significand / other.significand
        val newRemainderSignificand = this.significand % other.significand
        return Pair(
            roundOrDont(newSignificand, newExponent, decimalMode),
            roundOrDont(newRemainderSignificand, newExponent, decimalMode)
        )
    }

    operator fun plus(other: BigDecimal): BigDecimal {
        return this.plus(other, DecimalMode())
    }

    operator fun minus(other: BigDecimal): BigDecimal {
        return this.minus(other, DecimalMode())
    }

    operator fun times(other: BigDecimal): BigDecimal {
        return this.multiply(other, DecimalMode())
    }

    operator fun div(other: BigDecimal): BigDecimal {
        return this.div(other, DecimalMode())
    }

    operator fun rem(other: BigDecimal): BigDecimal {
        return this.rem(other, DecimalMode())
    }

    fun unaryMinus(): BigDecimal {
        return BigDecimal(significand.negate(), exponent)
    }

    fun inc(): BigDecimal {
        return this + 1
    }

    fun dec(): BigDecimal {
        return this - 1
    }

    fun abs(): BigDecimal {
        return BigDecimal(significand.abs(), exponent)
    }

    fun negate(): BigDecimal {
        return BigDecimal(significand.negate(), exponent)
    }

    fun pow(powerExponent: Long): BigDecimal {
        return BigDecimal(significand, exponent * powerExponent)
    }

    private fun bringToSameExponent(first: BigDecimal, second: BigDecimal): Pair<BigDecimal, BigDecimal> {
        val firstExponent = first.exponent
        val secondExponent = second.exponent
//        val exponentDifference = (firstExponent.abs() - secondExponent.abs()).abs()
        return when {
            firstExponent < secondExponent -> {
                val exponentDifference = secondExponent - firstExponent
                val preparedSecond = second.significand * 10.toBigInteger().pow(exponentDifference.abs() - first.significand.numberOfDigits() + 1)
                val preparedSecondExponent = second.exponent - exponentDifference
                return Pair(first, BigDecimal(preparedSecond, preparedSecondExponent))
            }
            firstExponent > secondExponent -> {
                val exponentDifference = firstExponent - secondExponent
                val preparedFirst = first.significand * 10.toBigInteger().pow(exponentDifference.abs() - first.significand.numberOfDigits() + 1)
                val preparedFirstExponent = first.exponent - exponentDifference
                return Pair(BigDecimal(preparedFirst, preparedFirstExponent), second)
            }
            firstExponent == secondExponent -> {
                return Pair(first, second)
            }
            else -> {
                throw RuntimeException("Invalid comparison state BigInteger: $firstExponent, $secondExponent")
            }
        }

    }


    fun compare(other: BigDecimal): Int {
        if (exponent == other.exponent) {
            return significand.compare(other.significand)
        }
        val (preparedFirst, preparedSecond) = bringToSameExponent(this, other)
        return preparedFirst.compare(preparedSecond)
    }

    override fun compareTo(other: Any): Int {
        return when (other) {
            is BigDecimal -> compare(other)
            is Long -> compare(BigDecimal.fromLong(other))
            is Int -> compare(BigDecimal.fromInt(other))
            is Short -> compare(BigDecimal.fromShort(other))
            is Byte -> compare(BigDecimal.fromByte(other))
            else -> throw RuntimeException("Invalid comparison type for BigDecimal: ${other::class.simpleName}")
        }

    }

    override fun equals(other: Any?): Boolean {
        val comparison = when (other) {
            is BigDecimal -> compare(other)
            is Long -> compare(BigDecimal.fromLong(other))
            is Int -> compare(BigDecimal.fromInt(other))
            is Short -> compare(BigDecimal.fromShort(other))
            is Byte -> compare(BigDecimal.fromByte(other))
            else -> -1
        }
        return comparison == 0
    }

    override fun toString(): String {
        val significandString = significand.toString(10)
        return when {
            exponent > 0 -> "${placeADotInString(significandString, significandString.length - 1)}E+$exponent"
            exponent < 0 -> "${placeADotInString(significandString, significandString.length - 1)}E$exponent"
            exponent == BigInteger.ZERO -> noExponentStringtoScientificNotation(significandString)
            else -> throw RuntimeException("Invalid state, please report a bug (Integer compareTo invalid)")
        }
    }

    fun toStringExpanded(): String {
        val digits = significand.numberOfDigits()
        if (exponent > Int.MAX_VALUE) {
            throw RuntimeException("Invalid toStringExpanded request (expoenent > Int.MAX_VALUE)")
        }
        val significandString = significand.toString(10)
        return when {
            exponent > 0 -> {
                if (exponent - digits + 1 > 0) {
                    placeADotInString(significandString + ((exponent - digits + 1) * '0'), exponent.magnitude[0].toInt().absoluteValue)
                } else {
                    placeADotInString(significandString, exponent.magnitude[0].toInt().absoluteValue)
                }

            } //
            exponent < 0 -> placeADotInString(significandString, exponent.magnitude[0].toInt().absoluteValue)
            exponent == BigInteger.ZERO -> significandString

            else -> throw RuntimeException("Invalid state, please report a bug (Integer compareTo invalid)")
        }
    }

    private fun noExponentStringtoScientificNotation(input: String): String {
        return placeADotInString(input, input.length - 1) + "E+${input.length - 1}"
    }

    private fun placeADotInString(input: String, position: Int): String {
        val prepared = if (input.length < position) {
            val builder = buildString(input.length + (position - input.length)) {
                for (i in 0 until position - input.length) {
                    this.append('0')
                }
                append(input)
            }
            "0.${builder}"
        } else {
            val prefix = input.substring(0 until input.length - position)
            val suffix = input.substring(input.length - position until input.length)
            prefix + '.' + suffix
        }
        return prepared.dropLastWhile { it == '0' }

    }


    //
    //
    // ----------------- Interop with basic types ----------------------
    //
    //


    // ------------- Addition -----------


    operator fun plus(int: Int): BigDecimal {
        return this.plus(BigDecimal.fromInt(int))
    }


    operator fun plus(long: Long): BigDecimal {
        return this.plus(BigDecimal.fromLong(long))
    }


    operator fun plus(short: Short): BigDecimal {
        return this.plus(BigDecimal.fromShort(short))
    }


    operator fun plus(byte: Byte): BigDecimal {
        return this.plus(BigDecimal.fromByte(byte))
    }

    // ------------- Multiplication -----------


    operator fun times(int: Int): BigDecimal {
        return this.multiply(BigDecimal.fromInt(int))
    }


    operator fun times(long: Long): BigDecimal {
        return this.multiply(BigDecimal.fromLong(long))
    }


    operator fun times(short: Short): BigDecimal {
        return this.multiply(BigDecimal.fromShort(short))
    }


    operator fun times(byte: Byte): BigDecimal {
        return this.multiply(BigDecimal.fromByte(byte))
    }


    // ------------- Subtraction -----------


    operator fun minus(int: Int): BigDecimal {
        return this.minus(BigDecimal.fromInt(int))
    }


    operator fun minus(long: Long): BigDecimal {
        return this.minus(BigDecimal.fromLong(long))
    }


    operator fun minus(short: Short): BigDecimal {
        return this.minus(BigDecimal.fromShort(short))
    }


    operator fun minus(byte: Byte): BigDecimal {
        return this.minus(BigDecimal.fromByte(byte))
    }

    // ------------- Division -----------


    operator fun div(int: Int): BigDecimal {
        return this.div(BigDecimal.fromInt(int))
    }


    operator fun div(long: Long): BigDecimal {
        return this.div(BigDecimal.fromLong(long))
    }


    operator fun div(short: Short): BigDecimal {
        return this.div(BigDecimal.fromShort(short))
    }


    operator fun div(byte: Byte): BigDecimal {
        return this.div(BigDecimal.fromByte(byte))
    }


    operator fun rem(int: Int): BigDecimal {
        return this.rem(BigDecimal.fromInt(int))
    }


    operator fun rem(long: Long): BigDecimal {
        return this.rem(BigDecimal.fromLong(long))
    }


    operator fun rem(short: Short): BigDecimal {
        return this.rem(BigDecimal.fromShort(short))
    }


    operator fun rem(byte: Byte): BigDecimal {
        return this.rem(BigDecimal.fromByte(byte))
    }


}