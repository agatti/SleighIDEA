// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.FoldingGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiTreeUtil;
import it.frob.sleighidea.psi.*;
import it.frob.sleighidea.psi.impl.SleighPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SleighFoldingBuilder extends FoldingBuilderEx implements DumbAware {
    @Override
    public FoldingDescriptor @NotNull [] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean quick) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();

        for (PsiElement constructor : PsiTreeUtil.collectElements(root,
                filterElement -> (filterElement instanceof SleighCtorstart) &&
                        (filterElement.getFirstChild() instanceof SleighDisplay))) {
            FoldingGroup group = FoldingGroup.newGroup(SleighAnnotator.SLEIGH_PREFIX_STRING);
            PsiElement parent = constructor.getParent();
            descriptors.add(new FoldingDescriptor(parent.getNode(), parent.getTextRange(), group));
        }

        for (PsiElement macro : PsiTreeUtil.collectElements(root,
                filterElement -> filterElement instanceof SleighMacrodef)) {
            FoldingGroup group = FoldingGroup.newGroup(SleighAnnotator.SLEIGH_PREFIX_STRING);
            PsiElement parent = macro.getParent();
            descriptors.add(new FoldingDescriptor(parent.getNode(), parent.getTextRange(), group));
        }

        return descriptors.toArray(new FoldingDescriptor[0]);
    }

    private String getMacroPlaceholderText(ASTNode macro) {
        ASTNode nameNode = macro.findChildByType(SleighTypes.IDENTIFIER);
        if (nameNode == null) {
            return null;
        }

        String name = nameNode.getText().trim();
        List<String> arguments = new ArrayList<>();

        ASTNode argumentsNode = macro.findChildByType(SleighTypes.ARGUMENTS);
        if (argumentsNode != null) {
            ASTNode operandsListNode = argumentsNode.findChildByType(SleighTypes.OPLIST);
            if (operandsListNode != null) {
                arguments = Arrays.stream(operandsListNode.getChildren(TokenSet.create(SleighTypes.IDENTIFIER)))
                        .map(child -> child.getText().trim()).collect(Collectors.toList());
            }
        }

        return name + "(" + String.join(", ", arguments) + ")";
    }

    @Override
    public @Nullable String getPlaceholderText(@NotNull ASTNode node) {
        ASTNode macroNode = node.findChildByType(SleighTypes.MACRODEF);
        if (macroNode != null) {
            return getMacroPlaceholderText(macroNode);
        }

        ASTNode instructionNode = node.findChildByType(SleighTypes.CTORSTART);
        if (instructionNode != null) {
            ASTNode displayNode = instructionNode.findChildByType(SleighTypes.DISPLAY);
            if (displayNode != null) {
                return SleighPsiImplUtil.getDisplayText(displayNode);
            }
        }

        return node.getText();
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
