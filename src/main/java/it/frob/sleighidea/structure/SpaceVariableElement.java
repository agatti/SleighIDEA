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
import it.frob.sleighidea.model.space.Variable;
import org.jetbrains.annotations.NotNull;

/**
 * Structure view element wrapper for memory space variables.
 */
public class SpaceVariableElement implements StructureViewTreeElement, SortableTreeElement {

    /**
     * The memory space variable to wrap.
     */
    private final Variable variable;

    /**
     * Create a {@code SpaceVariableElement} wrapping the given {@link Variable} instance.
     *
     * @param variable the {@link Variable} instance to wrap.
     */
    public SpaceVariableElement(@NotNull Variable variable) {
        this.variable = variable;
    }

    @Override
    public Object getValue() {
        return variable;
    }

    @Override
    public @NotNull String getAlphaSortKey() {
        return variable.getName();
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        PsiFile containingFile = variable.getElement().getContainingFile();
        String location = containingFile == null ? "" : containingFile.getName();

        return new PresentationData(variable.getName(), location, PlatformIcons.VARIABLE_ICON, null);
    }

    @Override
    public TreeElement @NotNull [] getChildren() {
        return TreeElement.EMPTY_ARRAY;
    }

    @Override
    public void navigate(boolean requestFocus) {
        ((NavigatablePsiElement) variable.getElement()).navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return ((NavigatablePsiElement) variable.getElement()).canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return ((NavigatablePsiElement) variable.getElement()).canNavigateToSource();
    }
}
