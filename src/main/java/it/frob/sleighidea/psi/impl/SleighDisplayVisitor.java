// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl;
import it.frob.sleighidea.psi.SleighPrintpiece;
import it.frob.sleighidea.psi.SleighVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class SleighDisplayVisitor extends SleighVisitor {
    private enum SleighDisplayVisitingState {
        COLLECTING_NAME,
        COLLECTING_ARGUMENTS
    }

    private SleighDisplayVisitingState state = SleighDisplayVisitingState.COLLECTING_NAME;
    private final StringBuilder builder = new StringBuilder();
    private final List<PsiElement> arguments = new ArrayList<>();

    @Override
    public void visitPrintpiece(@NotNull SleighPrintpiece visited) {
        switch (state) {
            case COLLECTING_NAME:
                builder.append(visited.getText().trim());
                break;

            case COLLECTING_ARGUMENTS:
                arguments.add(visited);
                break;

            default:
                break;
        }
    }

    @Override
    public void visitWhiteSpace(@NotNull PsiWhiteSpace space) {
        switch (state) {
            case COLLECTING_NAME:
                state = SleighDisplayVisitingState.COLLECTING_ARGUMENTS;
                break;

            case COLLECTING_ARGUMENTS:
                if (!(arguments.get(arguments.size() - 1) instanceof PsiWhiteSpace)) {
                    arguments.add(new PsiWhiteSpaceImpl(" "));
                }
                break;

            default:
                break;
        }
    }

    public String getDisplayText() {
        if (!arguments.isEmpty()) {
            builder.append(" ")
                    .append(arguments.stream()
                            .map(PsiElement::getText)
                            .collect(Collectors.joining("")).trim());
        }

        return builder.toString();
    }
}
