// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea;

import com.intellij.lexer.FlexAdapter;

public class SleighLexerAdapter extends FlexAdapter {
    public SleighLexerAdapter() {
        super(new SleighLexer(null));
    }
}
