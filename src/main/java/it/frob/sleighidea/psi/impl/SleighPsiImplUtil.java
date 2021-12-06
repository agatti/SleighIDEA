// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import it.frob.sleighidea.psi.SleighDisplay;
import it.frob.sleighidea.psi.SleighIdentifier;
import it.frob.sleighidea.psi.SleighMacrodef;
import it.frob.sleighidea.psi.SleighOplist;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class SleighPsiImplUtil {

    /**
     * Extract a placeholder text string from a SleighDisplay element.
     *
     * @param element the element to get the placeholder text for.
     * @return the placeholder text derived from the given element.
     */
    public static String getPlaceholderText(@NotNull SleighDisplay element) {
        SleighDisplayPlaceholderVisitor visitor = new SleighDisplayPlaceholderVisitor();
        element.acceptChildren(visitor);

        return visitor.getPlaceholderText();
    }

    /**
     * Extract a placeholder text string from a SleighMacrodef element.
     *
     * @param element the element to get the placeholder text for.
     * @return the placeholder text derived from the given element.
     */
    public static String getPlaceholderText(@NotNull SleighMacrodef element) {
        SleighIdentifier nameElement = PsiTreeUtil.findChildOfType(element, SleighIdentifier.class);
        if (nameElement == null) {
            return null;
        }

        return nameElement.getText().trim() +
                "(" +
                PsiTreeUtil.getChildrenOfTypeAsList(PsiTreeUtil.findChildOfType(element, SleighOplist.class),
                                SleighIdentifier.class).stream()
                        .map(PsiElement::getText)
                        .collect(Collectors.joining(", ")).trim() +
                ")";
    }
}
