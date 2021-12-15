// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.inspections;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import it.frob.sleighidea.psi.SleighSpaceDefinition;
import it.frob.sleighidea.psi.SleighVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Inspection to check for multiple memory spaces with the same name.
 */
public class DuplicateSpaceDefinitionInspection extends BaseInspection {

    @Override
    protected SleighVisitor buildSleighVisitor(@NotNull ProblemsHolder holder,
                                               @NotNull LocalInspectionToolSession session) {
        return new InspectionVisitor(holder);
    }

    /**
     * Private PSI tree visitor that looks for multiple memory spaces with the same name.
     */
    private static class InspectionVisitor extends SleighVisitor {
        /**
         * The problem holder instance to attach problem entries to.
         */
        private final ProblemsHolder holder;

        /**
         * All the already seen memory space names.
         */
        private final List<String> definedSpaces = new ArrayList<>();

        /**
         * Create a {@link InspectionVisitor} instance with the given problem holder.
         *
         * @param holder the problem holder to attach problem entries to.
         */
        public InspectionVisitor(@NotNull ProblemsHolder holder) {
            this.holder = holder;
        }

        @Override
        public void visitSpaceDefinition(@NotNull SleighSpaceDefinition visited) {
            if (definedSpaces.contains(visited.getName())) {
                holder.registerProblem(holder.getManager().createProblemDescriptor(visited.getIdentifier(),
                        String.format("Memory space \"%s\" is already defined", visited.getIdentifier().getText()),
                        (LocalQuickFix) null, ProblemHighlightType.ERROR, true));
                return;
            }

            definedSpaces.add(visited.getIdentifier().getText());
        }
    }
}
