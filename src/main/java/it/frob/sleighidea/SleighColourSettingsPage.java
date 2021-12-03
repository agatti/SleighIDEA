// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class SleighColourSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
            new AttributesDescriptor("Bad value", SleighSyntaxHighlighter.BAD_CHARACTER),
            new AttributesDescriptor("Function call", SleighSyntaxHighlighter.FUNCTION_CALL),
            new AttributesDescriptor("Keyword", SleighSyntaxHighlighter.KEYWORD),
            new AttributesDescriptor("Label", SleighSyntaxHighlighter.LABEL),
            new AttributesDescriptor("Number", SleighSyntaxHighlighter.NUMBER),
    };

    @Nullable
    @Override
    public Icon getIcon() {
        return SleighIcons.FILE;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new SleighSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "# sleigh specification file\n" +
                "define endian=little;\n" +
                "define alignment=1;\n" +
                "define space RAM type=ram_space size=2 default;\n" +
                "define register offset=0x00 size=1 [ A X Y P ];\n" +
                "define token opbyte (8)\n" +
                "   op       = (0,7)\n" +
                ";\n" +
                "define pcodeop readIRQ;\n" +
                "REL: reloc is rel [ reloc = inst_next + rel; ] { export *:2 reloc; } \n" +
                ":NOP is op=0b00000000 {\n" +
                "if carry(A, B) goto next_inst;\n" +
                "<skip>\n" +
                "}";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @NotNull
    @Override
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @NotNull
    @Override
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Sleigh";
    }
}
