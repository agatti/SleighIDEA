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
import it.frob.sleighidea.psi.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class SleighAnnotator implements Annotator, DumbAware {

    public static final String SLEIGH_PREFIX_STRING = "sleigh";
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

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof SleighLabel) {
            setHighlighting(element, holder, SleighSyntaxHighlighter.LABEL);
            return;
        }

        if (element instanceof SleighExprApply) {
            if (STD_LIBRARY_CALL.contains(element.getFirstChild().getText())) {
                setHighlighting(element.getFirstChild(), holder, SleighSyntaxHighlighter.FUNCTION_CALL);
            }
            return;
        }

        if (element instanceof SleighCtorstart) {
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
                        .create();
            }
        }
    }

    private void setHighlighting(@NotNull PsiElement element, @NotNull AnnotationHolder holder, @NotNull TextAttributesKey key) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .enforcedTextAttributes(TextAttributes.ERASE_MARKER)
                .create();

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element)
                .enforcedTextAttributes(EditorColorsManager.getInstance().getGlobalScheme().getAttributes(key))
                .create();
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
