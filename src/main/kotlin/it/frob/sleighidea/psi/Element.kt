// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.ide.projectView.PresentationData
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.tree.IElementType
import com.intellij.usageView.UsageViewUtil
import it.frob.sleighidea.SleighLanguage
import org.jetbrains.annotations.NonNls

/**
 * Interface for Sleigh elements that may or may not contain sub-elements.
 */
interface SleighCompositeElement : NavigatablePsiElement

interface SleighNamedElement : SleighCompositeElement, PsiNameIdentifierOwner

class SleighCompositeElementType(debugName: @NonNls String) : IElementType(debugName, SleighLanguage.INSTANCE)

class SleighElementType(debugName: @NonNls String) : IElementType(debugName, SleighLanguage.INSTANCE)

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

abstract class SleighNamedElementImpl(node: ASTNode) : SleighCompositeElementImpl(node), SleighNamedElement {
    override fun setName(name: String): PsiElement = this
}
