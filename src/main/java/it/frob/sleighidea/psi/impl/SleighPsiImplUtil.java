// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SleighPsiImplUtil {

    public static String getDisplayText(ASTNode element) {
        ASTNode @NotNull [] children = element.getChildren(null);
        if (children.length < 2) {
            return null;
        }

        int index = 0;
        StringBuilder builder = new StringBuilder();
        boolean opcodeDone = false;
        while (index < children.length && !opcodeDone) {
            ASTNode node = children[index++];

            if (node.getElementType() == SleighTypes.PRINTPIECE) {
                builder.append(node.getText());
            } else if (node instanceof PsiWhiteSpace) {
                opcodeDone = true;
            }
        }

        List<PsiElement> printPieces = new ArrayList<>();

        while (index < children.length) {
            ASTNode node = children[index++];
            if (node.getElementType() == SleighTypes.PRINTPIECE) {
                printPieces.add(node.getPsi());
            }
        }

        if (printPieces.size() > 0) {
            PsiElement first = printPieces.get(0);
            PsiElement last = printPieces.get(printPieces.size() - 1);

            int startOffset = first.getStartOffsetInParent();
            int endOffset = last.getStartOffsetInParent() + last.getTextLength();

            String printPiecesString = element.getText().substring(startOffset, endOffset);
            builder.append(" ").append(printPiecesString.replaceAll("\\s+", " ").trim());
        }

        return builder.toString();
    }
}