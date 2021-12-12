package it.frob.sleighidea.inspections;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import it.frob.sleighidea.psi.SleighFile;
import it.frob.sleighidea.psi.SleighVarnodedef;
import it.frob.sleighidea.psi.SleighVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Inspection to check for variables definitions nodes pointing to an unknown memory space.
 */
public class UnknownVarnodedefSpaceInspection extends BaseInspection {
    @Override
    protected SleighVisitor buildSleighVisitor(@NotNull ProblemsHolder holder, @NotNull LocalInspectionToolSession session) {
        return new SleighVisitor() {
            @Override
            public void visitVarnodedef(@NotNull SleighVarnodedef visited) {
                if (visited.getContainingFile() instanceof SleighFile) {
                    if (((SleighFile) visited.getContainingFile()).getSpaceForName(visited.getSpaceName()) == null) {
                        holder.registerProblem(holder.getManager().createProblemDescriptor(
                                visited.getIdentifier(), String.format("Unknown memory space '%s'",
                                        visited.getSpaceName()),
                                (LocalQuickFix) null, ProblemHighlightType.ERROR, true));

                    }
                }
            }
        };
    }
}
