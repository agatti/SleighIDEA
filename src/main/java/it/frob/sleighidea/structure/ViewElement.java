// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.structure;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import it.frob.sleighidea.SleighIcons;
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ViewElement implements StructureViewTreeElement, SortableTreeElement {

    /**
     * The contained PSI element.
     */
    private final NavigatablePsiElement element;

    /**
     * Create a new structure view element wrapping the given PSI element.
     * @param element the PSI element to wrap.
     */
    public ViewElement(NavigatablePsiElement element) {
        this.element = element;
    }

    @Override
    public Object getValue() {
        return element;
    }

    @Override
    public @NotNull String getAlphaSortKey() {
        return element.getName() != null ? element.getName() : "";
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        ItemPresentation presentation = element.getPresentation();
        if (presentation == null) {
            if ((element instanceof SleighCtorstart) && (element.getFirstChild() instanceof SleighDisplay)) {
                return new PresentationData(
                        ((SleighDisplay) element.getFirstChild()).getPlaceholderText(),
                        "",
                        SleighIcons.TABLE,
                        null
                );
            } else {
                presentation = new PresentationData();
            }
        }

        return presentation;
    }

    @Override
    public TreeElement @NotNull [] getChildren() {
        if (element instanceof SleighFile) {
            return Arrays.stream(PsiTreeUtil.collectElements(element,
                            filterElement -> filterElement instanceof SleighTokendef ||
                                    filterElement instanceof SleighMacrodef ||
                                    (filterElement instanceof SleighCtorstart) &&
                                            (filterElement.getFirstChild() instanceof SleighDisplay)))
                    .map(psiElement -> new ViewElement((NavigatablePsiElement) psiElement))
                    .toArray(TreeElement[]::new);
        }

        return EMPTY_ARRAY;
    }

    @Override
    public void navigate(boolean requestFocus) {
        element.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return element.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return element.canNavigateToSource();
    }
}