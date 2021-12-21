// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi.impl

import com.intellij.ide.projectView.PresentationData
import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.PlatformIcons
import it.frob.sleighidea.model.Endianness
import it.frob.sleighidea.psi.*

object SleighPsiImplUtil {

    /**
     * Get the file name of the given element's containing file.
     *
     * @param element the element to get the containing file name.
     * @return the containing file name as a string or `null` if none could be obtained.
     */
    fun getContainingFile(element: PsiElement): String? {
        val containingFile = element.containingFile
        return containingFile?.name
    }

    /**
     * Convert a String to an integer, taking care of base conversion.
     *
     * @param integer the string to convert.
     * @return the converted positive integer.
     */
    private fun baseAwareIntegerParser(integer: String): Int {
        val text = integer.lowercase().trim()
        return when {
            text.startsWith("-0b") -> -(text.substring(3).toInt(2))
            text.startsWith("-0x") -> -(text.substring(3).toInt(16))
            text.startsWith("0b") -> text.substring(2).toInt(2)
            text.startsWith("0x") -> text.substring(2).toInt(16)
            else -> text.toInt(10)
        }
    }

    /**
     * Extract a placeholder text string from a [SleighDisplay] element.
     *
     * @param element the element to get the placeholder text for.
     * @return the placeholder text derived from the given element.
     */
    @JvmStatic
    fun getPlaceholderText(element: SleighDisplay): String {
        val visitor = DisplayPlaceholderVisitor()
        element.acceptChildren(visitor)
        return visitor.placeholderText
    }

    /**
     * Extract a placeholder text string from a [SleighMacroDefinition] element.
     *
     * @param element the element to get the placeholder text for.
     * @return the placeholder text derived from the given element.
     */
    @JvmStatic
    fun getPlaceholderText(element: SleighMacroDefinition): String? {
        if (PsiTreeUtil.findChildOfType(element, SleighIdentifier::class.java) == null) return null
        return element.identifier.text +
                PsiTreeUtil.getChildrenOfTypeAsList(
                    PsiTreeUtil.findChildOfType(element, SleighOplist::class.java),
                    SleighIdentifier::class.java
                ).joinToString(", ", "(", ")") { item -> item.text }
    }

    /**
     * Create an [ItemPresentation] instance for the given [SleighMacroDefinition] element.
     *
     * @param element the [SleighMacroDefinition] element to create an item presentation for.
     * @return an [ItemPresentation] instance for the given element.
     */
    @JvmStatic
    fun getPresentation(element: SleighMacroDefinition): ItemPresentation = PresentationData(
        getPlaceholderText(element), getContainingFile(element),
        PlatformIcons.FUNCTION_ICON, null
    )

    /**
     * Extract the name of a [SleighSpaceDefinition] element.
     *
     * @param element the element to get the name of.
     * @return the space's name.
     */
    @JvmStatic
    fun getName(element: SleighSpaceDefinition): String = element.identifier.text

    /**
     * Extract a placeholder text string from a [SleighSpaceDefinition] element.
     *
     * @param element the element to get the placeholder text for.
     * @return the placeholder text derived from the given element.
     */
    @JvmStatic
    fun getPlaceholderText(element: SleighSpaceDefinition): String {
        val sizeElement = element.size.first().second
        val size = if (sizeElement.isExternal) sizeElement.text else sizeElement.toInteger().toString()
        val wordSize: String
        val wordSizes = element.wordSize
        wordSize = if (wordSizes.isEmpty()) {
            size
        } else {
            val wordSizeValue = wordSizes.first().second
            if (wordSizeValue.isExternal) wordSizeValue.text else wordSizeValue.toInteger().toString()
        }
        return "${getName(element)} (size: $size, word size: $wordSize)"
    }

    /**
     * Create an [ItemPresentation] instance for the given [SleighSpaceDefinition] element.
     *
     * @param element the [SleighSpaceDefinition] element to create an item presentation for.
     * @return an [ItemPresentation] instance for the given element.
     */
    @JvmStatic
    fun getPresentation(element: SleighSpaceDefinition): ItemPresentation = PresentationData(
        getPlaceholderText(element), getContainingFile(element),
        PlatformIcons.ANONYMOUS_CLASS_ICON, null
    )

    /**
     * Extract the size modifiers of a [SleighSpaceDefinition] element.
     *
     * @param element the element to get the size modifiers of.
     * @return a list of `Pair` of [SleighSpaceSizeModifier] element, together with the associated
     * [SleighInteger].
     */
    @JvmStatic
    fun getSize(element: SleighSpaceDefinition): List<Pair<SleighSpaceSizeModifier?, SleighInteger>> =
        element.spaceModifierList
            .mapNotNull { obj: SleighSpaceModifier ->
                obj.spaceSizeModifier?.let { modifier ->
                    return@mapNotNull Pair.create(
                        modifier,
                        modifier.integer
                    )
                }
            }
            .toList()

    /**
     * Extract the word size modifiers of a [SleighSpaceDefinition] element.
     *
     * @param element the element to get the word size modifiers of.
     * @return a list of `Pair` of [SleighSpaceWordsizeModifier] element, together with the associated
     * [SleighInteger].
     */
    @JvmStatic
    fun getWordSize(element: SleighSpaceDefinition): List<Pair<SleighSpaceWordsizeModifier?, SleighInteger>> =
        element.spaceModifierList
            .mapNotNull { obj: SleighSpaceModifier ->
                obj.spaceWordsizeModifier?.let { modifier ->
                    return@mapNotNull Pair.create(
                        modifier,
                        modifier.integer
                    )
                }
            }
            .toList()

    /**
     * Extract the type modifiers of a [SleighSpaceDefinition] element.
     *
     * @param element the element to get the word size modifiers of.
     * @return a list of `Pair` of [SleighSpaceTypeModifier] element, together with the associated string.
     */
    @JvmStatic
    fun getType(element: SleighSpaceDefinition): List<Pair<SleighSpaceTypeModifier?, String?>> =
        element.spaceModifierList
            .mapNotNull { obj: SleighSpaceModifier ->
                obj.spaceTypeModifier?.let { modifier ->
                    Pair.create(
                        modifier,
                        modifier.spaceTypeIdentifier.text
                    )
                }
            }
            .toList()

    /**
     * Extract the default flags of a [SleighSpaceDefinition] element.
     *
     * @param element the element to get the default flags of.
     * @return a list of `PsiElement` containing the default flag elements.
     */
    @JvmStatic
    fun getDefault(element: SleighSpaceDefinition): List<PsiElement?> = element.spaceModifierList
        .mapNotNull { obj: SleighSpaceModifier -> obj.keyDefault }
        .toList()

    /**
     * Extract the given [SleighVariablesNodeDefinition]'s bound memory space symbol.
     *
     * @param node the [SleighVariablesNodeDefinition] instance to extract data from.
     * @return the bound memory space symbol for the input element.
     */
    @JvmStatic
    fun getSpace(node: SleighVariablesNodeDefinition): SleighSymbol = node.symbol

    /**
     * Extract the name of a [SleighTokenDefinition] element.
     *
     * @param element the element to get the name of.
     * @return the token's name.
     */
    @JvmStatic
    fun getName(element: SleighTokenDefinition): String = element.symbol.value

    /**
     * Extract a placeholder text string from a [SleighTokenDefinition] element.
     *
     * @param element the element to get the placeholder text for.
     * @return the placeholder text derived from the given element.
     */
    @JvmStatic
    fun getPlaceholderText(element: SleighTokenDefinition): String = getName(element)

    /**
     * Create an [ItemPresentation] instance for the given [SleighTokenDefinition] element.
     *
     * @param element the [SleighTokenDefinition] element to create an item presentation for.
     * @return an [ItemPresentation] instance for the given element.
     */
    @JvmStatic
    fun getPresentation(element: SleighTokenDefinition): ItemPresentation = PresentationData(
        "${getPlaceholderText(element)} (${element.size ?: "?"})",
        getContainingFile(element),
        PlatformIcons.CLASS_ICON,
        null
    )

    @JvmStatic
    fun getSize(element: SleighTokenDefinition): Int? = element.integer.toInteger()

    /**
     * Extract the endianness of a [SleighTokenDefinition] element.
     *
     * @param element the element to get the endianness for.
     * @return a member of the [Endianness] enumeration indicating the element's endianness.
     */
    @JvmStatic
    fun getEndianness(element: SleighTokenDefinition): Endianness = when {
        element.endian == null -> Endianness.DEFAULT
        element.endian?.externalDefinition != null -> Endianness.EXTERNAL
        element.endian?.keyBig != null -> Endianness.BIG
        element.endian?.keyLittle != null -> Endianness.LITTLE
        else -> throw RuntimeException("Invalid endianness value.")
    }

    /**
     * Extract the hexadecimal base flags of a [SleighTokenFieldDefinition] element.
     *
     * @param element the element to get the hexadecimal base of.
     * @return a list of `PsiElement` containing the hexadecimal base elements.
     */
    @JvmStatic
    fun getHex(element: SleighTokenFieldDefinition): List<PsiElement?> = element.tokenFieldModifierList
        .mapNotNull { obj: SleighTokenFieldModifier -> obj.keyHex }
        .toList()

    /**
     * Extract the decimal base flags of a [SleighTokenFieldDefinition] element.
     *
     * @param element the element to get the decimal base of.
     * @return a list of `PsiElement` containing the decimal base elements.
     */
    @JvmStatic
    fun getDec(element: SleighTokenFieldDefinition): List<PsiElement?> = element.tokenFieldModifierList
        .mapNotNull { obj: SleighTokenFieldModifier -> obj.keyDec }
        .toList()

    /**
     * Extract the signed flags of a [SleighTokenFieldDefinition] element.
     *
     * @param element the element to get the signed flag of.
     * @return a list of `PsiElement` containing the signed elements.
     */
    @JvmStatic
    fun getSigned(element: SleighTokenFieldDefinition): List<PsiElement?> = element.tokenFieldModifierList
        .mapNotNull { obj: SleighTokenFieldModifier -> obj.keySigned }
        .toList()

    @JvmStatic
    fun getPresentation(element: SleighTokenFieldDefinition): ItemPresentation {
        val startBit = element.bitStart.toInteger() ?: "?"
        val endBit = element.bitEnd!!.toInteger() ?: "?"
        return PresentationData(
            "${element.symbol.value} ($startBit, $endBit)",
            getContainingFile(element), PlatformIcons.CLASS_ICON,
            null
        )
    }

    @JvmStatic
    fun getValue(symbol: SleighSymbol): String = symbol.externalDefinition?.text ?: symbol.symbolString!!.text

    @JvmStatic
    fun isExternal(symbol: SleighSymbol): Boolean = symbol.externalDefinition != null

    @JvmStatic
    fun toInteger(value: SleighInteger): Int? = if (value.isExternal) null else baseAwareIntegerParser(value.text)

    @JvmStatic
    fun isExternal(element: SleighInteger): Boolean = element.externalDefinition != null
}
