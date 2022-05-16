package it.frob.sleighidea

import com.intellij.lang.*
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.lexer.FlexAdapter
import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.PsiTreeUtil
import it.frob.sleighidea.lexer.SleighLexer
import it.frob.sleighidea.parser.SleighParser
import it.frob.sleighidea.psi.*
import it.frob.sleighidea.syntax.SLEIGH_PREFIX_STRING
import it.frob.sleighidea.syntax.SyntaxHighlighting
import javax.swing.Icon

/**
 * A list containing all built-in function calls.
 */
private val STD_LIBRARY_CALL = listOf(
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

fun isStandardLibraryCall(functionName: String): Boolean = functionName.trim() in STD_LIBRARY_CALL

/**
 * A list containing all built-in symbols.
 */
val BUILT_IN_SYMBOLS = listOf(
    "const",
    "epsilon",
    "inst_next",
    "inst_start",
    "instruction",
    "unique"
)

fun isBuiltInSymbol(symbolName: String): Boolean = symbolName.trim() in BUILT_IN_SYMBOLS

fun isBuiltInJumpTarget(symbolName: String): Boolean =
    isBuiltInSymbol(symbolName) && (symbolName == "inst_start" || symbolName == "inst_next")

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
        ParserDefinition.SpaceRequirements.MUST

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

class SleighBraceMatcher : PairedBraceMatcher {

    override fun getPairs(): Array<BracePair> = arrayOf(
        BracePair(SleighTypes.LBRACE, SleighTypes.RBRACE, true),
        BracePair(SleighTypes.LPAREN, SleighTypes.RPAREN, true),
        BracePair(SleighTypes.LBRACKET, SleighTypes.RBRACKET, true)
    )

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = false

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int = openingBraceOffset
}

class SleighColourSettingsPage : ColorSettingsPage {

    override fun getIcon(): Icon = SleighIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = SyntaxHighlighting()

    override fun getDemoText(): String = """# sleigh specification file
@include "file.sinc"
@define <def>DEFINEINT</def> 1
@define <def>DEFINESTR</def> "a"
define endian=little;
define alignment=1;
define space RAM type=ram_space size=2 default;
define register offset=0x00 size=1 [ <identifier>A</identifier> <identifier>B</identifier> ];
define token opbyte (8) op = (0,7) signed dec;
macro <macro>testmacro</macro>() {}
define pcodeop <pcodeop>readIRQ</pcodeop>;
REL: reloc is rel [ reloc = inst_next + rel; ] { export *:2 reloc; } 
:NOP is op=0b00000000 {
if <stdfun>carry</stdfun>(A, B) goto <builtinsymbol>inst_next</builtinsymbol>;
<label><skip></label>
<fun>testmacro</fun>()
}"""

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = ATTRIBUTES_KEY_MAP

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "Sleigh"

    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Bad value", SyntaxHighlighting.BAD_CHARACTER),
            AttributesDescriptor("Built-in function call", SyntaxHighlighting.STDLIB_FUNCTION_CALL),
            AttributesDescriptor("Built-in symbol", SyntaxHighlighting.BUILT_IN_SYMBOL),
            AttributesDescriptor("Function call", SyntaxHighlighting.FUNCTION_CALL),
            AttributesDescriptor("Keyword", SyntaxHighlighting.KEYWORD),
            AttributesDescriptor("Identifier", SyntaxHighlighting.IDENTIFIER),
            AttributesDescriptor("Label", SyntaxHighlighting.LABEL),
            AttributesDescriptor("Macro and PcodeOp", SyntaxHighlighting.MACRO),
            AttributesDescriptor("Number", SyntaxHighlighting.NUMBER),
            AttributesDescriptor("Preprocessor definition", SyntaxHighlighting.DEFINITION),
            AttributesDescriptor("Preprocessor directive", SyntaxHighlighting.PREPROCESSOR),
            AttributesDescriptor("String", SyntaxHighlighting.STRING)
        )

        private val ATTRIBUTES_KEY_MAP: Map<String, TextAttributesKey> = mapOf(
            Pair("builtinsymbol", SyntaxHighlighting.BUILT_IN_SYMBOL),
            Pair("def", SyntaxHighlighting.DEFINITION),
            Pair("fun", SyntaxHighlighting.FUNCTION_CALL),
            Pair("label", SyntaxHighlighting.LABEL),
            Pair("identifier", SyntaxHighlighting.IDENTIFIER),
            Pair("macro", SyntaxHighlighting.MACRO),
            Pair("pcodeop", SyntaxHighlighting.MACRO),
            Pair("stdfun", SyntaxHighlighting.STDLIB_FUNCTION_CALL),
        )
    }
}

class SleighCommenter : Commenter {

    override fun getLineCommentPrefix(): String = "#"

    override fun getBlockCommentPrefix(): String? = null

    override fun getBlockCommentSuffix(): String? = null

    override fun getCommentedBlockCommentPrefix(): String? = null

    override fun getCommentedBlockCommentSuffix(): String? = null
}

class SleighFoldingBuilder : FoldingBuilderEx(), DumbAware {

    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors: MutableList<FoldingDescriptor> = mutableListOf()

        descriptors.addAll(
            PsiTreeUtil.findChildrenOfType(root, SleighMacroDefinition::class.java)
                .map { element ->
                    FoldingDescriptor(
                        element.parent.node, element.parent.textRange, FoldingGroup.newGroup(SLEIGH_PREFIX_STRING)
                    )
                }
        )

        descriptors.addAll(
            PsiTreeUtil.findChildrenOfType(root, SleighTokenDefinition::class.java)
                .map { element ->
                    FoldingDescriptor(
                        element.parent.node, element.parent.textRange, FoldingGroup.newGroup(SLEIGH_PREFIX_STRING)
                    )
                }
        )

        descriptors.addAll(
            PsiTreeUtil.findChildrenOfType(root, SleighConstructor::class.java)
                .filter { element -> element.constructorStart.identifier == null }
                .map { element ->
                    FoldingDescriptor(
                        element.node,
                        element.textRange,
                        FoldingGroup.newGroup(SLEIGH_PREFIX_STRING),
                        element.constructorStart.display.placeholderText
                    )
                }
        )

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String? = null

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}

object SleighIcons {
    @JvmField
    val FILE = IconLoader.getIcon("/it/frob/sleighidea/ghidra_icon.png", SleighIcons::class.java)
    val TABLE = IconLoader.getIcon("/it/frob/sleighidea/table.png", SleighIcons::class.java)
    val TABLE_GO = IconLoader.getIcon("/it/frob/sleighidea/table_go.png", SleighIcons::class.java)

    @JvmField
    val TOKEN_VIEWER_ICON = IconLoader.getIcon("/it/frob/sleighidea/token_viewer_icon.png", SleighIcons::class.java)
}
