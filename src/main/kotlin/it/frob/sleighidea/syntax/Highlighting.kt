// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.syntax

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.util.PlatformIcons
import it.frob.sleighidea.SleighIcons
import it.frob.sleighidea.SleighLexerAdapter
import it.frob.sleighidea.isBuiltInJumpTarget
import it.frob.sleighidea.isStandardLibraryCall
import it.frob.sleighidea.psi.*

class SyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): SyntaxHighlighter =
        SyntaxHighlighting()
}

class SyntaxHighlighting : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer = SleighLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when {
        KEYWORD_TOKENS.contains(tokenType) -> KEYWORD_KEYS
        PREPROCESSOR_TOKENS.contains(tokenType) -> PREPROCESSOR_KEYS
        NUMBER_TOKENS.contains(tokenType) -> NUMBER_KEYS
        tokenType == SleighTypes.COMMENT -> COMMENT_KEYS
        tokenType == SleighTypes.QUOTED_STRING -> STRING_KEYS
        tokenType == TokenType.BAD_CHARACTER -> BAD_CHAR_KEYS
        else -> emptyArray()
    }

    companion object {

        @JvmField
        val KEYWORD =
            TextAttributesKey.createTextAttributesKey("SLEIGH_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)

        val COMMENT =
            TextAttributesKey.createTextAttributesKey("SLEIGH_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)

        @JvmField
        val BAD_CHARACTER =
            TextAttributesKey.createTextAttributesKey("SLEIGH_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        @JvmField
        val NUMBER = TextAttributesKey.createTextAttributesKey("SLEIGH_NUMBER", DefaultLanguageHighlighterColors.NUMBER)

        @JvmField
        val LABEL = TextAttributesKey.createTextAttributesKey("SLEIGH_LABEL", DefaultLanguageHighlighterColors.LABEL)

        @JvmField
        val FUNCTION_CALL = TextAttributesKey.createTextAttributesKey(
            "SLEIGH_FUNCTION_CALL",
            DefaultLanguageHighlighterColors.FUNCTION_CALL
        )

        @JvmField
        val STDLIB_FUNCTION_CALL = TextAttributesKey.createTextAttributesKey(
            "SLEIGH_STDLIB_FUNCTION_CALL",
            DefaultLanguageHighlighterColors.FUNCTION_CALL
        )

        @JvmField
        val MACRO = TextAttributesKey.createTextAttributesKey(
            "SLEIGH_MACRO",
            DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
        )

        val PCODEOP = TextAttributesKey.createTextAttributesKey(
            "SLEIGH_PCODEOP",
            DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
        )

        @JvmField
        val IDENTIFIER = TextAttributesKey.createTextAttributesKey(
            "SLEIGH_IDENTIFIER",
            DefaultLanguageHighlighterColors.GLOBAL_VARIABLE
        )

        @JvmField
        val BUILT_IN_SYMBOL = TextAttributesKey.createTextAttributesKey(
            "SLEIGH_BUILT_IN_SYMBOL",
            DefaultLanguageHighlighterColors.KEYWORD
        )

        val DEFINITION =
            TextAttributesKey.createTextAttributesKey("SLEIGH_DEFINITION", DefaultLanguageHighlighterColors.MARKUP_TAG)

        @JvmField
        val PREPROCESSOR =
            TextAttributesKey.createTextAttributesKey("SLEIGH_PREPROCESSOR", DefaultLanguageHighlighterColors.METADATA)

        @JvmField
        val STRING = TextAttributesKey.createTextAttributesKey("SLEIGH_STRING", DefaultLanguageHighlighterColors.STRING)

        private val PREPROCESSOR_KEYS = arrayOf(PREPROCESSOR)
        private val BAD_CHAR_KEYS = arrayOf(BAD_CHARACTER)
        private val KEYWORD_KEYS = arrayOf(KEYWORD)
        private val COMMENT_KEYS = arrayOf(COMMENT)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val STRING_KEYS = arrayOf(STRING)
        private val EMPTY_KEYS = arrayOfNulls<TextAttributesKey>(0)

        /**
         * Keyword tokens container.
         */
        private val KEYWORD_TOKENS = TokenSet.create(
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
        )

        /**
         * Number tokens container.
         */
        private val NUMBER_TOKENS = TokenSet.create(
            SleighTypes.BINNUMBER,
            SleighTypes.DECNUMBER,
            SleighTypes.HEXNUMBER
        )

        /**
         * Preprocessor tokens container.
         */
        private val PREPROCESSOR_TOKENS = TokenSet.create(
            SleighTypes.KEY_ATDEFINE,
            SleighTypes.KEY_ATELIF,
            SleighTypes.KEY_ATELSE,
            SleighTypes.KEY_ATENDIF,
            SleighTypes.KEY_ATIF,
            SleighTypes.KEY_ATIFDEF,
            SleighTypes.KEY_ATIFNDEF,
            SleighTypes.KEY_ATINCLUDE
        )
    }
}

open class SyntaxHighlightingVisitor(protected val holder: AnnotationHolder) : SleighVisitor() {
    override fun visitLabel(visited: SleighLabel) {
        highlight(visited, holder, SyntaxHighlighting.LABEL)
    }

    override fun visitMacroDefinition(visited: SleighMacroDefinition) {
        highlight(visited.symbol, holder, SyntaxHighlighting.MACRO)
        assignGutterIcon(visited, holder, PlatformIcons.FUNCTION_ICON)
    }

    override fun visitTokenDefinition(visited: SleighTokenDefinition) {
        assignGutterIcon(visited, holder, PlatformIcons.CLASS_ICON)
    }

    override fun visitPcodeopDefinition(visited: SleighPcodeopDefinition) {
        highlight(visited.symbol, holder, SyntaxHighlighting.PCODEOP)
        assignGutterIcon(visited, holder, PlatformIcons.ABSTRACT_CLASS_ICON)
    }

    override fun visitExpressionApply(visited: SleighExpressionApply) {
        highlight(
            visited.expressionApplyName, holder,
            if (isStandardLibraryCall(visited.expressionApplyName.text)) SyntaxHighlighting.STDLIB_FUNCTION_CALL
            else SyntaxHighlighting.FUNCTION_CALL
        )
    }

    override fun visitConstructorStart(visited: SleighConstructorStart) {
        val firstChild = visited.firstChild
        if (firstChild is SleighIdentifier) {
            assignGutterIcon(visited, holder, SleighIcons.TABLE_GO)
            return
        }
        if (firstChild is SleighDisplay) {
            assignGutterIcon(visited, holder, SleighIcons.TABLE)
        }
    }

    override fun visitSymbolOrWildcard(visited: SleighSymbolOrWildcard) {
        if (visited.symbol?.isExternal == true) {
            highlight(visited, holder, SyntaxHighlighting.IDENTIFIER)
        }
    }

    override fun visitJumpDestination(visited: SleighJumpDestination) {
        visited.identifier?.let { identifier ->
            if (isBuiltInJumpTarget(identifier.text)) {
                highlight(identifier, holder, SyntaxHighlighting.BUILT_IN_SYMBOL)
            }
        }
    }

    override fun visitSpaceDefinition(visited: SleighSpaceDefinition) {
        assignGutterIcon(visited, holder, PlatformIcons.ANONYMOUS_CLASS_ICON)
    }

    override fun visitInclude(visited: SleighInclude) {
        assignGutterIcon(visited, holder, SleighIcons.FILE)
    }

    override fun visitSpaceTypeIdentifier(visited: SleighSpaceTypeIdentifier) {
        visited.externalDefinition?.let { return }
        highlight(visited, holder, SyntaxHighlighting.BUILT_IN_SYMBOL)
    }

    override fun visitConstructor(visited: SleighConstructor) {
        visited.constructorSemantic.keyUnimpl?.let {
            holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Table is not implemented.").create()
        }
    }
}
