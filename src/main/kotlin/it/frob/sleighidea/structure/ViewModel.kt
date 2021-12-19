// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.structure

import com.intellij.ide.structureView.StructureViewModel.ElementInfoProvider
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.psi.PsiFile
import it.frob.sleighidea.psi.SleighDisplay
import it.frob.sleighidea.psi.SleighIdentifier

class ViewModel(psiFile: PsiFile?) : StructureViewModelBase(psiFile!!, ViewElement(psiFile)), ElementInfoProvider {
    override fun getSorters(): Array<Sorter> = arrayOf(Sorter.ALPHA_SORTER)

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean =
        (element is SpaceViewElement || element is TokenViewElement) && element.children.isNotEmpty()

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean =
        element.value is SleighIdentifier || element.value is SleighDisplay
}