// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi.impl;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PlatformIcons;
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

    // Accessors for SleighAligndef

    /**
     * Extract the alignment value from a {@link SleighAligndef} element.
     *
     * @param element the element to get the alignment value from.
     * @return the alignment value.
     */
    public static int getAlignment(@NotNull SleighAligndef element) {
        SleighInteger integer = PsiTreeUtil.findChildOfType(element, SleighInteger.class);
        assert integer != null;
        return integer.toInteger();
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
        return String.format("%s (size: %d, word size: %d)", getName(element), getSize(element), getWordSize(element));
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
     * Extract the size of a {@link SleighSpaceDefinition} element.
     *
     * @param element the element to get the size of.
     * @return the space's size.
     * @throws it.frob.sleighidea.model.ModelException if the incorrect number of modifiers is found.
     */
    public static int getSize(@NotNull SleighSpaceDefinition element) throws ModelException {
        SleighSpaceSizeModifier[] sizeModifiers = PsiTreeUtil.findChildrenOfType(element, SleighSpaceSizeModifier.class)
                .toArray(new SleighSpaceSizeModifier[0]);
        if (sizeModifiers.length != 1) {
            throw new ModelException(String.format("Invalid size modifiers found for `%s`: %d.",
                    element.getText(), sizeModifiers.length));
        }
        return sizeModifiers[0].getInteger().toInteger();
    }

    /**
     * Extract the word size of a {@link SleighSpaceDefinition} element.
     *
     * @param element the element to get the word size of.
     * @return the space's word size, defaulting to {@code 1} if none is defined.
     * @throws it.frob.sleighidea.model.ModelException if the incorrect number of modifiers is found.
     */
    public static int getWordSize(@NotNull SleighSpaceDefinition element) throws ModelException {
        SleighSpaceWordsizeModifier[] wordSizeModifiers =
                PsiTreeUtil.findChildrenOfType(element, SleighSpaceWordsizeModifier.class)
                        .toArray(new SleighSpaceWordsizeModifier[0]);

        switch (wordSizeModifiers.length) {
            case 0:
                return 1;

            case 1:
                return wordSizeModifiers[0].getInteger().toInteger();

            default:
                throw new ModelException(String.format("Invalid word size modifiers found for `%s`: %d.",
                        element.getText(), wordSizeModifiers.length));
        }
    }

    /**
     * Extract the type of a {@link SleighSpaceDefinition} element.
     *
     * @param element the element to get the type of.
     * @return the space's type, or {@code null} if none could be extracted.
     * @throws it.frob.sleighidea.model.ModelException if the incorrect number of modifiers is found.
     */
    public static @Nullable String getType(@NotNull SleighSpaceDefinition element) throws ModelException {
        SleighSpaceTypeModifier[] typeModifiers = PsiTreeUtil.findChildrenOfType(element, SleighSpaceTypeModifier.class)
                .toArray(new SleighSpaceTypeModifier[0]);
        switch (typeModifiers.length) {
            case 0:
                return null;

            case 1:
                return typeModifiers[0].getSpaceType().getText();

            default:
                throw new ModelException(String.format("Invalid type modifiers found for `%s`: %d.",
                        element.getText(), typeModifiers.length));
        }
    }

    /**
     * Extract the default flag of a {@link SleighSpaceDefinition} element.
     *
     * @param element the element to get the default flag of.
     * @return {@code true} if the space acts as a default space, {@code false} otherwise.
     */
    public static boolean isDefault(@NotNull SleighSpaceDefinition element) {
        return Arrays.stream(
                        PsiTreeUtil.collectElements(PsiTreeUtil.findChildOfType(element, SleighSpaceModifier.class),
                                spaceElement -> SleighTypes.KEY_DEFAULT.toString().equals(spaceElement.getText())))
                .findAny()
                .isPresent();
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

    // Accessors for SleighVarnodedef elements (and their children)

    /**
     * Extract the identifiers list contained in the given {@link SleighVarnodedef}.
     *
     * @param varnode the {@link SleighVarnodedef} instance to extract data from.
     * @return a list of identifiers defined in the source element, in string form.
     */
    @NotNull
    public static List<String> getIdentifiers(@NotNull SleighVarnodedef varnode) {
        return varnode.getIdentifierlist().getIdOrWildList().stream()
                .map(PsiElement::getText)
                .collect(Collectors.toList());
    }

    /**
     * Extract the variables list's start offset for the given {@link SleighVarnodedef}.
     *
     * @param varnode the {@link SleighVarnodedef} instance to extract data from.
     * @return the starting offset for the input {@link SleighVarnodedef}.
     */
    public static int getOffset(@NotNull SleighVarnodedef varnode) {
        Collection<SleighInteger> integers = PsiTreeUtil.findChildrenOfType(varnode, SleighInteger.class);
        return integers.toArray(new SleighInteger[0])[0].toInteger();
    }

    /**
     * Extract the variables list's size for the given {@link SleighVarnodedef}.
     *
     * @param varnode the {@link SleighVarnodedef} instance to extract data from.
     * @return the variables size for the input {@link SleighVarnodedef}.
     */
    public static int getSize(@NotNull SleighVarnodedef varnode) {
        Collection<SleighInteger> integers = PsiTreeUtil.findChildrenOfType(varnode, SleighInteger.class);
        return integers.toArray(new SleighInteger[0])[1].toInteger();
    }

    /**
     * Extract the given {@link SleighVarnodedef}'s bound memory space name.
     *
     * @param varnode the {@link SleighVarnodedef} instance to extract data from.
     * @return the bound memory space name for the input element.
     */
    @NotNull
    public static String getSpaceName(@NotNull SleighVarnodedef varnode) {
        return varnode.getIdentifier().getText().trim();
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
     * @return the token's name, or null if none could be extracted.
     */
    @Nullable
    public static String getName(@NotNull SleighTokenDefinition element) {
        SleighIdentifier identifier = PsiTreeUtil.findChildOfType(element, SleighIdentifier.class);
        return identifier != null ? identifier.getText().trim() : null;
    }

    /**
     * Extract a placeholder text string from a {@link SleighTokenDefinition} element.
     *
     * @param element the element to get the placeholder text for.
     * @return the placeholder text derived from the given element.
     */
    public static @Nullable String getPlaceholderText(@NotNull SleighTokenDefinition element) {
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
}
