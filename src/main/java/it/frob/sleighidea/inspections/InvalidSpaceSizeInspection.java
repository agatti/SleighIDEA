// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.inspections;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import it.frob.sleighidea.psi.SleighSizemod;
import it.frob.sleighidea.psi.SleighVisitor;
import it.frob.sleighidea.psi.SleighWordsizemod;
import org.jetbrains.annotations.NotNull;

/**
 * Inspection to check for invalid memory space sizes.
 */
public class InvalidSpaceSizeInspection extends BaseInspection {

    @Override
    protected SleighVisitor buildSleighVisitor(@NotNull ProblemsHolder holder,
                                               @NotNull LocalInspectionToolSession session) {
        return new SleighVisitor() {
            @Override
            public void visitSizemod(@NotNull SleighSizemod visited) {
                if (visited.getInteger().toPositiveInteger() == 0) {
                    holder.registerProblem(holder.getManager().createProblemDescriptor(visited.getInteger(),
                            "Memory space size must be greater than zero", (LocalQuickFix) null,
                            ProblemHighlightType.ERROR, true));
                }
            }

            @Override
            public void visitWordsizemod(@NotNull SleighWordsizemod visited) {
                if (visited.getInteger().toPositiveInteger() == 0) {
                    holder.registerProblem(holder.getManager().createProblemDescriptor(visited.getInteger(),
                            "Memory space word size must be greater than zero", (LocalQuickFix) null,
                            ProblemHighlightType.ERROR, true));
                }
            }
        };
    }
}
