// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SleighAnnotator implements Annotator {

    public static final String SLEIGH_PREFIX_STRING = "sleigh";

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof SleighLabel) {
            setHighlighting(element, holder, SleighSyntaxHighlighter.LABEL);
            return;
        }

        if (element instanceof SleighExprApply) {
            if (isStandardLibraryCall(element.getFirstChild().getText())) {
                setHighlighting(element.getFirstChild(), holder, SleighSyntaxHighlighter.FUNCTION_CALL);
            }
            return;
        }

        if (!(element instanceof SleighCtorstart)) {
            return;
        }

        PsiElement firstChild = element.getFirstChild();

        if (firstChild instanceof SleighIdentifier) {
            // Table
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element)
                    .gutterIconRenderer(tableGutterRendererFactory())
                    .create();
            return;
        }

        if (firstChild instanceof SleighDisplay) {
            // Opcode
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element)
                    .gutterIconRenderer(opcodeGutterRendererFactory())
                    .tooltip("Opcode " + firstChild.getText())
                    .create();
        }
    }

    private void setHighlighting(@NotNull PsiElement element, @NotNull AnnotationHolder holder, @NotNull TextAttributesKey key) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .textAttributes(key)
                .create();
    }

    private boolean isStandardLibraryCall(String functionName) {
        switch (functionName) {
            case "zext":
            case "sext":
            case "carry":
            case "scarry":
            case "sborrow":
            case "nan":
            case "abs":
            case "sqrt":
            case "int2float":
            case "float2float":
            case "trunc":
            case "ceil":
            case "floor":
            case "round":
            case "cpool":
            case "newobject":
            case "delayslot":
                return true;

            default:
                return false;
        }
    }

    private GutterRenderer tableGutterRendererFactory() {
        return new GutterRenderer(SleighIcons.TABLE_GO);
    }

    private GutterRenderer opcodeGutterRendererFactory() {
        return new GutterRenderer(SleighIcons.TABLE);
    }

    private static final class GutterRenderer extends GutterIconRenderer {
        private final Icon icon;

        public GutterRenderer(Icon icon) {
            super();

            this.icon = icon;
        }

        @Override
        public @NotNull Icon getIcon() {
            return icon;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }
}
