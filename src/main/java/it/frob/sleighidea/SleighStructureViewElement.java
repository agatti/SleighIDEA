// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import it.frob.sleighidea.psi.SleighCtorstart;
import it.frob.sleighidea.psi.SleighDisplay;
import it.frob.sleighidea.psi.SleighFile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class SleighStructureViewElement implements StructureViewTreeElement, SortableTreeElement {

    private final NavigatablePsiElement element;

    public SleighStructureViewElement(NavigatablePsiElement element) {
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
            // TODO: Manipulate the tree rather than this...

            String rawElementText = element.getText().trim();
            String itemLabel = rawElementText.replaceAll("\\s+", " ");
            presentation = new PresentationData(itemLabel.substring(0, itemLabel.length() - 2), "", SleighIcons.TABLE, null);
        }

        return presentation;
    }

    @Override
    public TreeElement @NotNull [] getChildren() {
        if (element instanceof SleighFile) {
            return Arrays.stream(PsiTreeUtil.collectElements(element, filterElement -> (filterElement instanceof SleighCtorstart) && (filterElement.getFirstChild() instanceof SleighDisplay)))
                    .map(psiElement -> new SleighStructureViewElement((NavigatablePsiElement) psiElement))
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
