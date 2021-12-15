// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PlatformIcons;
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class SleighAnnotator implements Annotator, DumbAware {

    /**
     * The prefix string marking Sleigh annotations.
     */
    public static final String SLEIGH_PREFIX_STRING = "sleigh";

    /**
     * A list containing all built-in function calls.
     *
     * TODO: Use an immutable data structure.
     */
    private static final List<String> STD_LIBRARY_CALL = Arrays.asList(
            "abs",
            "carry",
            "ceil",
            "cpool",
            "delayslot",
            "float2float",
            "floor",
            "int2float",
            "nan",
            "newobject",
            "round",
            "sborrow",
            "scarry",
            "sext",
            "sqrt",
            "trunc",
            "zext"
    );

    /**
     * A list containing all built-in symbols.
     *
     * TODO: Use an immutable data structure.
     */
    private static final List<String> BUILT_IN_SYMBOLS = Arrays.asList(
            "const",
            "epsilon",
            "inst_next",
            "inst_start",
            "instruction",
            "unique"
    );

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        element.accept(new SleighVisitor() {
            @Override
            public void visitLabel(@NotNull SleighLabel visited) {
                setHighlighting(visited, holder, SleighSyntaxHighlighter.LABEL);
            }

            @Override
            public void visitMacrodef(@NotNull SleighMacrodef visited) {
                assignGutterIcon(visited, holder, PlatformIcons.FUNCTION_ICON);
                SleighIdentifier macroElement = PsiTreeUtil.findChildOfType(visited, SleighIdentifier.class);
                if (macroElement != null) {
                    setHighlighting(macroElement, holder, SleighSyntaxHighlighter.MACRO);
                }
            }

            @Override
            public void visitTokenDefinition(@NotNull SleighTokenDefinition visited) {
                assignGutterIcon(visited, holder, PlatformIcons.CLASS_ICON);
            }

            @Override
            public void visitPcodeopdef(@NotNull SleighPcodeopdef visited) {
                SleighIdentifier pcodeopElement = PsiTreeUtil.findChildOfType(visited, SleighIdentifier.class);
                if (pcodeopElement != null) {
                    setHighlighting(pcodeopElement, holder, SleighSyntaxHighlighter.PCODEOP);
                }
            }

            @Override
            public void visitExprApply(@NotNull SleighExprApply visited) {
                if (STD_LIBRARY_CALL.contains(visited.getFirstChild().getText())) {
                    setHighlighting(visited.getFirstChild(), holder, SleighSyntaxHighlighter.FUNCTION_CALL);
                }
            }

            @Override
            public void visitCtorstart(@NotNull SleighCtorstart visited) {
                PsiElement firstChild = visited.getFirstChild();

                if (firstChild instanceof SleighIdentifier) {
                    assignGutterIcon(visited, holder, SleighIcons.TABLE_GO);
                    return;
                }

                if (firstChild instanceof SleighDisplay) {
                    assignGutterIcon(visited, holder, SleighIcons.TABLE);
                }
            }

            @Override
            public void visitIdentifierlist(@NotNull SleighIdentifierlist visited) {
                PsiTreeUtil.collectElementsOfType(visited, SleighIdentifier.class)
                        .forEach(identifier -> setHighlighting(identifier, holder, SleighSyntaxHighlighter.IDENTIFIER));
            }

            @Override
            public void visitJumpdest(@NotNull SleighJumpdest visited) {
                SleighIdentifier jumpTarget = PsiTreeUtil.getChildOfType(visited, SleighIdentifier.class);
                if (jumpTarget == null) {
                    return;
                }

                String jumpTargetString = jumpTarget.getText();
                if (jumpTargetString.equals("inst_next") || jumpTargetString.equals("inst_start")) {
                    setHighlighting(jumpTarget, holder, SleighSyntaxHighlighter.BUILT_IN_SYMBOL);
                }
            }

            @Override
            public void visitSpaceDefinition(@NotNull SleighSpaceDefinition visited) {
                assignGutterIcon(visited, holder, PlatformIcons.ANONYMOUS_CLASS_ICON);
            }

            @Override
            public void visitInclude(@NotNull SleighInclude visited) {
                assignGutterIcon(visited, holder, SleighIcons.FILE);
            }
        });
    }

    /**
     * Set a custom highlight rule for the given element.
     *
     * @param element the element to assign a highlight rule to.
     * @param holder  the annotation holder that will bind the given rule to the given element.
     * @param key     the text attributes to use when rendering the element.
     */
    private void setHighlighting(@NotNull PsiElement element, @NotNull AnnotationHolder holder,
                                 @NotNull TextAttributesKey key) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .enforcedTextAttributes(TextAttributes.ERASE_MARKER)
                .create();

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .enforcedTextAttributes(EditorColorsManager.getInstance().getGlobalScheme().getAttributes(key))
                .create();
    }

    /**
     * Assign a gutter icon to the given element.
     *
     * @param element the element to assign an icon to.
     * @param holder  the annotation holder that will bind the given icon to the given element.
     * @param icon    the icon to draw next to the element.
     */
    private void assignGutterIcon(@NotNull PsiElement element, @NotNull AnnotationHolder holder, @NotNull Icon icon) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .gutterIconRenderer(new GutterIconRenderer() {
                    @Override
                    public boolean equals(Object obj) {
                        return this == obj;
                    }

                    @Override
                    public int hashCode() {
                        return System.identityHashCode(this);
                    }

                    @Override
                    public @NotNull Icon getIcon() {
                        return icon;
                    }
                }).create();
    }
}
