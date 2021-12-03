// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.psi.PsiFile;
import it.frob.sleighidea.psi.SleighDisplay;
import it.frob.sleighidea.psi.SleighIdentifier;
import org.jetbrains.annotations.NotNull;

public class SleighStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {

    public SleighStructureViewModel(PsiFile psiFile) {
        super(psiFile, new SleighStructureViewElement(psiFile));
    }

    @Override
    public Sorter @NotNull [] getSorters() {
        return new Sorter[]{
                Sorter.ALPHA_SORTER
        };
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        Object elementValue = element.getValue();
        return elementValue instanceof SleighIdentifier || elementValue instanceof SleighDisplay;
    }
}
