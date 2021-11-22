// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi;

import com.intellij.psi.tree.IElementType;
import it.frob.sleighidea.SleighLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SleighTokenType extends IElementType {
    public SleighTokenType(@NotNull @NonNls String debugName) {
        super(debugName, SleighLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "SleighTokenType." + super.toString();
    }
}
