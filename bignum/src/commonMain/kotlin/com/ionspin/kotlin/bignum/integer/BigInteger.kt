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

package com.ionspin.kotlin.bignum.integer

import kotlin.math.ceil
import kotlin.math.log10


/**
 * Created by Ugljesa Jovanovic
 * ugljesa.jovanovic@ionspin.com
 * on 10-Mar-2019
 */

enum class Sign {
    POSITIVE, NEGATIVE, ZERO;

    operator fun not(): Sign {
        return when (this) {
            POSITIVE -> NEGATIVE
            NEGATIVE -> POSITIVE
            ZERO -> ZERO
        }
    }

    fun toInt(): Int {
        return when (this) {
            POSITIVE -> 1
            NEGATIVE -> -1
            ZERO -> 0
        }
    }
}

/**
 * Arbitrary precision integer arithmetic.
 *
 * Based on unsigned arrays, currently limited to [Int.MAX_VALUE] words.
 */
@ExperimentalUnsignedTypes
class BigInteger private constructor(wordArray: WordArray, val sign: Sign) : Comparable<Any> {

    constructor(long : Long) : this(arithmetic.fromLong(long), determinSignFromNumber(long))
    constructor(int : Int) : this(arithmetic.fromInt(int), determinSignFromNumber(int))
    constructor(short : Short) : this(arithmetic.fromShort(short), determinSignFromNumber(short))
    constructor(byte : Byte) : this(arithmetic.fromByte(byte), determinSignFromNumber(byte))


    @ExperimentalUnsignedTypes
    companion object : BigNumber.Creator<BigInteger>, BigNumber.Util<BigInteger> {
        private val arithmetic: BigIntegerArithmetic<WordArray, Word> = chosenArithmetic

        val ZERO = BigInteger(arithmetic.ZERO, Sign.ZERO)
        val ONE = BigInteger(arithmetic.ONE, Sign.POSITIVE)
        val TEN = BigInteger(arithmetic.TEN, Sign.POSITIVE)

        val LOG_10_OF_2 = log10(2.0)

        fun parseString(string: String, base: Int = 10): BigInteger {
            val signed = (string[0] == '-' || string[0] == '+')
            return if (signed) {
                if (string.length == 1) {
                    throw NumberFormatException("Invalid big integer: $string")
                }
                val isNegative = if (string[0] == '-') {
                    Sign.NEGATIVE
                } else {
                    Sign.POSITIVE
                }
                if (string.length == 2 && string[1] == '0') {
                    return ZERO
                }
                BigInteger(
                    arithmetic.parseForBase(string.substring(startIndex = 1, endIndex = string.length), base),
                    isNegative
                )
            } else {
                if (string.length == 1 && string[0] == '0') {
                    return ZERO
                }
                BigInteger(arithmetic.parseForBase(string, base), Sign.POSITIVE)
            }

        }

        internal fun fromWordArray(wordArray: WordArray, sign: Sign): BigInteger {
            return BigInteger(wordArray, sign)
        }

        private inline fun <reified T> determinSignFromNumber(number: Comparable<T>): Sign {
            return when (T::class) {
                Long::class -> {
                    number as Long
                    when {
                        number < 0 -> Sign.NEGATIVE
                        number > 0 -> Sign.POSITIVE
                        else -> Sign.ZERO
                    }
                }
                Int::class -> {
                    number as Int
                    when {
                        number < 0 -> Sign.NEGATIVE
                        number > 0 -> Sign.POSITIVE
                        else -> Sign.ZERO
                    }
                }
                Short::class -> {
                    number as Short
                    when {
                        number < 0 -> Sign.NEGATIVE
                        number > 0 -> Sign.POSITIVE
                        else -> Sign.ZERO
                    }
                }
                Byte::class -> {
                    number as Byte
                    when {
                        number < 0 -> Sign.NEGATIVE
                        number > 0 -> Sign.POSITIVE
                        else -> Sign.ZERO
                    }
                }
                else -> throw RuntimeException("Unsupported type ${T::class.simpleName}")
            }

        }

        fun fromULong(uLong: ULong) = BigInteger(arithmetic.fromULong(uLong), Sign.POSITIVE)
        fun fromUInt(uInt: UInt) = BigInteger(arithmetic.fromUInt(uInt), Sign.POSITIVE)
        fun fromUShort(uShort: UShort) = BigInteger(arithmetic.fromUShort(uShort), Sign.POSITIVE)
        fun fromUByte(uByte: UByte) = BigInteger(arithmetic.fromUByte(uByte), Sign.POSITIVE)
        fun fromLong(long: Long) = BigInteger(long)
        fun fromInt(int: Int) = BigInteger(int)
        fun fromShort(short: Short) = BigInteger(short)
        fun fromByte(byte: Byte) = BigInteger(byte)

        fun max(first: BigInteger, second: BigInteger): BigInteger {
            return if (first > second) {
                first
            } else {
                second
            }
        }

        fun min(first: BigInteger, second: BigInteger): BigInteger {
            return if (first < second) {
                first
            } else {
                second
            }
        }
    }

    internal val magnitude: WordArray = wordArray

    private fun isResultZero(resultMagnitude: WordArray): Boolean {
        return arithmetic.compare(resultMagnitude, arithmetic.ZERO) == 0
    }

    val numberOfWords = magnitude.size

    var stringRepresentation: String? = null

    override fun add(other: BigInteger): BigInteger {
        val comparison = arithmetic.compare(this.magnitude, other.magnitude)
        return if (other.sign == this.sign) {
            return BigInteger(arithmetic.add(this.magnitude, other.magnitude), sign)
        } else {
            when {
                comparison > 0 -> {
                    BigInteger(arithmetic.substract(this.magnitude, other.magnitude), sign)
                }
                comparison < 0 -> {
                    BigInteger(arithmetic.substract(other.magnitude, this.magnitude), other.sign)
                }
                else -> {
                    ZERO
                }
            }
        }

    }

    override fun subtract(other: BigInteger): BigInteger {
        val comparison = arithmetic.compare(this.magnitude, other.magnitude)
        if (this == ZERO) {
            return other.negate()
        }
        if (other == ZERO) {
            return this
        }
        return if (other.sign == this.sign) {
            when {
                comparison > 0 -> {
                    BigInteger(arithmetic.substract(this.magnitude, other.magnitude), sign)
                }
                comparison < 0 -> {
                    BigInteger(arithmetic.substract(other.magnitude, this.magnitude), !sign)
                }
                else -> {
                    ZERO
                }
            }
        } else {
            return BigInteger(arithmetic.add(this.magnitude, other.magnitude), sign)
        }
    }

    override fun multiply(other: BigInteger): BigInteger {
        if (this.isZero() || other.isZero()) {
            return ZERO
        }

        val sign = if (this.sign != other.sign) {
            Sign.NEGATIVE
        } else {
            Sign.POSITIVE
        }
        return if (sign == Sign.POSITIVE) {
            BigInteger(arithmetic.multiply(this.magnitude, other.magnitude), sign)
        } else {
            BigInteger(arithmetic.multiply(this.magnitude, other.magnitude), sign)
        }
    }

    override fun divide(other: BigInteger): BigInteger {
        if (other.isZero()) {
            throw ArithmeticException("Division by zero! $this / $other")
        }

        val result = arithmetic.divide(this.magnitude, other.magnitude).first
        return if (result == arithmetic.ZERO) {
            ZERO
        } else {
            val sign = if (this.sign != other.sign) {
                Sign.NEGATIVE
            } else {
                Sign.POSITIVE
            }
            BigInteger(result, sign)
        }


    }

    override fun remainder(other: BigInteger): BigInteger {
        if (other.isZero()) {
            throw ArithmeticException("Division by zero! $this / $other")
        }
        val sign = if (this.sign != other.sign) {
            Sign.NEGATIVE
        } else {
            Sign.POSITIVE
        }

        return BigInteger(arithmetic.divide(this.magnitude, other.magnitude).second, sign)
    }

    override fun divideAndRemainder(other: BigInteger): Pair<BigInteger, BigInteger> {
        if (other.isZero()) {
            throw ArithmeticException("Division by zero! $this / $other")
        }
        val sign = if (this.sign != other.sign) {
            Sign.NEGATIVE
        } else {
            Sign.POSITIVE
        }
        val result = arithmetic.divide(this.magnitude, other.magnitude)
        val quotient = if (result.first == arithmetic.ZERO) {
            ZERO
        } else {
            BigInteger(result.first, sign)
        }
        val remainder = if (result.second == arithmetic.ZERO) {
            ZERO
        } else {
            BigInteger(result.second, sign)
        }
        return Pair(
            quotient,
            remainder
        )
    }

    fun divideByReciprocal(other : BigInteger) : Pair<BigInteger, BigInteger> {
        TODO()
    }

    private fun reciprocal() : BigInteger {
        TODO()
    }

    fun sqrt(): SqareRootAndRemainder {
        TODO()
    }

    fun gcd(other: BigInteger) {
        TODO()
    }

    fun compare(other: BigInteger): Int {
        if (isZero() && other.isZero()) return 0
        if (other.isZero() && this.sign == Sign.POSITIVE) return 1
        if (other.isZero() && this.sign == Sign.NEGATIVE) return -1
        if (this.isZero() && other.sign == Sign.POSITIVE) return -1
        if (this.isZero() && other.sign == Sign.NEGATIVE) return 1
        if (sign != other.sign) return if (sign == Sign.POSITIVE) 1 else -1
        return arithmetic.compare(this.magnitude, other.magnitude)
    }

    override fun isZero(): Boolean = this.sign == Sign.ZERO

    override fun negate(): BigInteger {
        return BigInteger(wordArray = this.magnitude.copyOf(), sign = sign.not())
    }

    override fun abs(): BigInteger {
        return BigInteger(wordArray = this.magnitude.copyOf(), sign = Sign.POSITIVE)
    }

    override fun pow(exponent: BigInteger): BigInteger {
        if (exponent <= Long.MAX_VALUE) {
            return pow(exponent.magnitude[0].toLong())
        }
        //TODO this is not efficient
        var counter = exponent
        var result = ONE
        while (counter > 0) {
            counter--
            result *= this
        }

        return result
    }

    override fun pow(exponent: Long): BigInteger {
        val sign = if (sign == Sign.NEGATIVE) {
            if (exponent % 2 == 0L) {
                Sign.POSITIVE
            } else {
                Sign.NEGATIVE
            }
        } else {
            Sign.POSITIVE
        }
        return BigInteger(arithmetic.pow(magnitude, exponent), sign)
    }

    override fun pow(exponent: Int): BigInteger {
        return pow(exponent.toLong())
    }

    override fun signum(): Int = when (sign) {
        Sign.POSITIVE -> 1
        Sign.NEGATIVE -> -1
        Sign.ZERO -> 0
    }

    override fun bitAt(position: Long): Boolean {
        return arithmetic.bitAt(magnitude, position)
    }

    override fun setBitAt(position: Long, bit: Boolean): BigInteger {
        return BigInteger(arithmetic.setBitAt(magnitude, position, bit), sign)
    }

    override fun numberOfDecimalDigits(): Long {
        val bitLenght = arithmetic.bitLength(magnitude)
        val minDigit = ceil((bitLenght - 1) * LOG_10_OF_2)
//        val maxDigit = floor(bitLenght * LOG_10_OF_2) + 1
//        val correct = this / 10.toBigInteger().pow(maxDigit.toInt())
//        return when {
//            correct == ZERO -> maxDigit.toInt() - 1
//            correct > 0 && correct < 10 -> maxDigit.toInt()
//            else -> -1
//        }

        var tmp = this / 10.toBigInteger().pow(minDigit.toInt())
        var counter = 0L
        while (tmp.compareTo(0) != 0) {
            tmp /= 10
            counter++
        }
        return counter + minDigit.toInt()


    }


    override infix fun shl(places: Int): BigInteger {
        return BigInteger(arithmetic.shiftLeft(this.magnitude, places), sign)
    }

    override infix fun shr(places: Int): BigInteger {
        return BigInteger(arithmetic.shiftRight(this.magnitude, places), sign)
    }

    override operator fun unaryMinus(): BigInteger = negate()

    override operator fun plus(other: BigInteger): BigInteger {
        return add(other)
    }

    override operator fun minus(other: BigInteger): BigInteger {
        return subtract(other)
    }

    override operator fun times(other: BigInteger): BigInteger {
        return multiply(other)
    }

    override operator fun div(other: BigInteger): BigInteger {
        return divide(other)
    }

    override operator fun rem(other: BigInteger): BigInteger {
        return remainder(other)
    }

    operator fun dec(): BigInteger {
        return this - 1
    }

    operator fun inc(): BigInteger {
        return this + 1
    }

    infix fun divrem(other: BigInteger): QuotientAndRemainder {
        val result = divideAndRemainder(other)
        return QuotientAndRemainder(result.first, result.second)
    }

    override infix fun and(other: BigInteger): BigInteger {
        return BigInteger(arithmetic.and(this.magnitude, other.magnitude), sign)
    }

    override infix fun or(other: BigInteger): BigInteger {
        return BigInteger(arithmetic.or(this.magnitude, other.magnitude), sign)
    }

    override infix fun xor(other: BigInteger): BigInteger {
        return BigInteger(arithmetic.xor(this.magnitude, other.magnitude), sign)
    }

    /**
     * Inverts only up to chosen [arithmetic] [BigIntegerArithmetic.bitLength] bits.
     * This is different from Java biginteger which returns inverse in two's complement.
     *
     * I.e.: If the number was "1100" binary, not returns "0011" => "11" => 4 decimal
     */
    override fun not(): BigInteger {
        return BigInteger(arithmetic.not(this.magnitude), sign)
    }

    override fun compareTo(other: Any): Int {
        return when (other) {
            is BigInteger -> compare(other)
            is Long -> compare(BigInteger.fromLong(other))
            is Int -> compare(BigInteger.fromInt(other))
            is Short -> compare(BigInteger.fromShort(other))
            is Byte -> compare(BigInteger.fromByte(other))
            else -> throw RuntimeException("Invalid comparison type for BigInteger: ${other::class.simpleName}")
        }

    }

    override fun equals(other: Any?): Boolean {
        val comparison = when (other) {
            is BigInteger -> compare(other)
            is Long -> compare(BigInteger.fromLong(other))
            is Int -> compare(BigInteger.fromInt(other))
            is Short -> compare(BigInteger.fromShort(other))
            is Byte -> compare(BigInteger.fromByte(other))
            else -> -1
        }
        return comparison == 0
    }

    override fun hashCode(): Int {
        return magnitude.contentHashCode() + sign.hashCode()
    }

    override fun toString(): String {
        //TODO think about limiting the size of string, and offering a stream of characters instead of huge strings
//        if (stringRepresentation == null) {
//            stringRepresentation = toString(10)
//        }
//        return stringRepresentation!!

        //Linux build complains about mutating a frozen object, let's try without this representation caching
        return toString(10)
    }

    override fun toString(base: Int): String {
        val sign = if (sign == Sign.NEGATIVE) {
            "-"
        } else {
            ""
        }
        return sign + arithmetic.toString(this.magnitude, base)
    }

    data class QuotientAndRemainder(val quotient: BigInteger, val remainder: BigInteger)

    data class SqareRootAndRemainder(val squareRoot: BigInteger, val remainder: BigInteger)

    //
    //
    // ----------------- Interop with basic types ----------------------
    //
    //


    // ------------- Addition -----------


    override operator fun plus(int: Int): BigInteger {
        return this.plus(BigInteger.fromInt(int))
    }


    override operator fun plus(long: Long): BigInteger {
        return this.plus(BigInteger.fromLong(long))
    }


    override operator fun plus(short: Short): BigInteger {
        return this.plus(BigInteger.fromShort(short))
    }


    override operator fun plus(byte: Byte): BigInteger {
        return this.plus(BigInteger.fromByte(byte))
    }

    // ------------- Multiplication -----------


    override operator fun times(int: Int): BigInteger {
        return this.multiply(BigInteger.fromInt(int))
    }


    override operator fun times(long: Long): BigInteger {
        return this.multiply(BigInteger.fromLong(long))
    }


    override operator fun times(short: Short): BigInteger {
        return this.multiply(BigInteger.fromShort(short))
    }


    override operator fun times(byte: Byte): BigInteger {
        return this.multiply(BigInteger.fromByte(byte))
    }


    //TODO eh
    override operator fun times(char: Char): String {
        if (this < 0) {
            throw RuntimeException("Char cannot be multiplied with negative number")
        }
        var counter = this
        val stringBuilder = StringBuilder()
        while (counter > 0) {
            stringBuilder.append(char)
            counter--
        }
        return stringBuilder.toString()
    }

    // ------------- Subtraction -----------


    override operator fun minus(int: Int): BigInteger {
        return this.minus(BigInteger.fromInt(int))
    }


    override operator fun minus(long: Long): BigInteger {
        return this.minus(BigInteger.fromLong(long))
    }


    override operator fun minus(short: Short): BigInteger {
        return this.minus(BigInteger.fromShort(short))
    }


    override operator fun minus(byte: Byte): BigInteger {
        return this.minus(BigInteger.fromByte(byte))
    }

    // ------------- Division -----------


    override operator fun div(int: Int): BigInteger {
        return this.div(BigInteger.fromInt(int))
    }


    override operator fun div(long: Long): BigInteger {
        return this.div(BigInteger.fromLong(long))
    }


    override operator fun div(short: Short): BigInteger {
        return this.div(BigInteger.fromShort(short))
    }


    override operator fun div(byte: Byte): BigInteger {
        return this.div(BigInteger.fromByte(byte))
    }


    override operator fun rem(int: Int): BigInteger {
        return this.rem(BigInteger.fromInt(int))
    }


    override operator fun rem(long: Long): BigInteger {
        return this.rem(BigInteger.fromLong(long))
    }


    override operator fun rem(short: Short): BigInteger {
        return this.rem(BigInteger.fromShort(short))
    }


    override operator fun rem(byte: Byte): BigInteger {
        return this.rem(BigInteger.fromByte(byte))
    }

}