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
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SleighFoldingBuilder extends FoldingBuilderEx implements DumbAware {

    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document,
                                                          boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();

        root.acceptChildren(new SleighVisitor() {
            @Override
            public void visitDefinition(@NotNull SleighDefinition visited) {
                SleighTokenDefinition tokenContainer =
                        PsiTreeUtil.findChildOfType(visited, SleighTokenDefinition.class);
                SleighIdentifier identifier = PsiTreeUtil.findChildOfType(tokenContainer, SleighIdentifier.class);
                if (identifier != null) {
                    FoldingGroup group = FoldingGroup.newGroup(SleighAnnotator.SLEIGH_PREFIX_STRING);
                    descriptors.add(new FoldingDescriptor(visited.getNode(), visited.getTextRange(), group,
                            tokenContainer.getPlaceholderText()));
                }
            }

            @Override
            public void visitConstructorlike(@NotNull SleighConstructorlike visited) {
                visited.acceptChildren(new SleighVisitor() {

                    @Override
                    public void visitMacrodef(@NotNull SleighMacrodef visited) {
                        FoldingGroup group = FoldingGroup.newGroup(SleighAnnotator.SLEIGH_PREFIX_STRING);
                        PsiElement parent = visited.getParent();
                        descriptors.add(new FoldingDescriptor(parent.getNode(), parent.getTextRange(), group,
                                visited.getPlaceholderText()));
                    }

                    @Override
                    public void visitConstructor(@NotNull SleighConstructor visited) {
                        SleighCtorstart container = PsiTreeUtil.findChildOfType(visited, SleighCtorstart.class);
                        if (container == null) {
                            return;
                        }

                        if (container.getFirstChild() instanceof SleighDisplay) {
                            FoldingGroup group = FoldingGroup.newGroup(SleighAnnotator.SLEIGH_PREFIX_STRING);
                            PsiElement parent = visited.getParent();
                            descriptors.add(new FoldingDescriptor(parent.getNode(), parent.getTextRange(), group,
                                    ((SleighDisplay) container.getFirstChild()).getPlaceholderText()));
                        }
                    }
                });
            }
        });

        return descriptors.toArray(new FoldingDescriptor[0]);
    }

    @Override
    public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
        return null;
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
