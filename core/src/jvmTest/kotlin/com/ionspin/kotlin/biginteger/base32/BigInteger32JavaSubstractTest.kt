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

package com.ionspin.kotlin.biginteger.base32

import org.junit.Test
import java.math.BigInteger
import java.time.Duration
import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.random.nextUInt
import kotlin.test.assertTrue

/**
 * Created by Ugljesa Jovanovic
 * ugljesa.jovanovic@ionspin.com
 * on 09-Mar-3/9/19
 */
@ExperimentalUnsignedTypes
class BigInteger32JavaSubstractTest {

    val basePower = 32

    @Test
    fun substractionTest() {
        assertTrue {
            val a = uintArrayOf(10U, 20U)
            val b = uintArrayOf(15U, 5U)
            val c = BigInteger32Arithmetic.substract(a, b)

            val resultBigInt = c.toJavaBigInteger()

            val aBigInt = a.toJavaBigInteger()
            val bBigInt = b.toJavaBigInteger()
            val cBigInt = aBigInt - bBigInt

            resultBigInt == cBigInt


        }
    }


    @Test
    fun randomSubstractTest() {
        val seed = 1
        val random = Random(seed)
        for (i in 1..Int.MAX_VALUE step 99) {
            if ((i % 100000) in 1..100) {
                println(i)
            }
            substractSingleTest(random.nextUInt(), random.nextUInt(), random.nextUInt())
        }

    }

    @Test
    fun randomSubstractLotsOfElementsTest() {
        val seed = 1
        val random = Random(seed)
        val numberOfElements = 150000
        println("Number of elements $numberOfElements")

        val lotOfElements = UIntArray(numberOfElements) {
            random.nextUInt()
        }
        substractSingleTest(*lotOfElements)
    }

    fun substractSingleTest(vararg elements: UInt) {
        assertTrue("Failed on ${elements.contentToString()}") {
            val time = elements.size > 100
            lateinit var lastTime: LocalDateTime
            lateinit var startTime: LocalDateTime

            if (time) {
                lastTime = LocalDateTime.now()
                startTime = lastTime
            }

            val first = elements.copyOfRange(0, elements.size / 2).foldIndexed(UIntArray(1) { 1U }) { index, acc, uInt ->
                BigInteger32Arithmetic.multiply(acc, uInt)
            }
            val second = elements.copyOfRange(elements.size / 2, elements.size - 1).foldIndexed(UIntArray(1) { 1U }) { index, acc, uInt ->
                BigInteger32Arithmetic.multiply(acc, uInt)
            }
            val result = BigInteger32Arithmetic.substract(first, second)

            if (time) {
                lastTime = LocalDateTime.now()
                println("Total time ${Duration.between(startTime, lastTime)}")
                startTime = lastTime
            }

            val convertedResult = result.toJavaBigInteger()
            val bigIntFirst = elements.copyOfRange(0, elements.size / 2).foldIndexed(BigInteger.ONE) { index, acc, uInt ->
                acc * BigInteger(uInt.toString(), 10)
            }
            val bigIntSecond = elements.copyOfRange(elements.size / 2, elements.size - 1).foldIndexed(BigInteger.ONE) { index, acc, uInt ->
                acc * BigInteger(uInt.toString(), 10)
            }
            val bigIntResult = bigIntFirst - bigIntSecond
            if (time) {
                println("Result ${convertedResult}")
                println("Total time ${Duration.between(startTime, lastTime)}")
            }

            bigIntResult.abs() == convertedResult
        }
    }
}