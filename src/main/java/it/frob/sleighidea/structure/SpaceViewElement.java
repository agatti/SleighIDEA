// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import it.frob.sleighidea.model.space.Space;
import it.frob.sleighidea.psi.impl.SleighPsiImplUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Structure view element wrapper for memory spaces.
 */
public class SpaceViewElement implements StructureViewTreeElement, SortableTreeElement {

    /**
     * The memory space to wrap.
     */
    private final Space space;

    /**
     * Create a {@code SpaceViewElement} wrapping the given {@link Space} instance.
     *
     * @param space the {@link Space} instance to wrap.
     */
    public SpaceViewElement(@NotNull Space space) {
        this.space = space;
    }

    @Override
    public Object getValue() {
        return space;
    }

    @Override
    public @NotNull String getAlphaSortKey() {
        return space.getName();
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        return SleighPsiImplUtil.getPresentation(space.getDefinition());
    }

    @Override
    public TreeElement @NotNull [] getChildren() {
        return space.getVariables().values().stream()
                .map(SpaceVariableElement::new)
                .toArray(TreeElement[]::new);
    }

    @Override
    public void navigate(boolean requestFocus) {
        ((NavigatablePsiElement) space.getDefinition()).navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return ((NavigatablePsiElement) space.getDefinition()).canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return ((NavigatablePsiElement) space.getDefinition()).canNavigateToSource();
    }
}
