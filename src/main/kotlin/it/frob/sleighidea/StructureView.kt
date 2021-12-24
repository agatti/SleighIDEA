// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea

import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.*
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.util.PlatformIcons
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import it.frob.sleighidea.psi.*

class ViewModel(psiFile: PsiFile?) : StructureViewModelBase(psiFile!!, RootViewElement(psiFile)),
    StructureViewModel.ElementInfoProvider {
    override fun getSorters(): Array<Sorter> = arrayOf(Sorter.ALPHA_SORTER)

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean =
        (element is SpaceViewElement || element is TokenViewElement || element is TableViewElement) &&
                element.children.isNotEmpty()

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean =
        element.value is SleighIdentifier || element.value is SleighDisplay || element.value is SleighSymbol
}

class ViewFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder =
        object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?): StructureViewModel = ViewModel(psiFile)
        }
}

class RootViewElement(private val element: NavigatablePsiElement) : StructureViewTreeElement, SortableTreeElement {

    override fun getValue(): NavigatablePsiElement = element

    override fun getAlphaSortKey(): String = element.name ?: ""

    override fun getPresentation(): ItemPresentation = element.presentation ?: PresentationData()

    override fun getChildren(): Array<TreeElement> = if (element !is SleighFile) {
        emptyArray()
    } else buildChildrenList().toTypedArray()

    private fun buildChildrenList(): List<TreeElement> {
        val viewElements: MutableList<TreeElement> = mutableListOf()
        val tables: MutableMap<String, TableViewElement> = mutableMapOf()
        PsiTreeUtil.processElements(element) { psiElement ->
            psiElement.accept(object : SleighVisitor() {
                override fun visitSpaceDefinition(visited: SleighSpaceDefinition) {
                    viewElements.add(SpaceViewElement(visited))
                }

                override fun visitTokenDefinition(visited: SleighTokenDefinition) {
                    viewElements.add(TokenViewElement(visited))
                }

                override fun visitMacroDefinition(visited: SleighMacroDefinition) {
                    viewElements.add(MacroViewElement(visited))
                }

                override fun visitConstructorStart(visited: SleighConstructorStart) {
                    visited.identifier?.let { identifier ->
                        if (tables.containsKey(identifier.text)) {
                            tables[identifier.text]!!.addConstructor(visited)
                        } else {
                            val table = TableViewElement(visited)
                            tables[identifier.text] = table
                            viewElements.add(table)
                        }
                    } ?: run {
                        viewElements.add(OpcodeViewElement(visited))
                    }
                }
            })

            true
        }

        return viewElements
    }

    override fun navigate(requestFocus: Boolean) {
        element.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = element.canNavigate()

    override fun canNavigateToSource(): Boolean = element.canNavigateToSource()
}

abstract class ViewElementBase<T : SleighCompositeElement>(private val element: T) : StructureViewTreeElement,
    SortableTreeElement {

    override fun getChildren(): Array<TreeElement> = emptyArray()

    override fun navigate(requestFocus: Boolean) {
        (element as NavigatablePsiElement).navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = (element as NavigatablePsiElement).canNavigate()

    override fun canNavigateToSource(): Boolean = (element as NavigatablePsiElement).canNavigateToSource()

    override fun getValue(): T = element

    override fun getAlphaSortKey(): String = (element as? SleighNamedElement)?.name ?: ""

    override fun getPresentation(): ItemPresentation = element.presentation!!
}

/**
 * Structure view element wrapper for memory spaces.
 *
 * @param space the [SleighSpaceDefinition] instance to wrap.
 * @constructor Create a `SpaceViewElement` wrapping the given [SleighSpaceDefinition] instance.
 */
class SpaceViewElement(private val space: SleighSpaceDefinition) : ViewElementBase<SleighSpaceDefinition>(space) {

    override fun getChildren(): Array<TreeElement> =
        PsiTreeUtil.collectElementsOfType(space.containingFile, SleighVariablesNodeDefinition::class.java)
            .filter { node: SleighVariablesNodeDefinition -> node.space.value == space.name }
            .map { obj: SleighVariablesNodeDefinition -> obj.symbolOrWildcardList }
            .flatten()
            .mapNotNull { variable: SleighSymbolOrWildcard ->
                variable.symbol?.let { symbol -> SpaceVariableElement(symbol) }
            }
            .toTypedArray()
}

/**
 * Structure view element wrapper for memory space variables.
 *
 * @param variable the [SleighSymbol] instance to wrap.
 * @constructor Create a `SpaceVariableElement` wrapping the given [SleighSymbol] instance.
 */
class SpaceVariableElement(private val variable: SleighSymbol) : ViewElementBase<SleighSymbol>(variable) {

    override fun getPresentation(): ItemPresentation =
        PresentationData(
            variable.value, variable.containingFile?.name ?: "",
            PlatformIcons.VARIABLE_ICON, null
        )
}

/**
 * Structure view element wrapper for [SleighConstructorStart] table items.
 *
 * @param constructor the [SleighConstructorStart] instance to wrap.
 * @constructor Create a `TableViewElement` wrapping the given [SleighConstructorStart] instance.
 */
class TableViewElement(constructor: SleighConstructorStart) : ViewElementBase<SleighConstructorStart>(constructor) {

    /**
     * Sub-tables to show as children of the current table element.
     */
    private val tables: MutableList<SleighConstructorStart> = mutableListOf(constructor)

    /**
     * Add a [SleighConstructorStart] instance as a sub-item of the current table.
     */
    fun addConstructor(constructor: SleighConstructorStart) {
        tables.add(constructor)
    }

    override fun getValue(): SleighConstructorStart = tables.first()

    override fun getAlphaSortKey(): String = tables.first().identifier!!.text

    override fun getPresentation(): ItemPresentation {
        val item = tables.first()
        val identifier = item.identifier!!

        val presentationText = (if (tables.size > 1) {
            identifier.text
        } else {
            "${identifier.text} ${item.display.placeholderText} -> ${(item.parent as SleighConstructor).bitPattern.text}"
        }).replace(Regex("\\s+"), " ")

        return PresentationData(
            presentationText,
            getContainingFile(item),
            SleighIcons.TABLE_GO,
            null
        )
    }

    override fun getChildren(): Array<TreeElement> = if (tables.size > 1) {
        tables.map { table -> TableEntryViewElement(table) }.toTypedArray()
    } else {
        emptyArray()
    }

    override fun navigate(requestFocus: Boolean) {
        (tables.first() as NavigatablePsiElement).navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = (tables.first() as NavigatablePsiElement).canNavigate()

    override fun canNavigateToSource(): Boolean = (tables.first() as NavigatablePsiElement).canNavigateToSource()
}

class TableEntryViewElement(private val entry: SleighConstructorStart) :
    ViewElementBase<SleighConstructorStart>(entry) {

    override fun getAlphaSortKey(): String = entry.display.placeholderText

    override fun getPresentation(): ItemPresentation {
        return PresentationData(
            "${entry.display.placeholderText} -> ${(entry.parent as SleighConstructor).bitPattern.text}".replace(
                Regex("\\s+"),
                " "
            ),
            getContainingFile(entry),
            SleighIcons.TABLE_GO,
            null
        )
    }
}

class OpcodeViewElement(private val entry: SleighConstructorStart) : ViewElementBase<SleighConstructorStart>(entry) {

    override fun getAlphaSortKey(): String = entry.display.placeholderText

    override fun getPresentation(): ItemPresentation {
        return PresentationData(
            entry.display.placeholderText,
            getContainingFile(entry),
            SleighIcons.TABLE,
            null
        )
    }
}

/**
 * Structure view element wrapper for tokens.
 *
 * @param token the [SleighTokenDefinition] instance to wrap.
 * @constructor Create a `TokenViewElement` wrapping the given [SleighTokenDefinition] instance.
 */
class TokenViewElement(private val token: SleighTokenDefinition) : ViewElementBase<SleighTokenDefinition>(token) {

    override fun getChildren(): Array<TreeElement> =
        token.tokenFieldDefinitionList.map { field -> TokenFieldViewElement(field) }.toTypedArray()
}

class TokenFieldViewElement(private val field: SleighTokenFieldDefinition) :
    ViewElementBase<SleighTokenFieldDefinition>(field) {

    override fun getAlphaSortKey(): String = field.symbol.value
}

/**
 * Structure view element wrapper for macros.
 *
 * @param macro the [SleighMacroDefinition] instance to wrap.
 * @constructor Create a `TokenViewElement` wrapping the given [SleighMacroDefinition] instance.
 */
class MacroViewElement(private val macro: SleighMacroDefinition) : ViewElementBase<SleighMacroDefinition>(macro) {

    override fun getAlphaSortKey(): String = macro.placeholderText
}
