// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.highlighting

import com.intellij.grazie.utils.toLinkedSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import it.frob.sleighidea.psi.SleighAlignmentDefinition
import it.frob.sleighidea.psi.SleighSpaceDefinition
import it.frob.sleighidea.psi.SleighVariablesNodeDefinition

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

    private val firstDefaultSpace: SleighSpaceDefinition? by lazy {
        availableSpaces.first { space -> space.default.isNotEmpty() }
    }

    private val seenVariables: List<String> by lazy {
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
                second.integer?.let { integer ->
                    if (integer.toInteger() <= 0) {
                        markElementAsError(first, holder, "Memory space size must be greater than zero.")
                    }
                }
            }
            else -> visited.size.drop(1).forEach { element ->
                markElementAsError(element.first, holder, "Memory space already has a size modifier set.")
            }
        }

        if (visited.wordSize.size == 1) visited.wordSize.first().run {
            second.integer?.let { integer ->
                if (integer.toInteger() <= 0) {
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

        visited.integerValue.integer?.let { integer ->
            if (integer.toInteger() <= 0) {
                markElementAsError(visited.integerValue.parent, holder, "Alignment must be greater than zero.")
            }
        }
    }

    override fun visitVariablesNodeDefinition(visited: SleighVariablesNodeDefinition) {
        super.visitVariablesNodeDefinition(visited)

        visited.size!!.integer?.let { integer ->
            if (integer.toInteger() <= 0) {
                markElementAsError(
                    visited.keySize.textOffset,
                    visited.size!!.textOffset + visited.size!!.textLength,
                    holder,
                    "Variables' size must be greater than zero."
                )
            }
        }

        val seenVariablesList = seenVariables.toMutableList()

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
}
