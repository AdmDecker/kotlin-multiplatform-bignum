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

package com.ionspin.kotlin.bignum

import com.ionspin.kotlin.bignum.integer.TypeHelper
import com.ionspin.kotlin.bignum.integer.WordArray

/**
 * Created by Ugljesa Jovanovic
 * ugljesa.jovanovic@ionspin.com
 * on 20-Oct-2019
 */

@ExperimentalUnsignedTypes
fun ULongArray.toProperType(): WordArray {
    if ((TypeHelper.instance as Any) is ULongArray) {
        return this as WordArray
    }
    if ((TypeHelper.instance as Any) is List<*>) {
        return this.toList() as WordArray
    }
    throw RuntimeException("Invalid WordArray type")
}

@ExperimentalUnsignedTypes
fun List<ULong>.toProperType(): WordArray {
    if ((TypeHelper.instance as Any) is ULongArray) {
        return this as WordArray
    }
    if ((TypeHelper.instance as Any) is List<*>) {
        return this.toList() as WordArray
    }
    throw RuntimeException("Invalid WordArray type")
}

@ExperimentalUnsignedTypes
fun List<ULong>.contentEquals(other : List<ULong>): Boolean {
    return this == other
}