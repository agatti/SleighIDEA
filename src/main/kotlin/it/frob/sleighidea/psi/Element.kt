// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.tree.IElementType
import it.frob.sleighidea.SleighLanguage
import org.jetbrains.annotations.NonNls

/**
 * Interface for Sleigh elements that may or may not contain sub-elements.
 */
interface SleighCompositeElement : PsiElement

interface SleighNamedElement : SleighCompositeElement, PsiNameIdentifierOwner

class SleighCompositeElementType(debugName: @NonNls String) : IElementType(debugName, SleighLanguage.INSTANCE)

class SleighElementType(debugName: @NonNls String) : IElementType(debugName, SleighLanguage.INSTANCE)
