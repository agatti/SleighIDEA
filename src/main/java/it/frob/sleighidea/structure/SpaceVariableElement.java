// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.structure;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.PlatformIcons;
import it.frob.sleighidea.psi.SleighSymbol;
import org.jetbrains.annotations.NotNull;

/**
 * Structure view element wrapper for memory space variables.
 */
public class SpaceVariableElement implements StructureViewTreeElement, SortableTreeElement {

    /**
     * The memory space variable to wrap.
     */
    private final SleighSymbol variable;

    /**
     * Create a {@code SpaceVariableElement} wrapping the given {@link SleighSymbol} instance.
     *
     * @param variable the {@link SleighSymbol} instance to wrap.
     */
    public SpaceVariableElement(@NotNull SleighSymbol variable) {
        this.variable = variable;
    }

    @Override
    public Object getValue() {
        return variable;
    }

    @Override
    public @NotNull String getAlphaSortKey() {
        return variable.getValue();
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        PsiFile containingFile = variable.getContainingFile();
        String location = containingFile == null ? "" : containingFile.getName();

        return new PresentationData(variable.getValue(), location, PlatformIcons.VARIABLE_ICON, null);
    }

    @Override
    public TreeElement @NotNull [] getChildren() {
        return TreeElement.EMPTY_ARRAY;
    }

    @Override
    public void navigate(boolean requestFocus) {
        ((NavigatablePsiElement) variable).navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return ((NavigatablePsiElement) variable).canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return ((NavigatablePsiElement) variable).canNavigateToSource();
    }
}
