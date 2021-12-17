// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import it.frob.sleighidea.psi.SleighSpaceDefinition;
import it.frob.sleighidea.psi.SleighVariablesNodeDefinition;
import it.frob.sleighidea.psi.impl.SleighPsiImplUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

/**
 * Structure view element wrapper for memory spaces.
 */
public class SpaceViewElement implements StructureViewTreeElement, SortableTreeElement {

    /**
     * The memory space to wrap.
     */
    private final SleighSpaceDefinition space;

    /**
     * Create a {@code SpaceViewElement} wrapping the given {@link SleighSpaceDefinition} instance.
     *
     * @param space the {@link SleighSpaceDefinition} instance to wrap.
     */
    public SpaceViewElement(@NotNull SleighSpaceDefinition space) {
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
        return SleighPsiImplUtil.getPresentation(space);
    }

    @Override
    public TreeElement @NotNull [] getChildren() {
        return PsiTreeUtil.collectElementsOfType(space.getContainingFile(), SleighVariablesNodeDefinition.class)
                .stream()
                .filter(node -> node.getSpace().getValue().equals(space.getName()))
                .map(SleighVariablesNodeDefinition::getSymbolOrWildcardList)
                .flatMap(Collection::stream)
                .filter(variable -> variable.getSymbol() != null)
                .map(variable -> new SpaceVariableElement(Objects.requireNonNull(variable.getSymbol())))
                .toArray(TreeElement[]::new);
    }

    @Override
    public void navigate(boolean requestFocus) {
        ((NavigatablePsiElement) space).navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return ((NavigatablePsiElement) space).canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return ((NavigatablePsiElement) space).canNavigateToSource();
    }
}
