// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.structure

import com.intellij.psi.PsiFile
import com.intellij.ide.structureView.StructureViewModelBase
import it.frob.sleighidea.structure.ViewElement
import com.intellij.ide.structureView.StructureViewModel.ElementInfoProvider
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import it.frob.sleighidea.structure.SpaceViewElement
import it.frob.sleighidea.psi.SleighIdentifier
import it.frob.sleighidea.psi.SleighDisplay

class ViewModel(psiFile: PsiFile?) : StructureViewModelBase(psiFile!!, ViewElement(psiFile)), ElementInfoProvider {
    override fun getSorters(): Array<Sorter> = arrayOf(Sorter.ALPHA_SORTER)

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean =
        if (element is SpaceViewElement) element.getChildren().isNotEmpty() else false

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean =
        element.value is SleighIdentifier || element.value is SleighDisplay
}