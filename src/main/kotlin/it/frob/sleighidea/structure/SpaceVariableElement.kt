// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.structure

import it.frob.sleighidea.psi.SleighSymbol
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.util.PlatformIcons
import com.intellij.psi.NavigatablePsiElement

/**
 * Structure view element wrapper for memory space variables.
 *
 * @param variable the [SleighSymbol] instance to wrap.
 * @constructor Create a `SpaceVariableElement` wrapping the given [SleighSymbol] instance.
 */
class SpaceVariableElement(private val variable: SleighSymbol) : StructureViewTreeElement, SortableTreeElement {

    override fun getValue(): Any = variable

    override fun getAlphaSortKey(): String = variable.value

    override fun getPresentation(): ItemPresentation =
        PresentationData(variable.value, variable.containingFile?.name ?: "", PlatformIcons.VARIABLE_ICON, null)

    override fun getChildren(): Array<TreeElement> = TreeElement.EMPTY_ARRAY

    override fun navigate(requestFocus: Boolean) {
        (variable as NavigatablePsiElement).navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = (variable as NavigatablePsiElement).canNavigate()

    override fun canNavigateToSource(): Boolean = (variable as NavigatablePsiElement).canNavigateToSource()
}