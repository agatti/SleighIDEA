// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea

import com.intellij.lexer.FlexAdapter

class SleighLexerAdapter : FlexAdapter(_SleighLexer(null))