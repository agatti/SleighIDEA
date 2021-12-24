// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.structure

import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import it.frob.sleighidea.psi.*

/**
 * Structure view element wrapper for tokens.
 *
 * @param token the [SleighTokenDefinition] instance to wrap.
 * @constructor Create a `TokenViewElement` wrapping the given [SleighTokenDefinition] instance.
 */
class TokenViewElement(private val token: SleighTokenDefinition) : StructureViewTreeElement, SortableTreeElement {

    override fun getValue(): Any = token

    override fun getAlphaSortKey(): String = token.name!!

    override fun getPresentation(): ItemPresentation = token.presentation

    override fun getChildren(): Array<TreeElement> =
        token.tokenFieldDefinitionList.mapNotNull { field -> TokenFieldViewElement(field) }.toTypedArray()

    override fun navigate(requestFocus: Boolean) {
        (token as NavigatablePsiElement).navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = (token as NavigatablePsiElement).canNavigate()

    override fun canNavigateToSource(): Boolean = (token as NavigatablePsiElement).canNavigateToSource()
}

class TokenFieldViewElement(private val field: SleighTokenFieldDefinition) : StructureViewTreeElement,
    SortableTreeElement {

    override fun getValue(): Any = field

    override fun getAlphaSortKey(): String = field.symbol.value

    override fun getPresentation(): ItemPresentation = field.presentation

    override fun getChildren(): Array<TreeElement> = emptyArray()

    override fun navigate(requestFocus: Boolean) {
        (field as NavigatablePsiElement).navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = (field as NavigatablePsiElement).canNavigate()

    override fun canNavigateToSource(): Boolean = (field as NavigatablePsiElement).canNavigateToSource()
}