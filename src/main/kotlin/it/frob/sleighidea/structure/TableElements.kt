// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.structure

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement
import com.intellij.ide.util.treeView.smartTree.TreeElement
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.NavigatablePsiElement
import it.frob.sleighidea.SleighIcons
import it.frob.sleighidea.psi.*

/**
 * Structure view element wrapper for tables.
 *
 * @param tables the [SleighConstructorStart] instances to wrap.
 * @constructor Create a `TableViewElement` wrapping the given [SleighConstructorStart] instances.
 */
class TableViewElement(private val tables: List<SleighConstructorStart>) : StructureViewTreeElement,
    SortableTreeElement {

    override fun getValue(): Any = tables.first()

    override fun getAlphaSortKey(): String = tables.first().identifier!!.text

    override fun getPresentation(): ItemPresentation {
        val presentationText = (if (tables.size > 1) {
            tables.first().identifier!!.text
        } else {
            "${tables.first().identifier!!.text} ${tables.first().display.placeholderText} -> ${(tables.first().parent as SleighConstructor).bitPattern.text}"
        }).replace(Regex("\\s+"), " ")

        return PresentationData(
            presentationText,
            getContainingFile(tables.first()),
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

class TableEntryViewElement(private val entry: SleighConstructorStart) : StructureViewTreeElement,
    SortableTreeElement {

    override fun getValue(): Any = entry

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

    override fun getChildren(): Array<TreeElement> = emptyArray()

    override fun navigate(requestFocus: Boolean) {
        (entry as NavigatablePsiElement).navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = (entry as NavigatablePsiElement).canNavigate()

    override fun canNavigateToSource(): Boolean = (entry as NavigatablePsiElement).canNavigateToSource()
}

class OpcodeViewElement(private val entry: SleighConstructorStart) : StructureViewTreeElement, SortableTreeElement {

    override fun getValue(): Any = entry

    override fun getAlphaSortKey(): String = entry.display.placeholderText

    override fun getPresentation(): ItemPresentation {
        return PresentationData(
            entry.display.placeholderText,
            getContainingFile(entry),
            SleighIcons.TABLE,
            null
        )
    }

    override fun getChildren(): Array<TreeElement> = emptyArray()

    override fun navigate(requestFocus: Boolean) {
        (entry as NavigatablePsiElement).navigate(requestFocus)
    }

    override fun canNavigate(): Boolean = (entry as NavigatablePsiElement).canNavigate()

    override fun canNavigateToSource(): Boolean = (entry as NavigatablePsiElement).canNavigateToSource()
}
