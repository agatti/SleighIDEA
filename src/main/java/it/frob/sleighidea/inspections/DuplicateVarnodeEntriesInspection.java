package it.frob.sleighidea.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Inspection to check for duplicate variables definition.
 */
public class DuplicateVarnodeEntriesInspection extends BaseInspection {

    @Override
    protected void checkFile(@NotNull SleighFile file, @NotNull ProblemsHolder problemsHolder) {
        VarnodeVisitor visitor = new VarnodeVisitor();
        file.acceptChildren(visitor);

        visitor.variablesFound.forEach((space, variables) -> {
            // TODO: Find a faster way to achieve this.

            for (int sourceIndex = 0; sourceIndex < variables.size() - 1; sourceIndex++) {
                for (int targetIndex = sourceIndex + 1; targetIndex < variables.size(); targetIndex++) {
                    String sourceName = variables.get(sourceIndex).getText().trim();
                    String targetName = variables.get(targetIndex).getText().trim();

                    if (!sourceName.equals(targetName)) {
                        continue;
                    }

                    problemsHolder.registerProblem(problemsHolder.getManager().createProblemDescriptor(
                            variables.get(targetIndex), String.format("'%s' was already defined", targetName),
                            (LocalQuickFix) null, ProblemHighlightType.ERROR, true));
                }
            }
        });
    }

    /**
     * Private PSI tree visitor that extracts all variables being defined in a given file.
     */
    private static class VarnodeVisitor extends SleighVisitor {
        /**
         * A map containing all {@link SleighIdentifier} defined in all {@link SleighVarnodedef} elements.
         */
        private final Map<String, List<SleighIdentifier>> variablesFound = new HashMap<>();

        @Override
        public void visitDefinition(@NotNull SleighDefinition visited) {
            if (visited.getFirstChild() instanceof SleighVarnodedef) {
                visited.getFirstChild().accept(this);
            }
        }

        @Override
        public void visitVarnodedef(@NotNull SleighVarnodedef visited) {
            String spaceName = visited.getSpaceName();
            List<SleighIdentifier> variables = visited.getIdentifierlist().getIdOrWildList().stream()
                    .map(SleighIdOrWild::getIdentifier)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            if (variablesFound.containsKey(spaceName)) {
                variablesFound.get(spaceName).addAll(variables);
            } else {
                variablesFound.put(spaceName, new ArrayList<>(variables));
            }
        }
    }
}
