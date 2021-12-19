// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.structure

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.util.PsiTreeUtil
import it.frob.sleighidea.SleighIcons
import it.frob.sleighidea.psi.*

/**
 * Create a new structure view element wrapping the given PSI element.
 *
 * @param element the PSI element to wrap.
 */
class ViewElement(private val element: NavigatablePsiElement) : StructureViewTreeElement, SortableTreeElement {

    override fun getValue(): Any = element

    override fun getAlphaSortKey(): String = element.name ?: ""

    override fun getPresentation(): ItemPresentation =
        element.presentation?.let { presentation -> return presentation } ?: run {
            return if (element is SleighCtorstart && element.getFirstChild() is SleighDisplay) {
                return PresentationData(
                    (element.getFirstChild() as SleighDisplay).placeholderText,
                    "",
                    SleighIcons.TABLE,
                    null
                )
            } else {
                PresentationData()
            }
        }

    override fun getChildren(): Array<TreeElement> {
        if (element is SleighFile) {
            val file = element
            val viewElements: MutableList<StructureViewTreeElement> = ArrayList()
            viewElements.addAll(file.spaces
                .map { space: SleighSpaceDefinition? ->
                    SpaceViewElement(
                        space!!
                    )
                }
                .toList())
            viewElements.addAll(file.tokens
                .map { token: SleighTokenDefinition -> TokenViewElement(token) }
                .toList())
            viewElements.addAll(file.macros
                .map { macro: SleighMacrodef -> ViewElement(macro as NavigatablePsiElement) }
                .toList())
            viewElements.addAll(PsiTreeUtil.collectElementsOfType(element, SleighCtorstart::class.java)
                .filter { constructor: SleighCtorstart -> constructor.firstChild is SleighDisplay }
                .map { constructor: SleighCtorstart -> ViewElement(constructor as NavigatablePsiElement) }
                .toList())
            return viewElements.toTypedArray()
        }
        return emptyArray()
    }

    override fun navigate(requestFocus: Boolean) {
        element.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = element.canNavigate()

    override fun canNavigateToSource(): Boolean = element.canNavigateToSource()
}