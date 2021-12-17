// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.syntax

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import java.util.*
import javax.swing.Icon

internal fun markElementAsError(element: PsiElement, holder: AnnotationHolder, message: String) {
    holder.newAnnotation(HighlightSeverity.ERROR, message)
        .range(element)
        .create()
}

internal fun markElementAsError(textStart: Int, textEnd: Int, holder: AnnotationHolder, message: String) {
    holder.newAnnotation(HighlightSeverity.ERROR, message)
        .range(TextRange(textStart, textEnd))
        .create()
}

internal fun markElementAsError(
    startElement: PsiElement,
    endElement: PsiElement,
    holder: AnnotationHolder,
    message: String
) {
    holder.newAnnotation(HighlightSeverity.ERROR, message)
        .range(TextRange(startElement.textOffset, endElement.textOffset + endElement.textLength))
        .create()
}

/**
 * Set a custom highlight rule for the given element.
 *
 * @param element the element to assign a highlight rule to.
 * @param holder  the annotation holder that will bind the given rule to the given element.
 * @param key     the text attributes to use when rendering the element.
 */
internal fun highlight(
    element: PsiElement, holder: AnnotationHolder,
    key: TextAttributesKey
) {
    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(element)
        .enforcedTextAttributes(TextAttributes.ERASE_MARKER)
        .create()
    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(element)
        .enforcedTextAttributes(EditorColorsManager.getInstance().globalScheme.getAttributes(key))
        .create()
}

/**
 * Assign a gutter icon to the given element.
 *
 * @param element the element to assign an icon to.
 * @param holder  the annotation holder that will bind the given icon to the given element.
 * @param icon    the icon to draw next to the element.
 */
internal fun assignGutterIcon(element: PsiElement, holder: AnnotationHolder, icon: Icon) {
    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
        .range(element)
        .gutterIconRenderer(object : GutterIconRenderer() {
            override fun equals(other: Any?): Boolean = this === other

            override fun hashCode(): Int = System.identityHashCode(this)

            override fun getIcon(): Icon = icon
        }).create()
}

/**
 * The prefix string marking Sleigh annotations.
 */
const val SLEIGH_PREFIX_STRING = "sleigh"

/**
 * A list containing all built-in function calls.
 *
 * TODO: Use an immutable data structure.
 */
internal val STD_LIBRARY_CALL = Arrays.asList(
    "abs",
    "carry",
    "ceil",
    "cpool",
    "delayslot",
    "float2float",
    "floor",
    "int2float",
    "nan",
    "newobject",
    "round",
    "sborrow",
    "scarry",
    "sext",
    "sqrt",
    "trunc",
    "zext"
)

/**
 * A list containing all built-in symbols.
 *
 * TODO: Use an immutable data structure.
 */
internal val BUILT_IN_SYMBOLS = Arrays.asList(
    "const",
    "epsilon",
    "inst_next",
    "inst_start",
    "instruction",
    "unique"
)
