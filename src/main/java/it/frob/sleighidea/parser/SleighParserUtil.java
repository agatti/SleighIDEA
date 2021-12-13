// SPDX-License-Identifier: Apache-2.0

package it.frob.sleighidea.parser;

import com.intellij.lang.LighterASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import it.frob.sleighidea.psi.SleighTypes;
import it.frob.sleighidea.psi.impl.SleighPsiImplUtil;

import java.util.Objects;

public class SleighParserUtil extends GeneratedParserUtilBase {

    /**
     * Create and bind a greater than zero {@link it.frob.sleighidea.psi.SleighInteger} element to the parse tree.
     *
     * @param builder the PSI tree builder instance.
     * @param level the current parser level.
     * @return {@code true} if the tree-building process should proceed, {@code false} otherwise.
     */
    public static boolean parseGreaterThanZeroInteger(PsiBuilder builder, int level) {
        PsiBuilder.Marker mark = builder.mark();
        if (SleighParser.integer(builder, level)) {
            LighterASTNode marker = Objects.requireNonNull(builder.getLatestDoneMarker());

            int number;
            try {
                number = SleighPsiImplUtil.baseAwareIntegerParser(String.valueOf(builder.getOriginalText()
                                .subSequence(marker.getStartOffset(), marker.getEndOffset())).trim());
            } catch (NumberFormatException exception) {
                mark.error("Cannot parse number");
                return false;
            }

            if (number <= 0) {
                mark.error("Value must be greater than zero");
                return true;
            }

            mark.done(SleighTypes.INTEGER);
            return true;
        }

        mark.rollbackTo();
        return false;
    }
}
