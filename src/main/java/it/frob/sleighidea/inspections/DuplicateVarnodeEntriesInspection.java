package it.frob.sleighidea.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.util.PsiTreeUtil;
import it.frob.sleighidea.psi.SleighFile;
import it.frob.sleighidea.psi.SleighIdOrWild;
import it.frob.sleighidea.psi.SleighIdentifier;
import it.frob.sleighidea.psi.SleighVarnodedef;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Inspection to check for duplicate variables definition.
 */
public class DuplicateVarnodeEntriesInspection extends BaseInspection {

    @Override
    protected void checkFile(@NotNull SleighFile file, @NotNull ProblemsHolder problemsHolder) {
        List<SleighIdentifier> variables = PsiTreeUtil.collectElementsOfType(file, SleighVarnodedef.class).stream()
                .flatMap(container -> container.getIdentifierlist().getIdOrWildList().stream())
                .map(SleighIdOrWild::getIdentifier)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (int index = 1; index < variables.size(); index++) {
            SleighIdentifier currentVariable = variables.get(index);
            for (int checkIndex = 0; checkIndex < index; checkIndex++) {
                SleighIdentifier pastVariable = variables.get(checkIndex);
                if (currentVariable.getText().equals(pastVariable.getText())) {
                    problemsHolder.registerProblem(problemsHolder.getManager().createProblemDescriptor(
                            currentVariable, String.format("'%s' was already defined", currentVariable.getText()),
                            (LocalQuickFix) null, ProblemHighlightType.ERROR, true));
                    break;
                }
            }
        }
    }
}
