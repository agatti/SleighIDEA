// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi.impl;

import com.intellij.lang.ASTNode;
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;

public class SleighPsiImplUtil {
    public static String getDisplayText(@NotNull SleighDisplay element) {
        SleighDisplayVisitor visitor = new SleighDisplayVisitor();
        element.acceptChildren(visitor);

        return visitor.getDisplayText();
    }

    public static String getDisplayText(ASTNode element) {
        return getDisplayText(element.getPsi(SleighDisplay.class));
    }
}
