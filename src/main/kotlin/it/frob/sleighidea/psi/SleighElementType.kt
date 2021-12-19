// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi

import com.intellij.psi.tree.IElementType
import it.frob.sleighidea.SleighLanguage
import org.jetbrains.annotations.NonNls

class SleighElementType(debugName: @NonNls String) : IElementType(debugName, SleighLanguage.INSTANCE)