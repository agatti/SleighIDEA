// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import it.frob.sleighidea.syntax.SyntaxHighlighting
import javax.swing.Icon

class SleighColourSettingsPage : ColorSettingsPage {

    override fun getIcon(): Icon? = SleighIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = SyntaxHighlighting()

    override fun getDemoText(): String = """# sleigh specification file
@include "file.sinc"
define endian=little;
define alignment=1;
define space RAM type=ram_space size=2 default;
define register offset=0x00 size=1 [ <identifier>A</identifier> <identifier>B</identifier> ];
define token opbyte (8)
op       = (0,7) signed dec
;
macro <macro>testmacro</macro>() {}
define pcodeop <pcodeop>readIRQ</pcodeop>;
REL: reloc is rel [ reloc = inst_next + rel; ] { export *:2 reloc; } 
:NOP is op=0b00000000 {
if <function>carry</function>(A, B) goto <builtinsymbol>next_inst</builtinsymbol>;
<label><skip></label>
}"""

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = ATTRIBUTES_KEY_MAP

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "Sleigh"

    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Bad value", SyntaxHighlighting.BAD_CHARACTER),
            AttributesDescriptor("Built-in symbol", SyntaxHighlighting.BUILT_IN_SYMBOL),
            AttributesDescriptor("Function call", SyntaxHighlighting.FUNCTION_CALL),
            AttributesDescriptor("Keyword", SyntaxHighlighting.KEYWORD),
            AttributesDescriptor("Identifier", SyntaxHighlighting.IDENTIFIER),
            AttributesDescriptor("Label", SyntaxHighlighting.LABEL),
            AttributesDescriptor("Macro and PcodeOp", SyntaxHighlighting.MACRO),
            AttributesDescriptor("Number", SyntaxHighlighting.NUMBER),
            AttributesDescriptor("Preprocessor directive", SyntaxHighlighting.PREPROCESSOR),
            AttributesDescriptor("String", SyntaxHighlighting.STRING)
        )

        private val ATTRIBUTES_KEY_MAP: Map<String, TextAttributesKey> = mapOf(
            Pair("builtinsymbol", SyntaxHighlighting.BUILT_IN_SYMBOL),
            Pair("function", SyntaxHighlighting.FUNCTION_CALL),
            Pair("label", SyntaxHighlighting.LABEL),
            Pair("identifier", SyntaxHighlighting.IDENTIFIER),
            Pair("macro", SyntaxHighlighting.MACRO),
            Pair("pcodeop", SyntaxHighlighting.MACRO),
        )
    }
}