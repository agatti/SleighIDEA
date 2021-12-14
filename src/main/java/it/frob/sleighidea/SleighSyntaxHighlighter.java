// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import it.frob.sleighidea.psi.SleighTypes;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class SleighSyntaxHighlighter extends SyntaxHighlighterBase {
    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("SLEIGH_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey COMMENT =
            createTextAttributesKey("SLEIGH_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BAD_CHARACTER =
            createTextAttributesKey("SLEIGH_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("SLEIGH_NUMBER", DefaultLanguageHighlighterColors.NUMBER);
    public static final TextAttributesKey LABEL =
            createTextAttributesKey("SLEIGH_LABEL", DefaultLanguageHighlighterColors.LABEL);
    public static final TextAttributesKey FUNCTION_CALL =
            createTextAttributesKey("SLEIGH_FUNCTION_CALL", DefaultLanguageHighlighterColors.FUNCTION_CALL);
    public static final TextAttributesKey MACRO =
            createTextAttributesKey("SLEIGH_MACRO", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey PCODEOP =
            createTextAttributesKey("SLEIGH_PCODEOP", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey IDENTIFIER =
            createTextAttributesKey("SLEIGH_IDENTIFIER", DefaultLanguageHighlighterColors.GLOBAL_VARIABLE);
    public static final TextAttributesKey BUILT_IN_SYMBOL =
            createTextAttributesKey("SLEIGH_BUILT_IN_SYMBOL", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey DEFINITION =
            createTextAttributesKey("SLEIGH_DEFINITION", DefaultLanguageHighlighterColors.MARKUP_TAG);
    public static final TextAttributesKey PREPROCESSOR =
            createTextAttributesKey("SLEIGH_PREPROCESSOR", DefaultLanguageHighlighterColors.METADATA);
    public static final TextAttributesKey STRING =
            createTextAttributesKey("SLEIGH_STRING", DefaultLanguageHighlighterColors.STRING);

    private static final TextAttributesKey[] PREPROCESSOR_KEYS = new TextAttributesKey[]{PREPROCESSOR};
    private static final TextAttributesKey[] BAD_CHAR_KEYS = new TextAttributesKey[]{BAD_CHARACTER};
    private static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{KEYWORD};
    private static final TextAttributesKey[] COMMENT_KEYS = new TextAttributesKey[]{COMMENT};
    private static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{NUMBER};
    private static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{STRING};
    private static final TextAttributesKey[] EMPTY_KEYS = new TextAttributesKey[0];

    /**
     * Keyword tokens container.
     */
    private static final TokenSet KEYWORD_TOKENS = TokenSet.create(
            SleighTypes.RES_IF,
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
    );

    /**
     * Number tokens container.
     */
    private static final TokenSet NUMBER_TOKENS = TokenSet.create(
            SleighTypes.BINNUMBER,
            SleighTypes.DECNUMBER,
            SleighTypes.HEXNUMBER
    );

    /**
     * Preprocessor tokens container.
     */
    private static final TokenSet PREPROCESSOR_TOKENS = TokenSet.create(
            SleighTypes.KEY_ATDEFINE,
            SleighTypes.KEY_ATELIF,
            SleighTypes.KEY_ATELSE,
            SleighTypes.KEY_ATENDIF,
            SleighTypes.KEY_ATIF,
            SleighTypes.KEY_ATIFDEF,
            SleighTypes.KEY_ATIFNDEF,
            SleighTypes.KEY_ATINCLUDE
    );

    @NotNull
    @Override
    public Lexer getHighlightingLexer() {
        return new SleighLexerAdapter();
    }

    @NotNull
    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        if (KEYWORD_TOKENS.contains(tokenType)) {
            return KEYWORD_KEYS;
        }

        if (PREPROCESSOR_TOKENS.contains(tokenType)) {
            return PREPROCESSOR_KEYS;
        }

        if (NUMBER_TOKENS.contains(tokenType)) {
            return NUMBER_KEYS;
        }

        if (tokenType.equals(SleighTypes.COMMENT)) {
            return COMMENT_KEYS;
        }

        if (tokenType.equals(SleighTypes.STRING)) {
            return STRING_KEYS;
        }

        if (tokenType.equals(TokenType.BAD_CHARACTER)) {
            return BAD_CHAR_KEYS;
        }

        return EMPTY_KEYS;
    }
}
