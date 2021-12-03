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

import java.util.Arrays;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class SleighSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey KEYWORD = createTextAttributesKey("SLEIGH_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey COMMENT =
            createTextAttributesKey("SLEIGH_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("SLEIGH_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
    public static final TextAttributesKey NUMBER = createTextAttributesKey("SLEIGH_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey LABEL = createTextAttributesKey("LABEL", DefaultLanguageHighlighterColors.LABEL);
    public static final TextAttributesKey FUNCTION_CALL = createTextAttributesKey("FUNCTION_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);

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

    /**
     * Check if the given token type is for a keyword or not.
     *
     * @param tokenType the token type to check.
     * @return true if the token is for a keyword, false otherwise.
     */
    private boolean isKeyword(IElementType tokenType) {
        return Arrays.asList(SleighTypes.RES_IF,
                SleighTypes.RES_IS,
                SleighTypes.RES_WITH,
                SleighTypes.KEY_ALIGNMENT,
                SleighTypes.KEY_ATTACH,
                SleighTypes.KEY_BIG,
                SleighTypes.KEY_BITRANGE,
                SleighTypes.KEY_BUILD,
                SleighTypes.KEY_CALL,
                SleighTypes.KEY_CONTEXT,
                SleighTypes.KEY_CROSSBUILD,
                SleighTypes.KEY_DEC,
                SleighTypes.KEY_DEFAULT,
                SleighTypes.KEY_DEFINE,
                SleighTypes.KEY_DEFINED,
                SleighTypes.KEY_ENDIAN,
                SleighTypes.KEY_EXPORT,
                SleighTypes.KEY_GOTO,
                SleighTypes.KEY_HEX,
                SleighTypes.KEY_LITTLE,
                SleighTypes.KEY_LOCAL,
                SleighTypes.KEY_MACRO,
                SleighTypes.KEY_NAMES,
                SleighTypes.KEY_NOFLOW,
                SleighTypes.KEY_OFFSET,
                SleighTypes.KEY_PCODEOP,
                SleighTypes.KEY_RETURN,
                SleighTypes.KEY_SIGNED,
                SleighTypes.KEY_SIZE,
                SleighTypes.KEY_SPACE,
                SleighTypes.KEY_TOKEN,
                SleighTypes.KEY_TYPE,
                SleighTypes.KEY_UNIMPL,
                SleighTypes.KEY_VALUES,
                SleighTypes.KEY_VARIABLES,
                SleighTypes.KEY_WORDSIZE
        ).contains(tokenType);
    }

    @NotNull
    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (isKeyword(tokenType)) {
            return KEYWORD_KEYS;
        } else if (tokenType.equals(SleighTypes.COMMENT)) {
            return COMMENT_KEYS;
        } else if (tokenType.equals(TokenType.BAD_CHARACTER)) {
            return BAD_CHAR_KEYS;
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
