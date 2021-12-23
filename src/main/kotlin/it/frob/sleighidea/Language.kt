package it.frob.sleighidea

import com.intellij.lang.ASTNode
import com.intellij.lang.Language
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import it.frob.sleighidea.lexer.SleighLexer
import it.frob.sleighidea.parser.SleighParser
import it.frob.sleighidea.psi.SleighFile
import it.frob.sleighidea.psi.SleighTypes
import javax.swing.Icon

class SleighLanguage : Language("Sleigh") {
    companion object {
        val INSTANCE: SleighLanguage = SleighLanguage()
    }
}

open class SleighFileType protected constructor() : LanguageFileType(SleighLanguage.INSTANCE) {

    override fun getName(): String = "Sleigh File"

    override fun getDescription(): String = "Sleigh definition file"

    override fun getDefaultExtension(): String = FILE_EXTENSION

    override fun getIcon(): Icon = SleighIcons.FILE

    companion object {
        const val FILE_EXTENSION = "slaspec"

        @JvmField
        val INSTANCE = SleighFileType()
    }
}

class SleighLexerAdapter : FlexAdapter(SleighLexer(null))

class SleighParserDefinition : ParserDefinition {

    override fun getWhitespaceTokens(): TokenSet = WHITE_SPACES

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode, right: ASTNode): ParserDefinition.SpaceRequirements =
        ParserDefinition.SpaceRequirements.MAY

    override fun createLexer(project: Project): Lexer = SleighLexerAdapter()

    override fun createParser(project: Project): PsiParser = SleighParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = COMMENTS

    override fun getStringLiteralElements(): TokenSet = QSTRINGS

    override fun createElement(node: ASTNode): PsiElement = SleighTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = SleighFile(viewProvider)

    companion object {
        val WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE)
        val COMMENTS = TokenSet.create(SleighTypes.COMMENT)
        val QSTRINGS = TokenSet.create(SleighTypes.QUOTED_STRING)
        val FILE = IFileElementType(SleighLanguage.INSTANCE)
    }
}