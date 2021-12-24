// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import it.frob.sleighidea.isStandardLibraryCall

class FunctionCallReference(element: PsiElement, textRange: TextRange) :
    PsiReferenceBase<PsiElement?>(element, textRange) {
    override fun resolve(): PsiElement? = (element.containingFile as SleighFile).macros.firstOrNull { macro ->
        macro.symbol.value == (element as SleighExpressionApplyName).symbol.value
    } ?: (element.containingFile as SleighFile).pcodeops.firstOrNull { pcodeop ->
        pcodeop.symbol.value == (element as SleighExpressionApplyName).symbol.value
    }
}

class FunctionCallReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> =
        arrayOf(FunctionCallReference(element, (element as SleighExpressionApplyName).symbol.textRangeInParent))

    override fun acceptsHints(element: PsiElement, hints: PsiReferenceService.Hints): Boolean =
        !isStandardLibraryCall((element as SleighExpressionApplyName).symbol.value)
}

class SleighReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(SleighExpressionApplyName::class.java)
                .inFile(PlatformPatterns.psiFile(SleighFile::class.java)), FunctionCallReferenceProvider()
        )
    }
}
