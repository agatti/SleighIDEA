// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.usageView.UsageViewUtil;
import it.frob.sleighidea.psi.SleighCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SleighCompositeElementImpl extends ASTWrapperPsiElement implements SleighCompositeElement {
    public SleighCompositeElementImpl(ASTNode node) {
        super(node);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state,
                                       PsiElement lastParent, @NotNull PsiElement place) {
        return super.processDeclarations(processor, state, lastParent, place);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new PresentationData(UsageViewUtil.createNodeText(this), getContainingFile().getName(),
                getIcon(0), null);
    }

    static boolean processDeclarations(@NotNull PsiElement element, @NotNull PsiScopeProcessor processor,
                                       @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        return processor.execute(element, state) && processChildren(element, processor, state, lastParent, place);
    }

    static boolean processChildren(@NotNull PsiElement element, @NotNull PsiScopeProcessor processor,
                                   @NotNull ResolveState substitutor, @Nullable PsiElement lastParent,
                                   @NotNull PsiElement place) {
        PsiElement run = lastParent == null ? element.getLastChild() : lastParent.getPrevSibling();
        while (run != null) {
            if (run instanceof SleighCompositeElement && !run.processDeclarations(processor, substitutor,
                    null, place)) {
                return false;
            }
            run = run.getPrevSibling();
        }
        return true;
    }
}
