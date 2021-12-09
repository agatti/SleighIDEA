// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi.impl;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PlatformIcons;
import it.frob.sleighidea.model.ModelException;
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
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
     * Extract a placeholder text string from a {@link SleighTokendef} element.
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
     * Extract a placeholder text string from a {@link SleighDisplay} element.
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
     * Extract a placeholder text string from a {@link SleighMacrodef} element.
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
                        .collect(Collectors.joining(", ", "(", ")")).trim();
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

    /**
     * Extract the name of a {@link SleighSpacedef} element.
     *
     * @param element the element to get the name of.
     * @return the space's name.
     */
    public static @NotNull String getName(@NotNull SleighSpacedef element) {
        return element.getIdentifier().getText().trim();
    }

    /**
     * Extract the size of a {@link SleighSpacedef} element.
     *
     * @param element the element to get the size of.
     * @return the space's size.
     * @throws it.frob.sleighidea.model.ModelException if the incorrect number of modifiers is found.
     */
    public static int getSize(@NotNull SleighSpacedef element) throws ModelException {
        SleighSizemod[] sizeModifiers = PsiTreeUtil.findChildrenOfType(element, SleighSizemod.class)
                .toArray(new SleighSizemod[0]);
        if (sizeModifiers.length != 1) {
            throw new ModelException(String.format("Invalid size modifiers found for `%s`: %d.",
                    element.getText(), sizeModifiers.length));
        }
        return sizeModifiers[0].getInteger().toPositiveInteger();
    }

    /**
     * Extract the word size of a {@link SleighSpacedef} element.
     *
     * @apiNote this function will not check for duplicate {@code wordsize} modifiers in the space definition.
     * @param element the element to get the word size of.
     * @return the space's word size, defaulting to {@code 1} if none is defined.
     * @throws it.frob.sleighidea.model.ModelException if the incorrect number of modifiers is found.
     */
    public static int getWordSize(@NotNull SleighSpacedef element) throws ModelException {
        SleighWordsizemod[] wordSizeModifiers = PsiTreeUtil.findChildrenOfType(element, SleighWordsizemod.class)
                .toArray(new SleighWordsizemod[0]);

        switch (wordSizeModifiers.length) {
            case 0:
                return 1;

            case 1:
                return wordSizeModifiers[0].getInteger().toPositiveInteger();

            default:
                throw new ModelException(String.format("Invalid word size modifiers found for `%s`: %d.",
                        element.getText(), wordSizeModifiers.length));
        }
    }

    /**
     * Extract the type of a {@link SleighSpacedef} element.
     *
     * @param element the element to get the type of.
     * @return the space's type, or {@code null} if none could be extracted.
     * @throws it.frob.sleighidea.model.ModelException if the incorrect number of modifiers is found.
     */
    public static @Nullable String getType(@NotNull SleighSpacedef element) throws ModelException {
        SleighTypemod[] typeModifiers = PsiTreeUtil.findChildrenOfType(element, SleighTypemod.class)
                .toArray(new SleighTypemod[0]);
        switch (typeModifiers.length) {
            case 0:
                return null;

            case 1:
                return typeModifiers[0].getType().getText();

            default:
                throw new ModelException(String.format("Invalid type modifiers found for `%s`: %d.",
                        element.getText(), typeModifiers.length));
        }
    }

    /**
     * Extract the default flag of a {@link SleighSpacedef} element.
     *
     * @param element the element to get the default flag of.
     * @return {@code true} if the space acts as a default space, {@code false} otherwise.
     */
    public static boolean isDefault(@NotNull SleighSpacedef element) {
        return Arrays.stream(PsiTreeUtil.collectElements(PsiTreeUtil.findChildOfType(element, SleighSpacemod.class),
                        spaceElement -> SleighTypes.KEY_DEFAULT.toString().equals(spaceElement.getText())))
                .findAny()
                .isPresent();
    }

    /**
     * Convert a {@link SleighInteger} instance to an integer, taking care of base conversion.
     *
     * @param integer the {@link SleighInteger} instance to convert.
     * @return the converted positive integer.
     */
    public static int toPositiveInteger(@NotNull SleighInteger integer) {
        PsiElement binaryNumber = integer.getBinnumber();
        if (binaryNumber != null) {
            return Integer.parseInt(binaryNumber.getText().substring(2), 2);
        }

        PsiElement decimalNumber = integer.getDecnumber();
        if (decimalNumber != null) {
            return Integer.parseInt(decimalNumber.getText(), 10);
        }

        PsiElement hexadecimalNumber = integer.getHexnumber();
        if (hexadecimalNumber != null) {
            return Integer.parseInt(hexadecimalNumber.getText().substring(2), 16);
        }

        throw new IllegalStateException("Integer has no real value attached?");
    }
}
