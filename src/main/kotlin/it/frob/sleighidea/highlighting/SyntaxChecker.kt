// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import it.frob.sleighidea.psi.SleighSpaceDefinition

class SyntaxChecker(holder: AnnotationHolder) : SyntaxHighlighter(holder) {

    override fun visitSpaceDefinition(visited: SleighSpaceDefinition) {
        super.visitSpaceDefinition(visited)

        when (visited.size.size) {
            0 -> markElementAsError(visited.parent, holder, "Memory space must have a size modifier.")
            1 -> visited.size.first().run {
                second.integer?.let { integer ->
                    if (integer.toInteger() <= 0) {
                        markElementAsError(first, holder, "Memory space size must be greater than zero.")
                    }
                }
            }
            else -> visited.size.drop(1).forEach { element ->
                markElementAsError(element.first, holder, "Memory space already has a size modifier set.")
            }
        }

        if (visited.wordSize.size == 1) visited.wordSize.first().run {
            second.integer?.let { integer ->
                if (integer.toInteger() <= 0) {
                    markElementAsError(first, holder, "Memory space word size must be greater than zero.")
                }
            }
        }
        else visited.wordSize.drop(1).forEach { element ->
            markElementAsError(element.first, holder, "Memory space already has a word size modifier set.")
        }

        visited.type.drop(1).forEach { element ->
            markElementAsError(element.first, holder, "Memory space already has a type set.")
        }

        visited.default.drop(1).forEach { element ->
            markElementAsError(element, holder, "Memory space already has a default flag set.")
        }
    }
}
