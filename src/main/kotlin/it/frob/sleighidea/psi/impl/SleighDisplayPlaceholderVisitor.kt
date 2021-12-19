// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import it.frob.sleighidea.psi.SleighPrintPiece
import it.frob.sleighidea.psi.SleighVisitor
import java.util.stream.Collectors

internal class SleighDisplayPlaceholderVisitor : SleighVisitor() {
    private enum class SleighDisplayVisitingState {
        COLLECTING_NAME, COLLECTING_ARGUMENTS
    }

    private var state = SleighDisplayVisitingState.COLLECTING_NAME
    private val builder = StringBuilder()
    private val arguments: MutableList<PsiElement> = ArrayList()
    override fun visitPrintPiece(visited: SleighPrintPiece) {
        when (state) {
            SleighDisplayVisitingState.COLLECTING_NAME -> builder.append(visited.text.trim())
            SleighDisplayVisitingState.COLLECTING_ARGUMENTS -> arguments.add(visited)
        }
    }

    override fun visitWhiteSpace(space: PsiWhiteSpace) {
        when (state) {
            SleighDisplayVisitingState.COLLECTING_NAME -> state = SleighDisplayVisitingState.COLLECTING_ARGUMENTS
            SleighDisplayVisitingState.COLLECTING_ARGUMENTS -> if (arguments[arguments.size - 1] !is PsiWhiteSpace) {
                arguments.add(PsiWhiteSpaceImpl(" "))
            }
        }
    }

    val placeholderText: String
        get() {
            if (arguments.isNotEmpty()) {
                builder.append(" ")
                    .append(arguments.stream()
                        .map { obj: PsiElement -> obj.text }
                        .collect(Collectors.joining("")).trim())
            }
            return builder.toString()
        }
}