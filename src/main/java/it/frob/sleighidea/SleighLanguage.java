// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea;

import com.intellij.lang.Language;

public class SleighLanguage extends Language {
    public static final SleighLanguage INSTANCE = new SleighLanguage();

    private SleighLanguage() {
        super("Sleigh");
    }
}
