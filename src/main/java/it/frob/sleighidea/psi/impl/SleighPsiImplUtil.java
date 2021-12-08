// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi.impl;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PlatformIcons;
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class SleighPsiImplUtil {

    /**
     * Extract the name of a {@link SleighTokendef} element.
     *
     * @param element the element to get the name of.
     * @return the token's name, or null if none could be extracted.
     */
    public static @Nullable String getName(@NotNull SleighTokendef element) {
        SleighIdentifier identifier = PsiTreeUtil.findChildOfType(element, SleighIdentifier.class);
        return identifier != null ? identifier.getText().trim() : null;
    }

    /**
     * Extract a placeholder text string from a SleighTokendef element.
     *
     * @param element the element to get the placeholder text for.
     * @return the placeholder text derived from the given element.
     */
    public static @Nullable String getPlaceholderText(@NotNull SleighTokendef element) {
        return getName(element);
    }

    /**
     * Create an {@link ItemPresentation} instance for the given {@link SleighTokendef} element.
     *
     * @param element the {@link SleighTokendef} element to create an item presentation for.
     * @return an {@link ItemPresentation} instance for the given element.
     */
    public static ItemPresentation getPresentation(@NotNull SleighTokendef element) {
        return new ItemPresentation() {
            @Override
            public @NlsSafe @Nullable String getPresentableText() {
                return SleighPsiImplUtil.getPlaceholderText(element);
            }

            @Override
            public @NlsSafe @Nullable String getLocationString() {
                PsiFile containingFile = element.getContainingFile();
                return containingFile == null ? null : containingFile.getName();
            }

            @Override
            public @Nullable Icon getIcon(boolean unused) {
                return PlatformIcons.CLASS_ICON;
            }
        };
    }

    /**
     * Extract a placeholder text string from a SleighDisplay element.
     *
     * @param element the element to get the placeholder text for.
     * @return the placeholder text derived from the given element.
     */
    public static String getPlaceholderText(@NotNull SleighDisplay element) {
        SleighDisplayPlaceholderVisitor visitor = new SleighDisplayPlaceholderVisitor();
        element.acceptChildren(visitor);

        return visitor.getPlaceholderText();
    }

    /**
     * Extract a placeholder text string from a SleighMacrodef element.
     *
     * @param element the element to get the placeholder text for.
     * @return the placeholder text derived from the given element.
     */
    public static @Nullable String getPlaceholderText(@NotNull SleighMacrodef element) {
        SleighIdentifier nameElement = PsiTreeUtil.findChildOfType(element, SleighIdentifier.class);
        if (nameElement == null) {
            return null;
        }

        return nameElement.getText().trim() +
                PsiTreeUtil.getChildrenOfTypeAsList(PsiTreeUtil.findChildOfType(element, SleighOplist.class),
                                SleighIdentifier.class).stream()
                        .map(PsiElement::getText)
                        .collect(Collectors.joining(", ", "(" ,")")).trim();
    }

    /**
     * Create an {@link ItemPresentation} instance for the given {@link SleighMacrodef} element.
     *
     * @param element the {@link SleighMacrodef} element to create an item presentation for.
     * @return an {@link ItemPresentation} instance for the given element.
     */
    public static ItemPresentation getPresentation(@NotNull SleighMacrodef element) {
        return new ItemPresentation() {
            @Override
            public @NlsSafe @Nullable String getPresentableText() {
                return SleighPsiImplUtil.getPlaceholderText(element);
            }

            @Override
            public @NlsSafe @Nullable String getLocationString() {
                PsiFile containingFile = element.getContainingFile();
                return containingFile == null ? null : containingFile.getName();
            }

            @Override
            public @Nullable Icon getIcon(boolean unused) {
                return PlatformIcons.FUNCTION_ICON;
            }
        };
    }
}
