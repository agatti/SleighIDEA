// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.ide.projectView.PresentationData
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.usageView.UsageViewUtil
import it.frob.sleighidea.psi.SleighCompositeElement

open class SleighCompositeElementImpl(node: ASTNode?) : ASTWrapperPsiElement(node!!), SleighCompositeElement {

    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean = super.processDeclarations(processor, state, lastParent, place)

    override fun getPresentation(): ItemPresentation = PresentationData(
        UsageViewUtil.createNodeText(this), containingFile.name,
        getIcon(0), null
    )

    companion object {
        fun processDeclarations(
            element: PsiElement,
            processor: PsiScopeProcessor,
            state: ResolveState,
            lastParent: PsiElement?,
            place: PsiElement
        ): Boolean = processor.execute(element, state) && processChildren(element, processor, state, lastParent, place)

        fun processChildren(
            element: PsiElement, processor: PsiScopeProcessor,
            substitutor: ResolveState, lastParent: PsiElement?,
            place: PsiElement
        ): Boolean {
            var run = if (lastParent == null) element.lastChild else lastParent.prevSibling
            while (run != null) {
                if (run is SleighCompositeElement && !run.processDeclarations(processor, substitutor, null, place)) {
                    return false
                }
                run = run.prevSibling
            }

            return true
        }
    }
}
