// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.syntax

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.PlatformIcons
import it.frob.sleighidea.SleighIcons
import it.frob.sleighidea.psi.*

open class SyntaxHighlightingVisitor(protected val holder: AnnotationHolder) : SleighVisitor() {
    override fun visitLabel(visited: SleighLabel) {
        highlight(visited, holder, SyntaxHighlighting.LABEL)
    }

    override fun visitMacrodef(visited: SleighMacrodef) {
        PsiTreeUtil.findChildOfType(visited, SleighIdentifier::class.java)?.let { element ->
            highlight(element, holder, SyntaxHighlighting.MACRO)
            assignGutterIcon(visited, holder, PlatformIcons.FUNCTION_ICON)
        }
    }

    override fun visitTokenDefinition(visited: SleighTokenDefinition) {
        assignGutterIcon(visited, holder, PlatformIcons.CLASS_ICON)
    }

    override fun visitPcodeopDefinition(visited: SleighPcodeopDefinition) {
        PsiTreeUtil.findChildOfType(visited, SleighIdentifier::class.java)
            ?.let { element -> highlight(element, holder, SyntaxHighlighting.PCODEOP) }
    }

    override fun visitExprApply(visited: SleighExprApply) {
        if (STD_LIBRARY_CALL.contains(visited.firstChild.text)) {
            highlight(visited.firstChild, holder, SyntaxHighlighting.FUNCTION_CALL)
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
            highlight(visited, holder, SyntaxHighlighting.IDENTIFIER)
        }
    }

    override fun visitJumpdest(visited: SleighJumpdest) {
        val jumpTarget = PsiTreeUtil.getChildOfType(visited, SleighIdentifier::class.java) ?: return
        val jumpTargetString = jumpTarget.text
        if (jumpTargetString == "inst_next" || jumpTargetString == "inst_start") {
            highlight(jumpTarget, holder, SyntaxHighlighting.BUILT_IN_SYMBOL)
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
}
