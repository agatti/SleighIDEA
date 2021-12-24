// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import it.frob.sleighidea.SleighFileType
import it.frob.sleighidea.model.NumericBase
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
            NumericBase.HEX -> "0x${value.toString(16)}"
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
}