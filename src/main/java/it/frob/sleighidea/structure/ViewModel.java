// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.structure;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.psi.PsiFile;
import it.frob.sleighidea.model.space.Space;
import it.frob.sleighidea.psi.SleighDisplay;
import it.frob.sleighidea.psi.SleighIdentifier;
import org.jetbrains.annotations.NotNull;

public class ViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {

    public ViewModel(PsiFile psiFile) {
        super(psiFile, new ViewElement(psiFile));
    }

    @Override
    public Sorter @NotNull [] getSorters() {
        return new Sorter[]{
                Sorter.ALPHA_SORTER
        };
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        if (element instanceof SpaceViewElement) {
            return !((Space) element.getValue()).getVariables().isEmpty();
        }

        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        Object elementValue = element.getValue();
        return elementValue instanceof SleighIdentifier || elementValue instanceof SleighDisplay;
    }
}
