// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import it.frob.sleighidea.NumericBase
import it.frob.sleighidea.SleighFileType
import kotlin.math.absoluteValue

object SleighElementFactory {

    @JvmStatic
    fun createFile(project: Project, text: String): SleighFile {
        return PsiFileFactory.getInstance(project)
            .createFileFromText("_element_factory_.slaspec", SleighFileType.INSTANCE, text) as SleighFile
    }

    @JvmStatic
    fun createSleighInteger(project: Project, value: Int, base: NumericBase): SleighInteger {
        val valueString = when (base) {
            NumericBase.DEC -> value.absoluteValue.toString(10)
            NumericBase.BIN -> "0b${value.toString(2)}"
            NumericBase.HEX -> "0x${value.toString(16).uppercase()}"
        }
        return PsiTreeUtil.collectElementsOfType(
            createFile(
                project,
                "define alignment=${if (value < 0) "-" else ""}$valueString;"
            ), SleighInteger::class.java
        ).first()!!
    }

    @JvmStatic
    fun createSleighSymbol(project: Project, value: String): SleighSymbol {
        return PsiTreeUtil.collectElementsOfType(
            createFile(
                project,
                "define token ${value.trim()} (0);"
            ), SleighSymbol::class.java
        ).first()!!
    }

    @JvmStatic
    fun createSleighLiteralSymbol(project: Project, value: String): PsiElement {
        return PsiTreeUtil.collectElementsOfType(
            createFile(project, "@define ${value.trim()} 1"),
            PsiElement::class.java
        ).first { element ->
            element.elementType == SleighTypes.LITERALSYMBOL
        }
    }

    @JvmStatic
    fun createSleighExternalDefinition(project: Project, value: String): PsiElement {
        return PsiTreeUtil.collectElementsOfType(
            createFile(project, "define alignment=$($value);"),
            SleighExternalDefinition::class.java
        ).first()!!
    }

    @JvmStatic
    fun createSleighQuotedString(project: Project, value: String): PsiElement {
        return PsiTreeUtil.collectElementsOfType(
            createFile(project, "@define def \"${value.trim()}\""),
            SleighQuotedString::class.java
        ).first()!!
    }
}