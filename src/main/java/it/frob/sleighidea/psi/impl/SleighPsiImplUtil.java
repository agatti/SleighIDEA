// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi.impl;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PlatformIcons;
import it.frob.sleighidea.model.Endianness;
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class SleighPsiImplUtil {

    /**
     * Get the file name of the given element's containing file.
     *
     * @param element the element to get the containing file name.
     * @return the containing file name as a string or {@code null} if none could be obtained.
     */
    @Nullable
    private static String getContainingFile(@NotNull PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        return containingFile == null ? null : containingFile.getName();
    }

    /**
     * Convert a String to an integer, taking care of base conversion.
     *
     * @param integer the string to convert.
     * @return the converted positive integer.
     */
    public static int baseAwareIntegerParser(@NotNull String integer) {
        String lowerCase = integer.toLowerCase(Locale.ROOT).trim();

        if (lowerCase.startsWith("0b")) {
            return Integer.parseInt(integer.substring(2), 2);
        }

        if (lowerCase.startsWith("0x")) {
            return Integer.parseInt(integer.substring(2), 16);
        }

        return Integer.parseInt(integer, 10);
    }

    // Accessors for SleighDisplay

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

    // Accessors for SleighMacrodef

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
                        .collect(Collectors.joining(", ", "(", ")"))
                        .trim();
    }

    /**
     * Create an {@link ItemPresentation} instance for the given {@link SleighMacrodef} element.
     *
     * @param element the {@link SleighMacrodef} element to create an item presentation for.
     * @return an {@link ItemPresentation} instance for the given element.
     */
    public static ItemPresentation getPresentation(@NotNull SleighMacrodef element) {
        return new PresentationData(getPlaceholderText(element), getContainingFile(element),
                PlatformIcons.FUNCTION_ICON, null);
    }

    // Accessors for SleighSpaceDefinition elements (and their children)

    /**
     * Extract the name of a {@link SleighSpaceDefinition} element.
     *
     * @param element the element to get the name of.
     * @return the space's name.
     */
    @NotNull
    public static String getName(@NotNull SleighSpaceDefinition element) {
        return element.getIdentifier().getText().trim();
    }

    /**
     * Extract a placeholder text string from a {@link SleighSpaceDefinition} element.
     *
     * @param element the element to get the placeholder text for.
     * @return the placeholder text derived from the given element.
     */
    @NotNull
    public static String getPlaceholderText(@NotNull SleighSpaceDefinition element) {
        SleighPositiveIntegerValue sizeElement = element.getSize().get(0).second;
        String size = sizeElement.isExternal() ? sizeElement.getText() : String.valueOf(sizeElement.toInteger());

        String wordSize;
        List<Pair<@NotNull SleighSpaceWordsizeModifier, @NotNull SleighPositiveIntegerValue>> wordSizes = element.getWordSize();
        if (wordSizes.isEmpty()) {
            wordSize = size;
        } else {
            SleighPositiveIntegerValue wordSizeValue = wordSizes.get(0).second;
            wordSize = wordSizeValue.isExternal() ? wordSizeValue.getText() : String.valueOf(wordSizeValue.toInteger());
        }

        return String.format("%s (size: %s, word size: %s)", getName(element), size, wordSize);
    }

    /**
     * Create an {@link ItemPresentation} instance for the given {@link SleighSpaceDefinition} element.
     *
     * @param element the {@link SleighSpaceDefinition} element to create an item presentation for.
     * @return an {@link ItemPresentation} instance for the given element.
     */
    @NotNull
    public static ItemPresentation getPresentation(@NotNull SleighSpaceDefinition element) {
        return new PresentationData(getPlaceholderText(element), getContainingFile(element),
                PlatformIcons.ANONYMOUS_CLASS_ICON, null);
    }

    /**
     * Extract the size modifiers of a {@link SleighSpaceDefinition} element.
     *
     * @param element the element to get the size modifiers of.
     * @return a list of {@code Pair} of {@link SleighSpaceSizeModifier} element, together with the associated
     * {@link SleighPositiveIntegerValue}.
     */
    @NotNull
    public static List<Pair<@NotNull SleighSpaceSizeModifier, @NotNull SleighPositiveIntegerValue>> getSize(@NotNull SleighSpaceDefinition element) {
        return element.getSpaceModifierList().stream()
                .map(SleighSpaceModifier::getSpaceSizeModifier)
                .filter(Objects::nonNull)
                .map(modifier -> Pair.create(modifier, modifier.getPositiveIntegerValue()))
                .collect(Collectors.toList());
    }

    /**
     * Extract the word size modifiers of a {@link SleighSpaceDefinition} element.
     *
     * @param element the element to get the word size modifiers of.
     * @return a list of {@code Pair} of {@link SleighSpaceWordsizeModifier} element, together with the associated
     * {@link SleighPositiveIntegerValue}.
     */
    @NotNull
    public static List<Pair<@NotNull SleighSpaceWordsizeModifier, @NotNull SleighPositiveIntegerValue>> getWordSize(@NotNull SleighSpaceDefinition element) {
        return element.getSpaceModifierList().stream()
                .map(SleighSpaceModifier::getSpaceWordsizeModifier)
                .filter(Objects::nonNull)
                .map(modifier -> Pair.create(modifier, modifier.getPositiveIntegerValue()))
                .collect(Collectors.toList());
    }

    /**
     * Extract the type modifiers of a {@link SleighSpaceDefinition} element.
     *
     * @param element the element to get the word size modifiers of.
     * @return a list of {@code Pair} of {@link SleighSpaceTypeModifier} element, together with the associated string.
     */
    @NotNull
    public static List<Pair<@NotNull SleighSpaceTypeModifier, @NotNull String>> getType(@NotNull SleighSpaceDefinition element) {
        return element.getSpaceModifierList().stream()
                .map(SleighSpaceModifier::getSpaceTypeModifier)
                .filter(Objects::nonNull)
                .map(modifier -> Pair.create(modifier, modifier.getSpaceTypeIdentifier().getText()))
                .collect(Collectors.toList());
    }

    /**
     * Extract the default flags of a {@link SleighSpaceDefinition} element.
     *
     * @param element the element to get the default flags of.
     * @return a list of {@code PsiElement} containing the default flag elements.
     */
    @NotNull
    public static List<PsiElement> getDefault(@NotNull SleighSpaceDefinition element) {
        return element.getSpaceModifierList().stream()
                .map(SleighSpaceModifier::getKeyDefault)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // Accessors for SleighVariablesNodeDefinition elements (and their children)

    /**
     * Extract the given {@link SleighVariablesNodeDefinition}'s bound memory space symbol.
     *
     * @param node the {@link SleighVariablesNodeDefinition} instance to extract data from.
     * @return the bound memory space symbol for the input element.
     */
    @NotNull
    public static SleighSymbol getSpace(@NotNull SleighVariablesNodeDefinition node) {
        return node.getSymbol();
    }

    // Accessors for SleighTokenDefinition elements (and their children)

    /**
     * Extract the name of a {@link SleighTokenDefinition} element.
     *
     * @param element the element to get the name of.
     * @return the token's name.
     */
    @NotNull
    public static String getName(@NotNull SleighTokenDefinition element) {
        return element.getSymbol().getValue();
    }

    /**
     * Extract a placeholder text string from a {@link SleighTokenDefinition} element.
     *
     * @param element the element to get the placeholder text for.
     * @return the placeholder text derived from the given element.
     */
    @NotNull
    public static String getPlaceholderText(@NotNull SleighTokenDefinition element) {
        return getName(element);
    }

    /**
     * Create an {@link ItemPresentation} instance for the given {@link SleighTokenDefinition} element.
     *
     * @param element the {@link SleighTokenDefinition} element to create an item presentation for.
     * @return an {@link ItemPresentation} instance for the given element.
     */
    @NotNull
    public static ItemPresentation getPresentation(@NotNull SleighTokenDefinition element) {
        return new PresentationData(getPlaceholderText(element) + " (" + element.getSize() + ")", getContainingFile(element), PlatformIcons.CLASS_ICON,
                null);
    }

    @Nullable
    public static Integer getSize(@NotNull SleighTokenDefinition element) {
        return element.getPositiveIntegerValue().toInteger();
    }

    /**
     * Extract the endianness of a {@link SleighTokenDefinition} element.
     *
     * @param element the element to get the endianness for.
     * @return a member of the {@link Endianness} enumeration indicating the element's endianness.
     */
    @NotNull
    public static Endianness getEndianness(@NotNull SleighTokenDefinition element) {
        SleighEndian endianness = element.getEndian();
        if (endianness == null) {
            return Endianness.DEFAULT;
        }

        if (endianness.getExternalDefinition() != null) {
            return Endianness.EXTERNAL;
        }

        if (endianness.getKeyBig() != null) {
            return Endianness.BIG;
        }

        return Endianness.LITTLE;
    }

    /**
     * Extract the hexadecimal base flags of a {@link SleighTokenFieldDefinition} element.
     *
     * @param element the element to get the hexadecimal base of.
     * @return a list of {@code PsiElement} containing the hexadecimal base elements.
     */
    @NotNull
    public static List<PsiElement> getHex(@NotNull SleighTokenFieldDefinition element) {
        return element.getTokenFieldModifierList().stream()
                .map(SleighTokenFieldModifier::getKeyHex)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Extract the decimal base flags of a {@link SleighTokenFieldDefinition} element.
     *
     * @param element the element to get the decimal base of.
     * @return a list of {@code PsiElement} containing the decimal base elements.
     */
    @NotNull
    public static List<PsiElement> getDec(@NotNull SleighTokenFieldDefinition element) {
        return element.getTokenFieldModifierList().stream()
                .map(SleighTokenFieldModifier::getKeyDec)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Extract the signed flags of a {@link SleighTokenFieldDefinition} element.
     *
     * @param element the element to get the signed of.
     * @return a list of {@code PsiElement} containing the signed elements.
     */
    @NotNull
    public static List<PsiElement> getSigned(@NotNull SleighTokenFieldDefinition element) {
        return element.getTokenFieldModifierList().stream()
                .map(SleighTokenFieldModifier::getKeySigned)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @NotNull
    public static ItemPresentation getPresentation(@NotNull SleighTokenFieldDefinition element) {
        Integer startBit = element.getBitStart().toInteger();
        Integer endBit = Objects.requireNonNull(element.getBitEnd()).toInteger();

        return new PresentationData(
                String.format("%s (%s, %s)", element.getSymbol().getValue(),
                        startBit != null ? startBit.toString() : "?", endBit != null ? endBit.toString() : "?"),
                getContainingFile(element), PlatformIcons.CLASS_ICON,
                null);
    }

    @NotNull
    public static String getValue(@NotNull SleighSymbol symbol) {
        if (symbol.getExternalDefinition() != null) {
            return symbol.getExternalDefinition().getText();
        }

        assert symbol.getSymbolString() != null;
        return symbol.getSymbolString().getText();
    }

    public static boolean isExternal(@NotNull SleighSymbol symbol) {
        return symbol.getExternalDefinition() != null;
    }

    public static int toInteger(@NotNull SleighPositiveInteger value) {
        return baseAwareIntegerParser(value.getText());
    }

    public static int toInteger(@NotNull SleighNegativeInteger value) {
        return -baseAwareIntegerParser(value.getText().substring(1));
    }

    public static boolean isExternal(@NotNull SleighPositiveIntegerValue value) {
        return value.getExternalDefinition() != null;
    }

    public static boolean isExternal(@NotNull SleighNegativeIntegerValue value) {
        return value.getExternalDefinition() != null;
    }

    public static boolean isExternal(@NotNull SleighIntegerValue value) {
        if (value.getPositiveIntegerValue() != null) {
            return value.getPositiveIntegerValue().isExternal();
        }

        return Objects.requireNonNull(value.getNegativeIntegerValue()).isExternal();
    }

    @Nullable
    public static Integer toInteger(@NotNull SleighPositiveIntegerValue value) {
        if (value.isExternal()) {
            return null;
        }

        return Objects.requireNonNull(value.getPositiveInteger()).toInteger();
    }

    @Nullable
    public static Integer toInteger(@NotNull SleighNegativeIntegerValue value) {
        if (value.isExternal()) {
            return null;
        }

        return Objects.requireNonNull(value.getNegativeInteger()).toInteger();
    }

    @Nullable
    public static Integer toInteger(@NotNull SleighIntegerValue value) {
        if (value.isExternal()) {
            return null;
        }

        if (value.getPositiveIntegerValue() != null) {
            return value.getPositiveIntegerValue().toInteger();
        } else {
            return Objects.requireNonNull(value.getNegativeIntegerValue()).toInteger();
        }
    }
}
