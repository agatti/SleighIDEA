// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import it.frob.sleighidea.psi.SleighFile;
import it.frob.sleighidea.psi.SleighVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for Sleigh inspections.
 */
@SuppressWarnings("unused")
public abstract class BaseInspection extends LocalInspectionTool {
    /**
     * Dummy PSI tree visitor to use if none is provided.
     */
    private static final PsiElementVisitor DUMMY_VISITOR = new PsiElementVisitor() {
    };

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly,
                                                   @NotNull LocalInspectionToolSession session) {
        SleighFile file = ObjectUtils.tryCast(session.getFile(), SleighFile.class);
        return file != null && canRunOn(file) ? buildSleighVisitor(holder, session) : DUMMY_VISITOR;
    }

    /**
     * Check if the current inspection can run on the given {@link SleighFile}.
     *
     * @param file the {@link SleighFile} to check the current inspection for.
     * @return {@code true} if the inspection can run on this file, {@code false} otherwise.
     */
    protected boolean canRunOn(@NotNull SleighFile file) {
        return true;
    }

    /**
     * Build a {@link SleighVisitor} instance to operate on the current file.
     *
     * @param holder a {@link ProblemsHolder} instance to attach problems to.
     * @param session a {@link LocalInspectionToolSession} session to persist data if needed.
     * @return a {@link SleighVisitor} instance that will traverse the PSI tree.
     */
    protected SleighVisitor buildSleighVisitor(@NotNull final ProblemsHolder holder,
                                               @NotNull LocalInspectionToolSession session) {
        return new SleighVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                checkFile((SleighFile) file, holder);
            }
        };
    }

    /**
     * Check the given {@link SleighFile} for problems.
     *
     * @param file the {@link SleighFile} to run the inspection on.
     * @param problemsHolder a {@link ProblemsHolder} instance to attach problems to.
     */
    protected void checkFile(@NotNull SleighFile file, @NotNull ProblemsHolder problemsHolder) {
    }
}
