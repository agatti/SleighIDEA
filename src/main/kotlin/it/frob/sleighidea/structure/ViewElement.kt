// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.structure

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.*
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiFile
import it.frob.sleighidea.psi.*

class ViewModel(psiFile: PsiFile?) : StructureViewModelBase(psiFile!!, ViewElement(psiFile)),
    StructureViewModel.ElementInfoProvider {
    override fun getSorters(): Array<Sorter> = arrayOf(Sorter.ALPHA_SORTER)

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean =
        (element is SpaceViewElement || element is TokenViewElement || element is TableViewElement) && element.children.isNotEmpty()

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean =
        element.value is SleighIdentifier || element.value is SleighDisplay
}

class ViewFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder =
        object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?): StructureViewModel = ViewModel(psiFile)
        }
}

/**
 * Create a new structure view element wrapping the given PSI element.
 *
 * @param element the PSI element to wrap.
 */
class ViewElement(private val element: NavigatablePsiElement) : StructureViewTreeElement, SortableTreeElement {

    override fun getValue(): Any = element

    override fun getAlphaSortKey(): String = element.name ?: ""

    override fun getPresentation(): ItemPresentation =
        element.presentation ?: PresentationData()

    override fun getChildren(): Array<TreeElement> {
        if (element !is SleighFile) {
            return emptyArray()
        }

        val file = element
        val viewElements: MutableList<StructureViewTreeElement> = mutableListOf()
        viewElements.addAll(file.spaces
            .map { space: SleighSpaceDefinition -> SpaceViewElement(space) }
            .toList())
        viewElements.addAll(file.tokens
            .map { token: SleighTokenDefinition -> TokenViewElement(token) }
            .toList())
        viewElements.addAll(file.macros
            .map { macro: SleighMacroDefinition -> ViewElement(macro as NavigatablePsiElement) }
            .toList())

        viewElements.addAll(extractTableConstructors(file).values.map { tables -> TableViewElement(tables) }.toList())

        viewElements.addAll(file.constructorStarts.filter { constructor -> constructor.identifier == null }
            .map { opcode -> OpcodeViewElement(opcode) }.toList())

        return viewElements.toTypedArray()
    }

    private fun extractTableConstructors(file: SleighFile): Map<String, List<SleighConstructorStart>> {
        val constructors: MutableMap<String, MutableList<SleighConstructorStart>> = linkedMapOf()

        file.constructorStarts
            .filter { constructor -> constructor.identifier != null }
            .forEach { constructor ->
                constructors.computeIfAbsent(constructor.identifier!!.text) { mutableListOf() }.add(constructor)
            }

        return constructors
    }

    override fun navigate(requestFocus: Boolean) {
        element.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = element.canNavigate()

    override fun canNavigateToSource(): Boolean = element.canNavigateToSource()
}