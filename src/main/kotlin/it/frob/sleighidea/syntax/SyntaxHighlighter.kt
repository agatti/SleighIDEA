// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.syntax

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.PlatformIcons
import it.frob.sleighidea.SleighIcons
import it.frob.sleighidea.SleighSyntaxHighlighter
import it.frob.sleighidea.psi.*

open class SyntaxHighlighter(protected val holder: AnnotationHolder) : SleighVisitor() {
    override fun visitLabel(visited: SleighLabel) {
        highlight(visited, holder, SleighSyntaxHighlighter.LABEL)
    }

    override fun visitMacrodef(visited: SleighMacrodef) {
        PsiTreeUtil.findChildOfType(visited, SleighIdentifier::class.java)?.let { element ->
            highlight(element, holder, SleighSyntaxHighlighter.MACRO)
            assignGutterIcon(visited, holder, PlatformIcons.FUNCTION_ICON)
        }
    }

    override fun visitTokenDefinition(visited: SleighTokenDefinition) {
        assignGutterIcon(visited, holder, PlatformIcons.CLASS_ICON)
    }

    override fun visitPcodeopDefinition(visited: SleighPcodeopDefinition) {
        PsiTreeUtil.findChildOfType(visited, SleighIdentifier::class.java)
            ?.let { element -> highlight(element, holder, SleighSyntaxHighlighter.PCODEOP) }
    }

    override fun visitExprApply(visited: SleighExprApply) {
        if (STD_LIBRARY_CALL.contains(visited.firstChild.text)) {
            highlight(visited.firstChild, holder, SleighSyntaxHighlighter.FUNCTION_CALL)
        }
    }

    override fun visitCtorstart(visited: SleighCtorstart) {
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
            highlight(visited, holder, SleighSyntaxHighlighter.IDENTIFIER)
        }
    }

    override fun visitJumpdest(visited: SleighJumpdest) {
        val jumpTarget = PsiTreeUtil.getChildOfType(visited, SleighIdentifier::class.java) ?: return
        val jumpTargetString = jumpTarget.text
        if (jumpTargetString == "inst_next" || jumpTargetString == "inst_start") {
            highlight(jumpTarget, holder, SleighSyntaxHighlighter.BUILT_IN_SYMBOL)
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
        highlight(visited, holder, SleighSyntaxHighlighter.BUILT_IN_SYMBOL)
    }
}
