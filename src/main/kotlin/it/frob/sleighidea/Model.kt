// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea

import kotlin.reflect.typeOf

enum class Endianness {
    BIG, LITTLE, EXTERNAL, DEFAULT, UNKNOWN
}

enum class NumericBase {
    BIN, DEC, HEX
}

open class ExternalDefinition<T>(val name: String, val value: T)

class StringDefinition(name: String, value: String) : ExternalDefinition<String>(name, value)

class IntegerDefinition(name: String, value: Int) : ExternalDefinition<Int>(name, value)
