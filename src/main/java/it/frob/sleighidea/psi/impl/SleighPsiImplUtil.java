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
import it.frob.sleighidea.model.ModelException;
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnegative;
import java.util.*;
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

    // Accessors for SleighInteger

    /**
     * Convert a {@link SleighInteger} instance to an integer, taking care of base conversion.
     *
     * @param integer the {@link SleighInteger} instance to convert.
     * @return the converted positive integer.
     */
    public static int toInteger(@NotNull SleighInteger integer) {
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

        // Handle SleighInteger elements that were constructed via meta rules.
        return baseAwareIntegerParser(integer.getText());
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
    public static String getPlaceholderText(@NotNull SleighSpaceDefinition element) throws ModelException {
        SleighIntegerValue sizeElement = element.getSize().get(0).second;
        String size = sizeElement.getExternalDefinition() != null ?
                sizeElement.getExternalDefinition().getText() :
                String.valueOf(Objects.requireNonNull(sizeElement.getInteger()).toInteger());

        String wordSize;
        List<Pair<@NotNull SleighSpaceWordsizeModifier, @NotNull SleighIntegerValue>> wordSizes = element.getWordSize();
        if (wordSizes.isEmpty()) {
            wordSize = size;
        } else {
            SleighIntegerValue wordSizeValue = wordSizes.get(0).second;
            wordSize = wordSizeValue.getExternalDefinition() != null ?
                    wordSizeValue.getExternalDefinition().getText() :
                    String.valueOf(Objects.requireNonNull(wordSizeValue.getInteger()).toInteger());
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
        String placeholderText;

        try {
            placeholderText = getPlaceholderText(element);
        } catch (ModelException ignored) {
            placeholderText = "";
        }

        return new PresentationData(placeholderText, getContainingFile(element), PlatformIcons.ANONYMOUS_CLASS_ICON,
                null);
    }

    /**
     * Extract the size modifiers of a {@link SleighSpaceDefinition} element.
     *
     * @param element the element to get the size modifiers of.
     * @return a list of {@code Pair} of {@link SleighSpaceSizeModifier} element, together with the associated
     * {@link SleighIntegerValue}.
     */
    @NotNull
    public static List<Pair<@NotNull SleighSpaceSizeModifier, @NotNull SleighIntegerValue>> getSize(@NotNull SleighSpaceDefinition element) {
        return element.getSpaceModifierList().stream()
                .map(SleighSpaceModifier::getSpaceSizeModifier)
                .filter(Objects::nonNull)
                .map(modifier -> Pair.create(modifier, modifier.getIntegerValue()))
                .collect(Collectors.toList());
    }

    /**
     * Extract the word size modifiers of a {@link SleighSpaceDefinition} element.
     *
     * @param element the element to get the word size modifiers of.
     * @return a list of {@code Pair} of {@link SleighSpaceWordsizeModifier} element, together with the associated
     * {@link SleighIntegerValue}.
     */
    @NotNull
    public static List<Pair<@NotNull SleighSpaceWordsizeModifier, @NotNull SleighIntegerValue>> getWordSize(@NotNull SleighSpaceDefinition element) {
        return element.getSpaceModifierList().stream()
                .map(SleighSpaceModifier::getSpaceWordsizeModifier)
                .filter(Objects::nonNull)
                .map(modifier -> Pair.create(modifier, modifier.getIntegerValue()))
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
                .map(modifier -> Pair.create(modifier, modifier.getSpaceType().getText()))
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

    /**
     * Extract the {@link SleighInteger} instance contained in the given {@link SleighSpaceSizeModifier}.
     *
     * @param modifier the {@link SleighSpaceSizeModifier} element to extract data from.
     * @return the {@link SleighInteger} contained in the given element.
     */
    @NotNull
    public static SleighInteger getInteger(@NotNull SleighSpaceSizeModifier modifier) {
        return Objects.requireNonNull(PsiTreeUtil.findChildOfType(modifier, SleighInteger.class));
    }

    /**
     * Extract the {@link SleighInteger} instance contained in the given {@link SleighSpaceWordsizeModifier}.
     *
     * @param modifier the {@link SleighSpaceWordsizeModifier} element to extract data from.
     * @return the {@link SleighInteger} contained in the given element.
     */
    @NotNull
    public static SleighInteger getInteger(@NotNull SleighSpaceWordsizeModifier modifier) {
        return Objects.requireNonNull(PsiTreeUtil.findChildOfType(modifier, SleighInteger.class));
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
     * Extract the name of a {@link SleighTokenFieldDefinition} element.
     *
     * @param element the element to get the name of.
     * @return the token's name, or null if none could be extracted.
     */
    @NotNull
    public static String getFieldName(@NotNull SleighTokenFieldDefinition element) {
        return element.getStrictId().getText();
    }

    /**
     * Extract the start bit of a {@link SleighTokenFieldDefinition} element.
     *
     * @param element the element to extract the start bit of.
     * @return the field's start bit.
     */
    @Nonnegative
    public static int getFieldStart(@NotNull SleighTokenFieldDefinition element) {
        return element.getIntegerList().get(0).toInteger();
    }

    /**
     * Extract the end bit of a {@link SleighTokenFieldDefinition} element.
     *
     * @param element the element to extract the end bit of.
     * @return the field's end bit.
     */
    @Nonnegative
    public static int getFieldEnd(@NotNull SleighTokenFieldDefinition element) {
        return element.getIntegerList().get(1).toInteger();
    }

    /**
     * Extract the signedness flag of a {@link SleighTokenFieldDefinition} element.
     *
     * @param element the element to get the default flag of.
     * @return {@code true} if the field is signed, {@code false} otherwise.
     */
    public static boolean isSigned(@NotNull SleighTokenFieldDefinition element) {
        return Arrays.stream(PsiTreeUtil.collectElements(
                        PsiTreeUtil.findChildOfType(element, SleighTokenFieldModifier.class),
                        fieldElement -> SleighTypes.KEY_SIGNED.toString().equals(fieldElement.getText())))
                .findAny()
                .isPresent();
    }

    // Accessors for SleighTokendef elements (and their children)

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
        return new PresentationData(getPlaceholderText(element), getContainingFile(element), PlatformIcons.CLASS_ICON,
                null);
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

    @Nullable
    public static Integer toInteger(@NotNull SleighIntegerValue value) {
        if (value.getExternalDefinition() != null) {
            return null;
        }

        return Objects.requireNonNull(value.getInteger()).toInteger();
    }
}
