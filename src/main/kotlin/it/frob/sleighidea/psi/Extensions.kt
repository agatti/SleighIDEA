// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi

import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import it.frob.sleighidea.model.Endianness
import java.util.stream.Collectors

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

val SleighInteger.isExternal: Boolean
    get() = externalDefinition != null

fun SleighInteger.toInteger(): Int? = if (isExternal) null else baseAwareIntegerParser(text)

val SleighSymbol.value: String
    get() = externalDefinition?.text ?: symbolString!!.text

val SleighSymbol.isExternal: Boolean
    get() = externalDefinition != null

private fun getDisplayPlaceholderText(element: SleighDisplay): String {
    val visitor = DisplayPlaceholderVisitor()
    element.acceptChildren(visitor)
    return visitor.placeholderText
}

val SleighDisplay.placeholderText: String
    get() = getDisplayPlaceholderText(this)

val SleighMacroDefinition.placeholderText: String
    get() = symbol.value +
            arguments.identifierList.joinToString(", ", "(", ")") { item -> item.text }

val SleighTokenDefinition.placeholderText: String
    get() = name!!

val SleighTokenDefinition.size: Int?
    get() = integer.toInteger()

val SleighTokenDefinition.endianness: Endianness
    get() = when {
        endian == null -> Endianness.DEFAULT
        endian?.externalDefinition != null -> Endianness.EXTERNAL
        endian?.keyBig != null -> Endianness.BIG
        endian?.keyLittle != null -> Endianness.LITTLE
        else -> throw RuntimeException("Invalid endianness value.")
    }

val SleighTokenFieldDefinition.hexElements: List<PsiElement>
    get() = this.tokenFieldModifierList
        .mapNotNull { obj: SleighTokenFieldModifier -> obj.keyHex }
        .toList()

val SleighTokenFieldDefinition.decElements: List<PsiElement>
    get() = this.tokenFieldModifierList
        .mapNotNull { obj: SleighTokenFieldModifier -> obj.keyDec }
        .toList()

val SleighTokenFieldDefinition.signedElements: List<PsiElement>
    get() = this.tokenFieldModifierList
        .mapNotNull { obj: SleighTokenFieldModifier -> obj.keySigned }
        .toList()

val SleighTokenFieldDefinition.bitStart: SleighInteger
    get() = rangePair.integerList[0]

val SleighTokenFieldDefinition.bitEnd: SleighInteger
    get() = rangePair.integerList[1]

/**
 * Extract a placeholder text string from a [SleighSpaceDefinition] element.
 *
 * @param element the element to get the placeholder text for.
 * @return the placeholder text derived from the given element.
 */
private fun getSpacePlaceholderText(element: SleighSpaceDefinition): String {
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
    return "${element.name} (size: $size, word size: $wordSize)"
}

val SleighSpaceDefinition.placeholderText: String
    get() = getSpacePlaceholderText(this)

/**
 * Extract the size modifiers of a [SleighSpaceDefinition] element.
 *
 * @param element the element to get the size modifiers of.
 * @return a list of `Pair` of [SleighSpaceSizeModifier] element, together with the associated
 * [SleighInteger].
 */
private fun getSpaceSize(element: SleighSpaceDefinition): List<Pair<SleighSpaceSizeModifier, SleighInteger>> =
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

val SleighSpaceDefinition.size: List<Pair<SleighSpaceSizeModifier, SleighInteger>>
    get() = getSpaceSize(this)

/**
 * Extract the word size modifiers of a [SleighSpaceDefinition] element.
 *
 * @param element the element to get the word size modifiers of.
 * @return a list of `Pair` of [SleighSpaceWordsizeModifier] element, together with the associated
 * [SleighInteger].
 */
private fun getSpaceWordSize(element: SleighSpaceDefinition): List<Pair<SleighSpaceWordsizeModifier, SleighInteger>> =
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

val SleighSpaceDefinition.wordSize: List<Pair<SleighSpaceWordsizeModifier, SleighInteger>>
    get() = getSpaceWordSize(this)

/**
 * Extract the type modifiers of a [SleighSpaceDefinition] element.
 *
 * @param element the element to get the word size modifiers of.
 * @return a list of `Pair` of [SleighSpaceTypeModifier] element, together with the associated string.
 */
private fun getSpaceType(element: SleighSpaceDefinition): List<Pair<SleighSpaceTypeModifier, String?>> =
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

val SleighSpaceDefinition.type: List<Pair<SleighSpaceTypeModifier, String?>>
    get() = getSpaceType(this)

val SleighSpaceDefinition.default: List<PsiElement>
    get() = spaceModifierList.mapNotNull { obj: SleighSpaceModifier -> obj.keyDefault }.toList()

val SleighVariablesNodeDefinition.space: SleighSymbol
    get() = this.symbol

val SleighVariablesNodeDefinition.offset: SleighInteger
    get() = integerList[0]

val SleighVariablesNodeDefinition.size: SleighInteger
    get() = integerList[1]

val SleighBitRange.lValue: SleighSymbol
    get() = symbolList[0]

val SleighBitRange.rValue: SleighSymbol
    get() = symbolList[0]

val SleighBitRange.bitStart: SleighInteger
    get() = integerList[0]

val SleighBitRange.bitEnd: SleighInteger
    get() = integerList[1]

internal class DisplayPlaceholderVisitor : SleighVisitor() {
    private enum class SleighDisplayVisitingState {
        COLLECTING_NAME, COLLECTING_ARGUMENTS
    }

    private var state = SleighDisplayVisitingState.COLLECTING_NAME
    private val builder = StringBuilder()
    private val arguments: MutableList<PsiElement> = ArrayList()
    override fun visitPrintPiece(visited: SleighPrintPiece) {
        when (state) {
            SleighDisplayVisitingState.COLLECTING_NAME -> builder.append(visited.text.trim())
            SleighDisplayVisitingState.COLLECTING_ARGUMENTS -> arguments.add(visited)
        }
    }

    override fun visitWhiteSpace(space: PsiWhiteSpace) {
        when (state) {
            SleighDisplayVisitingState.COLLECTING_NAME -> state = SleighDisplayVisitingState.COLLECTING_ARGUMENTS
            SleighDisplayVisitingState.COLLECTING_ARGUMENTS -> if (arguments[arguments.size - 1] !is PsiWhiteSpace) {
                arguments.add(PsiWhiteSpaceImpl(" "))
            }
        }
    }

    val placeholderText: String
        get() {
            if (arguments.isNotEmpty()) {
                builder.append(" ")
                    .append(arguments.stream()
                        .map { obj: PsiElement -> obj.text }
                        .collect(Collectors.joining("")).trim())
            }
            return builder.toString()
        }
}