// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.intention

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.elementType
import it.frob.sleighidea.model.NumericBase
import it.frob.sleighidea.psi.*

private fun convertInteger(element: SleighInteger): Pair<NumericBase, Int> {
    return when {
        element.hexnumber != null -> Pair(NumericBase.HEX, element.toInteger()!!)
        element.decnumber != null -> Pair(NumericBase.DEC, element.toInteger()!!)
        element.binnumber != null -> Pair(NumericBase.BIN, element.toInteger()!!)
        else -> throw RuntimeException("Invalid number format.")
    }
}

private fun getNumberElement(element: PsiElement): Pair<NumericBase, Int>? = when {
    TokenSet.create(SleighTypes.HEXNUMBER, SleighTypes.DECNUMBER, SleighTypes.BINNUMBER)
        .contains(element.elementType) -> getNumberElement(element.parent)
    element is SleighInteger && !element.isExternal -> convertInteger(element)
    else -> null
}

abstract class BaseConversionIntention : PsiElementBaseIntentionAction(), IntentionAction

class ConvertToDecimalIntention : BaseConversionIntention() {
    override fun getText(): String = "Convert to decimal"

    override fun getFamilyName(): String = "Convert to decimal"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
        getNumberElement(element)?.let { number ->
            return number.first != NumericBase.DEC
        } ?: run {
            return false
        }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        getNumberElement(element)?.let { number ->
            element.parent.replace(SleighElementFactory.createSleighInteger(project, number.second, NumericBase.DEC))
        }
    }
}

class ConvertToHexadecimalIntention : BaseConversionIntention() {
    override fun getText(): String = "Convert to hexadecimal"

    override fun getFamilyName(): String = "Convert to hexadecimal"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
        getNumberElement(element)?.let { number ->
            return number.first != NumericBase.HEX
        } ?: run {
            return false
        }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        getNumberElement(element)?.let { number ->
            element.parent.replace(SleighElementFactory.createSleighInteger(project, number.second, NumericBase.HEX))
        }
    }
}

class ConvertToBinaryIntention : BaseConversionIntention() {
    override fun getText(): String = "Convert to binary"

    override fun getFamilyName(): String = "Convert to binary"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean =
        getNumberElement(element)?.let { number ->
            return number.first != NumericBase.BIN
        } ?: run {
            return false
        }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        getNumberElement(element)?.let { number ->
            element.parent.replace(SleighElementFactory.createSleighInteger(project, number.second, NumericBase.BIN))
        }
    }
}
