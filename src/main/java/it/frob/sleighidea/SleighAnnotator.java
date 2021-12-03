// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import it.frob.sleighidea.psi.SleighCtorstart;
import it.frob.sleighidea.psi.SleighDisplay;
import it.frob.sleighidea.psi.SleighIdentifier;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SleighAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
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
