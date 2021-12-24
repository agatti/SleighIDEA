// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.structure

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.util.PlatformIcons
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.util.PsiTreeUtil
import it.frob.sleighidea.psi.*

/**
 * Structure view element wrapper for memory spaces.
 *
 * @param space the [SleighSpaceDefinition] instance to wrap.
 * @constructor Create a `SpaceViewElement` wrapping the given [SleighSpaceDefinition] instance.
 */
class SpaceViewElement(private val space: SleighSpaceDefinition) : StructureViewTreeElement, SortableTreeElement {

    override fun getValue(): Any = space

    override fun getAlphaSortKey(): String = space.name!!

    override fun getPresentation(): ItemPresentation = space.presentation

    override fun getChildren(): Array<TreeElement> =
        PsiTreeUtil.collectElementsOfType(space.containingFile, SleighVariablesNodeDefinition::class.java)
            .filter { node: SleighVariablesNodeDefinition -> node.space.value == space.name }
            .map { obj: SleighVariablesNodeDefinition -> obj.symbolOrWildcardList }
            .flatten()
            .mapNotNull { variable: SleighSymbolOrWildcard ->
                variable.symbol?.let { symbol -> SpaceVariableElement(symbol) }
            }
            .toTypedArray()

    override fun navigate(requestFocus: Boolean) {
        (space as NavigatablePsiElement).navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = (space as NavigatablePsiElement).canNavigate()

    override fun canNavigateToSource(): Boolean = (space as NavigatablePsiElement).canNavigateToSource()
}

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

