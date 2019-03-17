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

package com.ionspin.kotlin.biginteger.concurrent

import com.ionspin.kotlin.biginteger.base32.BigInteger32Arithmetic
import com.ionspin.kotlin.biginteger.util.block
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

/**
 * Created by Ugljesa Jovanovic
 * ugljesa.jovanovic@ionspin.com
 * on 17-Mar-3/17/19
 */
suspend fun concurrentMultiply() {
//    if (BigInteger32Arithmetic.useCoroutines) {
//        val partialResults = second.mapIndexed { index, element ->
//            GlobalScope.async {
//                BigInteger32Arithmetic.multiply(first, element) shl (index * BigInteger32Arithmetic.basePowerOfTwo)
//            }
//        }
//
//
//        var result = uintArrayOf()
//        block {
//            partialResults.awaitAll()
//            result = partialResults.fold(UIntArray(0)) { acc, deferred ->
//                acc + (deferred.getCompleted())
//            }
//        }
//        return result
//    } else {
}