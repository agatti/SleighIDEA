// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.inspections;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import it.frob.sleighidea.psi.SleighSpacedef;
import it.frob.sleighidea.psi.SleighSpacemod;
import it.frob.sleighidea.psi.SleighVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Inspection to check for multiple default memory spaces.
 */
public class DuplicateDefaultSpaceDefinitionInspection extends BaseInspection {

    @Override
    protected SleighVisitor buildSleighVisitor(@NotNull ProblemsHolder holder,
                                               @NotNull LocalInspectionToolSession session) {
        return new InspectionVisitor(holder);
    }

    /**
     * Private PSI tree visitor that looks for multiple default memory spaces.
     */
    private static class InspectionVisitor extends SleighVisitor {

        /**
         * The problem holder instance to attach problem entries to.
         */
        private final ProblemsHolder holder;

        /**
         * Flag indicating whether a default memory space has been found or not.
         */
        private boolean defaultFound = false;

        /**
         * Create a {@link InspectionVisitor} instance with the given problem holder.
         *
         * @param holder the problem holder to attach problem entries to.
         */
        public InspectionVisitor(@NotNull ProblemsHolder holder) {
            this.holder = holder;
        }

        @Override
        public void visitSpacedef(@NotNull SleighSpacedef visited) {
            for (SleighSpacemod modifier : visited.getSpacemodList()) {
                if ((modifier.getTypemod() != null) || (modifier.getSizemod() != null) ||
                        (modifier.getWordsizemod() != null)) {
                    continue;
                }

                if ("default".equals(modifier.getText())) {
                    if (defaultFound) {
                        holder.registerProblem(holder.getManager().createProblemDescriptor(
                                modifier, "There can only be one default memory space",
                                (LocalQuickFix) null, ProblemHighlightType.ERROR, true));
                    } else {
                        defaultFound = true;
                    }
                }
            }
        }
    }
}
