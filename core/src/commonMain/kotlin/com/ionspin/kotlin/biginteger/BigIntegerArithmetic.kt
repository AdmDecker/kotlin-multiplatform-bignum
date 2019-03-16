package com.ionspin.kotlin.biginteger

/**
 * Created by Ugljesa Jovanovic
 * ugljesa.jovanovic@ionspin.com
 * on 10-Mar-3/10/19
 */
interface BigIntegerArithmetic<BackingCollectionType, BackingWordType> {
    val ZERO : BackingCollectionType
    val ONE : BackingCollectionType
    val basePowerOfTwo: Int
    /**
     * Hackers delight 5-11
     */
    fun numberOfLeadingZeroes(value: BackingWordType): Int
    fun bitLength(value: BackingCollectionType): Int
    fun shiftLeft(operand: BackingCollectionType, places: Int): BackingCollectionType
    fun shiftRight(operand: BackingCollectionType, places: Int): BackingCollectionType
    fun compare(first: BackingCollectionType, second: BackingCollectionType): Int
    fun add(first: BackingCollectionType, second: BackingCollectionType): BackingCollectionType
    fun substract(first: BackingCollectionType, second: BackingCollectionType): BackingCollectionType
    fun multiply(first: BackingCollectionType, second: BackingCollectionType): BackingCollectionType
    fun divide(first: BackingCollectionType, second: BackingCollectionType): Pair<BackingCollectionType, BackingCollectionType>
    fun parseForBase(number : String, base : Int) : BackingCollectionType
    fun toString(operand: BackingCollectionType, base : Int) : String


}

