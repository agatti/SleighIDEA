// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.highlighting

import com.intellij.grazie.utils.toLinkedSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import it.frob.sleighidea.psi.*
import kotlin.math.abs
import kotlin.math.pow

class SyntaxChecker(root: PsiElement, holder: AnnotationHolder) : SyntaxHighlighter(holder) {

    private val availableSpaces: List<SleighSpaceDefinition> by lazy {
        PsiTreeUtil.collectElementsOfType(
            root.containingFile,
            SleighSpaceDefinition::class.java
        ).toList()
    }

    private val availableSpaceNames: List<String> by lazy {
        availableSpaces.map { space -> space.name }.toList()
    }

    private val availableTokens: List<SleighTokenDefinition> by lazy {
        PsiTreeUtil.collectElementsOfType(
            root.containingFile,
            SleighTokenDefinition::class.java
        ).toList()
    }

    private val definedTokens: Map<String, List<SleighTokenDefinition>> by lazy {
        val tokens: MutableMap<String, MutableList<SleighTokenDefinition>> = mutableMapOf()

        availableTokens.forEach { token ->
            if (tokens.contains(token.name)) {
                tokens[token.name]!!.add(token)
            } else {
                tokens[token.name] = mutableListOf()
            }
        }

        tokens
    }

    private val availableTokenFields: List<SleighTokenFieldDefinition> by lazy {
        availableTokens.map { token -> token.tokenFieldDefinitionList }
            .flatten()
            .toList()
    }

    private val availableTokenFieldsMap: Map<String, SleighTokenFieldDefinition> by lazy {
        val fields: MutableMap<String, SleighTokenFieldDefinition> = mutableMapOf()

        availableTokenFields.forEach { field ->
            if (!fields.contains(field.symbol.value)) {
                fields[field.symbol.value] = field
            }
        }

        fields
    }

    private val definedTokenFields: Map<String, List<SleighTokenFieldDefinition>> by lazy {
        val fields: MutableMap<String, MutableList<SleighTokenFieldDefinition>> = mutableMapOf()

        availableTokenFields.forEach { field ->
            if (fields.contains(field.symbol.value)) {
                fields[field.symbol.value]!!.add(field)
            } else {
                fields[field.symbol.value] = mutableListOf()
            }
        }

        fields
    }

    private val firstDefaultSpace: SleighSpaceDefinition? by lazy {
        availableSpaces.first { space -> space.default.isNotEmpty() }
    }

    private val definedVariables: List<String> by lazy {
        PsiTreeUtil.collectElementsOfType(root.containingFile, SleighVariablesNodeDefinition::class.java)
            .map { node -> node.symbolOrWildcardList }
            .flatten()
            .mapNotNull { symbol -> symbol.symbol?.value }
            .toLinkedSet()
            .toList()
    }

    override fun visitSpaceDefinition(visited: SleighSpaceDefinition) {
        super.visitSpaceDefinition(visited)

        when (visited.size.size) {
            0 -> markElementAsError(visited.parent, holder, "Memory space must have a size modifier.")
            1 -> visited.size.first().run {
                second.toInteger()?.let { integer ->
                    if (integer <= 0) {
                        markElementAsError(first, holder, "Memory space size must be greater than zero.")
                    }
                }
            }
            else -> visited.size.drop(1).forEach { element ->
                markElementAsError(element.first, holder, "Memory space already has a size modifier set.")
            }
        }

        if (visited.wordSize.size == 1) visited.wordSize.first().run {
            second.toInteger()?.let { integer ->
                if (integer <= 0) {
                    markElementAsError(first, holder, "Memory space word size must be greater than zero.")
                }
            }
        }
        else visited.wordSize.drop(1).forEach { element ->
            markElementAsError(element.first, holder, "Memory space already has a word size modifier set.")
        }

        visited.type.drop(1).forEach { element ->
            markElementAsError(element.first, holder, "Memory space already has a type set.")
        }

        visited.default.drop(1).forEach { element ->
            markElementAsError(element, holder, "Memory space already has a default flag set.")
        }

        if (visited.default.isNotEmpty() && firstDefaultSpace != null && visited != firstDefaultSpace) {
            markElementAsError(visited.default.first(), holder, "There is already another default memory space set.")
        }
    }

    override fun visitAlignmentDefinition(visited: SleighAlignmentDefinition) {
        super.visitAlignmentDefinition(visited)

        visited.positiveIntegerValue.toInteger()?.let { integer ->
            if (integer <= 0) {
                markElementAsError(visited.positiveIntegerValue.parent, holder, "Alignment must be greater than zero.")
            }
        }
    }

    override fun visitVariablesNodeDefinition(visited: SleighVariablesNodeDefinition) {
        super.visitVariablesNodeDefinition(visited)

        visited.size!!.positiveInteger?.let { integer ->
            if (integer.toInteger() <= 0) {
                markElementAsError(
                    visited.keySize.textOffset,
                    visited.size!!.textOffset + visited.size!!.textLength,
                    holder,
                    "Variables' size must be greater than zero."
                )
            }
        }

        val seenVariablesList = definedVariables.toMutableList()

        visited.symbolOrWildcardList.mapNotNull { item -> item.symbol }
            .forEach { symbol ->
                val variableName = symbol.value

                if (seenVariablesList.contains(variableName)) {
                    seenVariablesList.remove(variableName)
                } else {
                    markElementAsError(symbol, holder, "Variable \"$variableName\" already defined.")
                }
            }

        if (!availableSpaceNames.contains(visited.symbol.value)) {
            markElementAsError(visited.symbol, holder, "Unknown memory space \"${visited.symbol.value}\".")
        }
    }

    override fun visitTokenDefinition(visited: SleighTokenDefinition) {
        super.visitTokenDefinition(visited)

        definedTokens[visited.name]?.let { duplicates ->
            if (duplicates.contains(visited)) {
                markElementAsError(visited.symbol, holder, "Token \"${visited.name}\" already defined.")
            }
        }

        visited.tokenFieldDefinitionList.forEach { field ->
            checkTokenFieldDefinition(visited, field)
        }
    }

    override fun visitVariableAttach(visited: SleighVariableAttach) {
        super.visitVariableAttach(visited)

        checkSymbolListAssignment(visited.symbolList, visited.symbolOrWildcardList.size.toULong())

        visited.symbolOrWildcardList
            .mapNotNull { symbol -> symbol.symbol }
            .filter { symbol -> !symbol.isExternal }
            .forEach { symbol ->
                if (!definedVariables.contains(symbol.value)) {
                    markElementAsError(symbol, holder, "Unknown variable \"${symbol.value}\".")
                }
            }
    }

    override fun visitValueAttach(visited: SleighValueAttach) {
        super.visitValueAttach(visited)

        checkSymbolListAssignment(visited.symbolList, visited.integerOrWildcardList.size.toULong())
    }

    private fun checkTokenFieldDefinition(token: SleighTokenDefinition, field: SleighTokenFieldDefinition) {
        val fieldName = field.symbol.value

        definedTokenFields[fieldName]?.let { duplicates ->
            if (duplicates.contains(field)) {
                markElementAsError(field, holder, "Field \"$fieldName\" already defined.")
            }
        }

        if (!field.bitStart.isExternal && !field.bitEnd!!.isExternal &&
            field.bitStart.toInteger()!! > field.bitEnd!!.toInteger()!!
        ) {
            markElementAsError(field.bitStart, field.bitEnd!!, holder, "Invalid bit extent definition.")
        }

        token.positiveIntegerValue.toInteger()?.let { tokenBits ->
            val extentBits = tokenBits - 1

            if (!field.bitStart.isExternal) {
                val startBit = field.bitStart.toInteger()!!

                if (startBit > extentBits) {
                    markElementAsError(field.bitStart, holder, "Bit extent start $startBit is out of bounds.")
                }
            }

            if (!field.bitEnd!!.isExternal) {
                val endBit = field.bitEnd!!.toInteger()!!

                if (endBit > extentBits) {
                    markElementAsError(field.bitEnd!!, holder, "Bit extent end $endBit is out of bounds.")
                }
            }
        }

        field.hex.drop(1).forEach { element ->
            markElementAsError(element, holder, "Token field already has a base 16 modified.")
        }

        field.dec.drop(1).forEach { element ->
            markElementAsError(element, holder, "Token field already has a base 10 modifier.")
        }

        field.signed.drop(1).forEach { element ->
            markElementAsError(element, holder, "Token field already has signed modifier.")
        }

        var hexSeen = false
        var decSeen = false
        field.tokenFieldModifierList.filter { modifier -> modifier.keyHex != null || modifier.keyDec != null }
            .forEach { modifier ->
                if (modifier.keyHex != null) {
                    if (decSeen) {
                        field.hex.forEach { baseField ->
                            markElementAsError(baseField, holder, "Cannot have both base 16 and base 10 modifiers.")
                        }
                        return@forEach
                    }

                    hexSeen = true
                } else {
                    if (hexSeen) {
                        field.dec.forEach { baseField ->
                            markElementAsError(baseField, holder, "Cannot have both base 16 and base 10 modifiers.")
                        }
                        return@forEach
                    }

                    decSeen = true
                }
            }
    }

    private fun checkSymbolListAssignment(symbolList: List<SleighSymbol>, valuesLength: ULong) {
        symbolList.forEach { symbol ->
            availableTokenFieldsMap[symbol.value]?.let { field ->
                if (field.bitStart.isExternal || field.bitEnd!!.isExternal) {
                    return@let
                }

                val fieldSize = abs(field.bitEnd!!.toInteger()!! - field.bitStart.toInteger()!!) + 1
                val expectedValuesLength = 2.toDouble().pow(fieldSize).toULong()

                if (expectedValuesLength != valuesLength) {
                    markElementAsError(
                        symbol, holder,
                        "Field \"${symbol.value}\" has the wrong size ($expectedValuesLength vs $valuesLength)."
                    )
                }
            } ?: run {
                if (!symbol.isExternal) {
                    markElementAsError(symbol, holder, "Unknown field \"${symbol.value}\".")
                }
            }
        }
    }
}
