// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement

class LanguageAnnotator : Annotator, DumbAware {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        element.accept(SyntaxChecker(holder))
    }
}
