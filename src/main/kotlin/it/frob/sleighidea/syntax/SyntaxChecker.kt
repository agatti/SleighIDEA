// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.syntax

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import it.frob.sleighidea.psi.*
import java.io.InvalidClassException
import kotlin.math.abs
import kotlin.math.pow

fun lookupInteger(element: SleighInteger): Int? = if (element.isExternal) {
    (element.containingFile as SleighFile).defines.takeWhile { define ->
        define.textRange.endOffset < element.textOffset
    }.find { define -> define.name == element.externalDefinition!!.symbolString.text }?.let { define ->
        define.defineValue.strictInteger?.let { integer ->
            return integer.toInt()
        } ?: run {
            throw InvalidClassException("Invalid definition content.")
        }
    }
    null
} else element.strictInteger!!.toInt()

class SyntaxChecker(private val root: PsiElement, holder: AnnotationHolder) : SyntaxHighlightingVisitor(holder) {

    private val availableSpaces: List<SleighSpaceDefinition> by lazy {
        PsiTreeUtil.collectElementsOfType(
            root.containingFile,
            SleighSpaceDefinition::class.java
        ).toList()
    }

    private val availableTokens: List<SleighTokenDefinition> by lazy {
        PsiTreeUtil.collectElementsOfType(
            root.containingFile,
            SleighTokenDefinition::class.java
        ).toList()
    }

    private val availableTokenFields: List<SleighTokenFieldDefinition> by lazy {
        availableTokens.map { token -> token.tokenFieldDefinitionList }
            .flatten()
            .toList()
    }

    private val availableVariables: List<SleighSymbolOrWildcard> by lazy {
        PsiTreeUtil.collectElementsOfType(root.containingFile, SleighVariablesNodeDefinition::class.java)
            .map { node -> node.symbolOrWildcardList }
            .flatten()
            .filter { symbol -> symbol.symbol != null }
            .toList()
    }

    override fun visitSpaceDefinition(visited: SleighSpaceDefinition) {
        super.visitSpaceDefinition(visited)

        val knownSpaces =
            availableSpaces.takeWhile { token -> (token.textOffset + token.textLength) < visited.textOffset }
                .map { token -> token.name }
                .toList()

        if (knownSpaces.contains(visited.name)) {
            markElementAsError(visited.symbol, holder, "Memory space \"${visited.name}\" already defined.")
            return
        }

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

        val firstDefaultSpace =
            availableSpaces.takeWhile { token -> (token.textOffset + token.textLength) < visited.textOffset }
                .firstOrNull { space -> space.default.isNotEmpty() }

        if (visited.default.isNotEmpty() && firstDefaultSpace != null && visited != firstDefaultSpace) {
            markElementAsError(visited.default.first(), holder, "There is already another default memory space set.")
        }
    }

    override fun visitAlignmentDefinition(visited: SleighAlignmentDefinition) {
        super.visitAlignmentDefinition(visited)

        PsiTreeUtil.findChildOfType(root.containingFile, SleighAlignmentDefinition::class.java)
            ?.let { firstDefinedAlignment ->
                if (firstDefinedAlignment != visited) {
                    markElementAsError(
                        visited,
                        holder,
                        "There is already another alignment directive being defined."
                    )
                    return
                }
            }

        try {
            lookupInteger(visited.integer)?.let { integer ->
                if (integer <= 0) {
                    markElementAsError(visited.integer, holder, "Alignment must be greater than zero.")
                }
            }
        } catch (exception: InvalidClassException) {
            markElementAsError(visited.integer, holder, "Invalid definition type.")
        }
    }

    override fun visitVariablesNodeDefinition(visited: SleighVariablesNodeDefinition) {
        super.visitVariablesNodeDefinition(visited)

        visited.size.toInteger()?.let { integer ->
            if (integer <= 0) {
                markElementAsError(
                    visited.size.textOffset,
                    visited.size.textOffset + visited.size.textLength,
                    holder,
                    "Variables' size must be greater than zero."
                )
            }
        }

        visited.offset.toInteger()?.let { integer ->
            if (integer < 0) {
                markElementAsError(
                    visited.offset.textOffset,
                    visited.offset.textOffset + visited.offset.textLength,
                    holder,
                    "Variables' offset must not be negative."
                )
            }
        }

        val seenVariablesList =
            availableVariables.takeWhile { variable -> (variable.textOffset + variable.textLength) < visited.textOffset }
                .mapNotNull { symbol -> symbol.symbol?.value }
                .toMutableList()

        visited.symbolOrWildcardList.mapNotNull { item -> item.symbol }
            .forEach { symbol ->
                val variableName = symbol.value

                if (seenVariablesList.contains(variableName)) {
                    markElementAsError(symbol, holder, "Variable \"$variableName\" already defined.")
                } else {
                    seenVariablesList.add(variableName)
                }
            }

        val knownSpaces =
            availableSpaces.takeWhile { token -> (token.textOffset + token.textLength) < visited.textOffset }
                .map { token -> token.name }
                .toList()

        if (!knownSpaces.contains(visited.symbol.value)) {
            markElementAsError(visited.symbol, holder, "Unknown memory space \"${visited.symbol.value}\".")
        }
    }

    override fun visitTokenDefinition(visited: SleighTokenDefinition) {
        super.visitTokenDefinition(visited)

        visited.size?.let { size ->
            if (size < 0) {
                markElementAsError(visited.integer, holder, "Token size must be zero or greater.")
            }
        }

        val seenTokens = availableTokens.asSequence()
            .takeWhile { token -> (token.textOffset + token.textLength) < visited.textOffset }
            .map { token -> token.name }
            .toList()

        if (seenTokens.contains(visited.name)) {
            markElementAsError(visited.symbol, holder, "Token \"${visited.name}\" already defined.")
        }

        val seenFields = availableTokens.asSequence()
            .takeWhile { token -> (token.textOffset + token.textLength) < visited.textOffset }
            .map { token -> token.tokenFieldDefinitionList }
            .flatten()
            .map { token -> token.symbol.value }
            .toList()

        visited.tokenFieldDefinitionList.forEach { field ->
            checkTokenFieldDefinition(visited, field, seenFields)
        }
    }

    override fun visitVariableAttach(visited: SleighVariableAttach) {
        super.visitVariableAttach(visited)

        checkSymbolListAssignment(visited, visited.symbolList, visited.symbolOrWildcardList.size.toULong())

        val seenVariablesList =
            availableVariables.takeWhile { variable -> (variable.textOffset + variable.textLength) < visited.textOffset }
                .mapNotNull { symbol -> symbol.symbol?.value }
                .toList()

        visited.symbolOrWildcardList
            .mapNotNull { symbol -> symbol.symbol }
            .filter { symbol -> !symbol.isExternal }
            .forEach { symbol ->
                if (!seenVariablesList.contains(symbol.value)) {
                    markElementAsError(symbol, holder, "Unknown variable \"${symbol.value}\".")
                }
            }
    }

    override fun visitValueAttach(visited: SleighValueAttach) {
        super.visitValueAttach(visited)

        checkSymbolListAssignment(visited, visited.symbolList, visited.integerOrWildcardList.size.toULong())
    }

    override fun visitNameAttach(visited: SleighNameAttach) {
        super.visitNameAttach(visited)

        checkSymbolListAssignment(visited, visited.symbolList, visited.nameOrWildcardList.size.toULong())
    }

    private fun checkTokenFieldDefinition(
        token: SleighTokenDefinition,
        field: SleighTokenFieldDefinition,
        seenFields: List<String>
    ) {
        if (seenFields.contains(field.symbol.value)) {
            markElementAsError(field, holder, "Field \"${field.symbol.value}\" already defined.")
        }

        if (!field.bitStart.isExternal && field.bitStart.toInteger()!! < 0) {
            markElementAsError(field.bitStart, holder, "Bit extents must not be negative.")
        }

        if (!field.bitEnd.isExternal && field.bitEnd.toInteger()!! < 0) {
            markElementAsError(field.bitEnd, holder, "Bit extents must not be negative.")
        }

        if (!field.bitStart.isExternal && !field.bitEnd.isExternal &&
            field.bitStart.toInteger()!! > field.bitEnd.toInteger()!!
        ) {
            markElementAsError(field.bitStart, field.bitEnd, holder, "Invalid bit extent order definition.")
        }

        token.integer.toInteger()?.let { tokenBits ->
            val extentBits = tokenBits - 1

            if (!field.bitStart.isExternal) {
                val startBit = field.bitStart.toInteger()!!

                if (startBit > extentBits) {
                    markElementAsError(field.bitStart, holder, "Bit extent start $startBit is out of bounds.")
                }
            }

            if (!field.bitEnd.isExternal) {
                val endBit = field.bitEnd.toInteger()!!

                if (endBit > extentBits) {
                    markElementAsError(field.bitEnd, holder, "Bit extent end $endBit is out of bounds.")
                }
            }
        }

        field.hexElements.drop(1).forEach { element ->
            markElementAsError(element, holder, "Token field already has a base 16 modified.")
        }

        field.decElements.drop(1).forEach { element ->
            markElementAsError(element, holder, "Token field already has a base 10 modifier.")
        }

        field.signedElements.drop(1).forEach { element ->
            markElementAsError(element, holder, "Token field already has signed modifier.")
        }

        var hexSeen = false
        var decSeen = false
        field.tokenFieldModifierList.filter { modifier -> modifier.keyHex != null || modifier.keyDec != null }
            .forEach { modifier ->
                if (modifier.keyHex != null) {
                    if (decSeen) {
                        markElementAsError(
                            field.hexElements.first(),
                            holder,
                            "Cannot have both base 16 and base 10 modifiers."
                        )
                        return@forEach
                    }

                    hexSeen = true
                } else {
                    if (hexSeen) {
                        markElementAsError(
                            field.decElements.first(),
                            holder,
                            "Cannot have both base 16 and base 10 modifiers."
                        )
                        return@forEach
                    }

                    decSeen = true
                }
            }
    }

    private fun checkSymbolListAssignment(element: PsiElement, symbolList: List<SleighSymbol>, valuesLength: ULong) {
        val availableFields =
            availableTokenFields
                .asSequence()
                .map { field -> field.symbol }
                .takeWhile { symbol -> (symbol.textOffset + symbol.textLength) < element.textOffset }
                .filter { symbol -> !symbol.isExternal }
                .map { symbol -> symbol.value }
                .toList()

        symbolList.forEach { symbol ->
            if (symbol.isExternal || !availableFields.contains(symbol.value)) {
                markElementAsError(symbol, holder, "Unknown field \"${symbol.value}\".")
                return@forEach
            }

            availableTokenFields.find { field -> field.symbol.value == symbol.value }?.let { field ->
                if (field.bitStart.isExternal || field.bitEnd.isExternal) {
                    return@forEach
                }

                val fieldSize = abs(field.bitEnd.toInteger()!! - field.bitStart.toInteger()!!) + 1
                val expectedValuesLength = 2.toDouble().pow(fieldSize).toULong()
                if (expectedValuesLength != valuesLength) {
                    markElementAsError(
                        symbol, holder,
                        "Field \"${symbol.value}\" has the wrong size ($expectedValuesLength vs $valuesLength)."
                    )
                }
            } ?: run {
                throw RuntimeException("Cannot find field for \"${symbol.value}\".")
            }
        }
    }
}
