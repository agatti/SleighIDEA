// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import it.frob.sleighidea.psi.SleighTypes;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class SleighSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey KEYWORD = createTextAttributesKey("SLEIGH_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey COMMENT =
            createTextAttributesKey("SLEIGH_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("SLEIGH_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
    public static final TextAttributesKey NUMBER = createTextAttributesKey("SLEIGH_NUMBER", DefaultLanguageHighlighterColors.NUMBER);


    private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
    private static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{KEYWORD};
    private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
    private static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{NUMBER};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new SleighLexerAdapter();
    }

    @NotNull
    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (tokenType.equals(SleighTypes.COMMENT)) {
            return COMMENT_KEYS;
        } else if (tokenType.equals(TokenType.BAD_CHARACTER)) {
            return BAD_CHAR_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_DEFINE)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_SIZE)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_WORDSIZE)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_DEFAULT)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_TOKEN)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_OFFSET)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_SIGNED)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_HEX)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_DEC)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.RES_IS)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.RES_IF)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.RES_WITH)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_EXPORT)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_MACRO)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_LOCAL)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_PCODEOP)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_TYPE)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_ENDIAN)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_BIG)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_LITTLE)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_SPACE)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.KEY_ALIGNMENT)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.HEXNUMBER)) {
            return NUMBER_KEYS;
        } else if (tokenType.equals(SleighTypes.BINNUMBER)) {
            return NUMBER_KEYS;
        } else if (tokenType.equals(SleighTypes.DECNUMBER)) {
            return NUMBER_KEYS;
        } else {
            return EMPTY_KEYS;
        }
    }

}
