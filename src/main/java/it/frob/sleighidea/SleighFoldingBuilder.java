// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import it.frob.sleighidea.psi.SleighCtorstart;
import it.frob.sleighidea.psi.SleighDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SleighFoldingBuilder extends FoldingBuilderEx implements DumbAware {
    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();
        PsiElement[] constructors = PsiTreeUtil.collectElements(root, filterElement -> (filterElement instanceof SleighCtorstart) && (filterElement.getFirstChild() instanceof SleighDisplay));

        for (PsiElement constructor : constructors) {
            FoldingGroup group = FoldingGroup.newGroup(SleighAnnotator.SLEIGH_PREFIX_STRING);
            PsiElement parent = constructor.getParent();
            descriptors.add(new FoldingDescriptor(parent.getNode(), parent.getTextRange(), group));
        }

        return descriptors.toArray(new FoldingDescriptor[0]);
    }

    @Override
    public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
        // TODO: Manipulate the AST rather than doing this...

        String rawPlaceholder = node.getFirstChildNode().getText().replaceAll("\\s+", " ").trim();
        return rawPlaceholder.substring(0, rawPlaceholder.length() - 2).trim();
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
