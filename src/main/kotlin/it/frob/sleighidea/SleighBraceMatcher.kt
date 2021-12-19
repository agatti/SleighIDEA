// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea

import com.intellij.lang.PairedBraceMatcher
import com.intellij.lang.BracePair
import it.frob.sleighidea.psi.SleighTypes
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

class SleighBraceMatcher : PairedBraceMatcher {

    override fun getPairs(): Array<BracePair> = arrayOf(
        BracePair(SleighTypes.LBRACE, SleighTypes.RBRACE, true),
        BracePair(SleighTypes.LPAREN, SleighTypes.RPAREN, true),
        BracePair(SleighTypes.LBRACKET, SleighTypes.RBRACKET, true)
    )

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = false

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int = openingBraceOffset
}